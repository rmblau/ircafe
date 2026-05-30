package cafe.woden.ircclient.config;

import cafe.woden.ircclient.model.InterceptorDefinition;
import cafe.woden.ircclient.model.InterceptorRule;
import cafe.woden.ircclient.model.InterceptorRuleMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted interceptor definitions under {@code ircafe.ui.interceptors}. */
class RuntimeConfigInterceptorStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigInterceptorStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigInterceptorStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized Map<String, List<InterceptorDefinition>> readDefinitions() {
    try {
      if (file.toString().isBlank()) return Map.of();
      if (!Files.exists(file)) return Map.of();

      Map<String, Object> doc = documentStore.load();
      Object ircafeObj = doc.get("ircafe");
      if (!(ircafeObj instanceof Map<?, ?> ircafe)) return Map.of();

      Object uiObj = ircafe.get("ui");
      if (!(uiObj instanceof Map<?, ?> ui)) return Map.of();

      Object interceptorsObj = ui.get("interceptors");
      if (!(interceptorsObj instanceof Map<?, ?> interceptors)) return Map.of();

      Object serversObj = interceptors.get("servers");
      if (!(serversObj instanceof Map<?, ?> servers)) return Map.of();

      LinkedHashMap<String, List<InterceptorDefinition>> out = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : servers.entrySet()) {
        String sid = Objects.toString(entry.getKey(), "").trim();
        if (sid.isEmpty()) continue;
        List<InterceptorDefinition> defs =
            parseInterceptorDefinitionsForServer(entry.getValue(), sid);
        if (!defs.isEmpty()) {
          out.put(sid, defs);
        }
      }

      if (out.isEmpty()) return Map.of();
      return Map.copyOf(out);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read interceptor definitions from '{}'", file, e);
      return Map.of();
    }
  }

  synchronized void rememberDefinitions(Map<String, List<InterceptorDefinition>> defsByServer) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ui = getOrCreateMap(ircafe, "ui");
      Map<String, Object> interceptors = getOrCreateMap(ui, "interceptors");

      LinkedHashMap<String, Object> serversOut = new LinkedHashMap<>();
      if (defsByServer != null) {
        for (Map.Entry<String, List<InterceptorDefinition>> entry : defsByServer.entrySet()) {
          String sid = Objects.toString(entry.getKey(), "").trim();
          if (sid.isEmpty()) continue;

          List<Map<String, Object>> defsOut = new ArrayList<>();
          List<InterceptorDefinition> defs = entry.getValue();
          if (defs != null) {
            for (InterceptorDefinition def : defs) {
              if (def == null) continue;
              String id = Objects.toString(def.id(), "").trim();
              if (id.isEmpty()) continue;

              Map<String, Object> m = new LinkedHashMap<>();
              m.put("id", id);
              m.put("name", Objects.toString(def.name(), "").trim());
              m.put("enabled", def.enabled());

              String scopeServerId = Objects.toString(def.scopeServerId(), "").trim();
              // Keep this key even when blank so "any server" survives round-trip.
              m.put("scopeServerId", scopeServerId);

              m.put(
                  "channelIncludeMode",
                  def.channelIncludeMode() != null
                      ? def.channelIncludeMode().name()
                      : InterceptorRuleMode.GLOB.name());
              String channelIncludes = Objects.toString(def.channelIncludes(), "").trim();
              if (!channelIncludes.isEmpty()) m.put("channelIncludes", channelIncludes);

              m.put(
                  "channelExcludeMode",
                  def.channelExcludeMode() != null
                      ? def.channelExcludeMode().name()
                      : InterceptorRuleMode.GLOB.name());
              String channelExcludes = Objects.toString(def.channelExcludes(), "").trim();
              if (!channelExcludes.isEmpty()) m.put("channelExcludes", channelExcludes);

              m.put("actionSoundEnabled", def.actionSoundEnabled());
              m.put("actionStatusBarEnabled", def.actionStatusBarEnabled());
              m.put("actionToastEnabled", def.actionToastEnabled());
              String soundId = Objects.toString(def.actionSoundId(), "").trim();
              m.put("actionSoundId", soundId.isEmpty() ? "NOTIF_1" : soundId);
              m.put("actionSoundUseCustom", def.actionSoundUseCustom());
              String soundCustom = Objects.toString(def.actionSoundCustomPath(), "").trim();
              if (!soundCustom.isEmpty()) m.put("actionSoundCustomPath", soundCustom);

              m.put("actionScriptEnabled", def.actionScriptEnabled());
              String scriptPath = Objects.toString(def.actionScriptPath(), "").trim();
              if (!scriptPath.isEmpty()) m.put("actionScriptPath", scriptPath);
              String scriptArgs = Objects.toString(def.actionScriptArgs(), "").trim();
              if (!scriptArgs.isEmpty()) m.put("actionScriptArgs", scriptArgs);
              String scriptWorkingDirectory =
                  Objects.toString(def.actionScriptWorkingDirectory(), "").trim();
              if (!scriptWorkingDirectory.isEmpty()) {
                m.put("actionScriptWorkingDirectory", scriptWorkingDirectory);
              }

              List<Map<String, Object>> rulesOut = new ArrayList<>();
              if (def.rules() != null) {
                for (InterceptorRule rule : def.rules()) {
                  if (rule == null) continue;
                  Map<String, Object> rm = new LinkedHashMap<>();
                  rm.put("enabled", rule.enabled());
                  rm.put("label", Objects.toString(rule.label(), "").trim());
                  String eventTypesCsv = Objects.toString(rule.eventTypesCsv(), "").trim();
                  if (!eventTypesCsv.isEmpty()) rm.put("eventTypesCsv", eventTypesCsv);

                  rm.put(
                      "messageMode",
                      rule.messageMode() != null
                          ? rule.messageMode().name()
                          : InterceptorRuleMode.LIKE.name());
                  String messagePattern = Objects.toString(rule.messagePattern(), "").trim();
                  if (!messagePattern.isEmpty()) rm.put("messagePattern", messagePattern);

                  rm.put(
                      "nickMode",
                      rule.nickMode() != null
                          ? rule.nickMode().name()
                          : InterceptorRuleMode.LIKE.name());
                  String nickPattern = Objects.toString(rule.nickPattern(), "").trim();
                  if (!nickPattern.isEmpty()) rm.put("nickPattern", nickPattern);

                  rm.put(
                      "hostmaskMode",
                      rule.hostmaskMode() != null
                          ? rule.hostmaskMode().name()
                          : InterceptorRuleMode.GLOB.name());
                  String hostmaskPattern = Objects.toString(rule.hostmaskPattern(), "").trim();
                  if (!hostmaskPattern.isEmpty()) rm.put("hostmaskPattern", hostmaskPattern);
                  rulesOut.add(rm);
                }
              }
              m.put("rules", rulesOut);
              defsOut.add(m);
            }
          }

          if (!defsOut.isEmpty()) {
            serversOut.put(sid, defsOut);
          }
        }
      }

      if (serversOut.isEmpty()) {
        interceptors.remove("servers");
        if (interceptors.isEmpty()) {
          ui.remove("interceptors");
        }
      } else {
        interceptors.put("servers", serversOut);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist interceptor definitions to '{}'", file, e);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }

  private static List<InterceptorDefinition> parseInterceptorDefinitionsForServer(
      Object rawList, String ownerServerId) {
    String owner = Objects.toString(ownerServerId, "").trim();
    if (!(rawList instanceof List<?> list) || owner.isEmpty()) return List.of();

    ArrayList<InterceptorDefinition> out = new ArrayList<>();
    for (Object item : list) {
      if (!(item instanceof Map<?, ?> m)) continue;

      String id = Objects.toString(m.get("id"), "").trim();
      if (id.isEmpty()) continue;
      String name = Objects.toString(m.get("name"), "").trim();
      boolean enabled = asBoolean(m.get("enabled")).orElse(Boolean.TRUE);

      String scopeServerId = Objects.toString(m.get("scopeServerId"), "").trim();
      if (!m.containsKey("scopeServerId")) {
        // Backward compatibility: old definitions were server-local.
        scopeServerId = owner;
      }

      InterceptorRuleMode channelIncludeMode =
          asRuleMode(m.get("channelIncludeMode"), InterceptorRuleMode.GLOB);
      String channelIncludes = Objects.toString(m.get("channelIncludes"), "").trim();
      if (channelIncludes.isEmpty()) {
        // Backward compatibility with the old "channelsCsv" shape.
        channelIncludes = Objects.toString(m.get("channelsCsv"), "").trim();
      }

      InterceptorRuleMode channelExcludeMode =
          asRuleMode(m.get("channelExcludeMode"), InterceptorRuleMode.GLOB);
      String channelExcludes = Objects.toString(m.get("channelExcludes"), "").trim();

      boolean actionSoundEnabled = asBoolean(m.get("actionSoundEnabled")).orElse(Boolean.FALSE);
      boolean actionStatusBarEnabled =
          asBoolean(m.get("actionStatusBarEnabled")).orElse(Boolean.FALSE);
      boolean actionToastEnabled = asBoolean(m.get("actionToastEnabled")).orElse(Boolean.FALSE);
      String actionSoundId = Objects.toString(m.get("actionSoundId"), "").trim();
      if (actionSoundId.isEmpty()) actionSoundId = "NOTIF_1";
      boolean actionSoundUseCustom = asBoolean(m.get("actionSoundUseCustom")).orElse(Boolean.FALSE);
      String actionSoundCustomPath = Objects.toString(m.get("actionSoundCustomPath"), "").trim();

      boolean actionScriptEnabled = asBoolean(m.get("actionScriptEnabled")).orElse(Boolean.FALSE);
      String actionScriptPath = Objects.toString(m.get("actionScriptPath"), "").trim();
      String actionScriptArgs = Objects.toString(m.get("actionScriptArgs"), "").trim();
      String actionScriptWorkingDirectory =
          Objects.toString(m.get("actionScriptWorkingDirectory"), "").trim();

      List<InterceptorRule> rules = parseInterceptorRules(m.get("rules"));
      if (rules.isEmpty()) {
        // Backward compatibility with the old single-dimension rule shape.
        rules =
            List.of(
                new InterceptorRule(
                    true,
                    "Rule 1",
                    "message,action",
                    asRuleMode(m.get("mode"), InterceptorRuleMode.LIKE),
                    Objects.toString(m.get("pattern"), "").trim(),
                    InterceptorRuleMode.LIKE,
                    "",
                    InterceptorRuleMode.GLOB,
                    ""));
      }

      out.add(
          new InterceptorDefinition(
              id,
              name,
              enabled,
              scopeServerId,
              channelIncludeMode,
              channelIncludes,
              channelExcludeMode,
              channelExcludes,
              actionSoundEnabled,
              actionStatusBarEnabled,
              actionToastEnabled,
              actionSoundId,
              actionSoundUseCustom,
              actionSoundCustomPath,
              actionScriptEnabled,
              actionScriptPath,
              actionScriptArgs,
              actionScriptWorkingDirectory,
              rules));
    }
    return List.copyOf(out);
  }

  private static List<InterceptorRule> parseInterceptorRules(Object rawRules) {
    if (!(rawRules instanceof List<?> list)) return List.of();

    ArrayList<InterceptorRule> out = new ArrayList<>();
    for (Object item : list) {
      if (!(item instanceof Map<?, ?> m)) continue;

      boolean enabled = asBoolean(m.get("enabled")).orElse(Boolean.TRUE);
      String label = Objects.toString(m.get("label"), "").trim();
      String eventTypesCsv = Objects.toString(m.get("eventTypesCsv"), "").trim();
      if (eventTypesCsv.isEmpty()) {
        // Backward compatibility with key variants.
        eventTypesCsv = Objects.toString(m.get("eventTypes"), "").trim();
      }

      InterceptorRuleMode messageMode =
          asRuleMode(
              m.containsKey("messageMode") ? m.get("messageMode") : m.get("mode"),
              InterceptorRuleMode.LIKE);
      String messagePattern =
          Objects.toString(
                  m.containsKey("messagePattern") ? m.get("messagePattern") : m.get("pattern"), "")
              .trim();

      InterceptorRuleMode nickMode = asRuleMode(m.get("nickMode"), InterceptorRuleMode.LIKE);
      String nickPattern = Objects.toString(m.get("nickPattern"), "").trim();

      InterceptorRuleMode hostmaskMode =
          asRuleMode(m.get("hostmaskMode"), InterceptorRuleMode.GLOB);
      String hostmaskPattern = Objects.toString(m.get("hostmaskPattern"), "").trim();

      out.add(
          new InterceptorRule(
              enabled,
              label,
              eventTypesCsv,
              messageMode,
              messagePattern,
              nickMode,
              nickPattern,
              hostmaskMode,
              hostmaskPattern));
    }
    return List.copyOf(out);
  }

  private static InterceptorRuleMode asRuleMode(Object value, InterceptorRuleMode fallback) {
    if (value instanceof InterceptorRuleMode mode) return mode;
    String raw = Objects.toString(value, "").trim();
    if (raw.isEmpty()) return fallback;
    try {
      return InterceptorRuleMode.valueOf(raw.toUpperCase(Locale.ROOT));
    } catch (Exception ignored) {
      return fallback;
    }
  }

  private static Optional<Boolean> asBoolean(Object value) {
    if (value instanceof Boolean b) return Optional.of(b);
    if (value instanceof String s) {
      String t = s.trim();
      if (t.equalsIgnoreCase("true")) return Optional.of(Boolean.TRUE);
      if (t.equalsIgnoreCase("false")) return Optional.of(Boolean.FALSE);
    }
    if (value instanceof Number n) {
      int i = n.intValue();
      if (i == 0) return Optional.of(Boolean.FALSE);
      if (i == 1) return Optional.of(Boolean.TRUE);
    }
    return Optional.empty();
  }
}
