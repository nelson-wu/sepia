package Actors

import FbMessenger._
import akka.actor.{ActorSystem, Props}
import ircserver.Globals
import org.joda.time.Instant
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

/**
  * Created by Nelson on 2018/08/12.
  */
class FbMessengerSpec extends WordSpec
  with Matchers
  with BeforeAndAfterEach {

  val system = ActorSystem("fb-messenger-spec")

  def setup(testClient: BaseFbClient) = {
    val probe = new IrcTestProbe(system)
    val fbMessenger = system.actorOf(Props(classOf[FbMessenger], probe.ref, probe.ref, testClient, true))
    (probe, fbMessenger)
  }

  "FB messenger actor" must {
    "synchronize with FB" which {
      "send PRIVMSG messages for new messages in threads" in {
         val client = StubFbClientCreator(
           threadList = Seq(
             Seq(
               FbThread("thread1", "1", false, 0, Set(Participant("a", "a")))
             )
           ),
           threadHistory = Seq(
             Seq(),
             Seq(FbMessage("hi", "1", new Instant(1)))
           )
         )

        val (probe, fbMessenger) = setup(client)
        fbMessenger ! Timing.Tick
        fbMessenger ! Timing.Tick
        probe.expectIrcMessage(Globals.serverName, s":${Globals.facebook} PRIVMSG 1 :hi")
      }

      "send JOIN messages for new threads" in {
        val client = StubFbClientCreator(
          threadList = Seq(
            Seq(),
            Seq(FbThread("First Thread", "1", true, 0, Set(
              Participant("bob", "user2")
            )))
          ),
          threadHistory = Seq()
        )
        val (probe, fbMessenger) = setup(client)
        fbMessenger ! Timing.Tick
        fbMessenger ! Timing.Tick
        probe.expectIrcMessage(Globals.serverName,
          s"bob JOIN #first-thread"
        )
      }

      "send JOIN messages for new users in threads" in {
        val client = StubFbClientCreator(
          threadList = Seq(
            Seq(),
            Seq(FbThread("First Thread", "1", true, 0, Set(
              Participant("Alice A", "user1"),
              Participant("Bob B", "user2")
            )))
          ),
          threadHistory = Seq()
        )
        val (probe, fbMessenger) = setup(client)
        fbMessenger ! Timing.Tick
        fbMessenger ! Timing.Tick
        probe.expectIrcMessage(Globals.serverName,
          s"BobB JOIN #first-thread"
        )
      }
    }
  }
}
