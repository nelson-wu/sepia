/**
  * Created by Nelson on 2017/12/22.
  */


import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.Tcp.Write
import akka.io.{IO, Tcp}
import akka.util.ByteString

object Globals {
  val servername = "localhost"
}

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

class Users(connection: ActorRef) extends Actor{
  private val users = collection.mutable.Seq[String]()
  val writer = context.actorOf(Props(classOf[Writer], connection))
  def receive = {
    case Nick(nick) ⇒
      if (users.contains(nick)) writer ! ERR_NICKNAMEINUSE(nick)
      else {
        users +: nick
        writer ! RPL_WELCOME(nick)
      }
  }
}

class Writer(connection: ActorRef) extends Actor{
  def receive = {
    case n: Numerical ⇒ {
      println("Sending: " + n.toMessage.utf8String)
      connection ! Write(n.toMessage)
    }
  }
}

sealed trait Action
case class Nick(nick: String) extends Action
case object NoAction extends Action

object Action{
  def apply(input: String): Action = {
    val tokens = input.split(" ")
    //tokens foreach (println _)
    tokens(0) match {
      case "NICK" ⇒ Nick(tokens(1))
      case _ ⇒ NoAction
    }
  }
}

class SimplisticHandler extends Actor {
  import Tcp._
  val usersActor = context.actorOf(Props(classOf[Users], sender))
  context.watch(usersActor)
  def receive = {
    case Received(data) ⇒ {
      data.utf8String.split("\r\n").foreach { line ⇒
        val action = Action(line)
        println("Received: " + line + " " + action)
        action match {
          case a: Nick ⇒ usersActor ! a
          case NoAction ⇒ Unit
        }
      }
    }
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
    ByteString(s":${Globals.servername} ${err} ${user} :${message.replace(" ", "%20")}\r\n")
  }
}

case class ERR_NICKNAMEINUSE(user: String) extends Numerical{
  val err = "433"
}
case class RPL_WELCOME(user: String) extends Numerical{
  val err = "001"
  override val message = "Welcome to the network!"
}