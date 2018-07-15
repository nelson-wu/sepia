package Actors

import Messages._
import akka.actor.{Actor, ActorRef}
import akka.io.Tcp.Write
import shapeless.TypeCase

import scala.collection.mutable

class Writer extends Actor {
  import Messages.MessageSerializer._

  var connectionList = mutable.Map[String, Connection]()

  val messageTarget = TypeCase[Message[Middle]]
  val messageSpecial = TypeCase[Message[Trailing]]
  val messageCompound = TypeCase[Message[Compound]]

  def receive = {
    case messageTarget(n) if connectionList.contains(n.recipient) ⇒
      connectionList(n.recipient).ref ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
    case messageSpecial(n) if connectionList.contains(n.recipient) ⇒
      connectionList(n.recipient).ref ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
    case messageCompound(n) if connectionList.contains(n.recipient) ⇒
      connectionList(n.recipient).ref ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
    case (s: String, c: Connection) ⇒
      connectionList += (s → c)
      sender ! "ACK"
    case p : Product ⇒ p.productIterator.foreach( e ⇒ parseMessage(e.asInstanceOf[Message[Params]]))
  }

  def parseMessage(m: Message[_]): Unit = m match {
    case messageTarget(n) if connectionList.contains(n.recipient)⇒
      connectionList(n.recipient).ref ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
    case messageSpecial(n) if connectionList.contains(n.recipient)⇒
      connectionList(n.recipient).ref ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
    case messageCompound(n) if connectionList.contains(n.recipient)⇒
      connectionList(n.recipient).ref ! Write(serialize(n))
      println(s"Sending: ${serialize(n).utf8String}")
  }
}
