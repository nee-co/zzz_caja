package models

import java.sql.Timestamp

case class ObjectProperty(objType: String, name: String, insertedBy: Int, insertedAt: Timestamp, updatedAt: Timestamp) {
  override def canEqual(other: Any): Boolean = other.isInstanceOf[ObjectProperty]
  override def hashCode: Int = (name.hashCode + 31) * 31 + insertedAt.hashCode
  override def equals(other: Any) = other match {
    case that: ObjectProperty =>
      that.canEqual(ObjectProperty.this) && objType == that.objType && name == that.name &&
      insertedBy == that.insertedBy && insertedAt == that.insertedAt && updatedAt == that.updatedAt
    case _ => false
  }
}