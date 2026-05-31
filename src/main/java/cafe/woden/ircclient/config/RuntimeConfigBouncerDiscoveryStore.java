package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;

import java.nio.file.Files;
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
    try {
      if (file.toString().isBlank()) return Map.of();
      if (!Files.exists(file)) return Map.of();

      Map<String, Object> doc = documentStore.load();
      Optional<Object> autoConnectObj =
          RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "bouncer", "autoConnect");
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
    } catch (Exception e) {
      log.warn("[ircafe] Could not read bouncer.autoConnect settings from '{}'", file, e);
      return Map.of();
    }
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
    try {
      if (file.toString().isBlank()) return;

      String normalized = Objects.toString(template, "").trim();
      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> bouncer = getOrCreateMap(ircafe, "bouncer");
      Map<String, Object> generic = getOrCreateMap(bouncer, "generic");

      if (normalized.isEmpty()) {
        generic.remove("loginTemplate");
      } else {
        generic.put("loginTemplate", normalized);
      }

      if (generic.isEmpty()) bouncer.remove("generic");
      if (bouncer.isEmpty()) ircafe.remove("bouncer");
      if (ircafe.isEmpty()) doc.remove("ircafe");

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist bouncer.generic.loginTemplate to '{}'", file, e);
    }
  }

  synchronized void rememberGenericBouncerPreferLoginHint(boolean enabled) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> bouncer = getOrCreateMap(ircafe, "bouncer");
      Map<String, Object> generic = getOrCreateMap(bouncer, "generic");

      generic.put("preferLoginHint", enabled);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist bouncer.generic.preferLoginHint to '{}'", file, e);
    }
  }

  private Optional<Object> readGenericBouncerValue(String description, String key) {
    try {
      if (file.toString().isBlank()) return Optional.empty();
      if (!Files.exists(file)) return Optional.empty();

      Map<String, Object> doc = documentStore.load();
      return RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "bouncer", "generic", key);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read {} from '{}'", description, file, e);
      return Optional.empty();
    }
  }

  private void rememberBouncerAutoConnectNetwork(
      String backendKey, String bouncerServerId, String networkName, boolean enabled) {
    String backend = Objects.toString(backendKey, "").trim().toLowerCase(Locale.ROOT);
    if (backend.isEmpty()) return;
    try {
      if (file.toString().isBlank()) return;

      String sid = Objects.toString(bouncerServerId, "").trim();
      String net = Objects.toString(networkName, "").trim();
      if (sid.isEmpty() || net.isEmpty()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> bouncerSection = getOrCreateMap(ircafe, backend);
      Map<String, Object> autoConnect = getOrCreateMap(bouncerSection, "autoConnect");

      @SuppressWarnings("unchecked")
      Map<String, Object> nets =
          (autoConnect.get(sid) instanceof Map<?, ?> mm)
              ? (Map<String, Object>) mm
              : new LinkedHashMap<>();

      if (enabled) {
        nets.put(net, true);
        autoConnect.put(sid, nets);
      } else {
        // Remove case-insensitively so users can toggle based on what the bouncer returns.
        nets.keySet().removeIf(k -> k != null && k.equalsIgnoreCase(net));
        if (nets.isEmpty()) {
          autoConnect.remove(sid);
        } else {
          autoConnect.put(sid, nets);
        }

        // Clean up empty structures to keep the YAML tidy.
        if (autoConnect.isEmpty()) {
          bouncerSection.remove("autoConnect");
        }
        if (bouncerSection.isEmpty()) {
          ircafe.remove(backend);
        }
        if (ircafe.isEmpty()) {
          doc.remove("ircafe");
        }
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn(
          "[ircafe] Could not persist {} auto-connect setting to '{}'",
          Objects.toString(backendKey, "").trim().toLowerCase(Locale.ROOT),
          file,
          e);
    }
  }

  private static String normalizeGenericBouncerLoginTemplate(Object template) {
    String raw = Objects.toString(template, "").trim();
    return raw.isEmpty() ? DEFAULT_GENERIC_BOUNCER_LOGIN_TEMPLATE : raw;
  }

}
