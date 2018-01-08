package ircserver

import akka.actor.{Actor, ActorRef}
import MessageFactory._

class Channels(writer: ActorRef) extends Actor {
  case class Channel (topic: String, users: Seq[String] = Seq(), log: Seq[String] = Seq())
  val channels = collection.mutable.Map[String, Channel]()
  def receive = {
    case Message(JoinCommand, Prefix(user), Target(channel), _) if !isUserInChannel(user, channel) ⇒ {
      val newUsers = channels(channel).users :+ user
      val newChannel = channels(channel).copy(users = newUsers)
      channels(channel) = newChannel
      val macroResponse = Seq(
        JOIN(user, channel),
        RPL_TOPIC(user, channel, channels(channel).topic),
        RPL_NAMREPLY(user, channel, channels(channel).users),
        RPL_ENDOFNAMES(user, channel)
      )
//      val responses = Seq(
//        JoinResponse(user, channel),
//        RPL_NAMREPLY(user, channel, channels(channel).users :+ "test"),
//        RPL_ENDOFNAMES(user, channel),
//        MessageResponse("test", channel, "test")
//      )
//      val macroResponse = MacroResponse(user, responses)
      writer ! macroResponse
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
    if (!channels.contains(channel)) channels(channel) = Channel("channel topic")
    channels(channel).users.contains(user)
  }
}