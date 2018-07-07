package Messages.Implicits

import Messages._

trait CanSerialize[-B <: Params] {
  def serialize(params: B): String
}

object CanSerialize {
  implicit object CanSerializeTarget extends CanSerialize[Target] {
    override def serialize(params: Target): String = params.underlying
  }
  implicit object CanSerializeSpecial extends CanSerialize[Special] {
    def serialize(params: Special): String = ":" + params.text
  }
  implicit object CanSerializeNone extends CanSerialize[NoParams.type] {
    def serialize(params: NoParams.type): String = ""
  }
  implicit object CanSerializeCompound extends CanSerialize[Compound] {
    def serialize(params: Compound): String = {
      val targets = params.targets.map(implicitly[CanSerialize[Target]].serialize).mkString(" ")
      val special = implicitly[CanSerialize[Special]].serialize(params.special)
      targets + " " + special
    }
  }
}