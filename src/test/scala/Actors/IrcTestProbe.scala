package Actors

import Messages.{Message, MessageSerializer, Params}
import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.duration._
import akka.testkit.{TestActor, TestProbe}
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by Nelson on 2018/07/20.
  */
class IrcTestProbe(system: ActorSystem) extends TestProbe(system)
  with Matchers {
  def expectIrcMessage(recipient: String, expectedMessages: String*) = {
    fishForMessage(100 millis){
      case msg: Message[Params] if msg.recipient == recipient && expectedMessages.head == MessageSerializer.serialize(msg).utf8String.trim
        ⇒ true
      case _ ⇒ false
    }
    expectedMessages.tail foreach { expected =>
      expectMsgPF(100 millis) {
        case msg: Message[Params] if msg.recipient == recipient =>
          println(MessageSerializer.serialize(msg) utf8String)
          MessageSerializer.serialize(msg).utf8String.trim shouldEqual expected
      }
    }
  }

  def expectWithinDuration[T](msg: T) = {
    fishForMessage(3 seconds) {
      case received: T ⇒ received == msg
      case x: Any ⇒
        println(x)
        false
    }
  }

  // TODO: make this actually work
  def expectNoSuchIrcMessage(recipient: String, messages: String*) = expectMsgPF() {
    case msg: Message[Params] ⇒
      val receivedMessage = MessageSerializer.serialize(msg).utf8String.trim
      val exactSameMessage = recipient == msg.recipient &&
        (messages contains(receivedMessage))
      if (exactSameMessage) throw new Exception(s"Did not expect to receive $receivedMessage")
  }

}

object IrcTestProbe{
  def apply(tag: String)(implicit system: ActorSystem): IrcTestProbe = {
    val probe = new IrcTestProbe(system)
    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any) = {
        val other = sender.path
        val me = probe.ref.path
        println(s"$tag $me received $msg from $other")
        this
      }
    })
    probe
  }
}
