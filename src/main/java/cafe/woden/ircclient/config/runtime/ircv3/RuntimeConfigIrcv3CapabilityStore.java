package cafe.woden.ircclient.config.runtime.ircv3;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;

import cafe.woden.ircclient.config.api.Ircv3CapabilityNameResolverPort;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted IRCv3 capability request overrides under {@code ircafe.ui.ircv3Capabilities}. */
public class RuntimeConfigIrcv3CapabilityStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigIrcv3CapabilityStore.class);

  private final RuntimeConfigYamlSection uiSection;
  private Ircv3CapabilityNameResolverPort capabilityNameResolver =
      new Ircv3CapabilityNameResolverPort() {};

  public RuntimeConfigIrcv3CapabilityStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public void setCapabilityNameResolver(Ircv3CapabilityNameResolverPort capabilityNameResolver) {
    this.capabilityNameResolver =
        capabilityNameResolver == null
            ? new Ircv3CapabilityNameResolverPort() {}
            : capabilityNameResolver;
  }

  /**
   * Reads persisted IRCv3 capability request overrides under {@code ircafe.ui.ircv3Capabilities}.
   *
   * <p>Keys are normalized to lowercase, values are booleans. Missing/invalid entries are ignored.
   */
  public synchronized Map<String, Boolean> readCapabilities() {
    Optional<Object> capsObj =
        uiSection.readExistingValue("IRCv3 capability settings", "ircv3Capabilities");
    if (capsObj.isEmpty()) return Map.of();
    if (!(capsObj.get() instanceof Map<?, ?> caps)) return Map.of();

    Map<String, Boolean> out = new LinkedHashMap<>();
    for (Map.Entry<?, ?> e : caps.entrySet()) {
      String key = normalizeCapabilityKey(Objects.toString(e.getKey(), ""));
      if (key == null) continue;
      Optional<Boolean> b = asBoolean(e.getValue());
      b.ifPresent(value -> out.put(key, value));
    }
    return out;
  }

  /**
   * Returns whether a given IRCv3 capability should be requested, falling back to {@code
   * defaultEnabled} when no explicit override is present.
   */
  public synchronized boolean isCapabilityEnabled(String capability, boolean defaultEnabled) {
    String key = normalizeCapabilityKey(capability);
    if (key == null) return defaultEnabled;
    Map<String, Boolean> caps = readCapabilities();
    return caps.getOrDefault(key, defaultEnabled);
  }

  /**
   * Persists an IRCv3 capability request override under {@code ircafe.ui.ircv3Capabilities}.
   *
   * <p>Default behavior is "enabled", so enabled values are removed to keep YAML concise.
   */
  public synchronized void rememberCapabilityEnabled(String capability, boolean enabled) {
    String key = normalizeCapabilityKey(capability);
    if (key == null) return;

    uiSection.mutateMapAndRemoveIfEmpty(
        "IRCv3 capability '" + capability + "' setting",
        caps -> {
          if (enabled) {
            caps.remove(key);
          } else {
            caps.put(key, false);
          }
        },
        "ircv3Capabilities");
  }

  private String normalizeCapabilityKey(String capability) {
    return capabilityNameResolver.normalizePreferenceKey(capability);
  }
}
