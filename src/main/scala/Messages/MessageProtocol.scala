package Messages

// From https://tools.ietf.org/html/rfc2812#page-5

trait BaseMessage
case class Message[+P <: Params](command: Command, prefix: Prefix, params: P = NoParams, recipient: String) extends BaseMessage

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
case object PrivmsgCommand extends Command { val text = "PRIVMSG" }
case object JoinCommand extends Command { val text = "JOIN" }
case object PartCommand extends Command { val text = "PART" }

object Command {
  def apply(str: String): Command = str match {
    case "NICK" ⇒ NickCommand
    case "JOIN" ⇒ JoinCommand
    case "PRIVMSG" ⇒ PrivmsgCommand
    case "PART" ⇒ PartCommand
    case _ ⇒ NoCommand
  }
}


