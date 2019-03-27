package FbMessenger
import Messages.Implicits.ImplicitConversions.ThreadId
import org.joda.time.Instant

import scala.concurrent.Future

/**
  * Created by Nelson on 2018/09/07.
  */
object StubFbClientCreator {
  def apply(
             _getCurrentId: String = defaultGetCurrentId(),
             threadHistory: Seq[Seq[FbMessage]],
             threadList: Seq[Seq[FbThread]]
           ): BaseFbClient = new BaseFbClient {

    var threadListCallCounter = 0
    var threadHistoryCallCounter = 0

    def getCurrentId() = Future.successful(_getCurrentId)

    def getThreadList(limit: Option[Int]): Future[Seq[FbThread]] = {
      val thread = threadList(threadListCallCounter)
      if (threadListCallCounter + 1 < threadList.length)
        threadListCallCounter = threadListCallCounter + 1
      Future.successful(thread)
    }

    def getThreadHistory(threadId: ThreadId, highWaterMark: Option[Instant], limit: Option[Int]): Future[Seq[FbMessage]] = {
      val history = threadHistory(threadHistoryCallCounter)
      if (threadHistoryCallCounter + 1 < threadHistory.length)
        threadHistoryCallCounter = threadHistoryCallCounter + 1
      Future.successful(history)
    }
  }

  private def defaultGetCurrentId(): String = "testId"
}
