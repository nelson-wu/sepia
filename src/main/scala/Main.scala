package ircserver
/**
  * Created by fujiko on 2017/12/22.
  */

import Actors.Server
import akka.actor.{ActorSystem, Props}

object Globals {
  val serverName = "localhost"
  val port = 35353
  val fbServerName = "localhost"
  val fbPort = 39393
  val facebook = "facebook"
}

object Main extends App{
  val system = ActorSystem.create("server-main")
  val serverActor = system.actorOf(Props[Server])
}
