package utils

import akka.actor.ActorRef

/**
  * Created by marcin on 6/17/17.
  */
object Jade {
  def nodeNames(implicit nodeMap: Map[String, ActorRef]): (String, List[String]) = "nodeNames" -> nodeMap.keys.toList

  def error(implicit reason: String): (String, String) = "error" -> reason

  def servletName(implicit servletName: String): (String, String) = "servletName" -> servletName

  def node(nodeName: String): (String, String) = "nodeName" -> nodeName
}
