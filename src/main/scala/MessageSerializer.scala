package ircserver

import akka.util.ByteString

import scala.reflect.ClassTag
import scala.util.Try

object MessageSerializer {
  import CanSerializeImplicits._
  val CRLF = "\r\n"
  def serialize(messages: Message*): ByteString = {
    messages.foldLeft(ByteString()) { (acc, message) ⇒
      val prefix = ":" + message.prefix.name
      val command = message.command.text
      val params = serializeParams(message.params)
      acc ++ ByteString(s"$prefix $command $params" + CRLF)
    }
  }

  def serializeParams[B <: Params : CanSerialize](params: B): Option[String] = {
    Try(implicitly[CanSerialize[B]].serialize(params)).toOption
  }
}