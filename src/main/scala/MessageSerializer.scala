package ircserver

import akka.util.ByteString

import scala.util.Try

object MessageSerializer {
  val CRLF = "\r\n"
  def serialize(message: Message): ByteString = {
    val prefix = ":" + message.prefix.name
    val command = message.command.text
    val params = message.params.map(serializeParams).getOrElse("")
    ByteString(s"$prefix $command $params" + CRLF)
  }

  def serializeParams[B <: Params : CanSerialize](params: B): Option[String] = {
    Try(implicitly[CanSerialize[B]].serialize(params)).toOption
  }
}