package Actors

import FbMessenger.{BaseFbClient, FbMessage, FbThread}
import Messages.Implicits.ImplicitConversions
import Messages.Implicits.ImplicitConversions.ThreadId
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by Nelson on 2018/08/12.
  */
class FbMessengerSpec extends WordSpec with Matchers {
  val testClient = new BaseFbClient {
    def getCurrentId(): String = "testId"
    abstract def getThreadHistory(threadId: ThreadId, highWaterMark: Long, limit: Option[Int]): Seq[FbMessage] = ???


    abstract def getThreadList(limit: Option[Int]): Seq[FbThread] = ???

  }
  "FB messenger actor" must {
    "synchronize with FB" which {
      "new messages in threads" in {

      }

      "new threads" in {

      }

      "new users in threads" in {

      }

      "existing users in new threads" in {

      }
    }
  }
}
