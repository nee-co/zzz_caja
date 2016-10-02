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

  def delete(path: String) = Action {
    val userId = 1
    //val userId = request.headers.get("x-consumer-custom-id").fold(0)(id => id.toInt)

    if (db.canDelete(path, userId)) {
      (s3.delete(path), db.deleteByPath(path)) match {
        case (true, true) => Status(204)
        case (   _,    _) => Status(500)
      }
    } else {
      Status(403)
    }
  }

  def changeTarget(path: String) = Action { implicit request =>
    val jsonValues: CajaRequest = Json.parse(request.body.asJson.get.toString).validate[CajaRequest].get
    val userId = 1
    //val userId = request.headers.get("x-consumer-custom-id").fold(0)(id => id.toInt)

    db.updateTargetByJson(path, jsonValues, userId) match {
      case true  => Status(204)
      case false => Status(500)
    }
  }
}
