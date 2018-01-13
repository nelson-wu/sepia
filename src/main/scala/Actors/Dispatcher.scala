package ircserver
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp
import akka.io.Tcp.{PeerClosed, Received, Register}

class Dispatcher(connection: ActorRef) extends Actor with ActorLogging{
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
        log.debug("Recieved: " + line)
        println("Received: " + line + "\nParsed: " + message)
        updateCurrentNick(message)
        message.command match {
          case NickCommand ⇒ usersActor ! message
          case JoinCommand | PartCommand | PrivmsgCommand ⇒ channelsActor ! message
          //case m: Privmsg ⇒ channelsActor ! m
          case NoCommand ⇒ Unit
        }

        def updateCurrentNick(message: ircserver.Message[_]): Unit = message match {
          case Message(NickCommand, _, Target(nick), _) ⇒ userNick = nick
          case _ ⇒ Unit
        }
      }
    }
    case PeerClosed     ⇒ context stop self
  }
}