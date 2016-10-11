package util

import models._
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable

class JsonFormatter {
  val colleges: Map[String, College] = Map("a" -> College("a", "クリエイター"), "b" -> College("b","ミュージック"),
                                           "c" -> College("c", "IT"), "d" -> College("D", "テクノロジー"),
                                           "e" -> College("e", "医療・保育"), "f" -> College("f","スポーツ"), "g" -> College("g", "デザイン"))

  def toJsonResponse(currentDir: Option[TargetProperty], objectList: Seq[ObjectProperty], userList: mutable.Map[Integer, User]): Option[JsValue] = {
    val result = for {
      dir <- currentDir
    } yield {
      val objects = objectList.map(obj =>
        Object(obj.objType, obj.name, userList(obj.insertedBy), new DateTime(obj.insertedAt).toString("yyyy/MM/dd HH:mm"), new DateTime(obj.updatedAt).toString("yyyy/MM/dd HH:mm"))
      ).toList

      dir.targetType match {
        case "user" =>
          val targets = dir.publicIds.map(str => str.split(",")).map(ids => ids.map(id => userList(id.toInt)).toList)
          Json.toJson(ResponseHasUser(PropertyHasUser(dir.name, dir.targetType, targets.fold(List.empty[User])(identity)), objects))
        case "college" =>
          val targets = dir.publicIds.map(str => str.split(",")).map(codes => codes.map(code => colleges(code)).toList)
          Json.toJson(ResponseHasCollege(PropertyHasCollege(dir.name, dir.targetType, targets.fold(List.empty[College])(identity)), objects))
      }
    }
    result.iterator.toStream.headOption
  }
}