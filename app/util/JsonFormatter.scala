package util

import models._
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable

class JsonFormatter {
  val colleges: Map[String, College] = Map("A" -> College("A", "クリエイター"), "B" -> College("B","ミュージック"),
                                           "C" -> College("C", "IT"), "D" -> College("D", "テクノロジー"),
                                           "E" -> College("E", "医療・保育"), "F" -> College("F","スポーツ"), "G" -> College("G", "デザイン"))

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