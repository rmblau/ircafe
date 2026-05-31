package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMap;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns optional child-JVM launch settings under {@code ircafe.launch.jvm}. */
class RuntimeConfigLaunchJvmStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigLaunchJvmStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigLaunchJvmStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized String readJavaCommand(String defaultValue) {
    String fallback = Objects.toString(defaultValue, "").trim();
    if (fallback.isEmpty()) fallback = "java";
    String raw =
        readValue("launch.jvm.javaCommand", "javaCommand")
            .map(value -> Objects.toString(value, "").trim())
            .orElse("");
    return raw.isEmpty() ? fallback : raw;
  }

  synchronized int readXmsMiB(int defaultValue) {
    int fallback = clampHeapMiB(defaultValue);
    return readValue("launch.jvm.xmsMiB", "xmsMiB")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigLaunchJvmStore::clampHeapMiB)
        .orElse(fallback);
  }

  synchronized int readXmxMiB(int defaultValue) {
    int fallback = clampHeapMiB(defaultValue);
    return readValue("launch.jvm.xmxMiB", "xmxMiB")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigLaunchJvmStore::clampHeapMiB)
        .orElse(fallback);
  }

  synchronized String readGc(String defaultValue) {
    String fallback = normalizeGc(defaultValue);
    return readValue("launch.jvm.gc", "gc")
        .map(RuntimeConfigLaunchJvmStore::normalizeGc)
        .orElse(fallback);
  }

  synchronized List<String> readArgs(List<String> defaultValue) {
    List<String> fallback = RuntimeConfigYamlSupport.sanitizeStringList(defaultValue);
    Object argsObj = readValue("launch.jvm.args", "args").orElse(null);
    if (!(argsObj instanceof List<?> raw)) return fallback;
    return RuntimeConfigYamlSupport.sanitizeStringList(raw);
  }

  synchronized void rememberJavaCommand(String javaCommand) {
    String cmd = Objects.toString(javaCommand, "").trim();
    if (cmd.isEmpty() || cmd.equalsIgnoreCase("java")) cmd = "";
    rememberJvmSetting("launch.jvm.javaCommand", "javaCommand", cmd);
  }

  synchronized void rememberXmsMiB(int xmsMiB) {
    rememberJvmSetting("launch.jvm.xmsMiB", "xmsMiB", clampHeapMiB(xmsMiB));
  }

  synchronized void rememberXmxMiB(int xmxMiB) {
    rememberJvmSetting("launch.jvm.xmxMiB", "xmxMiB", clampHeapMiB(xmxMiB));
  }

  synchronized void rememberGc(String gc) {
    rememberJvmSetting("launch.jvm.gc", "gc", normalizeGc(gc));
  }

  synchronized void rememberArgs(List<String> args) {
    rememberJvmSetting(
        "launch.jvm.args", "args", RuntimeConfigYamlSupport.sanitizeStringList(args));
  }

  private Optional<Object> readValue(String description, String key) {
    return RuntimeConfigYamlSupport.readExistingValue(
        file, documentStore, log, description, "ircafe", "launch", "jvm", key);
  }

  private void rememberJvmSetting(String description, String key, Object value) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      LaunchJvmWritePath path = getOrCreateWritePath(doc);
      Map<String, Object> jvm = path.jvm();

      if (isEmptyJvmSettingValue(value)) {
        jvm.remove(key);
      } else {
        jvm.put(key, value);
      }

      cleanup(path);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} to '{}'", description, file, e);
    }
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

  private static void cleanup(
      Map<String, Object> ircafe, Map<String, Object> launch, Map<String, Object> jvm) {
    if (jvm.isEmpty()) {
      launch.remove("jvm");
    }
    if (launch.isEmpty()) {
      ircafe.remove("launch");
    }
  }

  private static void cleanup(LaunchJvmWritePath path) {
    cleanup(path.ircafe(), path.launch(), path.jvm());
  }

  private static LaunchJvmWritePath getOrCreateWritePath(Map<String, Object> doc) {
    Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
    Map<String, Object> launch = getOrCreateMap(ircafe, "launch");
    Map<String, Object> jvm = getOrCreateMap(launch, "jvm");
    return new LaunchJvmWritePath(ircafe, launch, jvm);
  }

  private record LaunchJvmWritePath(
      Map<String, Object> ircafe, Map<String, Object> launch, Map<String, Object> jvm) {}
}
