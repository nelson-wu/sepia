package Actors

import Messages._
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by Nelson on 2018/07/15.
  */
class SerializerSpec extends WordSpec with Matchers{
  def includes = afterWord("includes")
  "Message serializer" must {
    "serialize Messages to ByteString" which includes {
      "messages with NoParams" in {
        MessageSerializer.serialize(
          Message(
            NickCommand,
            Prefix("user"),
            NoParams,
            "server"
          )
        ).utf8String.trim shouldEqual ":user NICK"
      }
      "messages with Middle" in {
        MessageSerializer.serialize(
          Message(
            JoinCommand,
            Prefix("user"),
            Middle("#channel"),
            "server"
          )
        ).utf8String.trim shouldEqual ":user JOIN #channel"
      }
      "messages with Trailing" in {
        MessageSerializer.serialize(
          Message(
            PrivmsgCommand,
            Prefix("user"),
            Trailing("hello world"),
            "server"
          )
        ).utf8String.trim shouldEqual ":user PRIVMSG :hello world"
      }
      "messages with Compound" in {
        MessageSerializer.serialize(
          Message(
            ReplyCommand("233"),
            Prefix("user"),
            Compound(
              Seq(Middle("a"), Middle("b")),
              Trailing("hello world")
            ),
            "server"
          )
        ).utf8String.trim shouldEqual ":user 233 a b :hello world"
      }
    }
  }
}
