package Actors

import org.scalatest.WordSpec

/**
  * Created by Nelson on 2018/07/15.
  */
class SerializerSpec extends WordSpec{
  def include = afterWord("include")
  "Message serializer" must {
    "resolve erased message Params types" which include {
      "NoParams" in {

      }
      "Middle params" in {

      }
      "Trailing params" in {

      }
      "UserList params" in {

      }
    }
    "serialize Messages to ByteString" which include {
      "messages from server" in {

      }
      "messages from another user" in {

      }
      "messages with user list" in {

      }
      "numeric reply messages" in {

      }
    }
  }
}
