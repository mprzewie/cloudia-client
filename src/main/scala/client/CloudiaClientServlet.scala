package client

import akka.actor.ActorSystem
import communication.Handshake
import index.DirectoryIndex

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import utils.{AppController, IndexUtils, JsonUtils, NodeData}





class CloudiaClientServlet extends CloudiaclientStack {

  implicit val timeout = akka.util.Timeout(new FiniteDuration(1, SECONDS)) // Timeout for the resolveOne call
  implicit val system = ActorSystem("cloudia-client")
  val controller = new AppController()
//  val homeNode = NodeData("cloudia-server", "127.0.0.1", 8888, "server")

  get("/"){
    "Welcome to cloudia!"
  }

  get("/*"){
    redirect(s"/${multiParams("splat").head}/ ")
  }


  get("/*/*") {
    contentType="text/html"
    val nodeName = multiParams("splat").head
    val path = multiParams("splat").tail.head

    controller.nodes().get(nodeName) match {
      case Some(data) => data.ref match {
          case Some(actorRef) =>{
            val index = Await.result(actorRef.ask(Handshake())(1 second).mapTo[DirectoryIndex], 1 second)
            IndexUtils.indexAt(index, path) match {
              case Some(found) => jade("homeindex", "index" -> found)
              case _ => jade("error", "reason" -> "No such directory!")
            }
          }
          case _ => jade("error", "reason" -> "No such node!")
        }
      case _ => jade("error", "reason" -> "No such node!")
    }

  }

  post("/home/*"){
    val path = multiParams("splat").head
    val filename = params("file")
    print("gonna push file ")
    println(path + "/" + filename)
    redirect("/home/"+path)
  }

}
