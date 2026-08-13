package cafe.woden.ircclient.config.runtime.launch;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns optional child-JVM launch settings under {@code ircafe.launch.jvm}. */
public class RuntimeConfigLaunchJvmStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigLaunchJvmStore.class);

  private final RuntimeConfigYamlSection ircafeSection;

  public RuntimeConfigLaunchJvmStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.ircafeSection = RuntimeConfigYamlSection.ircafe(file, documentStore, log);
  }

  public synchronized String readJavaCommand(String defaultValue) {
    String fallback =
        RuntimeConfigLaunchJvmSettingsCodec.normalizeJavaCommandFallback(defaultValue);
    String raw =
        readValue("launch.jvm.javaCommand", "javaCommand")
            .map(RuntimeConfigLaunchJvmSettingsCodec::normalizeJavaCommandReadValue)
            .orElse("");
    return raw.isEmpty() ? fallback : raw;
  }

  public synchronized int readXmsMiB(int defaultValue) {
    int fallback = RuntimeConfigLaunchJvmSettingsCodec.normalizeHeapMiB(defaultValue);
    return readValue("launch.jvm.xmsMiB", "xmsMiB")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigLaunchJvmSettingsCodec::normalizeHeapMiB)
        .orElse(fallback);
  }

  public synchronized int readXmxMiB(int defaultValue) {
    int fallback = RuntimeConfigLaunchJvmSettingsCodec.normalizeHeapMiB(defaultValue);
    return readValue("launch.jvm.xmxMiB", "xmxMiB")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigLaunchJvmSettingsCodec::normalizeHeapMiB)
        .orElse(fallback);
  }

  public synchronized String readGc(String defaultValue) {
    String fallback = RuntimeConfigLaunchJvmSettingsCodec.normalizeGc(defaultValue);
    return readValue("launch.jvm.gc", "gc")
        .map(RuntimeConfigLaunchJvmSettingsCodec::normalizeGc)
        .orElse(fallback);
  }

  public synchronized List<String> readArgs(List<String> defaultValue) {
    List<String> fallback = RuntimeConfigLaunchJvmSettingsCodec.normalizeArgs(defaultValue);
    Object argsObj = readValue("launch.jvm.args", "args").orElse(null);
    if (!(argsObj instanceof List<?> raw)) return fallback;
    return RuntimeConfigLaunchJvmSettingsCodec.normalizeArgs(raw);
  }

  public synchronized void rememberJavaCommand(String javaCommand) {
    String cmd = RuntimeConfigLaunchJvmSettingsCodec.normalizeJavaCommandSetting(javaCommand);
    rememberJvmSetting("launch.jvm.javaCommand", "javaCommand", cmd);
  }

  public synchronized void rememberXmsMiB(int xmsMiB) {
    rememberJvmSetting(
        "launch.jvm.xmsMiB",
        "xmsMiB",
        RuntimeConfigLaunchJvmSettingsCodec.normalizeHeapMiB(xmsMiB));
  }

  public synchronized void rememberXmxMiB(int xmxMiB) {
    rememberJvmSetting(
        "launch.jvm.xmxMiB",
        "xmxMiB",
        RuntimeConfigLaunchJvmSettingsCodec.normalizeHeapMiB(xmxMiB));
  }

  public synchronized void rememberGc(String gc) {
    rememberJvmSetting("launch.jvm.gc", "gc", RuntimeConfigLaunchJvmSettingsCodec.normalizeGc(gc));
  }

  public synchronized void rememberArgs(List<String> args) {
    rememberJvmSetting(
        "launch.jvm.args", "args", RuntimeConfigLaunchJvmSettingsCodec.normalizeArgs(args));
  }

  private Optional<Object> readValue(String description, String key) {
    return ircafeSection.readExistingValue(description, "launch", "jvm", key);
  }

  private void rememberJvmSetting(String description, String key, Object value) {
    ircafeSection.mutateMapAndRemoveIfEmpty(
        description,
        jvm -> {
          if (RuntimeConfigLaunchJvmSettingsCodec.isEmptyJvmSettingValue(value)) {
            jvm.remove(key);
          } else {
            jvm.put(key, value);
          }
        },
        "launch",
        "jvm");
  }
}
