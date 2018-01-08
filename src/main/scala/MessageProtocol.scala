package ircserver

// From https://tools.ietf.org/html/rfc2812#page-5

trait BaseMessage
case class Message(command: Command, prefix: Prefix, params: Params = NoParams, recipient: Option[String] = None) extends BaseMessage

object Message {
  def apply(str: String)(implicit source: String): Message = MessageParser.parse(str)(source)
}

case class Prefix(name: String)

trait Params
case class Target(target: String) extends Params
case class UserList(channel: String, users: Seq[String]) extends Params
case class Special(text: String) extends Params
case class Compound(targets: Seq[Target], special: Special) extends Params
case object NoParams extends Params

trait Command { def text: String }
case class ReplyCommand(text: String) extends Command
case object NickCommand extends Command { val text = "NICK" }
case object NoCommand extends Command { val text = "" }
case object JoinCommand extends Command { val text = "JOIN" }

object Command {
  def apply(str: String): Command = str match {
    case "NICK" ⇒ NickCommand
    case "JOIN" ⇒ JoinCommand
    case _ ⇒ NoCommand
  }
}


