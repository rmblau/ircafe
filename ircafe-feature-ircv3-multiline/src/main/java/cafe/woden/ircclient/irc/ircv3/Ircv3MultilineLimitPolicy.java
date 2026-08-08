package cafe.woden.ircclient.irc.ircv3;

/** Transport-independent user-facing reasoning for negotiated multiline limits. */
public final class Ircv3MultilineLimitPolicy {

  private Ircv3MultilineLimitPolicy() {}

  public static String limitReason(
      int lineCount, long payloadUtf8Bytes, long maxLines, long maxBytes) {
    if (maxLines > 0L && lineCount > maxLines) {
      return "Message has "
          + lineCount
          + " lines; negotiated multiline max-lines is "
          + maxLines
          + ".";
    }
    if (maxBytes > 0L && payloadUtf8Bytes > maxBytes) {
      return "Message is "
          + payloadUtf8Bytes
          + " UTF-8 bytes; negotiated multiline max-bytes is "
          + maxBytes
          + ".";
    }
    return "";
  }
}
