package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateStringList;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
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
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateIgnoreServer(
        sid,
        "ignore mask",
        server -> {
          List<String> masks = getOrCreateStringList(server, "masks");
          if (masks.stream().noneMatch(x -> x != null && x.equalsIgnoreCase(m))) {
            masks.add(m);
          }
        });
  }

  synchronized void rememberIgnoreMaskLevels(String serverId, String mask, List<String> levels) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    List<String> normalized = normalizeIgnoreLevels(levels);
    boolean isDefaultAll =
        normalized.size() == 1 && "ALL".equalsIgnoreCase(normalized.getFirst());

    mutateIgnoreServer(
        sid,
        "ignore mask levels",
        server -> {
          Map<String, Object> byMask = getOrCreateMap(server, "maskLevels");
          byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));

          if (!isDefaultAll) {
            byMask.put(m, new ArrayList<>(normalized));
          }
          removeIfEmpty(server, "maskLevels", byMask);
        });
  }

  synchronized void rememberIgnoreMaskChannels(
      String serverId, String mask, List<String> channels) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    List<String> normalized = normalizeIgnoreChannels(channels);

    mutateIgnoreServer(
        sid,
        "ignore mask channels",
        server -> {
          Map<String, Object> byMask = getOrCreateMap(server, "maskChannels");
          byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));

          if (!normalized.isEmpty()) {
            byMask.put(m, new ArrayList<>(normalized));
          }
          removeIfEmpty(server, "maskChannels", byMask);
        });
  }

  synchronized void rememberIgnoreMaskExpiresAt(
      String serverId, String mask, Long expiresAtEpochMs) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    long expiresAt = (expiresAtEpochMs == null) ? 0L : expiresAtEpochMs;

    mutateIgnoreServer(
        sid,
        "ignore mask expiry",
        server -> {
          Map<String, Object> byMask = getOrCreateMap(server, "maskExpiresAt");
          byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));

          if (expiresAt > 0L) {
            byMask.put(m, expiresAt);
          }
          removeIfEmpty(server, "maskExpiresAt", byMask);
        });
  }

  synchronized void rememberIgnoreMaskPattern(
      String serverId, String mask, String pattern, String modeToken) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    String normalizedPattern = Objects.toString(pattern, "").trim();
    String normalizedMode = normalizeIgnorePatternMode(modeToken);

    mutateIgnoreServer(
        sid,
        "ignore mask pattern",
        server -> {
          Map<String, Object> patternsByMask = getOrCreateMap(server, "maskPatterns");
          Map<String, Object> modesByMask = getOrCreateMap(server, "maskPatternModes");

          patternsByMask.entrySet()
              .removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));
          modesByMask.entrySet()
              .removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));

          if (!normalizedPattern.isEmpty()) {
            patternsByMask.put(m, normalizedPattern);
            if (!"glob".equals(normalizedMode)) {
              modesByMask.put(m, normalizedMode);
            }
          }

          removeIfEmpty(server, "maskPatterns", patternsByMask);
          removeIfEmpty(server, "maskPatternModes", modesByMask);
        });
  }

  synchronized void rememberIgnoreMaskReplies(
      String serverId, String mask, boolean repliesEnabled) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateIgnoreServer(
        sid,
        "ignore mask replies flag",
        server -> {
          Map<String, Object> byMask = getOrCreateMap(server, "maskReplies");
          byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(m));

          if (repliesEnabled) {
            byMask.put(m, Boolean.TRUE);
          }
          removeIfEmpty(server, "maskReplies", byMask);
        });
  }

  private void mutateIgnoreServer(
      String serverId, String description, Consumer<Map<String, Object>> mutation) {
    RuntimeConfigYamlSupport.mutateMap(
        file,
        documentStore,
        log,
        description,
        ignore -> {
          Map<String, Object> servers = getOrCreateMap(ignore, "servers");
          Map<String, Object> server = getOrCreateMap(servers, serverId);
          mutation.accept(server);
        },
        "ircafe",
        "ignore");
  }

  private void mutateIgnore(
      String description, Consumer<Map<String, Object>> mutation) {
    RuntimeConfigYamlSupport.mutateMap(
        file, documentStore, log, description, mutation, "ircafe", "ignore");
  }

  private static void removeIfEmpty(
      Map<String, Object> parent, String key, Map<String, Object> value) {
    if (value.isEmpty()) {
      parent.remove(key);
    }
  }

  private void mutateExistingIgnoreServer(
      String serverId, String description, ExistingIgnoreServerMutation mutation) {
    RuntimeConfigYamlSupport.mutateDocument(
        file,
        documentStore,
        log,
        description,
        doc -> {
          Map<String, Object> ircafe = existingMap(doc, "ircafe");
          if (ircafe == null) return false;
          Map<String, Object> ignore = existingMap(ircafe, "ignore");
          if (ignore == null) return false;
          Map<String, Object> servers = existingMap(ignore, "servers");
          if (servers == null) return false;
          Map<String, Object> server = existingMap(servers, serverId);
          if (server == null) return false;

          return mutation.mutate(ircafe, ignore, servers, server);
        });
  }

  @FunctionalInterface
  private interface ExistingIgnoreServerMutation {
    boolean mutate(
        Map<String, Object> ircafe,
        Map<String, Object> ignore,
        Map<String, Object> servers,
        Map<String, Object> server);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> existingMap(Map<String, Object> parent, String key) {
    Object value = parent.get(key);
    return (value instanceof Map<?, ?> map) ? (Map<String, Object>) map : null;
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

  @SuppressWarnings("unchecked")
  private static void removeMaskKey(Map<String, Object> server, String mapKey, String mask) {
    Object value = server.get(mapKey);
    if (!(value instanceof Map<?, ?> map)) {
      return;
    }

    Map<String, Object> byMask = (Map<String, Object>) map;
    byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(mask));
    if (byMask.isEmpty()) {
      server.remove(mapKey);
    }
  }

  private static void cleanupIgnoreServer(
      Map<String, Object> ircafe,
      Map<String, Object> ignore,
      Map<String, Object> servers,
      String serverId,
      Map<String, Object> server) {
    if (server.isEmpty()) {
      servers.remove(serverId);
    }
    if (servers.isEmpty()) {
      ignore.remove("servers");
    }
    if (ignore.isEmpty()) {
      ircafe.remove("ignore");
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
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateExistingIgnoreServer(
        sid,
        "ignore mask removal",
        (ircafe, ignore, servers, server) -> {
          if (!removeMaskFromList(server, "masks", m)) {
            return false;
          }

          removeMaskKey(server, "maskLevels", m);
          removeMaskKey(server, "maskChannels", m);
          removeMaskKey(server, "maskExpiresAt", m);
          removeMaskKey(server, "maskPatterns", m);
          removeMaskKey(server, "maskPatternModes", m);
          removeMaskKey(server, "maskReplies", m);
          cleanupIgnoreServer(ircafe, ignore, servers, sid, server);
          return true;
        });
  }

  synchronized void rememberSoftIgnoreMask(String serverId, String mask) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateIgnoreServer(
        sid,
        "soft-ignore mask",
        server -> {
          List<String> masks = getOrCreateStringList(server, "softMasks");
          if (masks.stream().noneMatch(x -> x != null && x.equalsIgnoreCase(m))) {
            masks.add(m);
          }
        });
  }

  synchronized void forgetSoftIgnoreMask(String serverId, String mask) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || m.isEmpty()) return;

    mutateExistingIgnoreServer(
        sid,
        "soft-ignore mask removal",
        (ircafe, ignore, servers, server) -> {
          if (!removeMaskFromList(server, "softMasks", m)) {
            return false;
          }

          cleanupIgnoreServer(ircafe, ignore, servers, sid, server);
          return true;
        });
  }

  synchronized void rememberHardIgnoreIncludesCtcp(boolean enabled) {
    mutateIgnore(
        "hard-ignore CTCP setting", ignore -> ignore.put("hardIgnoreIncludesCtcp", enabled));
  }

  synchronized void rememberSoftIgnoreIncludesCtcp(boolean enabled) {
    mutateIgnore(
        "soft-ignore CTCP setting", ignore -> ignore.put("softIgnoreIncludesCtcp", enabled));
  }
}
