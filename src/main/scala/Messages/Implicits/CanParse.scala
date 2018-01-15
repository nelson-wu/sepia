package Messages.Implicits

import Messages.{Command, Params, Target}

trait CanParse[-A <: Command] {
  def parse(tokens: Seq[String]): Params
}

// low priority implicit typeclasses
object CanParse {
  implicit val CanParseOnlyTarget: CanParse[Command] = new CanParse[Command]{
    def parse(tokens: Seq[String]): Params = {
      Target(tokens(0))
    }
  }
}
