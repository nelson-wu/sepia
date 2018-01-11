package ircserver

/**
  * Created by Nelson on 2018/01/10.
  */
object CanParseImplicits {
  implicit object CanParseJoin extends CanParse[JoinCommand.type] {
    def parse(tokens: Seq[String]): Params = Target(tokens(1).drop(1))
  }
  implicit object CanParsePrivmsg extends CanParse[PrivmsgCommand.type] {
    def parse(tokens: Seq[String]): Params = Compound(
      Seq(Target(tokens(1))),
      Special(tokens(2).drop(1))
    )
  }
}
