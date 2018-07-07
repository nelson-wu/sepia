package Messages

import Messages.Implicits.CanParse
import ircserver.Globals

import scala.util.Try

object MessageParser{

  def parse(str: String)(implicit defaultSource: String): Message[Params] = {
    import Implicits.CanParseImplicits._
    val tokens = str.split(" ")
    //println("source: " + defaultSource)
    //println("tokens(0): " + tokens(0))
    val (prefix, command, remainingTokens) = tokens(0).contains(":") match {
      case true ⇒ (Prefix(tokens(0)), Command(tokens(1)), tokens.drop(2))
      case false ⇒ (Prefix(defaultSource), Command(tokens(0)), tokens.drop(1))
    }
    //println("tokens: ")
    //remainingTokens foreach (println _)
    val canParse = command.text match {
      case "NICK" ⇒ implicitly[CanParse[NickCommand.type]]
      case "JOIN" ⇒ implicitly[CanParse[JoinCommand.type]]
      case "PRIVMSG" ⇒ implicitly[CanParse[PrivmsgCommand.type]]
      case "PART" ⇒ implicitly[CanParse[PartCommand.type]]
      case _ ⇒ implicitly[CanParse[NoCommand.type]]
    }
    val params = getParamsForCommand(remainingTokens)(canParse)
    Message(command, prefix, params, Globals.serverName)
  }

  def getParamsForCommand[A <: Command](tokens: Seq[String])(implicit ev: CanParse[A]): Params = {
    Try(ev.parse(tokens)).getOrElse(NoParams)
  }

}