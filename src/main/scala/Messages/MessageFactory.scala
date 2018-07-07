package Messages

import Messages.Implicits.ImplicitConversions.UserName

class MessageFactory(recipient: String) {
  val serverName = "localhost"

  def RPL_WELCOME(user: UserName): Message[Compound] = {
    val params = Compound(
      Seq(UserTarget(user)),
      Special(s"Welcome to the network $user!")
    )
    Message[Compound](ReplyCommand("001"), Prefix(serverName), params, recipient)
  }
  def ERR_NICKNAMEINUSE: Message[NoParams.type] = Message(ReplyCommand("433"), Prefix(serverName), NoParams, recipient)
  def JOIN(user: String, channel: String): Message[Special] = Message(JoinCommand, Prefix(user), Special(channel), recipient)

  def RPL_ENDOFNAMES(user: UserName, channel: String): Message[Compound] = {
    val params = Compound(
      Seq(UserTarget(user), AnyTarget(channel)),
      Special("End of /NAMES list")
    )
    Message(ReplyCommand("366"), Prefix(serverName), params, recipient)
  }

  def RPL_NAMREPLY(user: UserName, channel: String, userList: Seq[UserName]): Message[Compound] = {
    val params = Compound(
      Seq(
        UserTarget(user),
        AnyTarget("="),
        AnyTarget(channel)
      ),
      Special(userList.mkString(" "))
    )
    Message(ReplyCommand("353"), Prefix(serverName), params, recipient)
  }

  def RPL_TOPIC(user: UserName, channel: String, topic: String): Message[Compound] = {
    val params = Compound(
      Seq(
        UserTarget(user),
        AnyTarget(channel)
      ),
      Special(topic)
    )
    Message(ReplyCommand("332"), Prefix(serverName), params, recipient)
  }

  def PRIVMSG(user: String, channel: String, text: String): Message[Compound] = {
    val params = Compound(
      Seq(AnyTarget(channel)),
      Special(text)
    )
    Message(PrivmsgCommand, Prefix(user), params, recipient)
  }
}

