package cafe.woden.ircclient.config.runtime.ignore;

import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreMapKeySupport.maskMapKeysMatch;
import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreMapKeySupport.persistedMaskMapKey;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateStringList;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.removeIfEmpty;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Pure codec/policy helpers for persisted ignore-list metadata. */
final class RuntimeConfigIgnoreRulesCodec {

  private static final Set<String> KNOWN_IGNORE_LEVELS =
      Set.of(
          "ALL",
          "MSGS",
          "PUBLIC",
          "NOTICES",
          "CTCPS",
          "ACTIONS",
          "JOINS",
          "PARTS",
          "QUITS",
          "NICKS",
          "TOPICS",
          "WALLOPS",
          "INVITES",
          "MODES",
          "DCC",
          "DCCMSGS",
          "CLIENTCRAP",
          "CLIENTNOTICE",
          "CLIENTERRORS",
          "HILIGHT",
          "NOHILIGHT",
          "CRAP");

  private RuntimeConfigIgnoreRulesCodec() {}

  static List<String> normalizeIgnoreLevels(List<String> levels) {
    LinkedHashSet<String> out = new LinkedHashSet<>();
    if (levels != null) {
      for (String raw : levels) {
        String value = normalizeIgnoreLevel(raw);
        if (!value.isEmpty()) out.add(value);
      }
    }
    if (out.isEmpty()) out.add("ALL");
    return List.copyOf(out);
  }

  static List<String> normalizeIgnoreChannels(List<String> channels) {
    LinkedHashSet<String> out = new LinkedHashSet<>();
    if (channels != null) {
      for (String raw : channels) {
        String value = normalizeIgnoreChannel(raw);
        if (!value.isEmpty()) out.add(value);
      }
    }
    if (out.isEmpty()) return List.of();
    return List.copyOf(out);
  }

  static String normalizeIgnorePatternMode(String raw) {
    String value = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    return switch (value) {
      case "regexp", "regex" -> "regexp";
      case "full" -> "full";
      default -> "glob";
    };
  }

  static void putHardIgnoreMask(Map<String, Object> server, String mask) {
    addMaskToList(server, "masks", mask);
  }

  static void putSoftIgnoreMask(Map<String, Object> server, String mask) {
    addMaskToList(server, "softMasks", mask);
  }

  static void putIgnoreMaskLevels(Map<String, Object> server, String mask, List<String> levels) {
    List<String> normalized = normalizeIgnoreLevels(levels);
    boolean defaultAll = normalized.size() == 1 && "ALL".equalsIgnoreCase(normalized.getFirst());
    putMaskScopedValue(server, "maskLevels", mask, defaultAll ? null : new ArrayList<>(normalized));
  }

  static void putIgnoreMaskChannels(
      Map<String, Object> server, String mask, List<String> channels) {
    List<String> normalized = normalizeIgnoreChannels(channels);
    putMaskScopedValue(
        server, "maskChannels", mask, normalized.isEmpty() ? null : new ArrayList<>(normalized));
  }

  static void putIgnoreMaskExpiresAt(
      Map<String, Object> server, String mask, Long expiresAtEpochMs) {
    long expiresAt = (expiresAtEpochMs == null) ? 0L : expiresAtEpochMs;
    putMaskScopedValue(server, "maskExpiresAt", mask, expiresAt > 0L ? expiresAt : null);
  }

  static void putIgnoreMaskPattern(
      Map<String, Object> server, String mask, String pattern, String modeToken) {
    String normalizedPattern = Objects.toString(pattern, "").trim();
    String normalizedMode = normalizeIgnorePatternMode(modeToken);

    putMaskScopedValue(
        server, "maskPatterns", mask, normalizedPattern.isEmpty() ? null : normalizedPattern);
    putMaskScopedValue(
        server,
        "maskPatternModes",
        mask,
        !normalizedPattern.isEmpty() && !"glob".equals(normalizedMode) ? normalizedMode : null);
  }

  static void putIgnoreMaskReplies(
      Map<String, Object> server, String mask, boolean repliesEnabled) {
    putMaskScopedValue(server, "maskReplies", mask, repliesEnabled ? Boolean.TRUE : null);
  }

  static boolean removeHardIgnoreMask(Map<String, Object> server, String mask) {
    if (!removeMaskFromList(server, "masks", mask)) {
      return false;
    }

    removeMaskMetadata(server, mask);
    return true;
  }

  static boolean removeSoftIgnoreMask(Map<String, Object> server, String mask) {
    return removeMaskFromList(server, "softMasks", mask);
  }

  private static void addMaskToList(Map<String, Object> server, String listKey, String mask) {
    List<String> masks = getOrCreateStringList(server, listKey);
    if (masks.stream().noneMatch(x -> x != null && x.equalsIgnoreCase(mask))) {
      masks.add(mask);
    }
  }

  private static void putMaskScopedValue(
      Map<String, Object> server, String mapKey, String mask, Object value) {
    Map<String, Object> byMask = getOrCreateMap(server, mapKey);
    byMask.entrySet().removeIf(e -> maskMapKeysMatch(Objects.toString(e.getKey(), ""), mask));
    if (value != null) {
      byMask.put(persistedMaskMapKey(mask), value);
    }
    removeIfEmpty(server, mapKey, byMask);
  }

  @SuppressWarnings("unchecked")
  private static boolean removeMaskFromList(
      Map<String, Object> server, String listKey, String mask) {
    Object value = server.get(listKey);
    if (!(value instanceof List<?> list)) {
      return false;
    }

    List<String> masks = (List<String>) list;
    masks.removeIf(x -> x != null && x.equalsIgnoreCase(mask));
    if (masks.isEmpty()) {
      server.remove(listKey);
    }
    return true;
  }

  private static void removeMaskMetadata(Map<String, Object> server, String mask) {
    removeMaskKey(server, "maskLevels", mask);
    removeMaskKey(server, "maskChannels", mask);
    removeMaskKey(server, "maskExpiresAt", mask);
    removeMaskKey(server, "maskPatterns", mask);
    removeMaskKey(server, "maskPatternModes", mask);
    removeMaskKey(server, "maskReplies", mask);
  }

  @SuppressWarnings("unchecked")
  private static void removeMaskKey(Map<String, Object> server, String mapKey, String mask) {
    Object value = server.get(mapKey);
    if (!(value instanceof Map<?, ?> map)) {
      return;
    }

    Map<String, Object> byMask = (Map<String, Object>) map;
    byMask.entrySet().removeIf(e -> maskMapKeysMatch(Objects.toString(e.getKey(), ""), mask));
    if (byMask.isEmpty()) {
      server.remove(mapKey);
    }
  }

  private static String normalizeIgnoreLevel(String raw) {
    String value = Objects.toString(raw, "").trim().toUpperCase(Locale.ROOT);
    if (value.isEmpty()) return "";
    while (value.startsWith("+") || value.startsWith("-")) {
      value = value.substring(1).trim();
    }
    if (value.isEmpty()) return "";
    if ("*".equals(value)) value = "ALL";
    return KNOWN_IGNORE_LEVELS.contains(value) ? value : "";
  }

  private static String normalizeIgnoreChannel(String raw) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty()) return "";
    return (value.startsWith("#") || value.startsWith("&")) ? value : "";
  }
}
