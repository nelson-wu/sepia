package Messages.Implicits

import Messages._

/**
  * Created by Nelson on 2018/01/10.
  */
object CanParseImplicits {
  implicit object CanParseJoin extends CanParse[JoinCommand.type] {
    def parse(tokens: Seq[String]): Params = Middle(tokens.head)
  }
  implicit object CanParsePrivmsg extends CanParse[PrivmsgCommand.type] {
    def parse(tokens: Seq[String]): Params = {
      val text = tokens.drop(1).mkString(" ")
      val stripColon = text.drop(1)
      val destination = tokens.head

      Compound(
        Seq(Middle(destination)),
        Trailing(stripColon)
      )
    }
  }
}
