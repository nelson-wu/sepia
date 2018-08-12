package Actors

import Messages.{Message, MessageSerializer, Params}
import akka.actor.ActorSystem
import scala.concurrent.duration._
import akka.testkit.TestProbe
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

  // TODO: make this actually work
  def expectNoSuchIrcMessage(recipient: String, messages: String*) = expectMsgPF() {
    case msg: Message[Params] ⇒
      val receivedMessage = MessageSerializer.serialize(msg).utf8String.trim
      val exactSameMessage = recipient == msg.recipient &&
        (messages contains(receivedMessage))
      if (exactSameMessage) throw new Exception(s"Did not expect to receive $receivedMessage")
  }

}
