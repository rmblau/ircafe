package cafe.woden.ircclient.config.runtime.launch;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
    String fallback = Objects.toString(defaultValue, "").trim();
    if (fallback.isEmpty()) fallback = "java";
    String raw =
        readValue("launch.jvm.javaCommand", "javaCommand")
            .map(value -> Objects.toString(value, "").trim())
            .orElse("");
    return raw.isEmpty() ? fallback : raw;
  }

  public synchronized int readXmsMiB(int defaultValue) {
    int fallback = clampHeapMiB(defaultValue);
    return readValue("launch.jvm.xmsMiB", "xmsMiB")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigLaunchJvmStore::clampHeapMiB)
        .orElse(fallback);
  }

  public synchronized int readXmxMiB(int defaultValue) {
    int fallback = clampHeapMiB(defaultValue);
    return readValue("launch.jvm.xmxMiB", "xmxMiB")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigLaunchJvmStore::clampHeapMiB)
        .orElse(fallback);
  }

  public synchronized String readGc(String defaultValue) {
    String fallback = normalizeGc(defaultValue);
    return readValue("launch.jvm.gc", "gc")
        .map(RuntimeConfigLaunchJvmStore::normalizeGc)
        .orElse(fallback);
  }

  public synchronized List<String> readArgs(List<String> defaultValue) {
    List<String> fallback = RuntimeConfigYamlSupport.sanitizeStringList(defaultValue);
    Object argsObj = readValue("launch.jvm.args", "args").orElse(null);
    if (!(argsObj instanceof List<?> raw)) return fallback;
    return RuntimeConfigYamlSupport.sanitizeStringList(raw);
  }

  public synchronized void rememberJavaCommand(String javaCommand) {
    String cmd = Objects.toString(javaCommand, "").trim();
    if (cmd.isEmpty() || cmd.equalsIgnoreCase("java")) cmd = "";
    rememberJvmSetting("launch.jvm.javaCommand", "javaCommand", cmd);
  }

  public synchronized void rememberXmsMiB(int xmsMiB) {
    rememberJvmSetting("launch.jvm.xmsMiB", "xmsMiB", clampHeapMiB(xmsMiB));
  }

  public synchronized void rememberXmxMiB(int xmxMiB) {
    rememberJvmSetting("launch.jvm.xmxMiB", "xmxMiB", clampHeapMiB(xmxMiB));
  }

  public synchronized void rememberGc(String gc) {
    rememberJvmSetting("launch.jvm.gc", "gc", normalizeGc(gc));
  }

  public synchronized void rememberArgs(List<String> args) {
    rememberJvmSetting(
        "launch.jvm.args", "args", RuntimeConfigYamlSupport.sanitizeStringList(args));
  }

  private Optional<Object> readValue(String description, String key) {
    return ircafeSection.readExistingValue(description, "launch", "jvm", key);
  }

  private void rememberJvmSetting(String description, String key, Object value) {
    ircafeSection.mutateMapAndRemoveIfEmpty(
        description,
        jvm -> {
          if (isEmptyJvmSettingValue(value)) {
            jvm.remove(key);
          } else {
            jvm.put(key, value);
          }
        },
        "launch",
        "jvm");
  }

  private static boolean isEmptyJvmSettingValue(Object value) {
    if (value == null) return true;
    if (value instanceof String s) return s.isBlank();
    if (value instanceof Number n) return n.intValue() <= 0;
    if (value instanceof Collection<?> c) return c.isEmpty();
    return false;
  }

  private static int clampHeapMiB(int value) {
    if (value < 0) return 0;
    if (value > 262_144) return 262_144;
    return value;
  }

  private static String normalizeGc(Object raw) {
    String v = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    return switch (v) {
      case "", "default", "auto", "none" -> "";
      case "g1", "g1gc", "useg1gc", "useg1" -> "g1";
      case "z", "zgc", "usezgc", "usez" -> "zgc";
      case "shenandoah", "shenandoahgc", "useshenandoahgc", "useshenandoah" -> "shenandoah";
      case "parallel", "parallelgc", "useparallelgc", "useparallel" -> "parallel";
      case "serial", "serialgc", "useserialgc", "useserial" -> "serial";
      case "epsilon", "epsilongc", "useepsilongc", "useepsilon" -> "epsilon";
      default -> "";
    };
  }

}
