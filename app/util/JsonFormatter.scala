package util

import models._
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable

class JsonFormatter {
  val colleges: Map[String, College] = Map("A" -> College("A", "クリエイター"), "B" -> College("B","ミュージック"),
                                           "C" -> College("C", "IT"), "D" -> College("D", "テクノロジー"),
                                           "E" -> College("E", "医療・保育"), "F" -> College("F","スポーツ"), "G" -> College("G", "デザイン"))

  def toJsonResponse(current_dir: Option[TargetProperty], objectList: Seq[ObjectProperty], userList: mutable.Map[Integer, User]): Option[JsValue] = {
    val result = for {
      dir <- current_dir
    } yield {
      val objects = objectList.map(obj =>
        Object(obj.obj_type, obj.name, userList(obj.created_user), new DateTime(obj.created_at).toString("yyyy/MM/dd HH:mm"), new DateTime(obj.updated_at).toString("yyyy/MM/dd HH:mm"))
      ).toList

      dir.target_type match {
        case "user" =>
          val targets = dir.public_ids.map(str => str.split(",")).map(ids => ids.map(id => userList(id.toInt)).toList)
          Json.toJson(ResponseHasUser(PropertyHasUser(dir.name, dir.target_type, targets.fold(List.empty[User])(users => users)), objects))
        case "college" =>
          val targets = dir.public_ids.map(str => str.split(",")).map(codes => codes.map(code => colleges(code)).toList)
          Json.toJson(ResponseHasCollege(PropertyHasCollege(dir.name, dir.target_type, targets.fold(List.empty[College])(colleges => colleges)), objects))
      }
    }
    result.iterator.toStream.headOption
  }
}