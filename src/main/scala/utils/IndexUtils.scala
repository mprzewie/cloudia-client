package utils

import index._

/**
  * Created by marcin on 6/14/17.
  */
object IndexUtils {
  def toHtml(index: Index): String = {
    index match {
      case index:FileIndex => index.handler.getName
      case index:DirectoryIndex => index.handler.getName +
        "<ul>" +
        (for (i <- index.subDirectories ++ index.subFiles) yield  "<li>" + i.handler.getName +"</li>").fold("")(_+_) +
        "</ul>"
    }
  }

  def indexAt(root: DirectoryIndex, path: String): Option[DirectoryIndex]={
    def searchForAt(index: DirectoryIndex, path:List[String]): Option[DirectoryIndex] ={
      path match {
        case h::t =>
          if(index.subDirectories.exists(_.handler.getName==h)) searchForAt(index.subDirectories.filter(_.handler.getName==h).head, t)
          else None
        case _ => Some(index)
      }
    }
    searchForAt(root, path.split("/").toList.filter(!_.isEmpty) )
  }

}
