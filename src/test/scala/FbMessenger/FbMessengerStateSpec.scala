package FbMessenger

import Messages.Implicits.ImplicitConversions.ThreadId
import org.joda.time.Instant
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

/**
  * Created by Nelson on 2018/09/17.
  */
class FbMessengerStateSpec extends WordSpec
  with Matchers
  with BeforeAndAfterEach
{
  "FbMessengerSpec" must {
    "Delta method" must{
    "return new threads" in {
      val newState = FbMessengerState(
        threads = Seq(
          FbThread("newThread", "1", false, 2, Set()),
          FbThread("oldThread", "2", false, 1, Set())
        )
      )
      val oldState = FbMessengerState(
        threads = Seq(FbThread("oldThread", "2", false, 1, Set()))
      )
      FbMessengerState
        .deltaThreads(oldState.threads, newState.threads) should contain theSameElementsAs Seq(
        FbThread("newThread", "1", false, 2, Set())
      )
    }
    "not return same threads that have changed" in {
      val newState = FbMessengerState(
        threads = Seq(
          FbThread("oldThread", "2", false, 1, Set())
        )
      )
      val oldState = FbMessengerState(
        threads = Seq(FbThread("oldThread", "2", false, 1, Set(Participant("a", "a"))))
      )
      FbMessengerState
        .deltaThreads(oldState.threads, newState.threads) shouldBe empty
    }

    "return new messages" in {
      val participants = Set(
        Participant("user1", "userId1")
      )
      val oldState = FbMessengerState(
        threads = Seq(FbThread("thread", "thread1", true, 1, participants)),
        messages = Map(ThreadId("thread1") → Seq(FbMessage("hi", "userId1", "user1", new Instant(1))))
      )
      val newState = FbMessengerState(
        threads = oldState.threads,
        messages = Map(ThreadId("thread1") → Seq(
          FbMessage("hello", "userId1", "user1", new Instant(2))
        ))
      )

      FbMessengerState
        .deltaMessages(oldState.messages, newState.messages)(ThreadId("thread1")) shouldEqual Seq(
        FbMessage("hello", "userId1", "user1", new Instant(2))
      )
    }
    "return new members of threads" in {
      val alice = Participant("alice", "aliceId")
      val bob = Participant("bob", "bobId")
      val oldState = FbMessengerState(
        threads = Seq(FbThread("thread", "1", false, 1, Set(alice)))
      )
      val newState = FbMessengerState(
        threads = Seq(FbThread("thread", "1", false, 1, Set(alice, bob)))
      )
      FbMessengerState
        .deltaUsers(oldState.threads, newState.threads)
        .joined.head
        ._2 shouldEqual Set(bob)
    }
    "return members in threads who left" in {
      val alice = Participant("alice", "aliceId")
      val bob = Participant("bob", "bobId")
      val oldState = FbMessengerState(
        threads = Seq(FbThread("thread", "1", false, 1, Set(alice, bob)))
      )
      val newState = FbMessengerState(
        threads = Seq(FbThread("thread", "1", false, 1, Set(alice)))
      )
      FbMessengerState
        .deltaUsers(oldState.threads, newState.threads)
        .parted.head
        ._2 shouldEqual Set(bob)
    }

    // TODO: actually implement this
    "return renamed threads" ignore {}
  }
    "Synchronize method" must {
    "return new state when old state is empty" in {
      val newState = FbMessengerState(
        threads = Seq(FbThread("threadName", "threadId", isGroup = true, 1, Set()))
      )
      FbMessengerState.synchronize(
        FbMessengerState(),
        newState
      ) shouldEqual newState
    }

    "synchronize new threads" in {
      val newState = FbMessengerState(
        threads = Seq(FbThread("newThread", "1", false, 2, Set()))
      )
      val oldState = FbMessengerState(
        threads = Seq(FbThread("oldThread", "2", false, 1, Set()))
      )
      FbMessengerState
        .synchronize(oldState, newState)
        .threads should contain theSameElementsAs Seq(
        FbThread("newThread", "1", false, 2, Set()),
        FbThread("oldThread", "2", false, 1, Set())
      )

    }

    "synchronize new messages in threads" in {
      val participants = Set(
        Participant("user1", "userId1")
      )
      val oldState = FbMessengerState(
        threads = Seq(FbThread("thread", "thread1", true, 1, participants)),
        messages = Map(ThreadId("thread1") → Seq(FbMessage("hi", "userId1", "user1", new Instant(1))))
      )
      val newState = FbMessengerState(
        threads = oldState.threads,
        messages = Map(ThreadId("thread1") → Seq(
          FbMessage("hello", "userId1", "user1", new Instant(2))
        ))
      )

      FbMessengerState
        .synchronize(oldState, newState)
        .messages(ThreadId("thread1")) shouldEqual Seq(
        FbMessage("hi", "userId1", "user1", new Instant(1)),
        FbMessage("hello", "userId1", "user1", new Instant(2))
      )
    }

    "synchronize new users in threads" in {
      val alice = Participant("alice", "aliceId")
      val bob = Participant("bob", "bobId")
      val oldState = FbMessengerState(
        threads = Seq(FbThread("thread", "1", false, 1, Set(alice)))
      )
      val newState = FbMessengerState(
        threads = Seq(FbThread("thread", "1", false, 1, Set(alice, bob)))
      )
      FbMessengerState
        .synchronize(oldState, newState)
        .threads.head
        .participants shouldEqual Set(alice, bob)
    }

    "synchronize users who left in threads" in {
      val alice = Participant("alice", "aliceId")
      val bob = Participant("bob", "bobId")
      val oldState = FbMessengerState(
        threads = Seq(FbThread("thread", "1", false, 1, Set(alice, bob)))
      )
      val newState = FbMessengerState(
        threads = Seq(FbThread("thread", "1", false, 1, Set(alice)))
      )
      FbMessengerState
        .synchronize(oldState, newState)
        .threads.head
        .participants shouldEqual Set(alice)
    }
  }
  }
}
