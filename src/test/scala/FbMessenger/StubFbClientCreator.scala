package FbMessenger
import Messages.Implicits.ImplicitConversions
import Messages.Implicits.ImplicitConversions.ThreadId
import org.joda.time.Instant

import scala.concurrent.Future

/**
  * Created by Nelson on 2018/09/07.
  */
object StubFbClientCreator {
  def apply(
             _getCurrentId: String = defaultGetCurrentId,
             _getThreadHistory: (ThreadId, Option[Instant], Option[Int]) ⇒ Seq[FbMessage] = defaultGetThreadHistory,
             _getThreadList: (Option[Int]) ⇒ Seq[FbThread] = defaultGetThreadList
           ): BaseFbClient = new BaseFbClient {
    def getCurrentId() = Future.successful(_getCurrentId)

    def getThreadList(limit: Option[Int]): Future[Seq[FbThread]] = Future.successful(_getThreadList(limit))

    def getThreadHistory(threadId: ThreadId, highWaterMark: Option[Instant], limit: Option[Int]): Future[Seq[FbMessage]] = Future.successful(_getThreadHistory(threadId, highWaterMark, limit))
  }

  private def defaultGetCurrentId(): String = "testId"
  private def defaultGetThreadHistory(threadId: ThreadId, highWaterMark: Option[Instant] = None, limit: Option[Int] = None): Seq[FbMessage] = Seq(
      FbMessage("first message", "testSender", new Instant(1)),
      FbMessage("second message", "testSender2", new Instant(2)),
      FbMessage("third message", "testSender", new Instant(4))
    )
  private def defaultGetThreadList(limit: Option[Int] = None): Seq[FbThread] = Seq(
      FbThread("test thread", "threadId", isGroup = false, 3,
        Set(
          Participant("testSender", "testSenderId"),
          Participant("testSender2", "testSender2Id")
        )
      )
    )
}
