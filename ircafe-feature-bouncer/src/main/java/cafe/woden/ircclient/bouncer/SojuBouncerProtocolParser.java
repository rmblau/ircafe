package cafe.woden.ircclient.bouncer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Feature-owned parser for Soju bouncer discovery protocol values. */
public final class SojuBouncerProtocolParser {

  /** Parsed form of a {@code BOUNCER NETWORK} response. */
  public record ParsedNetwork(String networkId, String name, Map<String, String> attributes) {}

  /**
   * Parse RPL_ISUPPORT (005) for Soju's {@code BOUNCER_NETID} token.
   *
   * @return the network id, or {@code null} when the token is absent or empty
   */
  public String parseBouncerNetId(String rawLine) {
    if (rawLine == null || rawLine.isBlank()) return null;
    String source = rawLine;

    String upper = source.toUpperCase(Locale.ROOT);
    String needle = "BOUNCER_NETID=";
    int index = upper.indexOf(needle);
    if (index < 0) return null;

    int start = index + needle.length();
    if (start >= source.length()) return null;

    int end = source.indexOf(' ', start);
    String value = (end < 0) ? source.substring(start) : source.substring(start, end);
    value = value.trim();
    if (value.startsWith(":")) value = value.substring(1).trim();
    return value.isEmpty() ? null : value;
  }

  /**
   * Parse a Soju {@code BOUNCER NETWORK <network-id> <attributes>} response.
   *
   * @return the parsed network, or {@code null} when the line is not a network entry
   */
  public ParsedNetwork parseNetworkLine(String rawLine) {
    if (rawLine == null || rawLine.isBlank()) return null;
    String source = rawLine.trim();

    if (source.startsWith(":")) {
      int space = source.indexOf(' ');
      if (space > 1 && space + 1 < source.length()) {
        source = source.substring(space + 1).trim();
      }
    }

    String trailing = null;
    int trailingIndex = source.indexOf(" :");
    if (trailingIndex >= 0) {
      trailing = source.substring(trailingIndex + 2);
      source = source.substring(0, trailingIndex).trim();
    }

    if (source.isEmpty()) return null;

    String[] parts = source.split("\\s+");
    if (parts.length < 3) return null;
    if (!"BOUNCER".equalsIgnoreCase(parts[0])) return null;
    if (!"NETWORK".equalsIgnoreCase(parts[1])) return null;

    String networkId = parts[2] == null ? "" : parts[2].trim();
    if (networkId.isEmpty()) return null;

    String attributesText = null;
    if (parts.length >= 4) {
      StringBuilder out = new StringBuilder();
      for (int i = 3; i < parts.length; i++) {
        if (out.length() > 0) out.append(' ');
        out.append(parts[i]);
      }
      attributesText = out.toString().trim();
    }
    if ((attributesText == null || attributesText.isBlank())
        && trailing != null
        && !trailing.isBlank()) {
      attributesText = trailing.trim();
    }
    if (attributesText == null) attributesText = "";

    Map<String, String> attributes = parseAttributes(attributesText);
    String name = attributes.getOrDefault("name", "").trim();
    if (name.isEmpty()) name = "net-" + networkId;
    name = sanitizeNetworkName(name);

    return new ParsedNetwork(networkId, name, attributes);
  }

  /** Replace characters outside {@code [A-Za-z0-9._-]} with underscores. */
  public String sanitizeNetworkName(String name) {
    if (name == null) return "";
    StringBuilder out = new StringBuilder(name.length());
    for (int i = 0; i < name.length(); i++) {
      char character = name.charAt(i);
      if ((character >= 'a' && character <= 'z')
          || (character >= 'A' && character <= 'Z')
          || (character >= '0' && character <= '9')
          || character == '.'
          || character == '_'
          || character == '-') {
        out.append(character);
      } else {
        out.append('_');
      }
    }
    String value = out.toString();
    return value.isBlank() ? "" : value;
  }

  private Map<String, String> parseAttributes(String attributesText) {
    if (attributesText == null || attributesText.isBlank()) return Map.of();
    String source = attributesText.trim();
    Map<String, String> attributes = new HashMap<>();
    for (String part : source.split(";")) {
      if (part == null) continue;
      String token = part.trim();
      if (token.isEmpty()) continue;
      int equals = token.indexOf('=');
      if (equals < 0) {
        attributes.putIfAbsent(token, "");
        continue;
      }
      String key = token.substring(0, equals).trim();
      String value = token.substring(equals + 1).trim();
      if (!key.isEmpty()) attributes.put(key, value);
    }
    return attributes.isEmpty() ? Map.of() : attributes;
  }
}
