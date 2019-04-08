package Fb

import Messages.Implicits.ImplicitConversions.ThreadId

import scala.concurrent.Future

/**
  * Created by Nelson on 2018/08/12.
  */
trait BaseFbClient {
  def getCurrentId(): Future[String]
  def getThreadList(limit: Option[Int]): Future[Seq[FbThread]]
  def getThreadHistory(threadId: ThreadId, limit: Option[Int]): Future[Seq[FbMessage]]
}
