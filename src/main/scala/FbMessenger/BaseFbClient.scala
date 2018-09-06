package FbMessenger

import Messages.Implicits.ImplicitConversions.ThreadId

/**
  * Created by Nelson on 2018/08/12.
  */
trait BaseFbClient {
  abstract def getCurrentId(): String
  abstract def getThreadList(limit: Option[Int]): Seq[FbThread]
  abstract def getThreadHistory(threadId: ThreadId, highWaterMark: Option[Long], limit: Option[Int]): Seq[FbMessage]
}
