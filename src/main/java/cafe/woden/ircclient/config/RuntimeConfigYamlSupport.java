package cafe.woden.ircclient.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jmolecules.architecture.layered.InfrastructureLayer;

/** Shared YAML document helpers for the focused runtime-configuration stores. */
@InfrastructureLayer
final class RuntimeConfigYamlSupport {

  private RuntimeConfigYamlSupport() {}

  static Map<String, Object> getOrCreateMapPath(Map<String, Object> root, String... path) {
    Map<String, Object> current = root;
    for (String segment : path) {
      current = getOrCreateMap(current, segment);
    }
    return current;
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object existing = parent.get(key);
    if (existing instanceof Map<?, ?> map) return (Map<String, Object>) map;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }

  static Optional<Boolean> asBoolean(Object value) {
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

  @SuppressWarnings("unchecked")
  static List<String> getOrCreateStringList(Map<String, Object> parent, String key) {
    Object existing = parent.get(key);
    if (existing instanceof List<?>) {
      return (List<String>) existing;
    }
    List<String> created = new ArrayList<>();
    parent.put(key, created);
    return created;
  }

  static Optional<Integer> asInt(Object value) {
    if (value instanceof Number n) return Optional.of(n.intValue());
    if (value instanceof String s) {
      String t = s.trim();
      if (t.isEmpty()) return Optional.empty();
      try {
        return Optional.of(Integer.parseInt(t));
      } catch (Exception ignored) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }
}
