package cafe.woden.ircclient.irc.ircv3;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/** Orchestrates the SCRAM request/response sequence over a stateful exchange. */
public final class Ircv3ScramSaslConversation {

  private final String username;
  private final String secret;
  private Ircv3ScramSaslExchange exchange;

  public Ircv3ScramSaslConversation(String username, String secret) {
    this.username = Objects.toString(username, "");
    this.secret = Objects.toString(secret, "");
  }

  public String nextResponse(String digest, String serverMessage) throws Ircv3SaslException {
    if (exchange == null) {
      exchange = new Ircv3ScramSaslExchange(digest, username, secret);
      return encode(exchange.clientFirstMessage());
    }

    if (!exchange.hasSeenServerFirst()) {
      exchange.onServerFirst(Objects.toString(serverMessage, ""));
      return encode(exchange.clientFinalMessage());
    }

    if (!exchange.hasSeenServerFinal()) {
      exchange.onServerFinal(Objects.toString(serverMessage, ""));
      return "";
    }

    return null;
  }

  private static String encode(String message) {
    return Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
  }
}
