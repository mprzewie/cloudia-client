package utils

import index._

/**
  * Created by marcin on 6/14/17.
  */
object IndexUtils {
  def toHtml(index: Index): String = {
    index match {
      case index: FileIndex => index.handler.getName
      case index: DirectoryIndex => index.handler.getName +
        "<ul>" +
        (for (i <- index.subDirectories ++ index.subFiles) yield "<li>" + i.handler.getName + "</li>").fold("")(_ + _) +
        "</ul>"
    }
  }

  def indexAt(root: DirectoryIndex, path: String): Option[Index] = {
    def searchForAt(index: DirectoryIndex, path: List[String]): Option[Index] = {
      path match {
        case Nil => Some(index)
        case h :: t =>
          val toSearchIn = t match {
            case Nil => index.subDirectories ++ index.subFiles
            case _ => index.subDirectories
          }

          if (toSearchIn.exists(_.handler.getName == h)) {
            toSearchIn.filter(_.handler.getName == h).head match {
              case dirIndex: DirectoryIndex => searchForAt(dirIndex, t)
              case fileIndex: FileIndex => Some(fileIndex)
              case _ => None
            }
          }
          else None
      }
    }

    searchForAt(root, path.split("/").toList.filter(!_.isEmpty))
  }

}
