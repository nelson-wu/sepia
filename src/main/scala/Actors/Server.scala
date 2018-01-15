package Actors

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import ircserver.Globals

class Server extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", Globals.port))
  println("Listening on " + Globals.port)

  def receive = {
    case b @ Bound(localAddress) ⇒
      context.parent ! b

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) ⇒ {
      val connection = new Connection(sender())
      context.actorOf(Props(classOf[Dispatcher], connection.ref))
    }
  }

}

class Connection(val ref: ActorRef) extends AnyVal