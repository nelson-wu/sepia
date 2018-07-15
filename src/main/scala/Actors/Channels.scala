package Actors

import Messages.Implicits.ImplicitConversions.{ChannelName, ThreadId, UserName}
import akka.actor.{Actor, ActorLogging, ActorRef}
import Messages._

import scala.collection.mutable

class Channels(writer: ActorRef) extends Actor with ActorLogging {
  case class Channel (topic: String, users: mutable.Buffer[UserName] = mutable.Buffer(), log: mutable.Buffer[String] = mutable.Buffer())
  private val channels = collection.mutable.Map[String, Channel]()
  private val channelIdMap = collection.mutable.Map[ThreadId, String]()

  def receive = {
    case Message(JoinCommand, Prefix(user), Middle(channel), _) if !isUserInChannel(user, channel) ⇒ {
      val factory = new MessageFactory(user)
      import factory._
      channels(channel).users += user

      val joinMessages = channels(channel).users.map { u ⇒
        val userFactory = new MessageFactory(u.value)
        userFactory.JOIN(user, channel)
      }
      joinMessages.foreach(writer ! _)

      val macroResponse = (
        RPL_TOPIC(user, channel, channels(channel).topic),
        RPL_NAMREPLY(user, channel, channels(channel).users),
        RPL_ENDOFNAMES(user, channel)
      )
      writer ! macroResponse
    }

    case Message(NewFbThreadCommand(threadName, threadId), _, _, _) if !channelIdMap.contains(threadId) ⇒ {
      channelIdMap += (threadId → threadName)
      if(!channels.contains(threadName)) channels += (threadName → new Channel(threadName))
    }

    case Message(PrivmsgCommand, Prefix(user), Compound(list, message), _) ⇒ {
      val channel = list.head.underlying
      channels(channel).log += message.text
      val messages = channels(channel).users.filter(_ != UserName(user)).map { u ⇒
        val factory = new MessageFactory(u.value)
        factory.PRIVMSG(user, channel, message.text)
      }
      messages.foreach(writer ! _)
    }

    case Message(PartCommand, Prefix(user), Middle(channel), _) if isUserInChannel(user, channel)⇒ {
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
    // TODO: Deprecate this, since users don't need to create chanels
    if (!channels.contains(channel)) channels += (channel → Channel("channel topic"))
    channels(channel).users.contains(user)
  }

}