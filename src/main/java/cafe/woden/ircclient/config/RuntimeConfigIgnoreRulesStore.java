package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateStringList;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.removeIfEmpty;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
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

  private final RuntimeConfigYamlSection ignoreSection;

  RuntimeConfigIgnoreRulesStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.ignoreSection = new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "ignore");
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
        server ->
            rememberMaskScopedValue(
                server, "maskLevels", m, isDefaultAll ? null : new ArrayList<>(normalized)));
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
        server ->
            rememberMaskScopedValue(
                server,
                "maskChannels",
                m,
                normalized.isEmpty() ? null : new ArrayList<>(normalized)));
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
        server ->
            rememberMaskScopedValue(server, "maskExpiresAt", m, expiresAt > 0L ? expiresAt : null));
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
          rememberMaskScopedValue(
              server, "maskPatterns", m, normalizedPattern.isEmpty() ? null : normalizedPattern);
          rememberMaskScopedValue(
              server,
              "maskPatternModes",
              m,
              !normalizedPattern.isEmpty() && !"glob".equals(normalizedMode)
                  ? normalizedMode
                  : null);
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
        server ->
            rememberMaskScopedValue(server, "maskReplies", m, repliesEnabled ? Boolean.TRUE : null));
  }

  private static void rememberMaskScopedValue(
      Map<String, Object> server, String mapKey, String mask, Object value) {
    Map<String, Object> byMask = getOrCreateMap(server, mapKey);
    byMask.entrySet().removeIf(e -> Objects.toString(e.getKey(), "").equalsIgnoreCase(mask));
    if (value != null) {
      byMask.put(mask, value);
    }
    removeIfEmpty(server, mapKey, byMask);
  }

  private void mutateIgnoreServer(
      String serverId, String description, Consumer<Map<String, Object>> mutation) {
    ignoreSection.mutateMap(
        description,
        ignore -> {
          Map<String, Object> servers = getOrCreateMap(ignore, "servers");
          Map<String, Object> server = getOrCreateMap(servers, serverId);
          mutation.accept(server);
        });
  }

  private void mutateIgnore(
      String description, Consumer<Map<String, Object>> mutation) {
    ignoreSection.mutateMap(description, mutation);
  }

  private void mutateExistingIgnoreServer(
      String serverId, String description, Function<Map<String, Object>, Boolean> mutation) {
    ignoreSection.mutateExistingMapAndRemoveIfEmpty(description, mutation, "servers", serverId);
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
        server -> {
          if (!removeMaskFromList(server, "masks", m)) {
            return false;
          }

          removeMaskKey(server, "maskLevels", m);
          removeMaskKey(server, "maskChannels", m);
          removeMaskKey(server, "maskExpiresAt", m);
          removeMaskKey(server, "maskPatterns", m);
          removeMaskKey(server, "maskPatternModes", m);
          removeMaskKey(server, "maskReplies", m);
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
        server -> {
          if (!removeMaskFromList(server, "softMasks", m)) {
            return false;
          }

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
