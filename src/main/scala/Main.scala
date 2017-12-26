/**
  * Created by Nelson on 2017/12/22.
  */


import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString

val servername = "localhost"

object Main extends App{
  val system = ActorSystem.create("server-main")
  val serverActor = system.actorOf(Props[Server])
}

class Server extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 35353))

  def receive = {
    case b @ Bound(localAddress) ⇒
      context.parent ! b

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) ⇒ {
      val handler = context.actorOf(Props[SimplisticHandler])
      val connection = sender()
      connection ! Register(handler)
    }
  }

}

class UsersActor extends Actor{
  private val users = collection.mutable.Seq[String]()
  val writer = context.actorOf(Props[Writer])
  def receive = {
    case Nick(nick) ⇒
      if (users.contains(nick)) writer ! ERR_NICKNAMEINUSE(nick)
      else {
        users +: nick
        writer ! RPL_WELCOME(nick)
      }
  }
}

class Writer extends Actor{
  import Tcp._
  def receive = {
    case n: Numerical ⇒ sender ! Write(n.toMessage)
  }
}

sealed trait Action
case class Nick(nick: String) extends Action

object Action{
  def apply(input: String): Action = {
    val tokens = input.split(" ")
    tokens(0) match {
      case "NICK" ⇒ Nick(tokens(1))
    }
  }
}

class SimplisticHandler extends Actor {
  import Tcp._
  def receive = {
    case Received(data) ⇒
      println(data)
      sender ! Write(data)
    case PeerClosed     ⇒ context stop self
  }
}

sealed trait Response {
  val user: String
  val message: String = ""
}
sealed trait Numerical extends Response {
  val err: String
  def toMessage: ByteString = {
    ByteString(s":$servername ${err} ${user} ${message}")
  }
}

case class ERR_NICKNAMEINUSE(user: String) extends Numerical{
  val err = "433"
}
case class RPL_WELCOME(user: String) extends Numerical{
  val err = "001"
}