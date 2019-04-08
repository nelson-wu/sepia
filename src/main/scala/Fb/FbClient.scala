package Fb

import Messages.Implicits.ImplicitConversions.ThreadId
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import ircserver.Globals
import play.api.libs.json.{JsArray, JsValue, Json}

import scala.concurrent.Future

/**
  * Created by Nelson on 2018/08/12.
  */
class FbClient(system: ActorSystem) extends BaseFbClient{
  import system.dispatcher

  implicit val _system = system
  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
  val http = Http(system)

  private def createUrl(endpoint: String, params: Map[String, String] = Map.empty): String = {
    "http://" +
      Globals.fbServerName +
      ":" +
      Globals.fbPort +
      "/" +
      endpoint +
      "?" +
      params.map { case (k, v) ⇒ k + "=" + v}
        .mkString(",")
  }

  private def placeRequestTo(uri: String): Future[ByteString] = {
    http.singleRequest(HttpRequest(uri = uri))
      .flatMap { case HttpResponse(StatusCodes.OK, _, entity, _) ⇒
        entity.dataBytes.runFold(ByteString(""))(_ ++ _)
      }
  }

  override def getCurrentId(): Future[String] = {
    val fbIdEndpoint = createUrl("current-id")
    val currentId = placeRequestTo(fbIdEndpoint)
      .map(_.utf8String)

    currentId
  }

  override def getThreadList(limit: Option[Int]): Future[Seq[FbThread]] = {
    val fbThreadEndpoint = createUrl("thread-list")
    val threadList = placeRequestTo(fbThreadEndpoint)
      .map(body ⇒ Json.parse(body.utf8String).as[JsArray])
      .map{ x =>
        x.value.flatMap(FbThread.apply(_:JsValue))
      }

    threadList
  }

  def getThreadHistory(threadId: ThreadId, limit: Option[Int]): Future[Seq[FbMessage]] = {
    val fbHistoryEndpoint = createUrl("thread-history"  + "/" + threadId.value)
    val threadHistory = placeRequestTo(fbHistoryEndpoint)
      .map(body ⇒ Json.parse(body.utf8String).as[JsArray])
      .map(FbMessage.fromArray)

    threadHistory
  }
}
