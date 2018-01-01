package ircserver

trait CanParse[-A <: Command] {
  def parse(tokens: Seq[String]): Params
}

// low priority implicit typeclasses
object CanParse {
  implicit val CanParseOnlyTarget: CanParse[Command] = {
    def parse(tokens: Seq[String]): Params = {
      Target(tokens(0))
    }
  }
}
