package utils

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}

import scala.collection.mutable
import communication.{Ping, Request}

/**
  * Created by marcin on 6/15/17.
  */
private class AppController extends Actor {
  private var nodeMap = new mutable.HashMap[String, ActorRef]()
  def nodes() = nodeMap.toMap

  override def receive: Receive = {
    case Request(nodeName: String) => {
      println(s"pinged by $nodeName")
      nodeMap.put(nodeName, sender)
    }
    case Ping() => sender ! nodes()
    case ReceiveTimeout => nodeMap = nodeMap
    case _ => println("WTF")
  }

}


object AppController {
  def props() = Props(new AppController)

}
