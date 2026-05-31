package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateMap;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.model.InterceptorDefinition;
import cafe.woden.ircclient.model.InterceptorRule;
import cafe.woden.ircclient.model.InterceptorRuleMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted interceptor definitions under {@code ircafe.ui.interceptors}. */
class RuntimeConfigInterceptorStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigInterceptorStore.class);

  private final RuntimeConfigYamlSection uiSection;

  RuntimeConfigInterceptorStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "ui");
  }

  synchronized Map<String, List<InterceptorDefinition>> readDefinitions() {
    return uiSection
        .readExistingValue("interceptor definitions", "interceptors", "servers")
        .map(RuntimeConfigInterceptorStore::parseInterceptorDefinitionsByServer)
        .orElseGet(Map::of);
  }

  synchronized void rememberDefinitions(Map<String, List<InterceptorDefinition>> defsByServer) {
    uiSection.mutateMap(
        "interceptor definitions",
        ui -> {
          Map<String, Object> interceptors = getOrCreateMap(ui, "interceptors");
          Map<String, Object> serversOut = serializeDefinitionsByServer(defsByServer);

          if (serversOut.isEmpty()) {
            interceptors.remove("servers");
            if (interceptors.isEmpty()) {
              ui.remove("interceptors");
            }
          } else {
            interceptors.put("servers", serversOut);
          }
        });
  }

  private static Map<String, List<InterceptorDefinition>> parseInterceptorDefinitionsByServer(
      Object rawServers) {
    if (!(rawServers instanceof Map<?, ?> servers)) return Map.of();

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
    return out.isEmpty() ? Map.of() : Map.copyOf(out);
  }

  private static Map<String, Object> serializeDefinitionsByServer(
      Map<String, List<InterceptorDefinition>> defsByServer) {
    LinkedHashMap<String, Object> out = new LinkedHashMap<>();
    if (defsByServer == null) return out;

    for (Map.Entry<String, List<InterceptorDefinition>> entry : defsByServer.entrySet()) {
      String sid = Objects.toString(entry.getKey(), "").trim();
      if (sid.isEmpty()) continue;

      List<Map<String, Object>> defsOut = serializeDefinitions(entry.getValue());
      if (!defsOut.isEmpty()) {
        out.put(sid, defsOut);
      }
    }
    return out;
  }

  private static List<Map<String, Object>> serializeDefinitions(
      List<InterceptorDefinition> definitions) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (definitions == null) return out;

    for (InterceptorDefinition def : definitions) {
      Map<String, Object> serialized = serializeDefinition(def);
      if (!serialized.isEmpty()) out.add(serialized);
    }
    return out;
  }

  private static Map<String, Object> serializeDefinition(InterceptorDefinition def) {
    if (def == null) return Map.of();
    String id = Objects.toString(def.id(), "").trim();
    if (id.isEmpty()) return Map.of();

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
    String scriptWorkingDirectory = Objects.toString(def.actionScriptWorkingDirectory(), "").trim();
    if (!scriptWorkingDirectory.isEmpty()) {
      m.put("actionScriptWorkingDirectory", scriptWorkingDirectory);
    }

    m.put("rules", serializeRules(def.rules()));
    return m;
  }

  private static List<Map<String, Object>> serializeRules(List<InterceptorRule> rules) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (rules == null) return out;

    for (InterceptorRule rule : rules) {
      if (rule == null) continue;
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("enabled", rule.enabled());
      m.put("label", Objects.toString(rule.label(), "").trim());
      String eventTypesCsv = Objects.toString(rule.eventTypesCsv(), "").trim();
      if (!eventTypesCsv.isEmpty()) m.put("eventTypesCsv", eventTypesCsv);

      m.put(
          "messageMode",
          rule.messageMode() != null
              ? rule.messageMode().name()
              : InterceptorRuleMode.LIKE.name());
      String messagePattern = Objects.toString(rule.messagePattern(), "").trim();
      if (!messagePattern.isEmpty()) m.put("messagePattern", messagePattern);

      m.put(
          "nickMode",
          rule.nickMode() != null ? rule.nickMode().name() : InterceptorRuleMode.LIKE.name());
      String nickPattern = Objects.toString(rule.nickPattern(), "").trim();
      if (!nickPattern.isEmpty()) m.put("nickPattern", nickPattern);

      m.put(
          "hostmaskMode",
          rule.hostmaskMode() != null
              ? rule.hostmaskMode().name()
              : InterceptorRuleMode.GLOB.name());
      String hostmaskPattern = Objects.toString(rule.hostmaskPattern(), "").trim();
      if (!hostmaskPattern.isEmpty()) m.put("hostmaskPattern", hostmaskPattern);
      out.add(m);
    }
    return out;
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

}
