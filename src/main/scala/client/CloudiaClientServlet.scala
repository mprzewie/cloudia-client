package client

import akka.actor.{ActorRef, ActorSystem}
import communication.Ping
import index.DirectoryIndex

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import utils.{AppController, IndexUtils, JsonUtils}

import scala.util.Try


class CloudiaClientServlet(servletName: String) extends CloudiaclientStack {

  implicit val timeout = akka.util.Timeout(new FiniteDuration(1, SECONDS)) // Timeout for the resolveOne call
  implicit val system = ActorSystem("client")
  val controller = system.actorOf(AppController.props(), "controller")
  println(controller.path)

  before(){
    contentType="text/html"
  }

  get("/"){
    jade("start")
  }

  get("/:node"){
    redirect(s"/$servletName/${params("node")}/ ")
  }

  get("/:node/*") {
    contentType="text/html"
    val nodeName = params("node")
    val path = multiParams("splat").head

    val nodes = Await.result(controller.ask(Ping())(1 second).mapTo[Map[String, ActorRef]], 1 second)

    nodes.get(nodeName) match {
      case Some(actorRef) =>
        Try(Await.result(actorRef.ask(Ping())(1 second).mapTo[DirectoryIndex], 1 second)) match {
          case scala.util.Success(index) => IndexUtils.indexAt(index, path) match {
            case Some(found) => jade("nodeindex", "servletName" -> servletName, "nodeName" -> nodeName, "index" -> found)
            case _ => jade("error", "reason" -> "No such directory!")
          }
          case _ => jade("error", "reason" -> "Node disconnected!")
      }
      case _ => jade("error", "reason" -> "No such node!")
    }
  }


  post("/:node/*"){
    val nodeName = params("node")
    val path = multiParams("splat").head
    val filename = params("file")
    print("gonna push file ")
    println(path + "/" + filename)
    redirect(s"/$servletName/home/$path")
  }


}
