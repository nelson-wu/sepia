package Actors

import Messages._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import ircserver.Globals
import org.scalatest.{BeforeAndAfterEach, WordSpecLike}

import scala.concurrent.duration._

class ChannelsSpec extends WordSpecLike
  with BeforeAndAfterEach
{

  def handle = afterWord("handle")
  implicit val source = "testUser"
  val system = ActorSystem("channels-spec")

  def setup() = {
    val probe = new IrcTestProbe(system)
    val channels = system.actorOf(Props(classOf[Channels], probe.ref))
    (probe, channels)
  }

  "Channels actor" must handle{
    "JOIN" which {
      "reply to JOIN with topic and names" in {
        val (probe, channels) = setup()
        channels ! MessageParser.parse(":user JOIN #channel")
        probe.expectIrcMessage("user",
          ":user JOIN #channel",
          s":${Globals.serverName} 331 user #channel :No topic is set",
          s":${Globals.serverName} 353 user = #channel :user",
          s":${Globals.serverName} 366 user #channel :End of NAMES list"
        )
      }
      "broadcast JOINs to other users" in {
        val (probe, channels) = setup()
        channels ! MessageParser.parse(":alice JOIN #channel2")
        channels ! MessageParser.parse(":bob JOIN #channel2")
        probe.expectIrcMessage("alice",
          ":bob JOIN #channel2"
        )
      }
    }
    "PRIVMSG" which {
      "broadcast PRIVMSG to other users" in {
        val (probe, channels) = setup()
        channels ! MessageParser.parse(":alice JOIN #channel3")
        channels ! MessageParser.parse(":bob JOIN #channel3")
        channels ! MessageParser.parse(":alice PRIVMSG #channel3 :hello")
        probe.expectIrcMessage("bob",
          ":alice PRIVMSG #channel3 :hello"
        )
      }
    }
    "PART" which {
      "broadcasted to other users" in {
        val (probe, channels) = setup()
        channels ! MessageParser.parse(":alice JOIN #channel4")
        channels ! MessageParser.parse(":bob JOIN #channel4")
        channels ! MessageParser.parse(":alice PART #channel4 :test message")
        probe.expectIrcMessage("bob",
          ":alice PART #channel4 :test message"
        )
      }

      "removes user from channel" ignore {
        val (probe, channels) = setup()
        channels ! MessageParser.parse(":alice JOIN #channel4")
        channels ! MessageParser.parse(":bob JOIN #channel4")
        channels ! MessageParser.parse(":alice PART #channel4 :test message")
//        channels ! MessageParser.parse(":bob PRIVMSG #channel4 :ping")
        probe.expectNoSuchIrcMessage("bob",
          ":alice PART #channel4 :test message"
        )
      }
    }
  }

}
