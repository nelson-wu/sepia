package ircserver

trait CanSerialize[-B <: Params] {
  def serialize(params: B): String
}

object CanSerializeImplicits {
  implicit object CanSerializeTarget extends CanSerialize[Target] {
    override def serialize(params: Target): String = params.target
  }
  implicit object CanSerializeSpecial extends CanSerialize[Special] {
    def serialize(params: Special): String = ":" + params.text
  }
  implicit object CanSerializeCompound extends CanSerialize[Compound] {
    def serialize(params: Compound): String = {
      val targets = params.targets.map(implicitly[CanSerialize[Target]].serialize).mkString(" ")
      val special = implicitly[CanSerialize[Special]].serialize(params.special)
      targets + " " + special
    }
  }
}