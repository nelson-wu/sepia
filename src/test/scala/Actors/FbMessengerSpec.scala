package Actors

import FbMessenger._
import Messages.Implicits.ImplicitConversions.{ThreadId, UserName}
import Messages._
import akka.actor.{ActorSystem, Props}
import org.joda.time.Instant
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

import scala.concurrent.duration._

/**
  * Created by Nelson on 2018/08/12.
  */
class FbMessengerSpec extends WordSpec
  with Matchers
  with BeforeAndAfterEach {

  implicit val system = ActorSystem("fb-messenger-spec")

  def setup(testClient: BaseFbClient, tag: String = "") = {
    val probe = IrcTestProbe(tag)
    val fbMessenger = system.actorOf(Props(classOf[FbMessenger], probe.ref, probe.ref, testClient, true))
    (probe, fbMessenger)
  }

  "FB messenger actor" must {
    "synchronize with FB" which {
      "send NewFbMessage messages for new messages in threads" in {
         val client = StubFbClientCreator(
           threadList = Seq(
             Seq(
               FbThread("thread1", "1", false, 0, Set(Participant("a", "a")))
             )
           ),
           threadHistory = Seq(
             Seq(),
             Seq(FbMessage("hi", "1", "a", new Instant(1)))
           )
         )

        val (probe, fbMessenger) = setup(client, "FbMessage")
        fbMessenger ! Timing.Tick
        fbMessenger ! Timing.Tick
        probe.expectWithinDuration(
          NewFbMessage("a", "1", "hi")
        )

      }

      "send NewFbThread messages for new threads" in {
        val client = StubFbClientCreator(
          threadList = Seq(
            Seq(),
            Seq(FbThread("First Thread", "1", true, 0, Set(
              Participant("bob", "user2")
            )))
          ),
          threadHistory = Seq(Seq())
        )
        val (probe, fbMessenger) = setup(client, "FbThread")
        fbMessenger ! Timing.Tick
        fbMessenger ! Timing.Tick
        probe.expectWithinDuration(NewFbThread("#first-thread", ThreadId("1")))
      }
      "send FbUserJoin messages for new users in new threads" in {
        val client = StubFbClientCreator(
          threadList = Seq(
            Seq(),
            Seq(FbThread("First Thread", "1", true, 0, Set(
              Participant("bob", "user2")
            )))
          ),
          threadHistory = Seq(Seq())
        )
        val (probe, fbMessenger) = setup(client)
        fbMessenger ! Timing.Tick
        fbMessenger ! Timing.Tick
        probe.fishForMessage(10 seconds) {
          case msg: FbUserJoin if msg == FbUserJoin(UserName("bob"), "1")
            ⇒ true
          case _ ⇒ false
        }
      }
      "send FbUserJoin messages for new users in old threads" in {
        val client = StubFbClientCreator(
          threadList = Seq(
            Seq(),
            Seq(FbThread("First Thread", "1", true, 0, Set(
              Participant("Alice A", "user1"),
              Participant("Bob B", "user2")
            )))
          ),
          threadHistory = Seq(Seq())
        )
        val (probe, fbMessenger) = setup(client)
        fbMessenger ! Timing.Tick
        fbMessenger ! Timing.Tick
        probe.fishForMessage(10 seconds) {
          case msg: FbUserJoin if msg == FbUserJoin("bobb", "1")
            ⇒ true
          case _ ⇒ false
        }
      }
    }
  }
}
