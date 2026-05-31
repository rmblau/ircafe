package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
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

  private final RuntimeConfigYamlSection ircafeSection;

  RuntimeConfigBouncerDiscoveryStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.ircafeSection = new RuntimeConfigYamlSection(file, documentStore, log, "ircafe");
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
    ircafeSection.mutateMapAndRemoveIfEmpty(
        "bouncer.generic.loginTemplate",
        generic -> {
          if (normalized.isEmpty()) {
            generic.remove("loginTemplate");
          } else {
            generic.put("loginTemplate", normalized);
          }
        },
        "bouncer",
        "generic");
  }

  synchronized void rememberGenericBouncerPreferLoginHint(boolean enabled) {
    ircafeSection.mutateMap(
        "bouncer.generic.preferLoginHint",
        generic -> generic.put("preferLoginHint", enabled),
        "bouncer",
        "generic");
  }

  private Optional<Object> readGenericBouncerValue(String description, String key) {
    return ircafeSection.readExistingValue(description, "bouncer", "generic", key);
  }

  private void rememberBouncerAutoConnectNetwork(
      String backendKey, String bouncerServerId, String networkName, boolean enabled) {
    String backend = Objects.toString(backendKey, "").trim().toLowerCase(Locale.ROOT);
    String sid = Objects.toString(bouncerServerId, "").trim();
    String net = Objects.toString(networkName, "").trim();
    if (backend.isEmpty() || sid.isEmpty() || net.isEmpty()) return;

    ircafeSection.mutateMapAndRemoveIfEmpty(
        backend + " auto-connect setting",
        nets -> {
          if (enabled) {
            nets.put(net, true);
            return;
          }

          // Remove case-insensitively so users can toggle based on what the bouncer returns.
          nets.keySet().removeIf(k -> k != null && k.equalsIgnoreCase(net));
        },
        backend,
        "autoConnect",
        sid);
  }

  private Map<String, Map<String, Boolean>> readBouncerAutoConnectRules(
      String backend, String description) {
    Optional<Object> autoConnectObj =
        ircafeSection.readExistingValue(description, backend, "autoConnect");
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
