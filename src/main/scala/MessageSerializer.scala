package ircserver

import akka.util.ByteString

import scala.reflect.ClassTag
import scala.util.Try

object MessageSerializer {
  val CRLF = "\r\n"
  def serialize[P <: Params : CanSerialize](messages: Message[P]*): ByteString = {
    messages.foldLeft(ByteString()) { (acc, message) ⇒
      val prefix = ":" + message.prefix.name
      val command = message.command.text
      val params = serializeParams[P](message.params).getOrElse("")
      val writeString = s"$prefix $command $params"
      acc ++ ByteString(writeString + CRLF)
    }
  }

  def serializeParams[B <: Params : CanSerialize](params: B): Option[String] = {
    Try(implicitly[CanSerialize[B]].serialize(params)).toOption
  }
}