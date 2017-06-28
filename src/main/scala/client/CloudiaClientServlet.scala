package client

import akka.actor.{ActorRef, ActorSystem}
import communication.{Ping, Request}
import index.{DirectoryIndex, FileIndex}

import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._
import utils.Jade
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import utils.{NodeController, IndexUtils, IpUtils}

import scala.collection.mutable
import scala.util.Try


class CloudiaClientServlet() extends CloudiaclientStack {

  private val nodeMap = new mutable.HashMap[String, ActorRef]()
  implicit def nodes = nodeMap.toMap
  implicit val timeout = akka.util.Timeout(new FiniteDuration(1, SECONDS)) // Timeout for the resolveOne call
  implicit val system = ActorSystem("client",
    ConfigFactory.load().withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(IpUtils.inet())))
  implicit def servletName = request.getServletPath
  val controller = system.actorOf(NodeController.props(nodeMap), "controller")
  println(IpUtils.inet())



  before(){
    contentType="text/html"
  }

  get("/"){
    jade("start", Jade.servletName , Jade.nodeNames)
  }

//  get("/:node"){
//    redirect(s"/$servletName/${params("node")}/ ")
//  }

  get("/:node/*") {
    contentType="text/html"
    val nodeName = params("node")
    val path = multiParams("splat").head
    nodes.get(nodeName) match {
      case Some(actorRef) =>
        Try(Await.result(actorRef.ask(Ping())(1 second).mapTo[DirectoryIndex], 1 second)) match {
          case scala.util.Success(index) => IndexUtils.indexAt(index, path) match {
            case Some(found: DirectoryIndex) => jade("nodeindex", "index" -> found, Jade.node(nodeName), Jade.servletName, Jade.nodeNames)
            case Some(found: FileIndex) => jade("error", Jade.error(s"${found.handler.getName} is a file"), Jade.nodeNames, Jade.servletName)
            case _ => jade("error", Jade.error("No such directory!"), Jade.nodeNames, Jade.servletName)
          }
          case _ => jade("error", Jade.error("Node disconnected!"), Jade.nodeNames, Jade.servletName)
      }
      case _ => jade("error", Jade.error("No such node!"), Jade.nodeNames, Jade.servletName)
    }
  }


  post("/:node/*"){
    val nodesMap = nodes
    val filename = params("file")
    val path = multiParams("splat").head
    (nodesMap.get(params("node")), nodesMap.get(params("recipient"))) match {
      case (Some(sender), Some(recipient)) => Try(Await.result(sender.ask(Ping())(1 second).mapTo[DirectoryIndex], 1 second)) match {
          case scala.util.Success(index) => IndexUtils.indexAt(index, s"$path/$filename") match {
            case Some(found: FileIndex) => {
              sender.tell(Request(found), recipient)
            }
            case Some(found: DirectoryIndex) => println(s"directory")
            case _ => jade("error", Jade.error(s"$path not found"), Jade.nodeNames, Jade.servletName)
          }
          case _ => jade("error", Jade.error("Node disconnected!"), Jade.nodeNames, Jade.servletName)
        }
      case _ => jade("error", Jade.error("No such node!"), Jade.nodeNames, Jade.servletName)
    }
    println(params)
    println(s"$servletName${request.getPathInfo}/ ")
    redirect(s"$servletName/${params("node")}/${multiParams("splat").head}")
  }

  get("*"){
    if(request.getPathInfo.endsWith("/")) pass()
    else redirect(s"$servletName${request.getPathInfo}/ ")
  }


}
