package controllers

import javax.inject.Inject

import models.{ObjectProperty, User}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import util.{DBService, JsonFormatter, WsService}

import scala.collection.mutable

class DirectoryController @Inject()(db: DBService, ws: WsService, json: JsonFormatter) extends Controller {
  def ObjectList(path: String) = Action { //implicit request =>
    //val loginUser: Option[User] = ws.user(request.headers.get("x-consumer-custom-id").fold(0)(id => id.toInt))
    val loginUser: Option[User] = ws.user(1)
    val objectList = loginUser.fold(Seq.empty[ObjectProperty])(user => db.getByLoginProperty(path, user))
    val userMap = new mutable.HashMap[Integer, User]
    val currentDir = db.getDirProperty(path)

    ws.users(objectList.map(_.created_user.toString) ++ currentDir.flatMap(_.public_ids).fold(Seq.empty[String])(str => str.split(",").toSeq)).foreach(user => userMap.put(user.user_id, user))
    Ok(json.toJsonResponse(currentDir, objectList, userMap).getOrElse(Json.toJson("error")))
  }
}