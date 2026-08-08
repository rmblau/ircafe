package cafe.woden.ircclient.bouncer;

import java.util.List;
import java.util.Objects;

/** Portable server config produced by applying a bouncer mapping spec to a configured server. */
public record BouncerEphemeralServerConfig(
    String serverId,
    String host,
    int port,
    boolean tls,
    String serverPassword,
    String nick,
    String login,
    String realName,
    Sasl sasl,
    List<String> autoJoinChannels) {

  public BouncerEphemeralServerConfig {
    serverId = requireNonBlank(serverId, "serverId");
    serverPassword = Objects.toString(serverPassword, "");
    sasl = Objects.requireNonNull(sasl, "sasl");
    autoJoinChannels = autoJoinChannels == null ? List.of() : List.copyOf(autoJoinChannels);
  }

  public record Sasl(
      boolean enabled,
      String username,
      String password,
      String mechanism,
      Boolean disconnectOnFailure) {

    public Sasl {
      username = requireNonBlank(username, "username");
      password = Objects.toString(password, "");
      if (mechanism == null || mechanism.isBlank()) {
        mechanism = "PLAIN";
      }
      if (disconnectOnFailure == null) {
        disconnectOnFailure = true;
      }
    }
  }

  private static String requireNonBlank(String value, String field) {
    String v = Objects.toString(value, "").trim();
    if (v.isEmpty()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return v;
  }
}
