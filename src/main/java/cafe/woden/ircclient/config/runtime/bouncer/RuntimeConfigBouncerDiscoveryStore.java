package cafe.woden.ircclient.config.runtime.bouncer;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted bouncer discovery and auto-connect settings under {@code ircafe.*}. */
public class RuntimeConfigBouncerDiscoveryStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigBouncerDiscoveryStore.class);

  private final RuntimeConfigYamlSection ircafeSection;

  public RuntimeConfigBouncerDiscoveryStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.ircafeSection = RuntimeConfigYamlSection.ircafe(file, documentStore, log);
  }

  public synchronized void rememberSojuAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    rememberBouncerAutoConnectNetwork("soju", bouncerServerId, networkName, enabled);
  }

  public synchronized void rememberZncAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    rememberBouncerAutoConnectNetwork("znc", bouncerServerId, networkName, enabled);
  }

  public synchronized Map<String, Map<String, Boolean>> readGenericBouncerAutoConnectRules() {
    return readBouncerAutoConnectRules("bouncer", "bouncer.autoConnect settings");
  }

  public synchronized void rememberGenericBouncerAutoConnectNetwork(
      String bouncerServerId, String networkName, boolean enabled) {
    rememberBouncerAutoConnectNetwork("bouncer", bouncerServerId, networkName, enabled);
  }

  public synchronized String readGenericBouncerLoginTemplate(String defaultValue) {
    String fallback =
        RuntimeConfigBouncerDiscoveryCodec.normalizeGenericBouncerLoginTemplate(defaultValue);
    return readGenericBouncerValue("bouncer.generic.loginTemplate", "loginTemplate")
        .map(RuntimeConfigBouncerDiscoveryCodec::normalizeGenericBouncerLoginTemplate)
        .orElse(fallback);
  }

  public synchronized boolean readGenericBouncerPreferLoginHint(boolean defaultValue) {
    return readGenericBouncerValue("bouncer.generic.preferLoginHint", "preferLoginHint")
        .flatMap(RuntimeConfigBouncerDiscoveryCodec::readBoolean)
        .orElse(defaultValue);
  }

  public synchronized void rememberGenericBouncerLoginTemplate(String template) {
    String normalized = RuntimeConfigBouncerDiscoveryCodec.normalizeKey(template);
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

  public synchronized void rememberGenericBouncerPreferLoginHint(boolean enabled) {
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
    String backend = RuntimeConfigBouncerDiscoveryCodec.normalizeBackendKey(backendKey);
    String sid = RuntimeConfigBouncerDiscoveryCodec.normalizeKey(bouncerServerId);
    if (backend.isEmpty() || sid.isEmpty()) return;

    ircafeSection.mutateMapAndRemoveIfEmpty(
        backend + " auto-connect setting",
        nets ->
            RuntimeConfigBouncerDiscoveryCodec.mutateAutoConnectNetwork(nets, networkName, enabled),
        backend,
        "autoConnect",
        sid);
  }

  private Map<String, Map<String, Boolean>> readBouncerAutoConnectRules(
      String backend, String description) {
    Optional<Object> autoConnectObj =
        ircafeSection.readExistingValue(description, backend, "autoConnect");
    if (autoConnectObj.isEmpty()) return Map.of();
    return RuntimeConfigBouncerDiscoveryCodec.readAutoConnectRules(autoConnectObj.get());
  }
}
