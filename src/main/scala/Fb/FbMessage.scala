package Fb

import Messages.Implicits.ImplicitConversions.UserId
import org.joda.time.Instant
import play.api.libs.json.JsArray

import scala.util.Try

case class FbMessage(text: String, senderId: UserId, timestamp: Instant)

object FbMessage {
  def fromArray(parsed: JsArray): Seq[FbMessage] = {
    parsed.value.filter(v ⇒ (v \ "type").get.as[String] == "message").flatMap { v ⇒
      Try {
        val text = (v \ "body").get.as[String] match {
          case str if !str.isEmpty ⇒ str
          case _ ⇒ (v \ "attachments" \ 0 \ "type").toOption match {
            case Some(value) if value.as[String] == "image" ⇒ (v \ "attachments" \ 0 \ "largePreviewUrl").get.as[String]
            case _ ⇒ "Error: unsupported attachment format"
          }
        }
        FbMessage(
          text,
          (v \ "senderID").get.as[String],
          new Instant((v \ "timestamp").get.as[String].toLong)
        )
      }.toOption
    }
  }
}
