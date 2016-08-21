package util

import java.sql.Timestamp

import models.Tables.{Directories, DirectoriesRow, Files, FilesRow}
import javax.inject.Inject

import org.joda.time.LocalDateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.{Await, Future}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class ObjectDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val files       = new TableQuery(tag => new Files(tag))
  val directories = new TableQuery(tag => new Directories(tag))

  def findAll: Option[Seq[ObjectProperty]] = {
    var objects = ArrayBuffer.empty[ObjectProperty]
    val fileResult: Future[Seq[FilesRow]] = db.run(files.sortBy(_.id.asc).result)
    val dirResult: Future[Seq[DirectoriesRow]] = db.run(directories.sortBy(_.id.asc).result)

    Await.ready(fileResult, Duration.Inf)
    Await.ready(dirResult, Duration.Inf)

    fileResult.value.get match {
      case Success(rows) => rows.foreach(obj => objects += ObjectProperty("file", obj.name, obj.insertedBy, obj.insertedAt, obj.updatedAt))
      case Failure(t) => None
    }

    dirResult.value.get match {
      case Success(rows) => rows.foreach(obj => objects += ObjectProperty("dir", obj.name, obj.insertedBy, obj.insertedAt, obj.updatedAt))
      case Failure(t) => None
    }

    if (objects.isEmpty) None else Some(objects)
  }

  def findAllByDir(path: String): Option[Seq[ObjectProperty]] = {
    var objects = ArrayBuffer.empty[ObjectProperty]
    val parentId = findDirId(path)

    val filesResult: Future[Seq[FilesRow]] = parentId match {
      case Some(id) => db.run(files.filter(_.parentId === id).result)
      case None => db.run(files.filter(_.parentId.isEmpty).result)
    }

    val dirResult: Future[Seq[DirectoriesRow]] = parentId match {
      case Some(id) => db.run (directories.filter(_.parentId === id).result)
      case None => db.run(directories.filter(_.parentId.isEmpty).result)
    }

    Await.ready(filesResult, Duration.Inf)
    Await.ready(dirResult, Duration.Inf)

    filesResult.value.get match {
      case Success(rows) => rows.foreach(obj => objects += ObjectProperty("file", obj.name, obj.insertedBy, obj.insertedAt, obj.updatedAt))
      case Failure(t) => None
    }

    dirResult.value.get match {
      case Success(rows) => rows.foreach(obj => objects += ObjectProperty("dir", obj.name, obj.insertedBy, obj.insertedAt, obj.updatedAt))
      case Failure(t) => None
    }

    if (objects.isEmpty) None else Some(objects)
  }

  private def findDirId(name: String): Option[Int] = {
    if (name == "/") return None

    val dirId: Future[Option[Int]] = db.run(directories.filter(_.name === name).map(_.id).result.headOption)

    Await.ready(dirId, Duration.Inf)

    dirId.value.get match {
      case Success(result) => result match {
        case Some(id) => Some(id)
        case None => Some(0)
      }
      case Failure(t) => None
    }
  }

  private def findParentId(name: String): Option[Int] = {
    if ((name.count(_ == '/') == 1 && name.last == '/') || name.count(_ == '/') == 0) return None

    val id = name.last == '/' match {
      case true  => db.run(directories.filter(_.name === name.substring(0, name.dropRight(1).lastIndexOf("/") + 1)).map(_.id).result.headOption)
      case false => db.run(directories.filter(_.name === name.substring(0, name.lastIndexOf("/") + 1)).map(_.id).result.headOption)
    }

    Await.ready(id, Duration.Inf)

    id.value.get match {
      case Success(result) => result
      case Failure(t) => None
    }
  }

  def add[T](obj: T): Boolean = {
    val result = obj match {
      case obj: FilesRow => db.run(files += obj)
      case obj: DirectoriesRow => db.run(directories += obj)
      case other => return false
    }

    Await.ready(result, Duration.Inf)

    result.value.get match {
      case Success(addResult) => true
      case Failure(t) => false
    }
  }

  def update[T](obj: T): Boolean = {
    val result = obj match {
      case obj: FilesRow => db.run(files.filter(_.id === obj.id).update(obj))
      case obj: DirectoriesRow => db.run(directories.filter(_.id === obj.id).update(obj))
      case other => return false
    }

    Await.ready(result, Duration.Inf)

    result.value.get match {
      case Success(updateResult) => if (updateResult == 1) true else false
      case Failure(t) => false
    }
  }

  def delete[T](obj: T): Boolean = {
    val result = obj match {
      case obj: FilesRow => db.run(files.filter(_.id === obj.id).delete)
      case obj: DirectoriesRow => db.run(directories.filter(_.id === obj.id).delete)
      case other => return false
    }

    Await.ready(result, Duration.Inf)

    result.value.get match {
      case Success(deleteResult) => if (deleteResult == 1) true else false
      case Failure(t) => false
    }
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

    if (dirKeyList.isEmpty && canAdd(filePath.toString)) {
      add(FilesRow(0, None, Some(userIds), Some(collegeIds), filePath.toString, filePath.toString, userId, nowTimestamp, nowTimestamp))
    } else if (canAdd(path)) {
      dirKeyList.foreach { dirPath =>
        if (canAdd(dirPath)) {
          add(DirectoriesRow(0, findParentId(dirPath), Some(userIds), Some(collegeIds), dirPath, userId, nowTimestamp, nowTimestamp))
        } else {
          val result = db.run(directories.filter(_.id === findDirId(dirPath).get).map(_.updatedAt).update(nowTimestamp))

          Await.ready(result, Duration.Inf)

          result.value.get match {
            case Success(updateResult) => if (updateResult != 1) return false
            case Failure(t) => return false
          }
        }
      }

      if (filePath.nonEmpty && canAdd(filePath.toString)) {
        add(FilesRow(0, findParentId(filePath.toString), Some(userIds), Some(collegeIds), filePath.substring(filePath.lastIndexOf("/") + 1), filePath.toString, userId, nowTimestamp, nowTimestamp))
      } else {
        true
      }
    } else {
      false
    }
  }

  private def toDirKeyList(path: String): Seq[String] = {
    val dirList = path.split("/")

    val result = dirList.zipWithIndex.map { case (dirName, num) =>
      val buff = new StringBuilder
      for (i <- 0 to num) { buff.append(dirList.apply(i) + "/") }
      buff.toString()
    }
    result
  }

  private def canAdd(path: String): Boolean = {
    val obj = objType(path) match {
      case "dir"  => db.run(directories.filter(_.name === path).result.headOption)
      case "file" => db.run(files.filter(_.path === path).result.headOption)
    }

    Await.ready(obj, Duration.Inf)

    obj.value.get match {
      case Success(result) => if (result.isEmpty) true else false
      case Failure(t) => false
    }
  }

  private def objType(path: String): String = if (path.last == '/') "dir" else "file"
  private def nowTimestamp: Timestamp = new Timestamp(new LocalDateTime().toDateTime().getMillis)
}