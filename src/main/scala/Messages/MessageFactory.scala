package Messages

class MessageFactory(recipient: String) {
  val serverName = "localhost"

  def RPL_WELCOME(user: String): Message[Compound] = {
    val params = Compound(
      Seq(Target(user)),
      Special(s"Welcome to the network $user!")
    )
    Message[Compound](ReplyCommand("001"), Prefix(serverName), params, recipient)
  }
  def ERR_NICKNAMEINUSE: Message[NoParams.type] = Message(ReplyCommand("433"), Prefix(serverName), NoParams, recipient)
  def JOIN(user: String, channel: String): Message[Special] = Message(JoinCommand, Prefix(user), Special(channel), recipient)

  def RPL_ENDOFNAMES(user: String, channel: String): Message[Compound] = {
    val params = Compound(
      Seq(Target(user), Target(channel)),
      Special("End of /NAMES list")
    )
    Message(ReplyCommand("366"), Prefix(serverName), params, recipient)
  }

  def RPL_NAMREPLY(user: String, channel: String, userList: Seq[String]): Message[Compound] = {
    val params = Compound(
      Seq(
        Target(user),
        Target("="),
        Target(channel)
      ),
      Special(userList.mkString(" "))
    )
    Message(ReplyCommand("353"), Prefix(serverName), params, recipient)
  }

  def RPL_TOPIC(user: String, channel: String, topic: String): Message[Compound] = {
    val params = Compound(
      Seq(
        Target(user),
        Target(channel)
      ),
      Special(topic)
    )
    Message(ReplyCommand("332"), Prefix(serverName), params, recipient)
  }
}

