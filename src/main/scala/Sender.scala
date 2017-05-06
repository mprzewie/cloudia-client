

import java.io.File

import akka.actor._



/**
  * Created by marcin on 5/6/17.
  */
class Sender(implicit val host: String, implicit val port: Int) extends Actor{
  def addressOf(actorName: String) = s"$protocol://$system@$host:$port/user/$actorName"
  val protocol="akka.tcp"
  val system="cloudia-server"
  val receiverName="manifesto-receiver"

  val remote: ActorSelection = context.actorSelection(addressOf(receiverName))

  override def receive: PartialFunction[Any, Unit] = {
    case "ready" =>
      println("received ok")
    case msg: String =>
      println(s"Sender received '$msg'")
      remote ! FileManifesto(new File("build.sbt"), 1024)
  }
}

object Main extends App {
  implicit val system = ActorSystem("cloudia-client")
  implicit val host = "127.0.0.1"
  implicit val port = 8888
  val sender =system.actorOf(Props(new Sender), name = "sender")

  sender ! "start!"





}
