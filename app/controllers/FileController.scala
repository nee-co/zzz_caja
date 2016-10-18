package controllers

import java.io.{File, FileOutputStream}
import javax.inject.Inject

import models.CajaRequest
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import util.{DBService, S3Service, Using}

class FileController @Inject()(db: DBService, s3: S3Service) extends Controller {
  def download(path: String) = Action {
    val name: String = if (0 < path.count(_ == '/')) {
      path.substring(path.lastIndexOf("/") + 1)
    } else {
      path
    }

    val extension: String = if (path.matches(""".+[.].+""")) {
      path.substring(path.lastIndexOf('.'))
    } else {
      new String("")
    }

    val file: File = File.createTempFile("temp", extension)

    s3.download(path) match {
      case Some(result) =>
        for (out <- Using(new FileOutputStream(file))) out.write(result)
        Ok.sendFile(file, fileName = {f => name}, onClose = {() => file.delete})
      case None => Status(500)
    }
  }

  def delete(path: String) = Action { implicit request =>
    val userId = request.headers.get("x-consumer-custom-id").map(str => str.toInt)

    if (db.canDelete(path, userId)) {
      (s3.delete(path), db.deleteByPath(path)) match {
        case (true, true) => Status(204)
        case (   _,    _) => Status(500)
      }
    } else {
      Status(403)
    }
  }

  def update(path: String) = Action { implicit request =>
    val jsonValues: CajaRequest = Json.parse(request.body.asJson.get.toString).validate[CajaRequest].get
    val userId = request.headers.get("x-consumer-custom-id").map(str => str.toInt)

    db.updateTargetByJson(path, jsonValues, userId) match {
      case true  => Status(204)
      case false => Status(500)
    }
  }

  def create(path: String) = Action(parse.multipartFormData) { implicit request =>
    val userId = request.headers.get("x-consumer-custom-id").map(str => str.toInt)
    val targetType = request.body.dataParts("target_type").head
    val publicIds = request.body.dataParts("public_ids").mkString(",")
    val file = request.body.file("file").get

    (db.addFile(targetType, path, file.filename, userId, publicIds), s3.upload(path, file.filename, file.ref.file)) match {
      case (true, true) => Status(201)
      case (   _,    _) => Status(500)
    }
  }
}
