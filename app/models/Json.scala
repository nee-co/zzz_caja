package models

import play.api.libs.json.Json

case class College(code: String, name: String)
object College {
  implicit def jsonReads  = Json.reads[College]
  implicit def jsonWrites = Json.writes[College]
}

case class User(user_id: Int, number: String, name: String, image_path: String, college: College)
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

case class CajaRequest(name: String, target_type: String, public_ids: List[String])
object CajaRequest {
  implicit def jsonReads = Json.reads[CajaRequest]
}

case class PropertyHasUser(name: String, target_type: String, targets: List[User])
object PropertyHasUser {
  implicit def jsonWrites = Json.writes[PropertyHasUser]
}

case class PropertyHasCollege(name: String, target_type: String, targets: List[College])
object PropertyHasCollege {
  implicit def jsonWrites = Json.writes[PropertyHasCollege]
}

case class Object(`type`: String, name: String, created_user: User, created_at: String, updated_at: String)
object Object {
  implicit def jsonWrites = Json.writes[Object]
}

case class ResponseHasUser(current_dir: PropertyHasUser, elements: List[Object])
object ResponseHasUser {
  implicit def jsonWrites = Json.writes[ResponseHasUser]
}

case class ResponseHasCollege(current_dir: PropertyHasCollege, elements: List[Object])
object ResponseHasCollege {
  implicit def jsonWrites = Json.writes[ResponseHasCollege]
}