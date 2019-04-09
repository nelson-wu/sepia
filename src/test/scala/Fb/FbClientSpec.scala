package Fb

import akka.actor.ActorSystem
import org.scalatest.{DoNotDiscover, Matchers, AsyncWordSpec}

// Boundary tests on messenger-microservice

@DoNotDiscover
class FbClientSpec extends AsyncWordSpec
  with Matchers {

  implicit val system = ActorSystem(this.getClass.getSimpleName())

  "FbClient" must {
    "return current user ID" in {
      val client = new FbClient(system)
      val id = client.getCurrentId()

      id map {_id ⇒ _id should not be empty}
    }
    "return thread list" in {
      val client = new FbClient(system)
      val threadList = client.getThreadList(Some(5))

      threadList map {_.head.participants should not be empty}
    }
    "return thread history" in {
      val client = new FbClient(system)
      val threadId = client.getThreadList(Some(1))
        .map(_.head.threadId)
      val threadHistory = threadId.flatMap { id =>
        client.getThreadHistory(id, Some(5))
      }

      threadHistory map {_ should not be empty}
    }
  }

}
