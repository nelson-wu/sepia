package ircserver

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp.Write
import shapeless.TypeCase

class Writer(connection: ActorRef) extends Actor {
  import MessageSerializer._

  val messageTarget = TypeCase[Message[Target]]
  val messageSpecial = TypeCase[Message[Special]]
  val messageCompound = TypeCase[Message[Compound]]

  def receive = {
    case messageTarget(n) ⇒
      connection ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
    case messageSpecial(n) ⇒
      connection ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
    case messageCompound(n) ⇒
      connection ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
    case p : Product ⇒ p.productIterator.foreach( e ⇒ parseMessage(e.asInstanceOf[Message[Params]]))
  }

  def parseMessage(m: Message[_]): Unit = m match {
    case messageTarget(n) ⇒
      connection ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
    case messageSpecial(n) ⇒
      connection ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
    case messageCompound(n) ⇒
      connection ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
  }
}
