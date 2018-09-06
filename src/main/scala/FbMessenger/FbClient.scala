package FbMessenger

import Messages.Implicits.ImplicitConversions.ThreadId
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import ircserver.Globals
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.Future

/**
  * Created by Nelson on 2018/08/12.
  */
class FbClient(system: ActorSystem) extends BaseFbClient{
  override def getCurrentId(): String = ???

  override def getThreadList(limit: Option[Int]): Future[Seq[FbThread]] = {
    val fbEndpoint = "http://" + Globals.fbServerName + ":" + Globals.fbPort
    val http = Http(system)
    val threadList = http.singleRequest(HttpRequest(uri = s"$fbEndpoint/thread-list"))
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) ⇒ entity.dataBytes.runFold(ByteString(""))(_ ++ _)
          .map(body ⇒ Json.parse(body.utf8String).as[JsArray])
          .map{ x =>
            println(x)
            x.value.flatMap(FbThread.apply)
          }
      }

    threadList
  }

  override def getThreadHistory(threadId: ThreadId, highWaterMark: Long, limit: _root_.scala.Option[Int]): scala.Seq[_root_.FbMessenger.FbMessage] = ???
}
