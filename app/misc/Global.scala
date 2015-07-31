package misc

import actors.ATMMonitorActor
import akka.actor.{DeadLetter, ActorSystem, Props}
import play.api._
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future


/**
 * Created with IntelliJ IDEA.
 * User: luigi
 * Date: 18/04/13
 * Time: 00:19
 * To change this template use File | Settings | File Templates.
 */
object Global extends GlobalSettings {

  val sys = ActorSystem.apply("atm");
  println("Global sys:" + sys.startTime)
  val atmActor = sys.actorOf(Props[ATMMonitorActor], "ATMMonitorActor")

  override def onStart(app: Application): Unit = {
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(Redirect(controllers.routes.AppController.index()))
  }

  override def onStop(app: Application) = {
    atmActor ! DeadLetter
    sys.shutdown()
  }

}
