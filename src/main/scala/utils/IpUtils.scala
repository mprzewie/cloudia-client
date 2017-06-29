package utils

import java.net._

import scala.collection.mutable.ListBuffer

/**
  * Created by marcin on 6/17/17.
  */
object IpUtils {

  def inet(): String = {
    val ips = new ListBuffer[String]()
    val interface = NetworkInterface.getNetworkInterfaces
    val n = interface.nextElement match {
      case e: NetworkInterface => e
      case _ => ???
    }
    val iterator = n.getInetAddresses
    while (iterator.hasMoreElements) {
      iterator.nextElement match {
        case i: InetAddress => ips += i.getHostAddress
        case _ => ()
      }
    }
    ips.filter(_.startsWith("192.168")).head
  }
}
