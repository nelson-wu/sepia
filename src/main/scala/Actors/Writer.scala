package ircserver

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp.Write

class Writer(connection: ActorRef) extends Actor {
  import MessageSerializer._
  import CanSerializeImplicits._

  def receive = {
    case n: Message ⇒ connection ! Write(serialize(n))
    case s: Seq[Message] ⇒ connection ! Write(serialize(s: _*))
  }
}
