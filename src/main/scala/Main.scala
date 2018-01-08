package ircserver
/**
  * Created by fujiko on 2017/12/22.
  */

import java.net.InetSocketAddress

import Actors.Server
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

//class Server extends Actor {
//
//  import Tcp._
//  import context.system
//
//  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", Globals.port))
//  println("Listening on " + Globals.port)
//
//  def receive = {
//    case b @ Bound(localAddress) ⇒
//      context.parent ! b
//
//    case CommandFailed(_: Bind) ⇒ context stop self
//
//    case c @ Connected(remote, local) ⇒ {
//      val connection = sender()
//      context.actorOf(Props(classOf[Handler], connection))
//    }
//  }
//
//}

//class Users(writer: ActorRef) extends Actor{
//  private val users = collection.mutable.Seq[String]()
//  def receive = {
//    case Nick(nick) ⇒
//      if (users.contains(nick)) writer ! ERR_NICKNAMEINUSE(nick)
//      else {
//        users +: nick
//        writer ! RPL_WELCOME(nick)
//      }
//  }
//}

//case class Channel (topic: String, users: Seq[String] = Seq(), log: Seq[String] = Seq())
//
//class Channels(writer: ActorRef) extends Actor {
//  val channels = collection.mutable.Map[String, Channel]()
//  def receive = {
//    case j @ Join(user, channel) if !isUserInChannel(user, channel) ⇒ {
//      val newUsers = channels(channel).users :+ user
//      val newChannel = channels(channel).copy(users = newUsers)
//      channels(channel) = newChannel
//      val responses = Seq(
//        JoinResponse(user, channel),
//        RPL_NAMREPLY(user, channel, channels(channel).users :+ "test"),
//        RPL_ENDOFNAMES(user, channel),
//        MessageResponse("test", channel, "test")
//      )
//      val macroResponse = MacroResponse(user, responses)
//      writer ! macroResponse
//    }
//    case Privmsg(user, channel, message) ⇒ {
//      val newLog = channels(channel).log :+ message
//      val newChannel = channels(channel).copy(log = newLog)
//      channels(channel) = newChannel
//      println("Log: ")
//      channels(channel).log foreach { println _ }
//    }
//  }
//  def isUserInChannel(user: String, channel: String): Boolean = {
//    // TODO: Implement creating channels
//    if (!channels.contains(channel)) channels(channel) = Channel("channel")
//    channels(channel).users.contains(user)
//  }
//}

//class Writer(connection: ActorRef) extends Actor {
//
//  def send(n: Response) = {
//    println("Sending: " + n.toMessage.utf8String)
//    connection ! Write(n.toMessage)
//  }
//
//  def receive = {
//    case n: Response ⇒ send(n)
//  }
//}

//sealed trait Action
//sealed trait UserAction extends Action{
//  def nick: String
//}
//case class Nick(nick: String) extends UserAction
//case class Join(nick: String, channel: String) extends UserAction
//case class Privmsg(nick: String, channel: String, message: String) extends UserAction
//case object NoAction extends Action

//object Action{
//  def apply(input: String)(implicit user: String = ""): Action = {
//    val tokens = input.split(" ")
//    //tokens foreach (println _)
//    tokens(0) match {
//      case "NICK" ⇒ Nick(tokens(1))
//      case "JOIN" ⇒ Join(user, tokens(1))
//      case "PRIVMSG" ⇒ Privmsg(user, tokens(1), tokens.drop(2).mkString(" ").drop(1))
//      case _ ⇒ NoAction
//    }
//  }
//}

//class Handler(connection: ActorRef) extends Actor {
//  import Tcp._
//  val writerActor = context.actorOf(Props(classOf[Writer], connection))
//  val usersActor = context.actorOf(Props(classOf[Users], writerActor))
//  val channelsActor = context.actorOf(Props(classOf[Channels], writerActor))
//  implicit var userNick = ""
//  connection ! Register(self)
//
//  def receive = {
//    case Received(data) ⇒ {
//      data.utf8String.split("\r\n").foreach { line ⇒
//        //val action = Action(line)
//        println("Received: " + line + " " + action)
//        action match {
//          case a: Nick ⇒
//            userNick = a.nick
//            usersActor ! a
//          case j: Join ⇒
//            channelsActor ! j
//          case m: Privmsg ⇒ channelsActor ! m
//          case NoAction ⇒ Unit
//        }
//      }
//    }
//    case PeerClosed     ⇒ context stop self
//  }
//}

sealed trait Response {
  val user: String
  val message: String = ""
  val channel: String = ""
  def toMessage: ByteString
}
case class MacroResponse(user: String, responses: Seq[Response]) extends Response{
  override def toMessage: ByteString = responses.map(_.toMessage).reduce(_ ++ _) ++ ByteString("\r\n")
}
sealed trait Numerical extends Response {
  val err: String
  def toMessage: ByteString = {
    ByteString(s":${Globals.servername} $err $user $channel :$message\r\n")
  }
}
//sealed trait Command extends Response{
//  val command: String
//  def toMessage: ByteString = {
//    ByteString(s":$user $command $channel $message\r\n")
//  }
//}

//case class ERR_NICKNAMEINUSE(user: String) extends Numerical{
//  val err = "433"
//}

//case class RPL_WELCOME(user: String) extends Numerical{
//  val err = "001"
//  override val message = "Welcome to the network!"
//}

//case class JoinResponse(user: String, override val channel: String) extends Command {
//  val command = "JOIN"
//  override val message = channel
//}

//case class RPL_NAMREPLY(user: String, _channel: String, userList: Seq[String]) extends Numerical {
//  val err = "353"
//  override val channel = "= " + _channel
//  override val message = s"${userList.mkString(" ")}"
//}
//
//case class RPL_ENDOFNAMES(user: String, override val channel: String) extends Numerical {
//  val err = "366"
//  override val message = "End of /NAMES list"
//}
//
//case class RPL_TOPIC(user: String, override val channel: String, topic: String) extends Numerical {
//  val err = "332"
//  override val message = topic
//}

//case class MessageResponse(user: String, override val channel: String, override val message: String) extends Command {
//  val command = "PRIVMSG"
//}