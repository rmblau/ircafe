package cafe.woden.ircclient.config.runtime.server;

import cafe.woden.ircclient.config.api.AutoJoinEntryCodec;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Pure normalization helpers for persisted private-message auto-join targets. */
final class RuntimeConfigPrivateMessageTargetCodec {

  private RuntimeConfigPrivateMessageTargetCodec() {}

  static String normalizeNick(Object nick) {
    return Objects.toString(nick, "").trim();
  }

  static boolean containsPrivateMessageTarget(List<String> autoJoin, String nick) {
    String normalized = normalizeNick(nick);
    if (normalized.isEmpty()) return false;
    return AutoJoinEntryCodec.privateMessageNicks(autoJoin).stream()
        .anyMatch(existing -> existing.equalsIgnoreCase(normalized));
  }

  static String encodePrivateMessageTarget(String nick) {
    return AutoJoinEntryCodec.encodePrivateMessageNick(normalizeNick(nick));
  }

  static boolean privateMessageEntryMatches(Object entry, String nick) {
    String normalized = normalizeNick(nick);
    if (normalized.isEmpty()) return false;
    String decoded = AutoJoinEntryCodec.decodePrivateMessageNick(Objects.toString(entry, ""));
    return !decoded.isEmpty() && decoded.equalsIgnoreCase(normalized);
  }

  @SuppressWarnings("unchecked")
  static List<String> readPrivateMessageTargets(Map<String, Object> server) {
    Object autoJoinObj = server.get("autoJoin");
    if (!(autoJoinObj instanceof List<?> rawList)) return List.of();
    return List.copyOf(AutoJoinEntryCodec.privateMessageNicks((List<String>) rawList));
  }
}
