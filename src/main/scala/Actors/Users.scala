package ircserver

import akka.actor.{Actor, ActorRef}

class Users(writer: ActorRef) extends Actor{
  import MessageFactory._
  private val users = collection.mutable.Seq[String]()
  def receive = {
    case Message(NickCommand, _, Some(Target(nick)), _) ⇒
      if (users.contains(nick)) writer ! ERR_NICKNAMEINUSE(nick)
      else {
        users +: nick
        writer ! RPL_WELCOME
      }
  }
}