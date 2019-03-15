package FbMessenger

import Messages.Implicits.ImplicitConversions._
import org.joda.time.Instant


/**
  * Created by Nelson on 2018/09/07.
  */
case class FbMessengerState(
                             threads: Seq[FbThread] = Seq.empty,
                             messages: Map[ThreadId, Seq[FbMessage]] = Map.empty
                           )

case class DeltaUsers(
                       joined: Map[ThreadId, Set[Participant]] = Map.empty,
                       parted: Map[ThreadId, Set[Participant]] = Map.empty
                     )

object FbMessengerState {

  def synchronize(current: FbMessengerState, next: FbMessengerState): FbMessengerState = {
    val threadChanges = deltaThreads(current.threads, next.threads)
    val userChanges = deltaUsers(current.threads, next.threads)
    val messageChanges = deltaMessages(current.messages, next.messages)

    val newThreads = (current.threads ++ threadChanges)
      .map { t =>
        if ((userChanges.joined.keys.toSeq contains t.threadId) || (userChanges.parted.keys.toSeq contains t.threadId)) {
          val newUsers = t.participants ++ userChanges.joined(t.threadId) -- userChanges.parted(t.threadId)
          t.copy(participants = newUsers)
        }
        else t
      }

    val newMessages = current.messages.map {
      case (thread, messages) => if (messageChanges contains thread) {
        thread -> (messages ++ messageChanges(thread))
      } else thread -> messages
    }

    current.copy(
      threads = newThreads,
      messages = newMessages
    )
  }

  // threads are monotonically increasing
  def deltaThreads(current: Seq[FbThread], next: Seq[FbThread]): Seq[FbThread] =
    next.filter(t ⇒ !current.exists(_.threadId == t.threadId))

  def deltaUsers(current: Seq[FbThread], next: Seq[FbThread]): DeltaUsers = {
    next.foldLeft(DeltaUsers()){(delta, newThread) ⇒
      val oldThread = current.find(_.threadId == newThread.threadId)
      val joinedUsers = oldThread.map(thread ⇒
        Map(thread.threadId → (newThread.participants diff thread.participants))
      )
      val leftUsers = oldThread.map(thread ⇒
        Map(thread.threadId → (thread.participants diff newThread.participants))
      )

      delta.copy(
        joined = delta.joined ++ joinedUsers.getOrElse(Map.empty),
        parted = delta.parted ++ leftUsers.getOrElse(Map.empty)
      )
    }
  }

  // messages are monotonically increasing
  def deltaMessages(oldThreads: Map[ThreadId, Seq[FbMessage]], newThreads: Map[ThreadId, Seq[FbMessage]]): Map[ThreadId, Seq[FbMessage]] = {
    newThreads.map{ case (threadId, newMessages) ⇒
        val oldMessagesMaybe = oldThreads.get(threadId)

        val latestReadTimestamp = oldMessagesMaybe.map(_
          .map(_.timestamp.getMillis).max
        ).getOrElse(0L)

        val newerMessagesMaybe = newThreads
          .get(threadId)
          .map(_.filter(_.timestamp.getMillis > latestReadTimestamp))

        threadId → newerMessagesMaybe.getOrElse(Seq())
    }
  }
}
