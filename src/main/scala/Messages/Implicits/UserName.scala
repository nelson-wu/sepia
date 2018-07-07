package Messages.Implicits

/**
  * Created by Nelson on 2018/06/24.
  */
object ImplicitConversions {
  case class UserName(val value: String) extends AnyVal
  case class ThreadId(val value: String) extends AnyVal
  case class ChannelName(input: String) {
    val value = input.charAt(0) match {
      case '&' | '#' | '+' | '!' ⇒ sanitize(input)
      case _ ⇒ '#' + sanitize(input)
    }
  }

  private def sanitize(in: String): String = in.replaceAll(",| ", "")
  implicit def stringToUserName(str: String): UserName = UserName(str)
  implicit def stringToThreadId(str: String): ThreadId = ThreadId(str)
  implicit def stringToChannelName(str: String): ChannelName = ChannelName(str)
}
