package cafe.woden.ircclient.config.runtime.ignore;

import static cafe.woden.ircclient.config.runtime.ignore.RuntimeConfigIgnoreMapKeySupport.persistedMaskMapKey;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Rewrites legacy ignore metadata maps before Spring imports the runtime YAML.
 *
 * <p>Spring configuration binding requires bracket notation for map keys containing hostmask
 * characters such as {@code !} and {@code @}. Older IRCafe builds wrote those masks as plain YAML
 * keys, which can prevent startup before the normal runtime-config store gets a chance to repair
 * the file.
 */
public final class RuntimeConfigIgnoreLegacyMapKeySanitizer
    implements EnvironmentPostProcessor, Ordered {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigIgnoreLegacyMapKeySanitizer.class);
  private static final Set<String> MASK_METADATA_MAPS =
      Set.of(
          "maskLevels",
          "maskChannels",
          "maskExpiresAt",
          "maskPatterns",
          "maskPatternModes",
          "maskReplies");

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    Path configPath = runtimeConfigPath(environment);
    if (configPath == null || configPath.toString().isBlank() || !Files.isRegularFile(configPath)) {
      return;
    }

    try {
      Yaml yaml = new Yaml(dumperOptions());
      Map<String, Object> doc;
      try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
        Object loaded = yaml.load(reader);
        if (!(loaded instanceof Map<?, ?> map)) return;
        doc = castMap(map);
      }

      if (!sanitizeIgnoreMaskMaps(doc)) return;

      try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
        yaml.dump(doc, writer);
      }
    } catch (Exception ex) {
      log.warn("[ircafe] Could not sanitize legacy ignore mask keys in '{}'", configPath, ex);
    }
  }

  private static Path runtimeConfigPath(ConfigurableEnvironment environment) {
    boolean explicitRuntimeConfig =
        environment != null && environment.containsProperty("ircafe.runtime-config");
    String configured = "";
    if (explicitRuntimeConfig) {
      try {
        configured = Objects.toString(environment.getProperty("ircafe.runtime-config"), "").trim();
      } catch (IllegalArgumentException ex) {
        return null;
      }
      if (configured.contains("${")) return null;
    } else {
      String xdgConfigHome = Objects.toString(System.getenv("XDG_CONFIG_HOME"), "").trim();
      if (!xdgConfigHome.isEmpty()) {
        configured = xdgConfigHome + "/ircafe/ircafe.yml";
      } else {
        configured =
            Objects.toString(System.getProperty("user.home"), "").trim()
                + "/.config/ircafe/ircafe.yml";
      }
    }
    return configured.isBlank() ? null : Paths.get(configured);
  }

  private static boolean sanitizeIgnoreMaskMaps(Map<String, Object> doc) {
    Map<String, Object> ircafe = mapValue(doc.get("ircafe"));
    Map<String, Object> ignore = mapValue(ircafe.get("ignore"));
    Map<String, Object> servers = mapValue(ignore.get("servers"));
    if (servers.isEmpty()) return false;

    boolean changed = false;
    for (Object serverValue : servers.values()) {
      Map<String, Object> server = mapValue(serverValue);
      if (server.isEmpty()) continue;
      for (String mapName : MASK_METADATA_MAPS) {
        Object rawMap = server.get(mapName);
        if (!(rawMap instanceof Map<?, ?> map) || map.isEmpty()) continue;
        Map<String, Object> sanitized = sanitizeMaskMapKeys(map);
        if (!sanitized.equals(rawMap)) {
          server.put(mapName, sanitized);
          changed = true;
        }
      }
    }
    return changed;
  }

  private static Map<String, Object> sanitizeMaskMapKeys(Map<?, ?> raw) {
    LinkedHashMap<String, Object> out = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : raw.entrySet()) {
      String key = Objects.toString(entry.getKey(), "").trim();
      if (key.isEmpty()) continue;
      out.put(persistedMaskMapKey(key), entry.getValue());
    }
    return out;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> mapValue(Object raw) {
    if (!(raw instanceof Map<?, ?> map)) return Map.of();
    return (Map<String, Object>) map;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> castMap(Map<?, ?> raw) {
    return (Map<String, Object>) raw;
  }

  private static DumperOptions dumperOptions() {
    DumperOptions opts = new DumperOptions();
    opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    opts.setPrettyFlow(true);
    opts.setIndent(2);
    opts.setIndicatorIndent(1);
    opts.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    return opts;
  }
}
