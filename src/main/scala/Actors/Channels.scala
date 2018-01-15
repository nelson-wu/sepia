package Actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import Messages._

import scala.collection.mutable

class Channels(writer: ActorRef) extends Actor with ActorLogging {
  case class Channel (topic: String, users: mutable.Buffer[String] = mutable.Buffer(), log: mutable.Buffer[String] = mutable.Buffer())
  val channels = collection.mutable.Map[String, Channel]()
  def receive = {
    case Message(JoinCommand, Prefix(user), Target(channel), recipient) if !isUserInChannel(user, channel) ⇒ {
      val factory = new MessageFactory(user)
      import factory._
      //println(channels(channel))
      channels(channel).users += user
      //println(channels(channel).users)
      val macroResponse = (
        JOIN(user, channel),
        RPL_TOPIC(user, channel, channels(channel).topic),
        RPL_NAMREPLY(user, channel, channels(channel).users),
        RPL_ENDOFNAMES(user, channel)
      )
      writer ! macroResponse
    }
    case Message(PrivmsgCommand, Prefix(user), Compound(list, message), _) ⇒ {
      val channel = list.head.target
      channels(channel).log += message.text
    }
    case Message(PartCommand, Prefix(user), Target(channel), _) if isUserInChannel(user, channel)⇒ {
      channels(channel).users -= user
    }

    //    case Privmsg(user, channel, message) ⇒ {
    //      val newLog = channels(channel).log :+ message
    //      val newChannel = channels(channel).copy(log = newLog)
    //      channels(channel) = newChannel
    //      println("Log: ")
    //      channels(channel).log foreach { println _ }
    //    }
  }
  def isUserInChannel(user: String, channel: String): Boolean = {
    // TODO: Implement creating channels
    if (!channels.contains(channel)) channels += (channel → Channel("channel topic"))
    channels(channel).users.contains(user)
  }
}