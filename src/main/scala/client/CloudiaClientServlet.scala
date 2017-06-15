package client

import akka.actor.{ActorRef, ActorSystem}
import communication.Ping
import index.DirectoryIndex

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import utils.{AppController, IndexUtils}

import scala.util.Try


class CloudiaClientServlet(servletName: String) extends CloudiaclientStack {

  implicit val timeout = akka.util.Timeout(new FiniteDuration(1, SECONDS)) // Timeout for the resolveOne call
  implicit val system = ActorSystem("client")
  val controller = system.actorOf(AppController.props(), "controller")

  def nodes(timeout: FiniteDuration = 1 second) = {
    Await.result(controller.ask(Ping())(timeout).mapTo[Map[String, ActorRef]], timeout)
  }
  def jadeNodeNames() = "nodeNames" -> nodes().keys.toList

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


    nodes().get(nodeName) match {
      case Some(actorRef) =>
        Try(Await.result(actorRef.ask(Ping())(1 second).mapTo[DirectoryIndex], 1 second)) match {
          case scala.util.Success(index) => IndexUtils.indexAt(index, path) match {
            case Some(found) => jade("nodeindex", "servletName" -> servletName, "nodeName" -> nodeName, "index" -> found, jadeNodeNames())
            case _ => jade("error", "reason" -> "No such directory!", jadeNodeNames(), "servletName" -> servletName)
          }
          case _ => jade("error", "reason" -> "Node disconnected!", jadeNodeNames(), "servletName" -> servletName)
      }
      case _ => jade("error", "reason" -> "No such node!", jadeNodeNames(), "servletName" -> servletName)
    }
  }


  post("/:node/*"){
    val nodeName = params("node")
    val path = multiParams("splat").head
    val filename = params("file")
    println(params)
    redirect(s"/$servletName/$nodeName/$path")
  }


}
