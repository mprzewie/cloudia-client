

import java.io.File

import akka.actor._

import com.typesafe.config.ConfigFactory
import communication.Cloudia


/**
  * Created by marcin on 5/6/17.
  */


object Main extends App {
  implicit val host = ConfigFactory.load().getString("akka.remote.netty.tcp.hostname")
  implicit val port = ConfigFactory.load().getString("akka.remote.netty.tcp.port").toInt
  implicit val chunkSize: Long = 1024
  val system = ActorSystem("cloudia-server")
  val cloudia = system.actorOf(Props(new Cloudia()), name = "local")

//  val cloudia2 = system.actorOf(Props(new Cloudia()), name = "remote")
//  cloudia ! system.actorSelection(cloudia2.path)
  cloudia ! system.actorSelection("akka.tcp://cloudia-server@127.0.0.1:8888/user/receiver")





}
