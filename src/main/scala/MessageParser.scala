package ircserver

import scala.annotation.implicitNotFound
import scala.util.{Failure, Success, Try}

object MessageParser{

  def parse(str: String)(implicit defaultSource: String): Message = {
    val tokens = str.split(" ")
    val (prefix, command, remainingTokens) = tokens(0).contains(":") match {
      case true ⇒ (Prefix(tokens(0)), Command(tokens(1)), tokens.drop(2))
      case false ⇒ (Prefix(defaultSource), Command(tokens(0)), tokens.drop(1))
    }
    val params = getParamsForCommand[command.type](remainingTokens)
    Message(command, prefix, params)
  }

  def getParamsForCommand[A <: Command : CanParse](tokens: Seq[String]): Params = {
    Try(implicitly[CanParse[A]].parse(tokens)).getOrElse(NoParams)
  }
}