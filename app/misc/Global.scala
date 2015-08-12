package misc

import actors.{EventInfo, ATMMonitorActor}
import akka.actor.{DeadLetter, ActorSystem, Props}
import net.imadz.ATM
import net.imadz.lifecycle.AbsStateMachineRegistry.{LifecycleRegistry, StateMachineBuilder}
import net.imadz.lifecycle.{AbsStateMachineRegistry, LifecycleEvent, LifecycleEventHandler}
import org.joda.time.DateTime
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future


/**
 * Created By: Tracy Lu
 * Date: 12/08/15
 */
object Global extends GlobalSettings {

  val sys = ActorSystem.apply("atm");
  println("Global sys:" + sys.startTime)
  val atmActor = sys.actorOf(Props[ATMMonitorActor], "ATMMonitorActor")


  class DefaultLifecycleEventHandler extends LifecycleEventHandler {
    override def onEvent(lifecycleEvent: LifecycleEvent): Unit = {
      if (lifecycleEvent.getReactiveObject.isInstanceOf[ATM]) {
        val atm = lifecycleEvent.getReactiveObject.asInstanceOf[ATM]
        val userId = atm.operatorId
        val start = new DateTime(lifecycleEvent.startTime())
        val cost = lifecycleEvent.endTime - lifecycleEvent.startTime
        lifecycleEvent.event() match {
          case "Deposit" | "Withdraw" | "Recycle" =>
            println(s"${lifecycleEvent.event()} on ATM, ${lifecycleEvent.fromState()} => ${lifecycleEvent.toState} @ ${start} costs ${cost} millis")
            atmActor ! EventInfo(userId, lifecycleEvent.event(), atm.getTotalCash(), lifecycleEvent.fromState(), lifecycleEvent.toState(), start, cost)
          case _ =>
        }
      }
    }
  }

  override def onStart(app: Application): Unit = {
    @LifecycleRegistry(Array(classOf[ATM], classOf[DefaultLifecycleEventHandler]))
    @StateMachineBuilder
    class Registry() extends AbsStateMachineRegistry {}
    new Registry()
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(Redirect(controllers.routes.AppController.index()))
  }

  override def onStop(app: Application) = {
    atmActor ! DeadLetter
    sys.shutdown()
  }

}
