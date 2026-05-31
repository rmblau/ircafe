package cafe.woden.ircclient.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class BouncerAutoConnectPropertiesSupport {

  private BouncerAutoConnectPropertiesSupport() {}

  static Map<String, Boolean> autoConnectForBouncer(
      Map<String, Map<String, Boolean>> autoConnect, String bouncerServerId) {
    String id = Objects.toString(bouncerServerId, "").trim();
    if (id.isEmpty()) return Map.of();

    Map<String, Boolean> networks = safeAutoConnect(autoConnect).get(id);
    if (networks == null || networks.isEmpty()) return Map.of();
    return Map.copyOf(networks);
  }

  static Map<String, Map<String, Boolean>> autoConnectCopy(
      Map<String, Map<String, Boolean>> autoConnect) {
    Map<String, Map<String, Boolean>> out = new LinkedHashMap<>();
    for (var entry : safeAutoConnect(autoConnect).entrySet()) {
      if (entry == null) continue;

      String bouncerServerId = Objects.toString(entry.getKey(), "").trim();
      if (bouncerServerId.isEmpty()) continue;

      Map<String, Boolean> networks =
          entry.getValue() == null ? Map.of() : Map.copyOf(entry.getValue());
      out.put(bouncerServerId, networks);
    }
    return out;
  }

  private static Map<String, Map<String, Boolean>> safeAutoConnect(
      Map<String, Map<String, Boolean>> autoConnect) {
    return autoConnect == null ? Map.of() : autoConnect;
  }
}
