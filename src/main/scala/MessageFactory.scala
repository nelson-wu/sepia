package ircserver

object MessageFactory {
  val serverName = "localhost"
  object RPL_WELCOME {
    val apply: Message = new Message(ReplyCommand("001"), Prefix(serverName), Special(s"Welcome to the network!"))
  }
  object ERR_NICKNAMEINUSE {
    val apply: Message = new Message(ReplyCommand("433"), Prefix(serverName))
  }
  object JOIN {
    def apply(user: String, channel: String): Message = new Message(JoinCommand, Prefix(serverName), Special(channel))
  }

  object RPL_ENDOFNAMES {
    def apply(user: String, channel: String): Message = {
      val params = Compound(
        Seq(Target(user), Target(channel)),
        Special("End of /NAMES list")
      )
      new Message(ReplyCommand("366"), Prefix(serverName), params)
    }
  }

  object RPL_NAMREPLY {
    def apply(user: String, channel: String, userList: Seq[String]): Message = {
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
  }

  object RPL_TOPIC {
    def apply(user: String, channel: String, topic: String): Message = {
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
}

