package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Parses IRCv3 STS capability tokens and their transport policy directives. */
public final class Ircv3StsPolicyParser {

  private static final String STS = "sts";

  private Ircv3StsPolicyParser() {}

  public static List<String> findStsValues(String capListRaw) {
    String capList = Objects.toString(capListRaw, "").trim();
    if (capList.startsWith(":")) {
      capList = capList.substring(1).trim();
    }
    if (capList.isEmpty()) {
      return List.of();
    }

    List<String> values = new ArrayList<>();
    for (String token : capList.split("\\s+")) {
      String normalized = Objects.toString(token, "").trim();
      if (normalized.startsWith(":")) {
        normalized = normalized.substring(1).trim();
      }
      if (normalized.startsWith("-")) {
        normalized = normalized.substring(1).trim();
      }
      if (normalized.isEmpty()) {
        continue;
      }

      int equalsIndex = normalized.indexOf('=');
      String capability =
          equalsIndex >= 0 ? normalized.substring(0, equalsIndex).trim() : normalized;
      if (!STS.equalsIgnoreCase(capability)) {
        continue;
      }

      String value =
          equalsIndex >= 0 && equalsIndex + 1 < normalized.length()
              ? normalized.substring(equalsIndex + 1).trim()
              : "";
      values.add(value);
    }
    return List.copyOf(values);
  }

  public static Optional<Ircv3StsPolicyDirective> parse(String rawValue) {
    String raw = Objects.toString(rawValue, "").trim();
    if (raw.isEmpty()) {
      return Optional.empty();
    }

    Map<String, String> attributes = new HashMap<>();
    for (String partRaw : raw.split(",")) {
      String part = Objects.toString(partRaw, "").trim();
      if (part.isEmpty()) {
        continue;
      }

      int equalsIndex = part.indexOf('=');
      if (equalsIndex >= 0) {
        String key = part.substring(0, equalsIndex).trim().toLowerCase(Locale.ROOT);
        String value = part.substring(equalsIndex + 1).trim();
        if (!key.isEmpty()) {
          attributes.put(key, value);
        }
      } else {
        attributes.put(part.toLowerCase(Locale.ROOT), "true");
      }
    }

    String durationRaw = attributes.get("duration");
    if (durationRaw == null || durationRaw.isBlank()) {
      return Optional.empty();
    }

    long durationSeconds;
    try {
      durationSeconds = Long.parseLong(durationRaw);
    } catch (NumberFormatException ignored) {
      return Optional.empty();
    }
    if (durationSeconds < 0) {
      return Optional.empty();
    }

    Integer port = null;
    String portRaw = attributes.get("port");
    if (portRaw != null && !portRaw.isBlank()) {
      int parsedPort;
      try {
        parsedPort = Integer.parseInt(portRaw);
      } catch (NumberFormatException ignored) {
        return Optional.empty();
      }
      if (parsedPort <= 0 || parsedPort > 65_535) {
        return Optional.empty();
      }
      port = parsedPort;
    }

    return Optional.of(
        new Ircv3StsPolicyDirective(
            durationSeconds, port, attributes.containsKey("preload"), raw));
  }
}
