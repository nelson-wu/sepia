package Actors
import Messages._
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout

class Dispatcher(connection: Connection, channelsActor: ActorRef, usersActor: ActorRef, writerActor: ActorRef) extends Actor with ActorLogging{
  import Tcp._
  implicit var userNick: String = ""
  connection.ref ! Register(self)

  def receive = {
    case Received(data) ⇒ {
      data.utf8String.split("\r\n").foreach { line ⇒
        val message = MessageParser.parse(line)
        log.debug("Recieved: " + line)
        println("Received: " + line + "\nParsed: " + message)
        message.command match {
          case NickCommand ⇒ //usersActor ! message
            updateCurrentNick(message)
          case JoinCommand | PartCommand | PrivmsgCommand ⇒ channelsActor ! message
          //case m: Privmsg ⇒ channelsActor ! m
          case NoCommand ⇒ Unit
        }

        def updateCurrentNick(message: Messages.Message[_]): Unit = message match {
          case Message(NickCommand, _, Target(nick), _) ⇒
            userNick = nick
            implicit val timeout = Timeout(5 seconds)
            val future = writerActor ? (userNick, connection)
            Await.result(future, timeout.duration)
            usersActor ! message
          case _ ⇒ Unit
        }
      }
    }
    case PeerClosed     ⇒ context stop self
  }
}