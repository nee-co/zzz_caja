package util

import models.Tables.{Directories, DirectoriesRow, Files, FilesRow}
import javax.inject.Inject

import models.ObjectProperty
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

  def objType(path: String): String = if (path.last == '/') "dir" else "file"

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

  def findByLoginProperty(path: String, user_id: String, college_code: String): Seq[ObjectProperty] = {
    var objects   = new ArrayBuffer[ObjectProperty]
    val parentDir = getDirectory(path)

    parentDir.fold(Seq.empty[ObjectProperty])(parent => {
      val fileResult: Future[Seq[FilesRow]]      = db.run(files.filter(_.parentId === parent.id).result)
      val dirResult: Future[Seq[DirectoriesRow]] = db.run(directories.filter(_.parentId === parent.id).result)

      Await.ready(fileResult, Duration.Inf)
      Await.ready(dirResult,  Duration.Inf)

      fileResult.value.get match {
        case Success(rows) => rows.foreach(row =>
          if (row.userIds.fold(false)(ids => ids.split(",").indexOf(user_id) != -1) ||
              row.collegeIds.fold(false)(ids => ids.split(",").indexOf(college_code) != -1)) {
            objects += ObjectProperty("file", row.name, row.insertedBy, row.insertedAt, row.updatedAt)
          })

        case Failure(t) => None
      }

      dirResult.value.get match {
        case Success(rows) => rows.foreach(row => {
          val name = if (row.name.dropRight(1).count(_ == '/') != 0) {
            val buf = row.name.dropRight(1)
            buf.substring(buf.lastIndexOf("/") + 1)
          } else row.name.dropRight(1)

          if (row.userIds.fold(false)(ids => ids.split(",").indexOf(user_id) != -1) ||
              row.collegeIds.fold(false)(ids => ids.split(",").indexOf(college_code) != -1)) {
            objects += ObjectProperty("dir", name, row.insertedBy, row.insertedAt, row.updatedAt)
          }})

        case Failure(t) => None
      }

      if (objects.nonEmpty) objects else Seq.empty[ObjectProperty]
    })
  }

  def findDirId(name: String): Option[Int] = {
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

  def findParentId(name: String): Option[Int] = {
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

  def hasObj(path: String): Boolean = {
    val obj = objType(path) match {
      case "dir"  => db.run(directories.filter (_.name === path).result.headOption)
      case "file" => db.run(files.filter(_.path === path).result.headOption)
    }

    Await.ready(obj, Duration.Inf)

    obj.value.get match {
      case Success(result) => if (result.nonEmpty) true else false
      case Failure(t) => false
    }
  }

  def getDirectory(path: String): Option[DirectoriesRow] = {
    val dir = db.run(directories.filter(_.name === path).result.headOption)

    Await.ready(dir, Duration.Inf)

    dir.value.get match {
      case Success(result) => if (result.nonEmpty) result else None
      case Failure(t) => None
    }
  }

  def getFile(path: String): Option[FilesRow] = {
    val file = db.run(files.filter(_.path === path).result.headOption)

    Await.ready(file, Duration.Inf)

    file.value.get match {
      case Success(result) => if (result.nonEmpty) result else None
      case Failure(t) => None
    }
  }
}