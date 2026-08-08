package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;

/** Transport-independent CHATHISTORY negotiation and dependency policy. */
public final class Ircv3ChatHistoryAvailability {

  private Ircv3ChatHistoryAvailability() {}

  public static boolean isAvailable(boolean chatHistoryNegotiated, boolean batchNegotiated) {
    return chatHistoryNegotiated && batchNegotiated;
  }

  public static void requireAvailable(
      boolean chatHistoryNegotiated, boolean batchNegotiated, String serverId) {
    String sid = Objects.toString(serverId, "").trim();
    if (!chatHistoryNegotiated) {
      throw new IllegalStateException(
          "CHATHISTORY not negotiated (chathistory or draft/chathistory): " + sid);
    }
    if (!batchNegotiated) {
      throw new IllegalStateException("CHATHISTORY requires IRCv3 batch to be negotiated: " + sid);
    }
  }
}
