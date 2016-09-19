package util

import java.io.File

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.util.IOUtils
import com.typesafe.config.ConfigFactory

import scala.language.reflectiveCalls
import scala.util.{Failure, Success}
import scala.util.Try
import scala.collection.JavaConversions._

class S3Service {
  private val config     = ConfigFactory.parseFile(new File("./conf/application.conf"))
  private val accessKey  = config.getString("aws.s3.accesskey")
  private val secretKey  = config.getString("aws.s3.secretkey")
  private val bucketName = config.getString("aws.s3.bucketname")
  private val s3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey))

  def upload(path: String, name: String, file: File): Option[String] = {
    val fileKey = path + name

    Try(s3.putObject(bucketName, fileKey, file)) match {
      case Success(i) => if (s3.doesObjectExist(bucketName, fileKey)) Some(fileKey) else None
      case Failure(e) => None
    }
  }

  def download(fileKey: String): Option[Array[Byte]] = {
    val obj = Try(s3.getObject(bucketName, fileKey))

    obj match {
      case Success(i) => for (in <- Using(obj.get.getObjectContent)) Some(IOUtils.toByteArray(in))
      case Failure(e) => None
    }
  }

  def delete(fileKey: String): Boolean = {
    Try(s3.deleteObject(bucketName, fileKey)) match {
      case Success(i) => !s3.doesObjectExist(bucketName, fileKey)
      case Failure(e) => false
    }
  }

  def createDir(path: String): Boolean = {
    s3.putObject(bucketName, path, "")
    s3.doesObjectExist(bucketName, path)
  }

  def getUnderKeys(path: String): Seq[String] = {
    Try(s3.listObjectsV2(bucketName, path).getObjectSummaries.toList) match {
      case Success(result) => result.map(summary => summary.getKey)
      case Failure(t) => Seq.empty[String]
    }
  }
}