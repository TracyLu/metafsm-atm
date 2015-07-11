package misc

import net.imadz.ATM
import net.imadz.lifecycle.AbsStateMachineRegistry.{LifecycleRegistry, StateMachineBuilder}
import net.imadz.lifecycle.{AbsStateMachineRegistry, LifecycleEvent, LifecycleEventHandler}
import org.joda.time.DateTime
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

  class DefaultLifecycleEventHandler extends LifecycleEventHandler {
    override def onEvent(lifecycleEvent: LifecycleEvent): Unit = {
      if (lifecycleEvent.getReactiveObject.isInstanceOf[ATM]) {
        val atm = lifecycleEvent.getReactiveObject.asInstanceOf[ATM]
        val start = new DateTime(lifecycleEvent.startTime())
        val end = new DateTime(lifecycleEvent.endTime())
        val cost = lifecycleEvent.endTime - lifecycleEvent.startTime
        lifecycleEvent.event() match {
          case "Deposit" =>
            println(s"${lifecycleEvent.event()} on ATM, ${lifecycleEvent.fromState()} => ${lifecycleEvent.toState} @ ${start} ~ ${end} costs ${cost} millis")
          case "Withdraw" =>
            println(s"${lifecycleEvent.event()} on ATM, ${lifecycleEvent.fromState()} => ${lifecycleEvent.toState} @ ${start} ~ ${end} costs ${cost} millis")
          case "Stop" =>
            println(s"${lifecycleEvent.event()} on ATM, ${lifecycleEvent.fromState()} => ${lifecycleEvent.toState} @ ${start} ~ ${end} costs ${cost} millis")
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
  }

}
