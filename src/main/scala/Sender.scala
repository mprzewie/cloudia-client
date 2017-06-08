

import java.io.File

import akka.actor._
import com.typesafe.config.ConfigFactory
import communication.{Handshake, Node, Request}
import index.DirectoryIndex
import akka.pattern.{ask, pipe}

import scala.concurrent.Await
import scala.concurrent.duration._


/**
  * Created by marcin on 5/6/17.
  */


object Main extends App {

  implicit val chunkSize: Int = 100

  val system = ActorSystem("cloudia-client")
  val cloudia = system.actorOf(Node.props("/home/marcin/Documents/Coding/cloudia/test1"), name = "client")
  val server = system.actorSelection("akka.tcp://cloudia-server@127.0.0.1:8888/user/server")
  val index = Await.result(server.ask(Handshake())(1 second).mapTo[DirectoryIndex], 1 second)
  index.subDirectories.foreach(println(_))
  index.subFiles.foreach(println(_))
  server.tell(Request(index.subDirectories.head.subFiles.head), cloudia)
}
