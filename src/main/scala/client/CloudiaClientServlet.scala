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




class CloudiaClientServlet extends CloudiaclientStack {

  implicit val timeout = akka.util.Timeout(new FiniteDuration(1, SECONDS)) // Timeout for the resolveOne call
  val system = ActorSystem("cloudia-client")

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say
        <a href="hello-scalate">hello to Scalate</a>
        .
      </body>
    </html>
  }

  get("/cloudia") {

    val server = system.actorSelection("akka.tcp://cloudia-server@127.0.0.1:8888/user/server")
    server.resolveOne().onComplete {
      case actor =>
        println(actor)

    }

    val index = Await.result(server.ask(Handshake())(1 second).mapTo[DirectoryIndex], 1 second)

    contentType = "text/html"

    Jason.toJson(index)

  }

}
