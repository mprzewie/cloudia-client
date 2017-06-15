package client

import akka.actor.ActorSystem
import communication.Handshake
import index.DirectoryIndex

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import utils.{AppController, IndexUtils, JsonUtils, NodeData}


class CloudiaClientServlet(servletName: String) extends CloudiaclientStack {

  implicit val timeout = akka.util.Timeout(new FiniteDuration(1, SECONDS)) // Timeout for the resolveOne call
  implicit val system = ActorSystem("cloudia-client")
  val controller = new AppController()

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
    val nodeName = params("node")//.head
    val path = multiParams("splat").head

    controller.nodes().get(nodeName) match {
      case Some(data) => data.ref match {
          case Some(actorRef) =>{
            val index = Await.result(actorRef.ask(Handshake())(1 second).mapTo[DirectoryIndex], 1 second)
            IndexUtils.indexAt(index, path) match {
              case Some(found) => jade("nodeindex","servletName" -> servletName, "nodeName" -> nodeName, "index" -> found)
              case _ => jade("error", "reason" -> "No such directory!")
            }
          }
          case _ => jade("error", "reason" -> "No such node!")
        }
      case _ => jade("error", "reason" -> "No such node!")
    }

  }


  post("/register"){
    "registered!"

  }

  post("/:node/*"){
    val nodeName = params("node")
    val path = multiParams("splat").head
    val filename = params("file")
    print("gonna push file ")
    println(path + "/" + filename)
    redirect("/home/"+path)
  }


}
