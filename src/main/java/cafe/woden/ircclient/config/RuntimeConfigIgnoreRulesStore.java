package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted ignore-list rules and toggles under {@code ircafe.ignore}. */
class RuntimeConfigIgnoreRulesStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigIgnoreRulesStore.class);
  private static final java.util.Set<String> KNOWN_IGNORE_LEVELS =
      java.util.Set.of(
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

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigIgnoreRulesStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberIgnoreMask(String serverId, String mask) {
    try {
      if (file.toString().isBlank()) return;

      String sid = Objects.toString(serverId, "").trim();
      String m = Objects.toString(mask, "").trim();
      if (sid.isEmpty() || m.isEmpty()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");
      Map<String, Object> servers = getOrCreateMap(ignore, "servers");

      @SuppressWarnings("unchecked")
      Map<String, Object> server =
          (servers.get(sid) instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();
      servers.put(sid, server);

      List<String> masks = getOrCreateStringList(server, "masks");
      if (masks.stream().noneMatch(x -> x != null && x.equalsIgnoreCase(m))) {
        masks.add(m);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ignore mask to '{}'", file, e);
    }
  }

  synchronized void rememberIgnoreMaskLevels(String serverId, String mask, List<String> levels) {
    try {
      if (file.toString().isBlank()) return;

      String sid = Objects.toString(serverId, "").trim();
      String m = Objects.toString(mask, "").trim();
      if (sid.isEmpty() || m.isEmpty()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");
      Map<String, Object> servers = getOrCreateMap(ignore, "servers");

      @SuppressWarnings("unchecked")
      Map<String, Object> server =
          (servers.get(sid) instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();
      servers.put(sid, server);

      List<String> normalized = normalizeIgnoreLevels(levels);
      boolean isDefaultAll =
          normalized.size() == 1 && "ALL".equalsIgnoreCase(normalized.getFirst());

      @SuppressWarnings("unchecked")
      Map<String, Object> byMask =
          (server.get("maskLevels") instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();

      if (isDefaultAll) {
        byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
      } else {
        byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
        byMask.put(m, new java.util.ArrayList<>(normalized));
      }

      if (byMask.isEmpty()) {
        server.remove("maskLevels");
      } else {
        server.put("maskLevels", byMask);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ignore mask levels to '{}'", file, e);
    }
  }

  synchronized void rememberIgnoreMaskChannels(
      String serverId, String mask, List<String> channels) {
    try {
      if (file.toString().isBlank()) return;

      String sid = Objects.toString(serverId, "").trim();
      String m = Objects.toString(mask, "").trim();
      if (sid.isEmpty() || m.isEmpty()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");
      Map<String, Object> servers = getOrCreateMap(ignore, "servers");

      @SuppressWarnings("unchecked")
      Map<String, Object> server =
          (servers.get(sid) instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();
      servers.put(sid, server);

      List<String> normalized = normalizeIgnoreChannels(channels);

      @SuppressWarnings("unchecked")
      Map<String, Object> byMask =
          (server.get("maskChannels") instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();

      byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
      if (normalized.isEmpty()) {
        // Empty means no channel restriction; omit per-mask override from persisted YAML.
      } else {
        byMask.put(m, new java.util.ArrayList<>(normalized));
      }

      if (byMask.isEmpty()) {
        server.remove("maskChannels");
      } else {
        server.put("maskChannels", byMask);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ignore mask channels to '{}'", file, e);
    }
  }

  synchronized void rememberIgnoreMaskExpiresAt(
      String serverId, String mask, Long expiresAtEpochMs) {
    try {
      if (file.toString().isBlank()) return;

      String sid = Objects.toString(serverId, "").trim();
      String m = Objects.toString(mask, "").trim();
      if (sid.isEmpty() || m.isEmpty()) return;

      long expiresAt = (expiresAtEpochMs == null) ? 0L : expiresAtEpochMs;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");
      Map<String, Object> servers = getOrCreateMap(ignore, "servers");

      @SuppressWarnings("unchecked")
      Map<String, Object> server =
          (servers.get(sid) instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();
      servers.put(sid, server);

      @SuppressWarnings("unchecked")
      Map<String, Object> byMask =
          (server.get("maskExpiresAt") instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();

      byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
      if (expiresAt > 0L) {
        byMask.put(m, expiresAt);
      }

      if (byMask.isEmpty()) {
        server.remove("maskExpiresAt");
      } else {
        server.put("maskExpiresAt", byMask);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ignore mask expiry to '{}'", file, e);
    }
  }

  synchronized void rememberIgnoreMaskPattern(
      String serverId, String mask, String pattern, String modeToken) {
    try {
      if (file.toString().isBlank()) return;

      String sid = Objects.toString(serverId, "").trim();
      String m = Objects.toString(mask, "").trim();
      if (sid.isEmpty() || m.isEmpty()) return;

      String normalizedPattern = Objects.toString(pattern, "").trim();
      String normalizedMode = normalizeIgnorePatternMode(modeToken);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");
      Map<String, Object> servers = getOrCreateMap(ignore, "servers");

      @SuppressWarnings("unchecked")
      Map<String, Object> server =
          (servers.get(sid) instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();
      servers.put(sid, server);

      @SuppressWarnings("unchecked")
      Map<String, Object> patternsByMask =
          (server.get("maskPatterns") instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();
      @SuppressWarnings("unchecked")
      Map<String, Object> modesByMask =
          (server.get("maskPatternModes") instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();

      patternsByMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
      modesByMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));

      if (!normalizedPattern.isEmpty()) {
        patternsByMask.put(m, normalizedPattern);
        if (!"glob".equals(normalizedMode)) {
          modesByMask.put(m, normalizedMode);
        }
      }

      if (patternsByMask.isEmpty()) {
        server.remove("maskPatterns");
      } else {
        server.put("maskPatterns", patternsByMask);
      }
      if (modesByMask.isEmpty()) {
        server.remove("maskPatternModes");
      } else {
        server.put("maskPatternModes", modesByMask);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ignore mask pattern to '{}'", file, e);
    }
  }

  synchronized void rememberIgnoreMaskReplies(
      String serverId, String mask, boolean repliesEnabled) {
    try {
      if (file.toString().isBlank()) return;

      String sid = Objects.toString(serverId, "").trim();
      String m = Objects.toString(mask, "").trim();
      if (sid.isEmpty() || m.isEmpty()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");
      Map<String, Object> servers = getOrCreateMap(ignore, "servers");

      @SuppressWarnings("unchecked")
      Map<String, Object> server =
          (servers.get(sid) instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();
      servers.put(sid, server);

      @SuppressWarnings("unchecked")
      Map<String, Object> byMask =
          (server.get("maskReplies") instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();

      byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
      if (repliesEnabled) {
        byMask.put(m, Boolean.TRUE);
      }

      if (byMask.isEmpty()) {
        server.remove("maskReplies");
      } else {
        server.put("maskReplies", byMask);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ignore mask replies flag to '{}'", file, e);
    }
  }

  private static List<String> normalizeIgnoreLevels(List<String> levels) {
    java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
    if (levels != null) {
      for (String raw : levels) {
        String v = normalizeIgnoreLevel(raw);
        if (!v.isEmpty()) out.add(v);
      }
    }
    if (out.isEmpty()) out.add("ALL");
    return List.copyOf(out);
  }

  private static String normalizeIgnoreLevel(String raw) {
    String v = Objects.toString(raw, "").trim().toUpperCase(Locale.ROOT);
    if (v.isEmpty()) return "";
    while (v.startsWith("+") || v.startsWith("-")) {
      v = v.substring(1).trim();
    }
    if (v.isEmpty()) return "";
    if ("*".equals(v)) v = "ALL";
    return KNOWN_IGNORE_LEVELS.contains(v) ? v : "";
  }

  private static List<String> normalizeIgnoreChannels(List<String> channels) {
    java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
    if (channels != null) {
      for (String raw : channels) {
        String v = normalizeIgnoreChannel(raw);
        if (!v.isEmpty()) out.add(v);
      }
    }
    if (out.isEmpty()) return List.of();
    return List.copyOf(out);
  }

  private static String normalizeIgnoreChannel(String raw) {
    String v = Objects.toString(raw, "").trim();
    if (v.isEmpty()) return "";
    return (v.startsWith("#") || v.startsWith("&")) ? v : "";
  }

  private static String normalizeIgnorePatternMode(String raw) {
    String v = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    return switch (v) {
      case "regexp", "regex" -> "regexp";
      case "full" -> "full";
      default -> "glob";
    };
  }

  synchronized void forgetIgnoreMask(String serverId, String mask) {
    try {
      if (file.toString().isBlank()) return;

      String sid = Objects.toString(serverId, "").trim();
      String m = Objects.toString(mask, "").trim();
      if (sid.isEmpty() || m.isEmpty()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");
      Map<String, Object> servers = getOrCreateMap(ignore, "servers");

      Object so = servers.get(sid);
      if (!(so instanceof Map<?, ?>)) return;
      @SuppressWarnings("unchecked")
      Map<String, Object> server = (Map<String, Object>) so;

      Object o = server.get("masks");
      if (!(o instanceof List<?> list)) return;
      @SuppressWarnings("unchecked")
      List<String> masks = (List<String>) list;

      masks.removeIf(x -> x != null && x.equalsIgnoreCase(m));

      // Clean up empty structures to keep the YAML tidy.
      if (masks.isEmpty()) {
        server.remove("masks");
      }

      Object levelsObj = server.get("maskLevels");
      if (levelsObj instanceof Map<?, ?> levelsMap) {
        @SuppressWarnings("unchecked")
        Map<String, Object> byMask = (Map<String, Object>) levelsMap;
        byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
        if (byMask.isEmpty()) {
          server.remove("maskLevels");
        }
      }

      Object channelsObj = server.get("maskChannels");
      if (channelsObj instanceof Map<?, ?> channelsMap) {
        @SuppressWarnings("unchecked")
        Map<String, Object> byMask = (Map<String, Object>) channelsMap;
        byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
        if (byMask.isEmpty()) {
          server.remove("maskChannels");
        }
      }

      Object expiresObj = server.get("maskExpiresAt");
      if (expiresObj instanceof Map<?, ?> expiresMap) {
        @SuppressWarnings("unchecked")
        Map<String, Object> byMask = (Map<String, Object>) expiresMap;
        byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
        if (byMask.isEmpty()) {
          server.remove("maskExpiresAt");
        }
      }

      Object patternsObj = server.get("maskPatterns");
      if (patternsObj instanceof Map<?, ?> patternsMap) {
        @SuppressWarnings("unchecked")
        Map<String, Object> byMask = (Map<String, Object>) patternsMap;
        byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
        if (byMask.isEmpty()) {
          server.remove("maskPatterns");
        }
      }

      Object patternModesObj = server.get("maskPatternModes");
      if (patternModesObj instanceof Map<?, ?> modesMap) {
        @SuppressWarnings("unchecked")
        Map<String, Object> byMask = (Map<String, Object>) modesMap;
        byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
        if (byMask.isEmpty()) {
          server.remove("maskPatternModes");
        }
      }

      Object repliesObj = server.get("maskReplies");
      if (repliesObj instanceof Map<?, ?> repliesMap) {
        @SuppressWarnings("unchecked")
        Map<String, Object> byMask = (Map<String, Object>) repliesMap;
        byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
        if (byMask.isEmpty()) {
          server.remove("maskReplies");
        }
      }

      if (server.isEmpty()) {
        servers.remove(sid);
      }
      if (servers.isEmpty()) {
        ignore.remove("servers");
      }
      if (ignore.isEmpty()) {
        ircafe.remove("ignore");
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not remove ignore mask from '{}'", file, e);
    }
  }

  synchronized void rememberSoftIgnoreMask(String serverId, String mask) {
    try {
      if (file.toString().isBlank()) return;

      String sid = Objects.toString(serverId, "").trim();
      String m = Objects.toString(mask, "").trim();
      if (sid.isEmpty() || m.isEmpty()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");
      Map<String, Object> servers = getOrCreateMap(ignore, "servers");

      @SuppressWarnings("unchecked")
      Map<String, Object> server =
          (servers.get(sid) instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();
      servers.put(sid, server);

      List<String> masks = getOrCreateStringList(server, "softMasks");
      if (masks.stream().noneMatch(x -> x != null && x.equalsIgnoreCase(m))) {
        masks.add(m);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist soft-ignore mask to '{}'", file, e);
    }
  }

  synchronized void forgetSoftIgnoreMask(String serverId, String mask) {
    try {
      if (file.toString().isBlank()) return;

      String sid = Objects.toString(serverId, "").trim();
      String m = Objects.toString(mask, "").trim();
      if (sid.isEmpty() || m.isEmpty()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");
      Map<String, Object> servers = getOrCreateMap(ignore, "servers");

      Object so = servers.get(sid);
      if (!(so instanceof Map<?, ?>)) return;
      @SuppressWarnings("unchecked")
      Map<String, Object> server = (Map<String, Object>) so;

      Object o = server.get("softMasks");
      if (!(o instanceof List<?> list)) return;
      @SuppressWarnings("unchecked")
      List<String> masks = (List<String>) list;

      masks.removeIf(x -> x != null && x.equalsIgnoreCase(m));

      // Clean up empty structures to keep the YAML tidy.
      if (masks.isEmpty()) {
        server.remove("softMasks");
      }
      if (server.isEmpty()) {
        servers.remove(sid);
      }
      if (servers.isEmpty()) {
        ignore.remove("servers");
      }
      if (ignore.isEmpty()) {
        ircafe.remove("ignore");
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not remove soft-ignore mask from '{}'", file, e);
    }
  }

  synchronized void rememberHardIgnoreIncludesCtcp(boolean enabled) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");

      ignore.put("hardIgnoreIncludesCtcp", enabled);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist hard-ignore CTCP setting to '{}'", file, e);
    }
  }

  synchronized void rememberSoftIgnoreIncludesCtcp(boolean enabled) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ignore = getOrCreateMap(ircafe, "ignore");

      ignore.put("softIgnoreIncludesCtcp", enabled);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist soft-ignore CTCP setting to '{}'", file, e);
    }
  }

  @SuppressWarnings("unchecked")
  private static List<String> getOrCreateStringList(Map<String, Object> m, String key) {
    Object o = m.get(key);
    if (o instanceof List<?>) {
      // Cast defensively; we only store strings.
      return (List<String>) o;
    }
    List<String> created = new ArrayList<>();
    m.put(key, created);
    return created;
  }
}
