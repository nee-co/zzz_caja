package controllers

import javax.inject.Inject

import models.{CajaRequest, ObjectProperty, User}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import util.{DBService, JsonFormatter, S3Service, WsService}

import scala.collection.mutable

class DirectoryController @Inject()(db: DBService, ws: WsService, json: JsonFormatter, s3: S3Service) extends Controller {
  def objectList(path: String) = Action { implicit request =>
    val loginUser: Option[User] = ws.user(request.headers.get("x-consumer-custom-id").fold(0)(id => id.toInt))
    val currentDir = db.getDirProperty(path)

    if (loginUser.isEmpty) {
      Status(401)
    } else if (currentDir.nonEmpty) {
      val objectList = loginUser.fold(Seq.empty[ObjectProperty])(user => db.getByLoginProperty(path, user))
      val userMap = new mutable.HashMap[Integer, User]
      val userIds = currentDir.get.targetType match {
        case "user" => objectList.map(_.insertedBy.toString) ++ currentDir.flatMap(_.publicIds).fold(Seq.empty[String])(str => str.split(",").toSeq)
        case "college" => objectList.map(_.insertedBy.toString)
      }

      ws.users(userIds).foreach(user => userMap.put(user.user_id, user))

      json.toJsonResponse(currentDir, objectList, userMap) match {
        case Some(jsValue) => Ok(jsValue)
        case None => Status(500)
      }
    } else {
      Status(400)
    }
  }

  def updateOrCreate(path: String) = Action { implicit request =>
    val jsonValues = Json.parse(request.body.asJson.get.toString).validate[CajaRequest].get
    val userId = request.headers.get("x-consumer-custom-id").map(str => str.toInt)
    val dirPath = s"$path${jsonValues.name}/"

    if (s3.hasObject(dirPath)) {
      db.updateTargetByJson(dirPath, jsonValues, userId) match {
        case true  => Status(204)
        case false => Status(500)
      }
    } else {
      (db.addDirectory(dirPath, jsonValues, userId), s3.createDir(dirPath)) match {
        case (true, true) => Status(201)
        case (   _,    _) => Status(500)
      }
    }
  }

  def delete(path: String) = Action { implicit request =>
    val keys = s3.getUnderKeys(path)
    val userId = request.headers.get("x-consumer-custom-id").map(str => str.toInt)

    if (db.canDelete(path, userId)) {
      !keys.map(key => db.deleteByPath(key) && s3.delete(key)).contains(false) match {
        case true  => Status(204)
        case false => Status(500)
      }
    } else {
      Status(403)
    }
  }
}