package Actors

import FbMessenger._
import Messages.Implicits.ImplicitConversions.ThreadId
import Messages._
import akka.actor.{Actor, ActorLogging, ActorRef, Timers}
import akka.http.scaladsl.Http
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import ircserver.Globals

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

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

class FbMessenger(users: ActorRef,
                  channels: ActorRef,
                  client: BaseFbClient,
                  test: Boolean
                 ) extends Actor with ActorLogging with Timers {

  import Timing._
  import context.dispatcher

  if (!test) timers.startSingleTimer(TickKey, FirstTick, 500 millis)

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  val fbEndpoint = "http://" + Globals.fbServerName + ":" + Globals.fbPort
  val http = Http(context.system)

  var fbMessengerState = FbMessengerState()

  val threads = collection.mutable.HashSet[FbThread]()
  var messagesReceived = collection.mutable.Map[ThreadId, Int]()


  def getState(): Future[FbMessengerState] = {
    val threadList = client.getThreadList(Some(200))
    val threadHistory = threadList.flatMap(thread ⇒ {
      Future {
        thread.map(t ⇒ (t.threadId, Await.result(client.getThreadHistory(t.threadId, None, Some(10)), 5 seconds)))
      }
    })
    val state = (threadList zip threadHistory).map { case (list, hist) ⇒
      FbMessengerState(
        threads = list,
        messages = hist.toMap
      )
    }
    state
  }



  override def receive: Receive = {
    case FirstTick ⇒ timers.startPeriodicTimer(TickKey, Tick, 10 seconds)
    case Tick ⇒ getState() pipeTo self

    case receivedState: FbMessengerState ⇒ {
      val newThreads = FbMessengerState.deltaThreads(fbMessengerState.threads, receivedState.threads)
      val newUsers = FbMessengerState.deltaUsers(fbMessengerState.threads, receivedState.threads)
      val newMessages = FbMessengerState.deltaMessages(fbMessengerState.messages, receivedState.messages)

      createNewThreads(newThreads)
      addNewUsers(newUsers)
      broadcastNewMessages(newMessages)

      val newState = FbMessengerState.synchronize(fbMessengerState, receivedState)
      fbMessengerState = newState
    }


      def createNewThreads(newThreads: Seq[FbThread]): Unit =
        newThreads foreach { t ⇒
          channels ! NewFbThread(t.name, t.threadId)
        }

      def addNewUsers(newUsers: DeltaUsers): Unit =
        newUsers.joined foreach { case (threadId, users) =>
          users foreach { user =>
            channels ! FbUserJoin(user.name, threadId)
          }
        }

      def broadcastNewMessages(newMessages: Map[ThreadId, Seq[FbMessage]]): Unit =
        newMessages foreach { case (threadId, messages) ⇒
          messages foreach { message =>
            channels ! NewFbMessage(message.senderName, threadId, message.text)
          }
        }
  }
}

object Timing {
  case object Tick
  case object FirstTick
  case object TickKey
}

