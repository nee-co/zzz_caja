package models

case class TargetProperty(target_type: String, public_ids: Option[String], name: String){
  override def canEqual(other: Any): Boolean = other.isInstanceOf[TargetProperty]
  override def hashCode(): Int = (name.hashCode + 31) * 31 + target_type.hashCode
  override def equals(other: Any): Boolean = other match {
    case that: TargetProperty => that.canEqual(TargetProperty.this) &&
                                 target_type == that.target_type &&
                                 public_ids  == that.public_ids  &&
                                 name == that.name
    case _ => false
  }
}
