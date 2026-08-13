package cafe.woden.ircclient.config.runtime.launch;

import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Pure normalization helpers for persisted child-JVM launch settings. */
final class RuntimeConfigLaunchJvmSettingsCodec {

  private RuntimeConfigLaunchJvmSettingsCodec() {}

  static String normalizeJavaCommandFallback(String defaultValue) {
    String fallback = normalizeString(defaultValue);
    return fallback.isEmpty() ? "java" : fallback;
  }

  static String normalizeJavaCommandReadValue(Object value) {
    return normalizeString(value);
  }

  static String normalizeJavaCommandSetting(String javaCommand) {
    String command = normalizeString(javaCommand);
    return command.isEmpty() || command.equalsIgnoreCase("java") ? "" : command;
  }

  static int normalizeHeapMiB(int value) {
    return Math.max(0, Math.min(262_144, value));
  }

  static String normalizeGc(Object raw) {
    String value = normalizeString(raw).toLowerCase(Locale.ROOT);
    return switch (value) {
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

  static List<String> normalizeArgs(Object args) {
    return RuntimeConfigYamlSupport.sanitizeStringList(args);
  }

  static boolean isEmptyJvmSettingValue(Object value) {
    if (value == null) return true;
    if (value instanceof String text) return text.isBlank();
    if (value instanceof Number number) return number.intValue() <= 0;
    if (value instanceof Collection<?> collection) return collection.isEmpty();
    return false;
  }

  private static String normalizeString(Object value) {
    return Objects.toString(value, "").trim();
  }
}
