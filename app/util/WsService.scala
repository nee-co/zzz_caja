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
    val user: Future[User] = ws.url(s"http://127.0.0.1:4000/users/$id").get.map(response => Json.parse(response.json.toString).validate[User].get)

    Await.ready(user, Duration.Inf)

    user.value.get match {
      case Success(result) => Some(result)
      case Failure(t) => None
    }
  }

  def users(ids: Seq[String]): Seq[User] = {
    val user_ids = ids.mkString("+")
    val users: Future[UserList] = ws.url(s"http://127.0.0.1:4000/users/list?user_ids=$user_ids").get.map(response => Json.parse(response.json.toString).validate[UserList].get)

    Await.ready(users, Duration.Inf)

    users.value.get match {
      case Success(result) => result.users
      case Failure(t) => Seq.empty
    }
  }
}
