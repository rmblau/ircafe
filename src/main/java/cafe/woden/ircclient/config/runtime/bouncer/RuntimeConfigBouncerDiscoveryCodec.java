package cafe.woden.ircclient.config.runtime.bouncer;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;

import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Pure helpers for persisted bouncer discovery and auto-connect settings. */
final class RuntimeConfigBouncerDiscoveryCodec {

  private static final String DEFAULT_GENERIC_BOUNCER_LOGIN_TEMPLATE = "{base}/{network}";

  private RuntimeConfigBouncerDiscoveryCodec() {}

  static String normalizeBackendKey(Object backendKey) {
    return Objects.toString(backendKey, "").trim().toLowerCase(Locale.ROOT);
  }

  static String normalizeKey(Object key) {
    return Objects.toString(key, "").trim();
  }

  static String normalizeGenericBouncerLoginTemplate(Object template) {
    String raw = Objects.toString(template, "").trim();
    return raw.isEmpty() ? DEFAULT_GENERIC_BOUNCER_LOGIN_TEMPLATE : raw;
  }

  static Optional<Boolean> readBoolean(Object raw) {
    return RuntimeConfigYamlSupport.asBoolean(raw);
  }

  static void mutateAutoConnectNetwork(
      Map<String, Object> networks, String networkName, boolean enabled) {
    String net = normalizeKey(networkName);
    if (net.isEmpty()) return;
    if (enabled) {
      networks.put(net, true);
      return;
    }

    networks.keySet().removeIf(k -> k != null && k.equalsIgnoreCase(net));
  }

  static Map<String, Map<String, Boolean>> readAutoConnectRules(Object rawAutoConnect) {
    if (!(rawAutoConnect instanceof Map<?, ?> autoConnectByBouncer)) return Map.of();

    LinkedHashMap<String, Map<String, Boolean>> out = new LinkedHashMap<>();
    for (var bouncerEntry : autoConnectByBouncer.entrySet()) {
      String bouncerServerId = normalizeKey(bouncerEntry.getKey());
      if (bouncerServerId.isEmpty()) continue;
      if (!(bouncerEntry.getValue() instanceof Map<?, ?> byNetwork)) continue;

      LinkedHashMap<String, Boolean> networks = new LinkedHashMap<>();
      for (var networkEntry : byNetwork.entrySet()) {
        String networkName = normalizeKey(networkEntry.getKey());
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
}
