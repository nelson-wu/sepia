package Messages

import Messages.Implicits.ImplicitConversions.UserName

class MessageFactory(recipient: String) {
  val serverName = "localhost"

  def RPL_WELCOME(user: String): Message[Compound] = {
    val params = Compound(
      Seq(UserMiddle(user)),
      Trailing(s"Welcome to the network $user!")
    )
    Message[Compound](ReplyCommand("001"), Prefix(serverName), params, recipient)
  }
  def ERR_NICKNAMEINUSE: Message[NoParams.type] = Message(ReplyCommand("433"), Prefix(serverName), NoParams, recipient)
  def JOIN(user: String, channel: String): Message[Trailing] = Message(JoinCommand, Prefix(user), Trailing(channel), recipient)

  def RPL_ENDOFNAMES(user: UserName, channel: String): Message[Compound] = {
    val params = Compound(
      Seq(UserMiddle(user), AnyMiddle(channel)),
      Trailing("End of /NAMES list")
    )
    Message(ReplyCommand("366"), Prefix(serverName), params, recipient)
  }

  def RPL_NAMREPLY(user: UserName, channel: String, userList: Seq[UserName]): Message[Compound] = {
    val params = Compound(
      Seq(
        UserMiddle(user),
        AnyMiddle("="),
        AnyMiddle(channel)
      ),
      Trailing(userList.mkString(" "))
    )
    Message(ReplyCommand("353"), Prefix(serverName), params, recipient)
  }

  def RPL_TOPIC(user: UserName, channel: String, topic: String): Message[Compound] = {
    val params = Compound(
      Seq(
        UserMiddle(user),
        AnyMiddle(channel)
      ),
      Trailing(topic)
    )
    Message(ReplyCommand("332"), Prefix(serverName), params, recipient)
  }

  def PRIVMSG(user: String, channel: String, text: String): Message[Compound] = {
    val params = Compound(
      Seq(AnyMiddle(channel)),
      Trailing(text)
    )
    Message(PrivmsgCommand, Prefix(user), params, recipient)
  }
}

