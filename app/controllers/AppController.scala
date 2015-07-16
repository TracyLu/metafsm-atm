package controllers

import misc.Global
import net.imadz.ATM
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumerator, Iteratee}

import scala.concurrent.Future
import scala.concurrent.duration._
import actors._
import akka.actor.{ActorSystem, Actor, Props}
import akka.pattern.ask
import akka.util.Timeout
import actors.StartSocket
import actors.SocketClosed
import scala.util.Random
import play.api.Routes

/**
 * User: Luigi Antonini
 * Date: 17/06/13
 * Time: 23:25
 */
object AppController extends Controller with Secured{

  var atm: ATM = null
  val sys = Global.sys

  val atmActor  = sys.actorSelection("/user/ATMMonitorActor")

  def index = withAuth {
    implicit request => userId =>
      if(atm == null) atm = ATM(request.toInt)
      Ok(views.html.app.index())
  }

  /**
   * This function crate a WebSocket using the
   * enumertator linked to the current user,
   * retrieved from the TaskActor.
   */
  def indexWS = withAuthWS {
    userId =>

      implicit val timeout = Timeout(3 seconds)

      println("atmActorPath:" + atmActor.anchorPath)
      // using the ask pattern of Akka,
      // get the enumerator for that user
      (atmActor ? StartSocket(userId)) map {
        enumerator =>

          // create a Iteratee which ignore the input and
          // and send a SocketClosed message to the actor when
          // connection is closed from the client
          Right((Iteratee.ignore[JsValue] map {
            _ =>
              atmActor ! SocketClosed(userId)
          }, enumerator.asInstanceOf[Enumerator[JsValue]]))
      }
  }

  def deposit = withAuth {
    userId => implicit request =>
      atm.deposit()
      Ok
  }


  def withdraw = withAuth {
    userId => implicit request =>
      atm.withdraw
      Ok
  }

  def recycle = withAuth {
    userId => implicit request =>
      atm.recycle()
      Ok
  }

  def reset = withAuth {
    userId => implicit request =>
      atm = ATM(userId)
      Ok(views.html.app.index())
  }

  def javascriptRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          routes.javascript.AppController.indexWS,
          routes.javascript.AppController.deposit,
          routes.javascript.AppController.withdraw,
          routes.javascript.AppController.recycle,
          routes.javascript.AppController.reset
        )
      ).as("text/javascript")
  }

}

trait Secured {
  def username(request: RequestHeader) = {
    //verify or create session, this should be a real login
    request.session.get(Security.username)
  }

  /**
   * When user not have a session, this function create a 
   * random userId and reload index page
   */
  def unauthF(request: RequestHeader) = {
    val newId: String = new Random().nextInt().toString()
    Redirect(routes.AppController.index).withSession(Security.username -> newId)
  }

  /**
   * Basic authentication system
   * try to retrieve the username, call f() if it is present,
   * or unauthF() otherwise
   */
  def withAuth(f: => Int => Request[_ >: AnyContent] => Result): EssentialAction = {
    Security.Authenticated(username, unauthF) {
      username =>
        println("WithAuth:" + username.toInt)
        Action(request => f(username.toInt)(request))
    }
  }

  /**
   * This function provide a basic authentication for 
   * WebSocket, likely withAuth function try to retrieve the
   * the username form the session, and call f() funcion if find it,
   * or create an error Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])])
   * if username is none  
   */
  def withAuthWS(f: => Int => Future[Either[Result, (Iteratee[JsValue, Unit], Enumerator[JsValue])]]): WebSocket[JsValue, JsValue] = {

    // this function create an error Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])])
    // the iteratee ignore the input and do nothing,
    // and the enumerator just send a 'not authorized message'
    // and close the socket, sending Enumerator.eof
    def errorFuture = {
      // Just consume and ignore the input
      val in = Iteratee.ignore[JsValue]

      // Send a single 'Hello!' message and close
      val out = Enumerator(Json.toJson("not authorized")).andThen(Enumerator.eof)

      Future {
        Left(Unauthorized)
      }
    }

    WebSocket.tryAccept[JsValue] {
      request =>
        username(request) match {
          case None =>
            errorFuture
          case Some(id) =>
            f(id.toInt)
        }
    }
  }
}

