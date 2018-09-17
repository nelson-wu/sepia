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

case class DeltaUsers(plus: Map[ThreadId, Set[Participant]] = Map.empty, minus: Map[ThreadId, Set[Participant]] = Map.empty)

object FbMessengerState {

  def synchronize(current: FbMessengerState, next: FbMessengerState): FbMessengerState = {
    val threadChanges = deltaThreads(current.threads, next.threads)
    val userChanges = deltaUsers(current.threads, next.threads)
    val messageChanges = deltaMessages(current.messages, next.messages)

    val newThreads = (current.threads ++ threadChanges)
      .map { t =>
        if ((userChanges.plus.keys.toSeq contains t.threadId) || (userChanges.minus.keys.toSeq contains t.threadId)) {
          val newUsers = t.participants ++ userChanges.plus(t.threadId) -- userChanges.minus(t.threadId)
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
        plus = delta.plus ++ joinedUsers.getOrElse(Map.empty),
        minus = delta.minus ++ leftUsers.getOrElse(Map.empty)
      )
    }
  }

  // messages are monotonically increasing
  def deltaMessages(current: Map[ThreadId, Seq[FbMessage]], next: Map[ThreadId, Seq[FbMessage]]): Map[ThreadId, Seq[FbMessage]] = {
    next.map{ case (id, newMessages) ⇒
        val oldMessages = current(id)
        val latestReadMessage = oldMessages.maxBy(_.timestamp.getMillis)
        val newerMessages = next(id).filter(_.timestamp.getMillis > latestReadMessage.timestamp.getMillis)
        id → newerMessages
    }
  }
}
