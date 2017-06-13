package client

//import org.scalatra._
import akka.actor.ActorSystem
import communication.Handshake
import index.DirectoryIndex

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import cloudia.Jason

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.parsing.json.JSONObject




class CloudiaClientServlet extends CloudiaclientStack {

  implicit val timeout = akka.util.Timeout(new FiniteDuration(1, SECONDS)) // Timeout for the resolveOne call
  val system = ActorSystem("cloudia-client")
  val server = system.actorSelection("akka.tcp://cloudia-server@127.0.0.1:8888/user/server")


  get("/") {

    contentType="text/html"

//      val index = Await.result(server.ask(Handshake())(1 second).mapTo[DirectoryIndex], 1 second)
    try {

      val futureIndex = server.ask(Handshake())(1 second).mapTo[DirectoryIndex]
      val index = Await.result(server.ask(Handshake())(1 second).mapTo[DirectoryIndex], 1 second)
      jade("twonodes",
        "thisNode" -> "this",
        "thatNode" -> "that",
        "thisContents" -> Jason.toJson(index),
        "thatContents" -> JSONObject(Map()))
    }
    catch {
      case _:Exception =>
        jade("error")
    }


//    jade("hello-scalate", "headline" -> "korwo")
  }

  get("/cloudia") {

    server.resolveOne().onComplete {
      case actor =>
        println(actor)

    }

    val index = Await.result(server.ask(Handshake())(1 second).mapTo[DirectoryIndex], 1 second)

    contentType = "application/json"

    Jason.toJson(index)

  }

}
