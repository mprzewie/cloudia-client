package utils

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}
import akka.remote.WireFormats.FiniteDuration

import scala.collection.mutable
import communication.{Ping, Request}

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask

import scala.util.{Failure, Try}

/**
  * Created by marcin on 6/15/17.
  */
private class NodeController(nodeMap: mutable.HashMap[String, ActorRef]) extends Actor {

  implicit val timeout = akka.util.Timeout(30 seconds)

  context.setReceiveTimeout(timeout.duration)

  override def receive: Receive = {
    case Request(nodeName: String) => {
      println(s"pinged by $nodeName")
      if (!nodeMap.keys.exists(_ == nodeName)) {
        nodeMap.put(nodeName, sender)
        println(s"$nodeName registered")
      } else {
        println(s"$nodeName already exists!")
      }
    }
    case ReceiveTimeout => {
      nodeMap.par.foreach { case (name, nodeRef) =>
        Try(Await.result(nodeRef.ask(Ping()), 1 second)) match {
          case Failure(_) => nodeMap.remove(name)
          case _ => ()
        }
      }
      context.setReceiveTimeout(timeout.duration)
    }
    case _ => println("Sth else has happenned")
  }
}


object NodeController {
  def props(nodeMap: mutable.HashMap[String, ActorRef]) = Props(new NodeController(nodeMap))
}
