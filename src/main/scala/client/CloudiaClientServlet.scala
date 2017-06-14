package client

//import org.scalatra._
import akka.actor.ActorSystem
import communication.Handshake
import index.DirectoryIndex

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import utils.{IndexUtils, JsonUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.parsing.json.JSONObject




class CloudiaClientServlet extends CloudiaclientStack {

  implicit val timeout = akka.util.Timeout(new FiniteDuration(1, SECONDS)) // Timeout for the resolveOne call
  val system = ActorSystem("cloudia-client")
  val server = system.actorSelection("akka.tcp://cloudia-server@127.0.0.1:8888/user/server")



  get("/") {
    contentType="text/html"
    try {
      val futureIndex = server.ask(Handshake())(1 second).mapTo[DirectoryIndex]
      val index = Await.result(server.ask(Handshake())(1 second).mapTo[DirectoryIndex], 1 second)
      println(IndexUtils.toHtml(index))
      jade("twonodes",
        "thisNode" -> "this",
        "thatNode" -> "that",
        "thisContents" -> IndexUtils.toHtml(index),
        "thatContents" -> "")
    }
    catch {
      case e:Exception =>
        jade("error")
    }



  }

  get("/home") {redirect("/home/")}

  get("/home/*") {
    contentType="text/html"
    val path = multiParams("splat").head
    val futureIndex = server.ask(Handshake())(1 second).mapTo[DirectoryIndex]
    val index = Await.result(server.ask(Handshake())(1 second).mapTo[DirectoryIndex], 1 second)
    IndexUtils.indexAt(index, path) match {
      case Some(found) => jade("homeindex", "index" -> found)
      case _ => "No such directory!"
    }

  }

}
