package Messages.Implicits

/**
  * Created by Nelson on 2018/06/24.
  */
object ImplicitConversions {
  case class UserName(val value: String) extends AnyVal
  case class ThreadId(val value: String) extends AnyVal
  case class ChannelName(value: String)

  object ChannelName{
    def apply(input: String): ChannelName = new ChannelName(
      input.charAt(0) match {
        case '&' | '#' | '+' | '!' ⇒ sanitize(input)
        case _ ⇒ '#' + input
          .replaceAll("\\s+", "-")
          .toLowerCase()
      }
    )
  }

  object UserName{
    def apply(input: String): UserName = new UserName(
      input.replaceAll("\\s+", "")
    )
  }

  private def sanitize(in: String): String = in.replaceAll(",| ", "")
  implicit def stringToUserName(str: String): UserName = UserName(str)
  implicit def stringToThreadId(str: String): ThreadId = ThreadId(str)
  implicit def stringToChannelName(str: String): ChannelName = ChannelName(str)
}
