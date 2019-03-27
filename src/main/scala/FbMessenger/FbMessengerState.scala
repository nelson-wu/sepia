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
      .map(t ⇒ synchronizeUsersInThread(userChanges, t))

    val newMessages = current.messages.map {
      case (thread, messages) =>
        if (messageChanges contains thread)
          thread -> (messages ++ messageChanges(thread))
        else thread -> messages
    }

    current.copy(
      threads = newThreads,
      messages = newMessages
    )
  }

  def synchronizeUsersInThread(userChanges: DeltaUsers, t: FbThread): FbThread = {
      val joinedUsersMaybe = userChanges.joined.get(t.threadId)
      val leftUsersMaybe = userChanges.parted.get(t.threadId)

      val withJoined = joinedUsersMaybe.fold(t.participants)(t.participants ++ _)
      val withJoinedLeft = leftUsersMaybe.fold(withJoined)(withJoined -- _)

      t.copy(participants = withJoinedLeft)
  }

  // threads are monotonically increasing
  def deltaThreads(current: Seq[FbThread], next: Seq[FbThread]): Seq[FbThread] =
    next.filter(newThread ⇒ !current.exists(_.threadId == newThread.threadId))

  def deltaUsers(current: Seq[FbThread], next: Seq[FbThread]): DeltaUsers = {
    next.foldLeft(DeltaUsers()){(delta, newThread) ⇒
      val oldThreadMaybe = current.find(_.threadId == newThread.threadId)
      val joinedUsers = oldThreadMaybe.map(thread ⇒
        Map(thread.threadId → (newThread.participants diff thread.participants))
      ).getOrElse(
        Map(newThread.threadId → newThread.participants)
      )

      val leftUsers = oldThreadMaybe.map(thread ⇒
        Map(thread.threadId → (thread.participants diff newThread.participants))
      ).getOrElse(Map())

      delta.copy(
        joined = delta.joined ++ joinedUsers,
        parted = delta.parted ++ leftUsers
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
