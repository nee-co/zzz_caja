package util

import java.sql.Timestamp
import javax.inject.Inject

import models.{CajaRequest, LoginUser, ObjectProperty}
import models.Tables.{DirectoriesRow, FilesRow}
import org.joda.time.LocalDateTime

class DBService @Inject()(private val db: ObjectDao) {
  private def nowTimestamp: Timestamp = new Timestamp(new LocalDateTime().toDateTime().getMillis)

  private def toDirKeyList(path: String): Seq[String] = {
    val dirList = path.split("/")

    val result = dirList.zipWithIndex.map { case (dirName, num) =>
      val buff = new StringBuilder
      for (i <- 0 to num) { buff.append(dirList.apply(i) + "/") }
      buff.toString()
    }
    result
  }

  def addByFilepath(userId: Int, userIds: String, collegeIds:String, path: String): Boolean = {
    val filePath = new StringBuilder
    var dirKeyList = Seq.empty[String]

    if (path.count(_ == '/') == 0) {
      filePath.append(path)
    } else if (path.last == '/') {
      dirKeyList = toDirKeyList(path)
    } else {
      filePath.append(path)
      dirKeyList = toDirKeyList(path.substring(0, path.lastIndexOf("/")))
    }

    if (dirKeyList.isEmpty && !db.hasObj(filePath.toString)) {
      db.add(FilesRow(0, None, Some(userIds), Some(collegeIds), filePath.toString, filePath.toString, userId, nowTimestamp, nowTimestamp))
    } else if (!db.hasObj(path)) {
      dirKeyList.map { dirPath =>
        if (!db.hasObj(dirPath)) {
          db.add(DirectoriesRow(0, db.findParentId(dirPath), Some(userIds), Some(collegeIds), dirPath, userId, nowTimestamp, nowTimestamp))
        } else {
          db.getDirectory(dirPath).map(dir => db.update(DirectoriesRow(dir.id, dir.parentId, dir.userIds, dir.collegeIds, dir.name, dir.insertedBy, dir.insertedAt, nowTimestamp))).get
        }
      }
      dirKeyList.foreach(result => if (result == false) return false)

      if (filePath.nonEmpty && !db.hasObj(filePath.toString)) {
        db.add(FilesRow(0, db.findParentId(filePath.toString), Some(userIds), Some(collegeIds), filePath.substring(filePath.lastIndexOf("/") + 1), filePath.toString, userId, nowTimestamp, nowTimestamp))
      } else {
        true
      }
    } else {
      false
    }
  }

  def deleteByFilepath(path: String): Boolean = {
    db.getFile(path) match {
      case Some(file) => if (db.delete(file)) true else false
      case None => false
    }
  }

  def updateTargetByJson(path: String, jsonValues: CajaRequest): Boolean = {
    db.objType(path) match {

      case "dir"  =>
        val obj = db.getDirectory(path)

        if (obj.nonEmpty && jsonValues.target_type == "college") {
          db.update(DirectoriesRow(obj.get.id, obj.get.parentId, obj.get.userIds, Some(jsonValues.public_ids.mkString(",")), obj.get.name, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
        } else if (obj.nonEmpty && jsonValues.target_type == "user") {
          db.update(DirectoriesRow(obj.get.id, obj.get.parentId, Some(jsonValues.public_ids.mkString(",")), obj.get.collegeIds, obj.get.name, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
        } else {
          false
        }

      case "file" =>
        val obj = db.getFile(path)

        if (obj.nonEmpty && jsonValues.target_type == "college") {
          db.update(FilesRow(obj.get.id, obj.get.parentId, obj.get.userIds, Some(jsonValues.public_ids.mkString(",")), obj.get.name, obj.get.path, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
        } else if (obj.nonEmpty && jsonValues.target_type == "user") {
          db.update(FilesRow(obj.get.id, obj.get.parentId, Some(jsonValues.public_ids.mkString(",")), obj.get.collegeIds, obj.get.name, obj.get.path, obj.get.insertedBy, obj.get.insertedAt, obj.get.updatedAt))
        } else {
          false
        }
    }
  }

  def getByLoginProperty(path: String, user: LoginUser): Seq[ObjectProperty] = db.findByLoginProperty(path, user.user_id.toString, user.college.code)
}