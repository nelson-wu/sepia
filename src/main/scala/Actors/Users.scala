package ircserver

import akka.actor.{Actor, ActorRef}

class Users(writer: ActorRef) extends Actor{
  import MessageFactory._
  private val users = collection.mutable.Seq[String]()
  def receive = {
    case Message(NickCommand, _, Target(nick), _) ⇒
      if (users.contains(nick)) writer ! ERR_NICKNAMEINUSE
      else {
        users +: nick
        writer ! RPL_WELCOME
      }
  }
}