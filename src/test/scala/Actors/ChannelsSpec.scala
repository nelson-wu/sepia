package Actors

import Messages._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterEach, WordSpecLike}

import scala.concurrent.duration._

class ChannelsSpec extends TestKit(ActorSystem("ChannelsSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterEach
  {

    var channels = ActorRef.noSender
    implicit val source = "testUser"
    override def beforeEach(): Unit = {
      channels = system.actorOf(Props(classOf[Channels], testActor))
    }

    def handle = afterWord("handle")
    def include =
    "Channels actor" must handle{
      "JOIN" which {

        "reply to JOIN with topic and names" in {

        }
        "ignore duplicate JOIN" in {

        }
        "broadcast JOINs to other users" in {

        }
      }
      "PRIVMSG" which {
        "broadcast PRIVMSG to other users" in {

        }
      }
      "PART" which {
        "removes user from channel" in {

        }
      }
    }
}
