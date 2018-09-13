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
    FbMessengerState(
      synchronizeThreads(current, next),
      synchronizeMessages(current, next)
    )
  }

  def deltaThreads(current: Seq[FbThread], next: Seq[FbThread]): Seq[FbThread] =
    next.diff(current)

  def deltaUsers(current: Seq[FbThread], next: Seq[FbThread]): DeltaUsers = {
    next.foldLeft(DeltaUsers()){(delta, newThread) ⇒
      val oldThread = current.filter(_.threadId == newThread.threadId).head
      val joinedUsers = newThread.participants diff oldThread.participants
      val leftUsers = oldThread.participants diff newThread.participants

      delta.copy(
        plus = delta.plus + (newThread.threadId → joinedUsers),
        minus = delta.minus + (newThread.threadId → leftUsers)
      )
    }
  }

  def deltaMessages(current: Map[ThreadId, Seq[FbMessage]], next: Map[ThreadId, Seq[FbMessage]]): Map[ThreadId, Seq[FbMessage]] = {
    next.map{ case (id, newMessages) ⇒
        val oldMessages = current(id)
        val latestReadMessage = oldMessages.max
    }
  }

  private def synchronizeMessages(current: FbMessengerState, next: FbMessengerState): Map[ThreadId, Seq[FbMessage]] = {
    current.messages.map{ case (id, seq) ⇒
      val oldMessages = seq
      val latestReadMessage = oldMessages.maxBy(_.timestamp.getMillis)
      val newerMessages = next.messages(id).filter(_.timestamp.getMillis > latestReadMessage.timestamp.getMillis)
      id → (oldMessages ++ newerMessages)
    }
  }

  private def synchronizeThreads(current: FbMessengerState, next: FbMessengerState): Seq[FbThread] = {
    (current.threads ++ next.threads).distinct
  }

}
