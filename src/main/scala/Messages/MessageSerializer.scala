package Messages

import Messages.Implicits.{CanParse, CanSerialize}
import akka.util.ByteString
import shapeless.TypeCase

import scala.util.Try

object MessageSerializer {
  val CRLF = "\r\n"
  def serialize(message: Message[Params]*): ByteString = {
    message.foldLeft(ByteString.empty){ (acc, next) ⇒
      val messageMiddle = TypeCase[Message[Middle]]
      val messageTrailing = TypeCase[Message[Trailing]]
      val messageCompound = TypeCase[Message[Compound]]

      implicit val ev = next match {
        case messageMiddle(m) ⇒ implicitly[CanSerialize[Middle]]
        case messageTrailing(m) ⇒ implicitly[CanSerialize[Trailing]]
        case messageCompound(m) ⇒ implicitly[CanSerialize[Compound]]
        case _ ⇒ implicitly[CanSerialize[NoParams.type]]
      }
      acc ++ serializeToByteString(next)(ev)
    }
  }

  def serializeToByteString[P <: Params](message: Message[Params])(implicit ev: CanSerialize[P]): ByteString = {
    val prefix = ":" + message.prefix.name
    val command = message.command.text
    val params = serializeParams[P](message.params.asInstanceOf[P]).getOrElse("")
    val writeString = s"$prefix $command $params"
    ByteString(writeString + CRLF)
  }

  def serializeParams[B <: Params : CanSerialize](params: B): Option[String] = {
    Try(implicitly[CanSerialize[B]].serialize(params)).toOption
  }
}