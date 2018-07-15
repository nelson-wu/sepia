package Actors

import Messages.{Message, MessageFactory, NickCommand, Middle}
import akka.actor.{Actor, ActorRef}

class Users(writer: ActorRef) extends Actor{
  private val users = collection.mutable.Buffer[String]()
  def receive = {
    case Message(NickCommand, _, Middle(nick), recipient) ⇒ {
      val factory = new MessageFactory(nick)
      import factory._

      if (users.contains(nick)) writer ! ERR_NICKNAMEINUSE
      else {
        users += nick
        //println(users)
        writer ! RPL_WELCOME(nick)
      }
    }
  }
}