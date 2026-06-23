package cafe.woden.ircclient.irc.znc;

import cafe.woden.ircclient.bouncer.spi.BuiltInBouncerNetworkNaming;

/**
 * Parses ZNC-style usernames.
 *
 * <p>Common forms:
 *
 * <ul>
 *   <li>{@code user}
 *   <li>{@code user/network}
 *   <li>{@code user@clientid/network}
 * </ul>
 */
public record ZncLoginParts(String baseUser, String clientId, String network) {

  public ZncLoginParts {
    if (baseUser == null) baseUser = "";
    if (clientId == null) clientId = "";
    if (network == null) network = "";
  }

  public boolean hasNetwork() {
    return network != null && !network.isBlank();
  }

  public boolean hasClientId() {
    return clientId != null && !clientId.isBlank();
  }

  public static ZncLoginParts parse(String login) {
    BuiltInBouncerNetworkNaming.ZncLoginParts parsed =
        BuiltInBouncerNetworkNaming.parseZncLogin(login);
    return new ZncLoginParts(parsed.baseUser(), parsed.clientId(), parsed.network());
  }

  /** Merge two parses, preferring non-empty fields from {@code this}. */
  public ZncLoginParts mergePreferThis(ZncLoginParts other) {
    if (other == null) return this;
    String u = (this.baseUser != null && !this.baseUser.isBlank()) ? this.baseUser : other.baseUser;
    String c = (this.clientId != null && !this.clientId.isBlank()) ? this.clientId : other.clientId;
    String n = (this.network != null && !this.network.isBlank()) ? this.network : other.network;
    return new ZncLoginParts(u, c, n);
  }
}
