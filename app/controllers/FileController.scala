package controllers

import java.io.{File, FileOutputStream}
import javax.inject.Inject
import play.api.mvc.{Action, Controller}
import util.{DBService, S3Service, Using}

class FileController @Inject()(db: DBService, s3: S3Service) extends Controller {
  def download(path: String) = Action {
    val name: String = if (0 < path.count(_ == '/')) {
      path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf('.'))
    } else {
      path.substring(0, path.lastIndexOf('.'))
    }
    val extension: String = path.substring(path.lastIndexOf('.'))
    val file: File = File.createTempFile("temp", extension)

    s3.download(path) match {
      case Some(result) =>
        for (out <- Using(new FileOutputStream(file))) out.write(result)
        Ok.sendFile(file, fileName = {f => s"$name$extension"}, onClose = {() => file.delete})
      case None => Status(500)
    }
  }

  def delete(path: String) = Action {
    s3.delete(path) match {
      case true  => if (db.deleteByFilepath(path)) Status(204) else Status(403)
      case false => Status(403)
    }
  }
}
