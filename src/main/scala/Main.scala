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
  val port = 35353
}

object Main extends App{
  val system = ActorSystem.create("server-main")
  val serverActor = system.actorOf(Props[Server])
}

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
      context.actorOf(Props(classOf[SimplisticHandler], connection))
    }
  }

}

class Users(writer: ActorRef) extends Actor{
  private val users = collection.mutable.Seq[String]()
  def receive = {
    case Nick(nick) ⇒
      if (users.contains(nick)) writer ! ERR_NICKNAMEINUSE(nick)
      else {
        users +: nick
        writer ! RPL_WELCOME(nick)
      }
  }
}

class Channels(writer: ActorRef) extends Actor {
  val channels = collection.mutable.Map[String, collection.mutable.Set[String]]()
  def receive = {
    case j @ Join(user, channel) if !isUserInChannel(user, channel) ⇒ {
      channels(user) + channel
      writer ! JoinResponse(user, channel)
    }
  }
  def isUserInChannel(user: String, channel: String): Boolean = {
    if (!channels.contains(user)) channels(user) = collection.mutable.Set[String]()
    channels(user).contains(channel)
  }
}

class Writer(connection: ActorRef) extends Actor{
  def receive = {
    case n: Response ⇒ {
      println("Sending: " + n.toMessage.utf8String)
      connection ! Write(n.toMessage)
    }
  }
}

sealed trait Action
sealed trait UserAction extends Action{
  def nick: String
}
case class Nick(nick: String) extends UserAction
case class Join(nick: String, channel: String) extends UserAction
case object NoAction extends Action

object Action{
  def apply(input: String)(implicit user: String = ""): Action = {
    val tokens = input.split(" ")
    //tokens foreach (println _)
    tokens(0) match {
      case "NICK" ⇒ Nick(tokens(1))
      case "JOIN" ⇒ Join(user, tokens(1))
      case _ ⇒ NoAction
    }
  }
}

class SimplisticHandler(connection: ActorRef) extends Actor {
  import Tcp._
  val writerActor = context.actorOf(Props(classOf[Writer], connection))
  val usersActor = context.actorOf(Props(classOf[Users], writerActor))
  val channelsActor = context.actorOf(Props(classOf[Channels], writerActor))
  implicit var userNick = ""
  connection ! Register(self)

  def receive = {
    case Received(data) ⇒ {
      data.utf8String.split("\r\n").foreach { line ⇒
        val action = Action(line)
        println("Received: " + line + " " + action)
        action match {
          case a: Nick ⇒
            userNick = a.nick
            usersActor ! a
          case j: Join ⇒
            channelsActor ! j
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
  def toMessage: ByteString
}
sealed trait Numerical extends Response {
  val err: String
  def toMessage: ByteString = {
    ByteString(s":${Globals.servername} ${err} ${user} :${message}\r\n")
  }
}
sealed trait Command extends Response{
  val command: String
  def toMessage: ByteString = {
    ByteString(s"$user $command $message\r\n")
  }
}

case class ERR_NICKNAMEINUSE(user: String) extends Numerical{
  val err = "433"
}

case class RPL_WELCOME(user: String) extends Numerical{
  val err = "001"
  override val message = "Welcome to the network!"
}

case class JoinResponse(user: String, channel: String) extends Command {
  val command = "JOIN"
  override val toMessage = ByteString("Nelson JOIN #channel\r\n332 #channel :test\r\n353 Nelson = #channel :user Nelson\r\n:localhost 366 Nelson #channel :End of NAMES list\r\n:user!user@localhost PRIVMSG #channel :test\r\n")
  override val message = s"$channel"
}