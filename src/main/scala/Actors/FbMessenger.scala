package Actors

import FbMessenger.{BaseFbClient, FbMessage, FbThread, Participant}
import Messages.Implicits.ImplicitConversions.ThreadId
import Messages._
import akka.actor.{Actor, ActorLogging, ActorRef, Timers}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import ircserver.Globals
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

// Synchronization strategy:
// Messenger-microservice has 3 endpoints:
//   - /current-id
//   - /thread-list
//   - /thread-history
// At a certain interval, this actor will poll the thread-list endpoint.
//   - The returned list of threads will be compared to the local list, and for every new thread:
//     - Create a new channel if the thread does not exist
//     - For every person in the thread that's not self, JOIN that user to the channel.
//   - Compare the latest timestamp variable to the one that's stored locally.
//     - All the unread messages are the ones that have greater timestamps than local
//     - If the returned number is greater, for that thread, call thread-history and send unread
//       messages as PRIVMSGs to Channels actor.

class FbMessenger(users: ActorRef, channels: ActorRef, client: BaseFbClient) extends Actor with ActorLogging with Timers {
  import akka.pattern.pipe
  import context.dispatcher
  import Timing._

  timers.startSingleTimer(TickKey, FirstTick, 500 millis)

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  val fbEndpoint = "http://" + Globals.fbServerName + ":" + Globals.fbPort
  val http = Http(context.system)

  val threads = collection.mutable.HashSet[FbThread]()
  var messagesReceived = collection.mutable.Map[ThreadId, Int]()

  case class FbMessengerState(
                             threads: Seq[FbThread] = Seq.empty,
                             messages: Map[ThreadId, Seq[FbMessage]] = Map.empty
                             )

  override def receive: Receive = {
    case FirstTick ⇒ timers.startPeriodicTimer(TickKey, Tick, 10 seconds)
    case Tick ⇒ {
      val threadList = http.singleRequest(HttpRequest(uri = s"$fbEndpoint/thread-list"))
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) ⇒ entity.dataBytes.runFold(ByteString(""))(_ ++ _)
          .map(body ⇒ Json.parse(body.utf8String).as[JsArray])
          .map{ x =>
            println(x)
            x.value.map(FbThread.apply)
          }
      }

      threadList.onComplete{
        case Success(receivedThreads) ⇒
          val newThreads = receivedThreads.flatten.toSet.diff(threads)
          addNewThreads(newThreads)

//          addNewUsers()

          receivedThreads.foreach{
          case Some(thread) ⇒ {
//            messagesReceived.update(thread.threadId, Math.max(messagesReceived(thread.threadId), thread.messageCount))
//            receivedThreads.add(thread)
          }
          case None ⇒ println("None");
        }
        case Failure(f) ⇒ println(f)
      }

    }
    //    case HttpResponse(StatusCodes.OK, headers, entity, _) ⇒
    //      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
    //        log.info("Got response, body: " + Json.parse(body.utf8String))
    //      }
    //    case resp @ HttpResponse(code, _, _, _) =>
    //      log.info("Request failed, response code: " + code)
    //      resp.discardEntityBytes()
  }

  def addNewThreads(newThreads: Set[FbThread]): Unit = {
    newThreads.foreach(t ⇒ channels ! Message(NewFbThreadCommand(t.name, t.threadId), Prefix(""), NoParams, ""))
    threads ++= newThreads
  }

  def synchronize(oldState: FbMessengerState): FbMessengerState = {

  }

  private object Timing {
    case object Tick
    case object FirstTick
    case object TickKey
  }
}



