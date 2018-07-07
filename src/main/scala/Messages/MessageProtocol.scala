package Messages
import Messages.Implicits.ImplicitConversions.{ThreadId, UserName}

// From https://tools.ietf.org/html/rfc2812#page-5

trait BaseMessage
case class Message[+P <: Params](command: Command, prefix: Prefix, params: P = NoParams, recipient: String) extends BaseMessage

case class Prefix(name: String)

trait Params
trait Target extends Params {val underlying: String }
case class UserTarget(target: UserName) extends Target {
  val underlying: String = target.value
}
case class AnyTarget(underlying: String) extends Target
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

trait InternalCommand extends Command
case class NewFbUserCommand(text: String, userId: String) extends InternalCommand{
  val userName = UserName(text)
}
case class NewFbThreadCommand(text: String, threadId: ThreadId) extends InternalCommand

object Command {
  def apply(str: String): Command = str match {
    case "NICK" ⇒ NickCommand
    case "JOIN" ⇒ JoinCommand
    case "PRIVMSG" ⇒ PrivmsgCommand
    case "PART" ⇒ PartCommand
    case _ ⇒ NoCommand
  }
}

object Target{
  def apply(value: String) = new AnyTarget(value)
  def unapply(arg: Target): Option[String] = arg.underlying match {
    case null ⇒ None
    case s ⇒ Some(s)
  }
}
