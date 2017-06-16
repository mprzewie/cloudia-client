package utils

import akka.actor.ActorRef

/**
  * Created by marcin on 6/17/17.
  */
object Jade {
  def nodeNames(implicit nodeMap: Map[String, ActorRef]) = "nodeNames" -> nodeMap.keys.toList
  def error(implicit reason: String) = "error" -> reason
  def servletName(implicit servletName: String) = "servletName" -> servletName
  def node(nodeName: String) = "nodeName" -> nodeName
}
