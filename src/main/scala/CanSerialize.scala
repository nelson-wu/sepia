package ircserver

trait CanSerialize[-B <: Params] {
  def serialize(params: B): String
}

object CanSerialize {
  implicit val CanSerializeTarget: CanSerialize[Target] = new CanSerialize[Target] {
    def serialize(params: Target): String = params.target
  }
  implicit val CanSerializeSpecial: CanSerialize[Special] = new CanSerialize[Special] {
    def serialize(params: Special): String = ":" + params.text
  }
  implicit def CanSerializeSeq[A : CanSerialize]: CanSerialize[Seq[A]] = new CanSerialize[Seq[A]] {
    override def serialize(params: Seq[A]): String = params.map(implicitly[CanSerialize[A]].serialize).mkString(" ")
  }
}