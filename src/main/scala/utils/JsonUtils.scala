package utils

import index._

import scala.collection.mutable
import scala.util.parsing.json.{JSONArray, JSONObject}
/**
  * Created by marcin on 6/12/17.
  */
object JsonUtils {

  def toJson(index: Index): JSONObject = {
    val name = index.handler.getName
    val map = new mutable.HashMap[String, Any]()
    map.put("name", name)
    index match {
      case directory: DirectoryIndex =>
        val subs = JSONArray((directory.subDirectories ++ directory.subFiles).map(toJson(_)))
        map.put("subs", subs)
        JSONObject(map.toMap)

      case file:FileIndex =>
        JSONObject(map.toMap)


    }
  }

}
