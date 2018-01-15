package Messages

import Messages.Implicits.CanParse
import ircserver._

import scala.util.Try

object MessageParser{

  def parse(str: String)(implicit defaultSource: String): Message[Params] = {
    val tokens = str.split(" ")
    //println("source: " + defaultSource)
    //println("tokens(0): " + tokens(0))
    val (prefix, command, remainingTokens) = tokens(0).contains(":") match {
      case true ⇒ (Prefix(tokens(0)), Command(tokens(1)), tokens.drop(2))
      case false ⇒ (Prefix(defaultSource), Command(tokens(0)), tokens.drop(1))
    }
    val params = getParamsForCommand[command.type](remainingTokens)
    Message(command, prefix, params, "localhost")
  }

  def getParamsForCommand[A <: Command : CanParse](tokens: Seq[String]): Params = {
    Try(implicitly[CanParse[A]].parse(tokens)).getOrElse(NoParams)
  }
}