package util

import javax.inject.Inject

import models.{User, UserList}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class WsService @Inject()(ws: WSClient) {
  def user(id: Int): Option[User] = {
    val user: Future[User] = ws.url(s"${sys.env("CUENTA_URL")}/internal/users/$id").get.map(response => Json.parse(response.json.toString).validate[User].get)

    Await.ready(user, Duration.Inf)

    user.value.get match {
      case Success(result) => Some(result)
      case Failure(t) => None
    }
  }

  def users(ids: Seq[String]): Seq[User] = {
    val userIds = ids.mkString("+")
    val users: Future[UserList] = ws.url(s"${sys.env("CUENTA_URL")}/internal/users/list?user_ids=$userIds").get.map(response => Json.parse(response.json.toString).validate[UserList].get)

    Await.ready(users, Duration.Inf)

    users.value.get match {
      case Success(result) => result.users
      case Failure(t) => Seq.empty
    }
  }
}
