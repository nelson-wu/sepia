package Actors

import akka.actor.{Actor, ActorLogging, Timers}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString

import scala.concurrent.duration._

class FbMessenger extends Actor with ActorLogging with Timers {
  import akka.pattern.pipe
  import context.dispatcher
  import Timing._

  timers.startSingleTimer(TickKey, FirstTick, 500 millis)

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  val http = Http(context.system)

  override def receive: Receive = {
    case FirstTick ⇒ timers.startPeriodicTimer(TickKey, Tick, 1 minute)
    case Tick ⇒ http.singleRequest(HttpRequest(uri = "localhost:39393/thread-history")).pipeTo(self)
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info("Got response, body: " + body.utf8String)
      }
    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }

  private object Timing{
    case object Tick
    case object FirstTick
    case object TickKey
  }
}

