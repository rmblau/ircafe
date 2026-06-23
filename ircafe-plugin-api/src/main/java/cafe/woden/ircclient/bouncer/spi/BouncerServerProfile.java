package cafe.woden.ircclient.bouncer.spi;

import java.util.Objects;

/** Portable view of the configured bouncer server needed by plugin mapping strategies. */
public record BouncerServerProfile(String id, String login, String saslUsername) {

  public BouncerServerProfile {
    id = requireNonBlank(id, "id");
    login = normalize(login);
    saslUsername = normalize(saslUsername);
  }

  /** Returns the preferred base login identity, with SASL username taking precedence. */
  public String preferredLoginUser() {
    return saslUsername != null ? saslUsername : login;
  }

  private static String requireNonBlank(String value, String field) {
    String v = normalize(value);
    if (v == null) {
      throw new IllegalArgumentException(field + " is required");
    }
    return v;
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
