package Messages

import Messages.Implicits.ImplicitConversions.UserName
import ircserver.Globals

class MessageFactory(recipient: String) {

  def RPL_WELCOME(user: String): Message[Compound] = {
    val params = Compound(
      Seq(Middle(user)),
      Trailing(s"Welcome to the network $user!")
    )
    Message[Compound](ReplyCommand("001"), Prefix(Globals.serverName), params, recipient)
  }
  def ERR_NICKNAMEINUSE: Message[NoParams.type] = Message(ReplyCommand("433"), Prefix(Globals.serverName), NoParams, recipient)
  def JOIN(user: String, channel: String): Message[Middle] = Message(JoinCommand, Prefix(user), Middle(channel), recipient)

  def RPL_ENDOFNAMES(user: UserName, channel: String): Message[Compound] = {
    val params = Compound(
      Seq(Middle(user.value), Middle(channel)),
      Trailing("End of NAMES list")
    )
    Message(ReplyCommand("366"), Prefix(Globals.serverName), params, recipient)
  }

  def RPL_NAMREPLY(user: UserName, channel: String, userList: Seq[UserName]): Message[Compound] = {
    val params = Compound(
      Seq(
        Middle(user.value),
        Middle("="),
        Middle(channel)
      ),
      Trailing(userList.map(_.value).mkString(" "))
    )
    Message(ReplyCommand("353"), Prefix(Globals.serverName), params, recipient)
  }

  def RPL_TOPIC(user: UserName, channel: String, topic: String): Message[Compound] = {
    val params = Compound(
      Seq(
        Middle(user.value),
        Middle(channel)
      ),
      Trailing(topic)
    )
    Message(ReplyCommand("332"), Prefix(Globals.serverName), params, recipient)
  }

  def RPL_NOTOPIC(user: UserName, channel: String): Message[Compound] = {
    val params = Compound(
      Seq(
        Middle(user.value),
        Middle(channel)
      ),
      Trailing("No topic is set")
    )
    Message(ReplyCommand("331"), Prefix(Globals.serverName), params, recipient)
  }

  def PRIVMSG(user: String, channel: String, text: String): Message[Compound] = {
    val params = Compound(
      Seq(Middle(channel)),
      Trailing(text)
    )
    Message(PrivmsgCommand, Prefix(user), params, recipient)
  }
}

