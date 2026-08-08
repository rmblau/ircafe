package cafe.woden.ircclient.config.runtime.ircv3;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;

import cafe.woden.ircclient.config.api.Ircv3CapabilityNameResolverPort;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Pure codec/policy helpers for persisted IRCv3 capability request overrides. */
final class RuntimeConfigIrcv3CapabilityCodec {

  private RuntimeConfigIrcv3CapabilityCodec() {}

  static Map<String, Boolean> parseCapabilities(
      Map<?, ?> rawCapabilities, Ircv3CapabilityNameResolverPort resolver) {
    if (rawCapabilities == null || rawCapabilities.isEmpty()) return Map.of();

    Map<String, Boolean> out = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : rawCapabilities.entrySet()) {
      String key = normalizeCapabilityKey(entry.getKey(), resolver);
      if (key == null) continue;
      Optional<Boolean> enabled = asBoolean(entry.getValue());
      enabled.ifPresent(value -> out.put(key, value));
    }
    return out;
  }

  static String normalizeCapabilityKey(
      Object capability, Ircv3CapabilityNameResolverPort resolver) {
    return safeResolver(resolver).normalizePreferenceKey(Objects.toString(capability, ""));
  }

  static boolean isCapabilityEnabled(
      Map<String, Boolean> capabilities, String key, boolean defaultEnabled) {
    if (key == null) return defaultEnabled;
    Map<String, Boolean> safeCapabilities = capabilities == null ? Map.of() : capabilities;
    return safeCapabilities.getOrDefault(key, defaultEnabled);
  }

  private static Ircv3CapabilityNameResolverPort safeResolver(
      Ircv3CapabilityNameResolverPort resolver) {
    return resolver == null ? new Ircv3CapabilityNameResolverPort() {} : resolver;
  }
}
