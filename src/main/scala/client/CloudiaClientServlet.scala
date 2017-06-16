package client

import akka.actor.{ActorRef, ActorSystem}
import communication.{Ping, Request}
import index.{DirectoryIndex, FileIndex}

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import utils.{NodeController, IndexUtils, IpUtils}

import scala.collection.mutable
import scala.util.Try


class CloudiaClientServlet(servletName: String) extends CloudiaclientStack {

  implicit val timeout = akka.util.Timeout(new FiniteDuration(1, SECONDS)) // Timeout for the resolveOne call
  println(IpUtils.inet())

  implicit val system = ActorSystem("client",
    ConfigFactory.load().withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(IpUtils.inet())))
  private val nodeMap = new mutable.HashMap[String, ActorRef]()
  val controller = system.actorOf(NodeController.props(nodeMap), "controller")

  def nodes(): Map[String, ActorRef] =nodeMap.toMap
  def jadeNodeNames(): (String, List[String]) = "nodeNames" -> nodes().keys.toList

  before(){
    contentType="text/html"
  }

  get("/"){
    jade("start", "servletName" -> servletName, jadeNodeNames())
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
            case Some(found: DirectoryIndex) => jade("nodeindex", "index" -> found, "nodeName" -> nodeName, "servletName" -> servletName,   jadeNodeNames())
            case Some(found: FileIndex) => jade("error", "reason" -> s"${found.handler.getName} is a file", jadeNodeNames(), "servletName" -> servletName)
            case _ => jade("error", "reason" -> "No such directory!", jadeNodeNames(), "servletName" -> servletName)
          }
          case _ => jade("error", "reason" -> "Node disconnected!", jadeNodeNames(), "servletName" -> servletName)
      }
      case _ => jade("error", "reason" -> "No such node!", jadeNodeNames(), "servletName" -> servletName)
    }
  }


  post("/:node/*"){
    val nodesMap = nodes()
    val filename = params("file")
    val path = multiParams("splat").head
    (nodesMap.get(params("node")), nodesMap.get(params("recipient"))) match {
      case (Some(sender), Some(recipient)) => Try(Await.result(sender.ask(Ping())(1 second).mapTo[DirectoryIndex], 1 second)) match {
          case scala.util.Success(index) => IndexUtils.indexAt(index, s"$path/$filename") match {
            case Some(found: FileIndex) => {
              sender.tell(Request(found), recipient)
            }
            case Some(found: DirectoryIndex) => println(s"directory")
            case _ => jade("error", "reason" -> s"$path not found", jadeNodeNames(), "servletName" -> servletName)
          }
          case _ => jade("error", "reason" -> "Node disconnected!", jadeNodeNames(), "servletName" -> servletName)
        }
      case _ => jade("error", "reason" -> "No such node!", jadeNodeNames(), "servletName" -> servletName)
    }
    println(params)
    redirect(s"/$servletName/${params("node")}/${multiParams("splat").head}")
  }


}
