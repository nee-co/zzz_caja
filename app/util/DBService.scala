package util

import java.sql.Timestamp
import javax.inject.Inject

import models.{CajaRequest, ObjectProperty, TargetProperty, User}
import models.Tables.{DirectoriesRow, FilesRow}
import org.joda.time.LocalDateTime

class DBService @Inject()(private val db: ObjectDao) {
  private def nowTimestamp: Timestamp = new Timestamp(new LocalDateTime().toDateTime().getMillis)

  private def toDirKeyList(path: String): Seq[String] = {
    val dirList = path.split("/")

    dirList.zipWithIndex.map { case (dirName, num) =>
      val buff = new StringBuilder
      for (i <- 0 to num) { buff.append(dirList.apply(i) + "/") }
      buff.toString()
    }
  }

  private def updateDirs(dirPaths: Seq[String]): Boolean = {
    !dirPaths.map { dirPath =>
      db.getDirectory(dirPath).map { dir =>
        db.update(DirectoriesRow(dir.id, dir.parentId, dir.userIds, dir.collegeCodes, dir.name, dir.insertedBy, dir.insertedAt, nowTimestamp))
      }
    }.contains(false)
  }

  def getByLoginProperty(path: String, user: User): Seq[ObjectProperty] = db.findByLoginProperty(path, user.user_id.toString, user.college.code)

  def getDirProperty(path: String): Option[TargetProperty] = {
    val dir = db.getDirectory(path)

    if (dir.isEmpty) return None

    val name = if (path.count(_ == '/') != 0) {
      val buf = path.dropRight(1)
      if (buf.count(_ == '/') == 0) buf else buf.substring(buf.lastIndexOf("/") + 1)
    } else path

    Some(dir.flatMap(_.userIds).fold(TargetProperty("college", dir.get.collegeCodes, name))(ids => TargetProperty("user", Some(ids), name)))
  }

  def addFile(path: String, targets: CajaRequest, userId: Option[Int]): Boolean = {
    val dirPaths = toDirKeyList(path.substring(0, path.lastIndexOf("/")))

    if (userId.isEmpty) return false

    db.findParentId(path).fold(return false)(id => targets.target_type match {
      case "user"    => db.add(FilesRow(0, Some(id), Some(targets.public_ids.mkString(",")), None, path.substring(path.lastIndexOf('/') + 1), path, userId.get, nowTimestamp, nowTimestamp))
      case "college" => db.add(FilesRow(0, Some(id), None, Some(targets.public_ids.mkString(",")), path.substring(path.lastIndexOf('/') + 1), path, userId.get, nowTimestamp, nowTimestamp))
    })

    updateDirs(dirPaths)
  }

  def addDirectory(path: String, jsonValues: CajaRequest, userId: Option[Int]): Boolean = {
    val dirPaths = toDirKeyList(path)

    if (userId.isEmpty) return false

    db.findParentId(path).fold(false)(id => jsonValues.target_type match {
      case "user"    => db.add(DirectoriesRow(0, Some(id), Some(jsonValues.public_ids.mkString(",")), None, path, userId.get, nowTimestamp, nowTimestamp))
      case "college" => db.add(DirectoriesRow(0, Some(id), None, Some(jsonValues.public_ids.mkString(",")), path, userId.get, nowTimestamp, nowTimestamp))
    })

    updateDirs(dirPaths)
  }

  def updateTargetByJson(path: String, jsonValues: CajaRequest, userId: Option[Int]): Boolean = {
    db.objType(path) match {
      case "dir"  =>
        val obj = db.getDirectory(path)
        obj.fold(return false)(obj => if (obj.insertedBy != userId.fold(return false)(identity)) return false)

        jsonValues.target_type match {
          case "user"    => db.update(DirectoriesRow(obj.get.id, obj.get.parentId, Some(jsonValues.public_ids.mkString(",")), obj.get.collegeCodes, obj.get.name, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
          case "college" => db.update(DirectoriesRow(obj.get.id, obj.get.parentId, obj.get.userIds, Some(jsonValues.public_ids.mkString(",")), obj.get.name, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
        }

      case "file" =>
        val obj = db.getFile(path)
        obj.fold(return false)(obj => if (obj.insertedBy != userId.fold(return false)(identity)) return false)

        jsonValues.target_type match {
          case "user"    => db.update(FilesRow(obj.get.id, obj.get.parentId, Some(jsonValues.public_ids.mkString(",")), obj.get.collegeCodes, obj.get.name, obj.get.path, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
          case "college" => db.update(FilesRow(obj.get.id, obj.get.parentId, obj.get.userIds, Some(jsonValues.public_ids.mkString(",")), obj.get.name, obj.get.path, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
        }
    }
  }

  def deleteByPath(path: String): Boolean = {
    val dirPaths = if (path.last == '/') {
      val buf = path.dropRight(1)
      toDirKeyList(buf.substring(0, buf.lastIndexOf("/") + 1))
    } else if (0 < path.count(_ == '/')) {
      toDirKeyList(path.substring(0, path.lastIndexOf("/") + 1))
    } else Seq.empty[String]

    if (path.last == '/') {
      db.getDirectory(path) match {
        case Some(result) => db.delete(result) && updateDirs(dirPaths)
        case None => false
      }
    } else {
      db.getFile(path) match {
        case Some(result) => db.delete(result) && updateDirs(dirPaths)
        case None => false
      }
    }
  }

  def canDelete(path: String, userId: Option[Int]): Boolean = {
    if (path.last == '/') {
      val obj = db.getDirectory(path)
      obj.fold(false)(obj => obj.insertedBy == userId.fold(return false)(identity))
    } else {
      val obj = db.getFile(path)
      obj.fold(false)(obj => obj.insertedBy == userId.fold(return false)(identity))
    }
  }
}