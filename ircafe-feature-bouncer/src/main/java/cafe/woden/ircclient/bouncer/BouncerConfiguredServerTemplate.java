package cafe.woden.ircclient.bouncer;

import java.util.Objects;

/** Portable configured-server fields needed to materialize a discovered bouncer network. */
public record BouncerConfiguredServerTemplate(
    String host,
    int port,
    boolean tls,
    String serverPassword,
    String nick,
    String login,
    String realName,
    Sasl sasl) {

  public BouncerConfiguredServerTemplate {
    serverPassword = Objects.toString(serverPassword, "");
    sasl = sasl == null ? Sasl.defaults() : sasl;
  }

  public record Sasl(
      boolean enabled,
      String username,
      String password,
      String mechanism,
      Boolean disconnectOnFailure) {

    public Sasl {
      username = Objects.toString(username, "");
      password = Objects.toString(password, "");
      if (mechanism == null || mechanism.isBlank()) {
        mechanism = "PLAIN";
      }
      if (disconnectOnFailure == null) {
        disconnectOnFailure = true;
      }
    }

    public static Sasl defaults() {
      return new Sasl(false, "", "", "PLAIN", true);
    }
  }
}
