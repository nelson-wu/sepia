package Actors

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}
import ircserver.{Globals, Dispatcher}

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
      val connection = sender()
      context.actorOf(Props(classOf[Dispatcher], connection))
    }
  }

}
