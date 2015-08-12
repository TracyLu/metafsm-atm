package actors

import akka.actor.Actor
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.libs.json.Json._
import play.api.libs.json._

/**
 * Created By: Tracy Lu
 * Date: 12/08/15
 */
class ATMMonitorActor extends Actor {

  case class UserChannel(userId: Int, var channelsCount: Int, enumerator: Enumerator[JsValue], channel: Channel[JsValue])

  lazy val log = Logger("application." + this.getClass.getName)

  // this map relate every user with his UserChannel
  var webSockets = Map[Int, UserChannel]()

  // this map relate every user with his current time
  //  var usersTimes = Map[Int, Int]()

  override def receive = {

    case StartSocket(userId) =>

      log.debug(s"start new socket for user $userId")

      // get or create the touple (Enumerator[JsValue], Channel[JsValue]) for current user
      // Channel is very useful class, it allows to write data inside its related 
      // enumerator, that allow to create WebSocket or Streams around that enumerator and
      // write data inside that using its related Channel
      val userChannel: UserChannel = webSockets.get(userId) getOrElse {
        val broadcast: (Enumerator[JsValue], Channel[JsValue]) = Concurrent.broadcast[JsValue]
        UserChannel(userId, 0, broadcast._1, broadcast._2)
      }

      // if user open more then one connection, increment just a counter instead of create
      // another tuple (Enumerator, Channel), and return current enumerator,
      // in that way when we write in the channel,
      // all opened WebSocket of that user receive the same data
      userChannel.channelsCount = userChannel.channelsCount + 1
      webSockets += (userId -> userChannel)

      log debug s"channel for user : $userId count : ${userChannel.channelsCount}"
      log debug s"channel count : ${webSockets.size}"

      // return the enumerator related to the user channel,
      // this will be used for create the WebSocket
      sender ! userChannel.enumerator

    //    case Start(userId) =>
    //      usersTimes += (userId -> 0)

    case Stop(userId) =>
      //      removeUserTimer(userId)

      val json = Map("data" -> toJson(0))
      webSockets.get(userId).get.channel push Json.toJson(json)


    case SocketClosed(userId) =>

      log debug s"closed socket for $userId"

      val userChannel = webSockets.get(userId).get

      if (userChannel.channelsCount > 1) {
        userChannel.channelsCount = userChannel.channelsCount - 1
        webSockets += (userId -> userChannel)
        log debug s"channel for user : $userId count : ${userChannel.channelsCount}"
      } else {
        removeUserChannel(userId)
        //        removeUserTimer(userId)
        log debug s"removed channel and timer for $userId"
      }


    case eventInfo@EventInfo(userId: Int, event: String, totalCash: Int, from: String, to: String, start: DateTime, cost: Long) =>
      import play.api.libs.json._
      import play.api.libs.functional.syntax._

      implicit val eventInfoWrites = new Writes[EventInfo] {
        def writes(event: EventInfo) = Json.obj(
          "userId" -> event.userId,
          "eventName" -> event.eventName,
          "totalCash" -> event.totalCash,
          "fromState" -> event.fromState,
          "toState" -> event.toState,
          "start" -> event.start,
          "cost" -> event.cost
        )
      }

      implicit val eventInfoReads: Reads[EventInfo] = (
        (JsPath \ "userId").read[Int] and
          (JsPath \ "eventName").read[String] and
          (JsPath \ "totalCash").read[Int] and
          (JsPath \ "fromState").read[String] and
          (JsPath \ "toState").read[String] and
          (JsPath \ "start").read[DateTime] and
          (JsPath \ "cost").read[Long]
        )(EventInfo.apply _)

      val json = Map("eventInfo" -> toJson(eventInfo))
      webSockets.get(userId).get.channel push Json.toJson(json)

    case currentStatus@CurrentStatus(userId: Int, totalCash: Int, state: String) =>
      import play.api.libs.json._
      import play.api.libs.functional.syntax._

      implicit val currentStatusWrites = new Writes[CurrentStatus] {
        def writes(status: CurrentStatus) = Json.obj(
          "userId" -> status.userId,
          "totalCash" -> status.totalCash,
          "state" -> status.state
        )
      }

      implicit val currentStatusReads: Reads[CurrentStatus] = (
        (JsPath \ "userId").read[Int] and
          (JsPath \ "totalCash").read[Int] and
          (JsPath \ "state").read[String]
        )(CurrentStatus.apply _)

      val json = Map("currentStatus" -> toJson(currentStatus))
      webSockets.get(userId).get.channel push Json.toJson(json)
  }

  //  def removeUserTimer(userId: Int) = usersTimes -= userId
  def removeUserChannel(userId: Int) = webSockets -= userId

}


sealed trait SocketMessage

case class StartSocket(userId: Int) extends SocketMessage

case class SocketClosed(userId: Int) extends SocketMessage

case class Stop(userId: Int) extends SocketMessage

case class EventInfo(userId: Int, eventName: String, totalCash: Int, fromState: String, toState: String, start: DateTime, cost: Long) extends SocketMessage

case class CurrentStatus(userId: Int, totalCash: Int, state: String) extends SocketMessage

