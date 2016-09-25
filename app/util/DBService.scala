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

  def getByLoginProperty(path: String, user: User): Seq[ObjectProperty] = db.findByLoginProperty(path, user.user_id.toString, user.college.code)

  def getDirProperty(path: String): Option[TargetProperty] = {
    val dir = db.getDirectory(path)

    if (dir.isEmpty) return None

    val name = if (path.count(_ == '/') != 0) {
      val buf = path.dropRight(1)
      if (buf.count(_ == '/') == 0) buf else buf.substring(buf.lastIndexOf("/") + 1)
    } else path

    Some(dir.flatMap(_.userIds).fold(TargetProperty("college", dir.get.collegeIds, name))(ids => TargetProperty("user", Some(ids), name)))
  }

  def addFile(path: String, targets: CajaRequest, userId: Int): Boolean = {
    val dirPaths = toDirKeyList(path.substring(0, path.lastIndexOf("/")))

    db.findParentId(path).fold(return false)(id => targets.target_type match {
      case "user"    => db.add(FilesRow(0, Some(id), Some(targets.public_ids.mkString(",")), None, path.substring(path.lastIndexOf('/') + 1), path, userId, nowTimestamp, nowTimestamp))
      case "college" => db.add(FilesRow(0, Some(id), None, Some(targets.public_ids.mkString(",")), path.substring(path.lastIndexOf('/') + 1), path, userId, nowTimestamp, nowTimestamp))
    })

    !dirPaths.map { dirPath =>
      db.getDirectory(dirPath).map { dir =>
        db.update(DirectoriesRow(dir.id, dir.parentId, dir.userIds, dir.collegeIds, dir.name, dir.insertedBy, dir.insertedAt, nowTimestamp))
      }
    }.contains(false)
  }

  def addDirectory(path: String, targets: CajaRequest, userId: Int): Boolean = {
    db.findParentId(path).fold(false)(id => targets.target_type match {
      case "user"    => db.add(DirectoriesRow(0, Some(id), Some(targets.public_ids.mkString(",")), None, path, userId, nowTimestamp, nowTimestamp))
      case "college" => db.add(DirectoriesRow(0, Some(id), None, Some(targets.public_ids.mkString(",")), path, userId, nowTimestamp, nowTimestamp))
    })
  }

  def updateTargetByJson(path: String, jsonValues: CajaRequest): Boolean = {
    db.objType(path) match {
      case "dir"  =>
        val obj = db.getDirectory(path)

        (obj.nonEmpty, jsonValues.target_type) match {
          case (true, "user"   ) => db.update(DirectoriesRow(obj.get.id, obj.get.parentId, Some(jsonValues.public_ids.mkString(",")), obj.get.collegeIds, obj.get.name, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
          case (true, "college") => db.update(DirectoriesRow(obj.get.id, obj.get.parentId, obj.get.userIds, Some(jsonValues.public_ids.mkString(",")), obj.get.name, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
          case (_   , _        ) => false
        }

      case "file" =>
        val obj = db.getFile(path)

        (obj.nonEmpty, jsonValues.target_type) match {
          case (true, "user"   ) => db.update(FilesRow(obj.get.id, obj.get.parentId, Some(jsonValues.public_ids.mkString(",")), obj.get.collegeIds, obj.get.name, obj.get.path, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
          case (true, "college") => db.update(FilesRow(obj.get.id, obj.get.parentId, obj.get.userIds, Some(jsonValues.public_ids.mkString(",")), obj.get.name, obj.get.path, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
          case (_   , _        ) => false
        }
    }
  }

  def deleteByPath(path: String): Boolean = {
    if (path.last == '/') {
      db.getDirectory(path) match {
        case Some(result) => db.delete(result)
        case None => false
      }
    } else {
      db.getFile(path) match {
        case Some(result) => db.delete(result)
        case None => false
      }
    }
  }
}