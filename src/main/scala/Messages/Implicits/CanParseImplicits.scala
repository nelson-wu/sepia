package Messages.Implicits

import Messages._

/**
  * Created by Nelson on 2018/01/10.
  */
object CanParseImplicits {
  implicit object CanParseJoin extends CanParse[JoinCommand.type] {
    def parse(tokens: Seq[String]): Params = Target(tokens(1).drop(1))
  }
  implicit object CanParsePrivmsg extends CanParse[PrivmsgCommand.type] {
    def parse(tokens: Seq[String]): Params = {
      val text = tokens.drop(2).mkString(" ")
      val stripColon = text.drop(1)
      val destination = tokens(1)

      Compound(
        Seq(Target(destination)),
        Special(stripColon)
      )
    }
  }
}
