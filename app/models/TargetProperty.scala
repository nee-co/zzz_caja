package models

case class TargetProperty(targetType: String, publicIds: Option[String], name: String){
  override def canEqual(other: Any): Boolean = other.isInstanceOf[TargetProperty]
  override def hashCode(): Int = (name.hashCode + 31) * 31 + targetType.hashCode
  override def equals(other: Any): Boolean = other match {
    case that: TargetProperty =>
      that.canEqual(TargetProperty.this) && targetType == that.targetType &&
      publicIds  == that.publicIds && name == that.name
    case _ => false
  }
}
