package models

import java.sql.Timestamp

case class ObjectProperty(obj_type: String, name: String, created_user: Int, created_at: Timestamp, updated_at: Timestamp) {
  override def canEqual(other: Any): Boolean = other.isInstanceOf[ObjectProperty]
  override def hashCode: Int = (name.hashCode + 31) * 31 + created_at.hashCode
  override def equals(other: Any) = other match {
    case that: ObjectProperty =>
      that.canEqual(ObjectProperty.this) && obj_type == that.obj_type && name == that.name &&
      created_user == that.created_user && created_at == that.created_at && updated_at == that.updated_at
    case _ => false
  }
}