package ircserver

object MessageFactory {
  val serverName = "localhost"
  object RPL_WELCOME {
    val apply: Message = new Message(ReplyCommand("001"), Prefix(serverName), Some(Special(s"Welcome to the network!")))
  }
  object ERR_NICKNAMEINUSE {
    val apply: Message = new Message(ReplyCommand("433"), Prefix(serverName))
  }
  object JOIN {
    def apply(user: String, channel: String) = new Message(ReplyCommand("JOIN"), Prefix(serverName), Some(Special(channel)))
  }
}

