package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.Objects;

/** Plugin-facing request for rendering one outbound IRCv3 message mutation. */
public record Ircv3MessageMutationRequest(String target, String messageId, String payload) {

  public Ircv3MessageMutationRequest {
    target = Objects.toString(target, "");
    messageId = Objects.toString(messageId, "");
    payload = Objects.toString(payload, "");
  }
}
