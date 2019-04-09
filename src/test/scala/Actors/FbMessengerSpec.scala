package Actors

import Actors.DataTypes.Timing
import Fb._
import Messages.Implicits.ImplicitConversions.{ThreadId, UserId, UserName}
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

  implicit val system = ActorSystem(this.getClass.getSimpleName)

  def setup(testClient: BaseFbClient, tag: String = "") = {
    val probe = IrcTestProbe(tag, 20 seconds)
    val fbMessenger = system.actorOf(Props(classOf[FbMessenger], probe.ref, probe.ref, testClient, true))
    (probe, fbMessenger)
  }

  "FB messenger actor" must {
    "synchronize with FB" which {
      "send NewFbMessage messages for new messages in threads" in {
         val client = StubFbClientCreator(
           threadList = Seq(
             Seq(
               FbThread("thread1", "thread1", false, 0, Set(Participant("a", "userA")))
             )
           ),
           threadHistory = Seq(
             Seq(),
             Seq(FbMessage("hi", "userA", new Instant(1)))
           )
         )

        val (probe, fbMessenger) = setup(client, "FbMessage")
        fbMessenger ! Timing.Tick
        fbMessenger ! Timing.Tick
        probe.expectWithinDuration(
          NewFbMessage(UserId("userA"), ThreadId("thread1"), "hi")
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
        probe.expectWithinDuration(
          NewFbThread("#first-thread", ThreadId("1"))
        )
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
        probe.expectWithinDuration(
          FbUserJoin(UserName("bob"), UserId("user2"), "1")
        )
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
        probe.expectWithinDuration(
          FbUserJoin("BobB", "user2", "1")
        )
      }
    }
  }
}
