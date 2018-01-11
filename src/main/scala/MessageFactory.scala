package ircserver

object MessageFactory {
  val serverName = "localhost"
  def RPL_WELCOME(user: String): Message[Compound] = {
    val params = Compound(
      Seq(Target(user)),
      Special(s"Welcome to the network $user!")
    )
    new Message(ReplyCommand("001"), Prefix(serverName), params)
  }
  def ERR_NICKNAMEINUSE: Message[NoParams.type] = new Message(ReplyCommand("433"), Prefix(serverName))
  def JOIN(user: String, channel: String): Message[Special] = new Message(JoinCommand, Prefix(user), Special(channel))

  def RPL_ENDOFNAMES(user: String, channel: String): Message[Compound] = {
    val params = Compound(
      Seq(Target(user), Target(channel)),
      Special("End of /NAMES list")
    )
    new Message(ReplyCommand("366"), Prefix(serverName), params)
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
    new Message(ReplyCommand("353"), Prefix(serverName), params)
  }

  def RPL_TOPIC(user: String, channel: String, topic: String): Message[Compound] = {
    val params = Compound(
      Seq(
        Target(user),
        Target(channel)
      ),
      Special(topic)
    )
    new Message(ReplyCommand("332"), Prefix(serverName), params)
  }
}

