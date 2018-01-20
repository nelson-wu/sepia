package ircserver
/**
  * Created by fujiko on 2017/12/22.
  */

import java.net.InetSocketAddress

import Actors.Server
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.Tcp.Write
import akka.io.{IO, Tcp}
import akka.util.ByteString

object Globals {
  val servername = "localhost"
  val port = 35353
}

object Main extends App{
  val system = ActorSystem.create("server-main")
  val serverActor = system.actorOf(Props[Server])
}
