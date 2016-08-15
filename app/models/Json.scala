package models

import play.api.libs.json.Json

case class College(code: String, name: String)
object College {
  implicit def jsonReads  = Json.reads[College]
  implicit def jsonWrites = Json.writes[College]
}

case class User(user_id: Int, number: String, name: String, user_image: String, college: College)
object User {
  implicit def jsonReads  = Json.reads[User]
  implicit def jsonWrites = Json.writes[User]
}

/*
 * Cuenta List
 */
case class UserList(users: List[User])
object UserList {
  implicit def jsonReads  = Json.reads[UserList]
  implicit def jsonWrites = Json.writes[UserList]
}

/*
 * Cuenta Search
 */
case class Users(total_count: Int, users: List[User])
object Users {
  implicit def jsonReads  = Json.reads[Users]
  implicit def jsonWrites = Json.writes[Users]
}