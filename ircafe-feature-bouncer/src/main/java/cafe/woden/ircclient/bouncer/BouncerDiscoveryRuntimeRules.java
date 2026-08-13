package cafe.woden.ircclient.bouncer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Feature-owned pure rules used while applying discovered bouncer networks at runtime. */
public final class BouncerDiscoveryRuntimeRules {

  public boolean backendMatches(String expectedBackendId, String discoveredBackendId) {
    String expected = normalize(expectedBackendId);
    String actual = normalize(discoveredBackendId);
    if (expected == null || actual == null) return false;
    return expected.equalsIgnoreCase(actual);
  }

  public boolean originMatchesServerId(String serverId, String originServerId) {
    String server = normalize(serverId);
    String origin = normalize(originServerId);
    if (server == null || origin == null) return false;

    int firstColon = server.indexOf(':');
    if (firstColon <= 0 || firstColon + 1 >= server.length()) return false;
    int secondColon = server.indexOf(':', firstColon + 1);
    if (secondColon <= firstColon + 1) return false;

    String parsedOrigin = server.substring(firstColon + 1, secondColon).trim();
    return origin.equals(parsedOrigin);
  }

  public List<String> autoJoinChannels(
      List<String> channels, Predicate<String> autoReattachEnabled) {
    if (channels == null || channels.isEmpty()) return List.of();
    Predicate<String> include = autoReattachEnabled == null ? channel -> true : autoReattachEnabled;

    ArrayList<String> out = new ArrayList<>();
    for (String channel : channels) {
      String ch = normalize(channel);
      if (ch == null) continue;
      if (!include.test(ch)) continue;
      if (containsIgnoreCase(out, ch)) continue;
      out.add(ch);
    }
    return out.isEmpty() ? List.of() : List.copyOf(out);
  }

  private static boolean containsIgnoreCase(List<String> values, String needle) {
    if (values == null || values.isEmpty()) return false;
    String n = normalize(needle);
    if (n == null) return false;
    for (String value : values) {
      if (value != null && value.equalsIgnoreCase(n)) return true;
    }
    return false;
  }

  private static String normalize(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? null : v;
  }
}
