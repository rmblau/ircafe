package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateMap;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.mutateDocument;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.readExistingValue;
import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.removeIfEmpty;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted bouncer discovery and auto-connect settings under {@code ircafe.*}. */
class RuntimeConfigBouncerDiscoveryStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigBouncerDiscoveryStore.class);
  private static final String DEFAULT_GENERIC_BOUNCER_LOGIN_TEMPLATE = "{base}/{network}";

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigBouncerDiscoveryStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberSojuAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    rememberBouncerAutoConnectNetwork("soju", bouncerServerId, networkName, enabled);
  }

  synchronized void rememberZncAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    rememberBouncerAutoConnectNetwork("znc", bouncerServerId, networkName, enabled);
  }

  synchronized Map<String, Map<String, Boolean>> readGenericBouncerAutoConnectRules() {
    return readBouncerAutoConnectRules("bouncer", "bouncer.autoConnect settings");
  }

  synchronized void rememberGenericBouncerAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    rememberBouncerAutoConnectNetwork("bouncer", bouncerServerId, networkName, enabled);
  }

  synchronized String readGenericBouncerLoginTemplate(String defaultValue) {
    String fallback = normalizeGenericBouncerLoginTemplate(defaultValue);
    return readGenericBouncerValue("bouncer.generic.loginTemplate", "loginTemplate")
        .map(RuntimeConfigBouncerDiscoveryStore::normalizeGenericBouncerLoginTemplate)
        .orElse(fallback);
  }

  synchronized boolean readGenericBouncerPreferLoginHint(boolean defaultValue) {
    return readGenericBouncerValue("bouncer.generic.preferLoginHint", "preferLoginHint")
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  synchronized void rememberGenericBouncerLoginTemplate(String template) {
    String normalized = Objects.toString(template, "").trim();
    mutateDocument(
        file,
        documentStore,
        log,
        "bouncer.generic.loginTemplate",
        doc -> {
          Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
          Map<String, Object> bouncer = getOrCreateMap(ircafe, "bouncer");
          Map<String, Object> generic = getOrCreateMap(bouncer, "generic");

          if (normalized.isEmpty()) {
            generic.remove("loginTemplate");
          } else {
            generic.put("loginTemplate", normalized);
          }

          removeIfEmpty(bouncer, "generic", generic);
          removeIfEmpty(ircafe, "bouncer", bouncer);
          removeIfEmpty(doc, "ircafe", ircafe);
          return true;
        });
  }

  synchronized void rememberGenericBouncerPreferLoginHint(boolean enabled) {
    mutateDocument(
        file,
        documentStore,
        log,
        "bouncer.generic.preferLoginHint",
        doc -> {
          Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
          Map<String, Object> bouncer = getOrCreateMap(ircafe, "bouncer");
          Map<String, Object> generic = getOrCreateMap(bouncer, "generic");

          generic.put("preferLoginHint", enabled);
          return true;
        });
  }

  private Optional<Object> readGenericBouncerValue(String description, String key) {
    return readExistingValue(
        file, documentStore, log, description, "ircafe", "bouncer", "generic", key);
  }

  private void rememberBouncerAutoConnectNetwork(
      String backendKey, String bouncerServerId, String networkName, boolean enabled) {
    String backend = Objects.toString(backendKey, "").trim().toLowerCase(Locale.ROOT);
    String sid = Objects.toString(bouncerServerId, "").trim();
    String net = Objects.toString(networkName, "").trim();
    if (backend.isEmpty() || sid.isEmpty() || net.isEmpty()) return;

    mutateDocument(
        file,
        documentStore,
        log,
        backend + " auto-connect setting",
        doc -> {
          Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
          Map<String, Object> bouncerSection = getOrCreateMap(ircafe, backend);
          Map<String, Object> autoConnect = getOrCreateMap(bouncerSection, "autoConnect");
          Map<String, Object> nets = getOrCreateMap(autoConnect, sid);

          if (enabled) {
            nets.put(net, true);
            return true;
          }

          // Remove case-insensitively so users can toggle based on what the bouncer returns.
          nets.keySet().removeIf(k -> k != null && k.equalsIgnoreCase(net));
          removeIfEmpty(autoConnect, sid, nets);

          // Clean up empty structures to keep the YAML tidy.
          removeIfEmpty(bouncerSection, "autoConnect", autoConnect);
          removeIfEmpty(ircafe, backend, bouncerSection);
          removeIfEmpty(doc, "ircafe", ircafe);
          return true;
        });
  }

  private Map<String, Map<String, Boolean>> readBouncerAutoConnectRules(
      String backend, String description) {
    Optional<Object> autoConnectObj =
        readExistingValue(file, documentStore, log, description, "ircafe", backend, "autoConnect");
    if (autoConnectObj.isEmpty()) return Map.of();
    if (!(autoConnectObj.get() instanceof Map<?, ?> autoConnectByBouncer)) return Map.of();

    LinkedHashMap<String, Map<String, Boolean>> out = new LinkedHashMap<>();
    for (var bouncerEntry : autoConnectByBouncer.entrySet()) {
      String bouncerServerId = Objects.toString(bouncerEntry.getKey(), "").trim();
      if (bouncerServerId.isEmpty()) continue;
      if (!(bouncerEntry.getValue() instanceof Map<?, ?> byNetwork)) continue;

      LinkedHashMap<String, Boolean> networks = new LinkedHashMap<>();
      for (var networkEntry : byNetwork.entrySet()) {
        String networkName = Objects.toString(networkEntry.getKey(), "").trim();
        if (networkName.isEmpty()) continue;
        boolean enabled = asBoolean(networkEntry.getValue()).orElse(false);
        if (enabled) networks.put(networkName, true);
      }

      if (!networks.isEmpty()) {
        out.put(bouncerServerId, Map.copyOf(networks));
      }
    }
    return out.isEmpty() ? Map.of() : Map.copyOf(out);
  }

  private static String normalizeGenericBouncerLoginTemplate(Object template) {
    String raw = Objects.toString(template, "").trim();
    return raw.isEmpty() ? DEFAULT_GENERIC_BOUNCER_LOGIN_TEMPLATE : raw;
  }

}
