package FbMessenger

import Messages.Implicits.ImplicitConversions.ThreadId
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import ircserver.Globals
import org.joda.time.Instant
import play.api.libs.json.{JsArray, JsValue, Json}

import scala.concurrent.Future

/**
  * Created by Nelson on 2018/08/12.
  */
class FbClient(system: ActorSystem) extends BaseFbClient{
  import system.dispatcher

  implicit val _system = system
  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
  override def getCurrentId(): Future[String] = ???

  override def getThreadList(limit: Option[Int]): Future[Seq[FbThread]] = {
    val fbEndpoint = "http://" + Globals.fbServerName + ":" + Globals.fbPort
    val http = Http(system)
    val threadList = http.singleRequest(HttpRequest(uri = s"$fbEndpoint/thread-list"))
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) ⇒ entity.dataBytes.runFold(ByteString(""))(_ ++ _)
          .map(body ⇒ Json.parse(body.utf8String).as[JsArray])
          .map{ x =>
            println(x)
            x.value.flatMap(FbThread.apply(_:JsValue))
          }
      }

    threadList
  }

  def getThreadHistory(threadId: ThreadId, highWaterMark: Option[Instant], limit: Option[Int]): Future[Seq[FbMessage]] = ???
}
