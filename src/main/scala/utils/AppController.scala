package utils

import akka.actor.{ActorRef, ActorSystem}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Success, Try}

/**
  * Created by marcin on 6/15/17.
  */
class AppController(implicit val actorSystem: ActorSystem) {
  private val nodeMap = new mutable.HashMap[String, NodeData]()
  nodeMap.put("home", NodeData("cloudia-server", "127.0.0.1", 8888, "server"))

  def nodes(): Map[String, NodeData] = nodeMap.toMap

}

case class NodeData(systemName: String, host: String, port: Int, nodeName: String)
                   (implicit val system: ActorSystem){
  def path: String = s"akka.tcp://$systemName@$host:$port/user/$nodeName"

  def ref: Option[ActorRef]= {
    val timeout = FiniteDuration(1, SECONDS)
    val selection = system.actorSelection(path)
    Try(Await.result(selection.resolveOne(timeout), timeout)) match {
      case Success(result) => Some(result)
      case _ => None
    }
  }

}
