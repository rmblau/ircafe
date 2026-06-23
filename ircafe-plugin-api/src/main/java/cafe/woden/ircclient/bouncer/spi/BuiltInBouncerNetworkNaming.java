package cafe.woden.ircclient.bouncer.spi;

import java.util.Locale;
import java.util.Objects;

/**
 * Portable naming helpers shared by built-in bouncer providers and app-side bouncer discovery.
 *
 * <p>These helpers are plugin-facing so the app can normalize built-in Soju/ZNC ids without
 * compiling against the concrete built-in provider jar.
 */
public final class BuiltInBouncerNetworkNaming {

  public static final String SOJU_EPHEMERAL_ID_PREFIX = "soju:";
  public static final String SOJU_DEFAULT_CLIENT_SUFFIX = "ircafe";
  public static final String ZNC_EPHEMERAL_ID_PREFIX = "znc:";

  private BuiltInBouncerNetworkNaming() {}

  /** Parsed ZNC-style login identity. */
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

    /** Merge two parses, preferring non-empty fields from {@code this}. */
    public ZncLoginParts mergePreferThis(ZncLoginParts other) {
      if (other == null) return this;
      String u = (baseUser != null && !baseUser.isBlank()) ? baseUser : other.baseUser;
      String c = (clientId != null && !clientId.isBlank()) ? clientId : other.clientId;
      String n = (network != null && !network.isBlank()) ? network : other.network;
      return new ZncLoginParts(u, c, n);
    }
  }

  /** Parse ZNC-style usernames such as {@code user}, {@code user/network}, or {@code user@client/network}. */
  public static ZncLoginParts parseZncLogin(String login) {
    String s = Objects.toString(login, "").trim();
    if (s.isBlank()) return new ZncLoginParts("", "", "");

    String left;
    String net;
    int slash = s.indexOf('/');
    if (slash >= 0) {
      left = s.substring(0, slash).trim();
      net = s.substring(slash + 1).trim();
    } else {
      left = s.trim();
      net = "";
    }

    String user;
    String client;
    int at = left.indexOf('@');
    if (at >= 0) {
      user = left.substring(0, at).trim();
      client = left.substring(at + 1).trim();
    } else {
      user = left.trim();
      client = "";
    }

    return new ZncLoginParts(user, client, net);
  }

  /** Select the base login for Soju connections (SASL username preferred). */
  public static String pickSojuBaseUser(BouncerServerProfile bouncerServer) {
    if (bouncerServer == null) return null;
    return bouncerServer.preferredLoginUser();
  }

  /**
   * Normalize a Soju base username by stripping any existing network selection or client suffix.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code user/libera} -> {@code user}
   *   <li>{@code user@laptop} -> {@code user}
   *   <li>{@code user/libera@laptop} -> {@code user}
   * </ul>
   */
  public static String normalizeSojuBaseUser(String user) {
    String u = normalize(user);
    if (u == null) return null;

    int slash = u.indexOf('/');
    if (slash >= 0) {
      u = u.substring(0, slash);
    }
    int at = u.indexOf('@');
    if (at >= 0) {
      u = u.substring(0, at);
    }

    u = u.trim();
    return u.isEmpty() ? null : u;
  }

  /** Sanitize a Soju network name to safe characters for usernames. */
  public static String sanitizeSojuNetworkName(String name) {
    if (name == null) return "";
    StringBuilder sb = new StringBuilder(name.length());
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (isSafeNetworkChar(c)) {
        sb.append(c);
      } else {
        sb.append('_');
      }
    }
    String v = sb.toString();
    return v.isBlank() ? "" : v;
  }

  /** Select the base login for ZNC connections (SASL username preferred). */
  public static String pickZncBaseLoginUser(BouncerServerProfile bouncerServer) {
    if (bouncerServer == null) return null;
    return bouncerServer.preferredLoginUser();
  }

  /** Normalize a ZNC network key used for ids/persistence. */
  public static String normalizeZncNetworkKey(String networkName) {
    String s = sanitizeZncNetworkSegment(networkName);
    return s.toLowerCase(Locale.ROOT);
  }

  /** Sanitize a ZNC network name to safe characters for usernames. */
  public static String sanitizeZncNetworkSegment(String networkName) {
    String s = Objects.toString(networkName, "").trim();
    if (s.isEmpty()) return "";

    StringBuilder out = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      out.append(isSafeNetworkChar(c) ? c : '_');
    }

    int start = 0;
    int end = out.length();
    while (start < end && out.charAt(start) == '_') start++;
    while (end > start && out.charAt(end - 1) == '_') end--;

    return out.substring(start, end);
  }

  private static boolean isSafeNetworkChar(char c) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || (c >= '0' && c <= '9')
        || c == '.'
        || c == '_'
        || c == '-';
  }

  private static String normalize(String s) {
    String v = Objects.toString(s, "").trim();
    return v.isEmpty() ? null : v;
  }
}
