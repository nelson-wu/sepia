package ircserver
import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp
import akka.io.Tcp.{PeerClosed, Received, Register}

class Dispatcher(connection: ActorRef) extends Actor {
  import Tcp._
  val writerActor = context.actorOf(Props(classOf[Writer], connection))
  val usersActor = context.actorOf(Props(classOf[Users], writerActor))
  val channelsActor = context.actorOf(Props(classOf[Channels], writerActor))
  implicit var userNick: String = ""
  connection ! Register(self)

  def receive = {
    case Received(data) ⇒ {
      data.utf8String.split("\r\n").foreach { line ⇒
        val message = Message(line)
        println("Received: " + line + " " + Message)
        updateCurrentNick(message)
        message.command match {
          case NickCommand ⇒ usersActor ! message
          case JoinCommand ⇒ channelsActor ! message
          //case m: Privmsg ⇒ channelsActor ! m
          case NoCommand ⇒ Unit
        }

        def updateCurrentNick(message: ircserver.Message): Unit = message match {
          case Message(NickCommand, Prefix(nick), _, _) ⇒ userNick = nick
        }
      }
    }
    case PeerClosed     ⇒ context stop self
  }
}