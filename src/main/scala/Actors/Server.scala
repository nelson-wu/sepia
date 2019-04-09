package Actors

import java.net.InetSocketAddress

import Actors.DataTypes.Connection
import Fb.FbClient
import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}
import ircserver.Globals

class Server extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", Globals.port))
  println("Listening on " + Globals.port)

  val writerActor = context.actorOf(Props(classOf[Writer]))
  val usersActor = context.actorOf(Props(classOf[Users], writerActor))
  val channelsActor = context.actorOf(Props(classOf[Channels], writerActor))
  val fbClient = new FbClient(system)
  val fbMessengerActor = context.actorOf(Props(classOf[FbMessenger], usersActor, channelsActor, fbClient, false))

  def receive = {
    case b @ Bound(localAddress) ⇒
      context.parent ! b

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) ⇒ {
      val connection = new Connection(sender())


      context.actorOf(Props(classOf[Dispatcher], connection.ref, channelsActor, usersActor, writerActor))
    }
  }

}

