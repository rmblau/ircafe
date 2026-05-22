package cafe.woden.ircclient.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
        .flatMap(RuntimeConfigLaunchJvmStore::asInt)
        .map(RuntimeConfigLaunchJvmStore::clampHeapMiB)
        .orElse(fallback);
  }

  synchronized int readXmxMiB(int defaultValue) {
    int fallback = clampHeapMiB(defaultValue);
    return readValue("launch.jvm.xmxMiB", "xmxMiB")
        .flatMap(RuntimeConfigLaunchJvmStore::asInt)
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
    List<String> fallback = sanitizeArgs(defaultValue);
    Object argsObj = readValue("launch.jvm.args", "args").orElse(null);
    if (!(argsObj instanceof List<?> raw)) return fallback;
    return sanitizeArgs(raw);
  }

  synchronized void rememberJavaCommand(String javaCommand) {
    try {
      if (file.toString().isBlank()) return;

      String cmd = Objects.toString(javaCommand, "").trim();
      if (cmd.isEmpty() || cmd.equalsIgnoreCase("java")) cmd = "";

      Map<String, Object> doc = documentStore.loadOrEmpty();
      LaunchJvmWritePath path = getOrCreateWritePath(doc);
      Map<String, Object> jvm = path.jvm();

      if (cmd.isEmpty()) {
        jvm.remove("javaCommand");
      } else {
        jvm.put("javaCommand", cmd);
      }

      cleanup(path);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist launch.jvm.javaCommand to '{}'", file, e);
    }
  }

  synchronized void rememberXmsMiB(int xmsMiB) {
    try {
      if (file.toString().isBlank()) return;

      int v = clampHeapMiB(xmsMiB);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      LaunchJvmWritePath path = getOrCreateWritePath(doc);
      Map<String, Object> jvm = path.jvm();

      if (v <= 0) {
        jvm.remove("xmsMiB");
      } else {
        jvm.put("xmsMiB", v);
      }

      cleanup(path);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist launch.jvm.xmsMiB to '{}'", file, e);
    }
  }

  synchronized void rememberXmxMiB(int xmxMiB) {
    try {
      if (file.toString().isBlank()) return;

      int v = clampHeapMiB(xmxMiB);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      LaunchJvmWritePath path = getOrCreateWritePath(doc);
      Map<String, Object> jvm = path.jvm();

      if (v <= 0) {
        jvm.remove("xmxMiB");
      } else {
        jvm.put("xmxMiB", v);
      }

      cleanup(path);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist launch.jvm.xmxMiB to '{}'", file, e);
    }
  }

  synchronized void rememberGc(String gc) {
    try {
      if (file.toString().isBlank()) return;

      String normalized = normalizeGc(gc);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      LaunchJvmWritePath path = getOrCreateWritePath(doc);
      Map<String, Object> jvm = path.jvm();

      if (normalized.isEmpty()) {
        jvm.remove("gc");
      } else {
        jvm.put("gc", normalized);
      }

      cleanup(path);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist launch.jvm.gc to '{}'", file, e);
    }
  }

  synchronized void rememberArgs(List<String> args) {
    try {
      if (file.toString().isBlank()) return;

      List<String> sanitized = sanitizeArgs(args);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      LaunchJvmWritePath path = getOrCreateWritePath(doc);
      Map<String, Object> jvm = path.jvm();

      if (sanitized.isEmpty()) {
        jvm.remove("args");
      } else {
        jvm.put("args", sanitized);
      }

      cleanup(path);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist launch.jvm.args to '{}'", file, e);
    }
  }

  private Optional<Object> readValue(String description, String key) {
    try {
      if (file.toString().isBlank()) return Optional.empty();
      if (!Files.exists(file)) return Optional.empty();

      Map<String, Object> doc = documentStore.load();
      return RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "launch", "jvm", key);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read {} from '{}'", description, file, e);
      return Optional.empty();
    }
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

  private static List<String> sanitizeArgs(List<?> args) {
    if (args == null || args.isEmpty()) return List.of();
    List<String> out = new ArrayList<>();
    for (Object arg : args) {
      String t = Objects.toString(arg, "").trim();
      if (!t.isEmpty()) out.add(t);
    }
    return List.copyOf(out);
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

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }

  private static Optional<Integer> asInt(Object value) {
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

  private record LaunchJvmWritePath(
      Map<String, Object> ircafe, Map<String, Object> launch, Map<String, Object> jvm) {}
}
