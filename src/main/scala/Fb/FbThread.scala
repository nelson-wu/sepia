package Fb

import Messages.Implicits.ImplicitConversions.ThreadId
import play.api.libs.json.{JsArray, JsValue}

import scala.util.Try

case class FbThread(name: String, threadId: ThreadId, isGroup: Boolean, messageCount: Int, participants: Set[Participant])

object FbThread {
  def apply(in: JsValue): Option[FbThread] = Try {
      FbThread(
        (in \ "name").get.as[String],
        (in \ "threadID").get.as[String],
        (in \ "isGroup").get.as[Boolean],
        (in \ "messageCount").get.as[Int],
        Participant.fromArray((in \ "participants").as[JsArray])
      )
    }.toOption
}
