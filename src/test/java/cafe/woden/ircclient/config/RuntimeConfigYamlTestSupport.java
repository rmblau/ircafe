package cafe.woden.ircclient.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

final class RuntimeConfigYamlTestSupport {

  private RuntimeConfigYamlTestSupport() {}

  @SuppressWarnings("unchecked")
  static Map<String, Object> loadYaml(Path cfg) throws IOException {
    return (Map<String, Object>) new Yaml().load(Files.readString(cfg));
  }

  static Map<String, Object> uiSection(Path cfg, String... path) throws IOException {
    return section(section(loadYaml(cfg), "ircafe", "ui"), path);
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> section(Map<String, Object> parent, String... path) {
    Map<String, Object> current = parent;
    for (String key : path) {
      current = (Map<String, Object>) current.get(key);
    }
    return current;
  }
}
