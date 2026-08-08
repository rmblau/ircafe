package cafe.woden.ircclient.config.runtime.server;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.containsIgnoreCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Pure normalization helpers for persisted IRCv3 MONITOR rosters. */
final class RuntimeConfigMonitorRosterCodec {

  private RuntimeConfigMonitorRosterCodec() {}

  static List<String> sanitizeMonitorNickList(Object rawList) {
    if (!(rawList instanceof List<?> list) || list.isEmpty()) return new ArrayList<>();
    ArrayList<String> out = new ArrayList<>();
    for (Object raw : list) {
      String nick = normalizeMonitorNick(raw);
      if (nick.isEmpty()) continue;
      if (!containsIgnoreCase(out, nick)) out.add(nick);
    }
    if (out.isEmpty()) return new ArrayList<>();
    return out;
  }

  static String normalizeMonitorNick(Object rawNick) {
    String nick = Objects.toString(rawNick, "").trim();
    if (nick.isEmpty()) return "";
    if (nick.startsWith(":")) nick = nick.substring(1).trim();
    int comma = nick.indexOf(',');
    if (comma >= 0) nick = nick.substring(0, comma).trim();
    int bang = nick.indexOf('!');
    if (bang > 0) nick = nick.substring(0, bang).trim();
    if (nick.isEmpty()) return "";
    if (nick.indexOf(' ') >= 0 || nick.indexOf('\t') >= 0) return "";
    if (nick.startsWith("#") || nick.startsWith("&")) return "";
    return nick;
  }
}
