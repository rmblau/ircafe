package cafe.woden.ircclient.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns application diagnostics settings under {@code ircafe.ui.appDiagnostics}. */
class RuntimeConfigAppDiagnosticsStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigAppDiagnosticsStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigAppDiagnosticsStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized boolean readApplicationJfrEnabled(boolean defaultValue) {
    return readBooleanSetting(defaultValue, "ui.appDiagnostics.jfr.enabled", "jfr", "enabled");
  }

  synchronized void rememberApplicationJfrEnabled(boolean enabled) {
    rememberSectionSetting("jfr", "enabled", enabled, false);
  }

  synchronized boolean readAssertjSwingEnabled(boolean defaultValue) {
    return readAssertjSwingBoolean("enabled", defaultValue);
  }

  synchronized boolean readAssertjSwingFreezeWatchdogEnabled(boolean defaultValue) {
    return readAssertjSwingBoolean("edtFreezeWatchdogEnabled", defaultValue);
  }

  synchronized int readAssertjSwingFreezeThresholdMs(int defaultValue) {
    int fallback = clampAssertjFreezeThresholdMs(defaultValue);
    return readSetting(
            "ui.appDiagnostics.assertjSwing.edtFreezeThresholdMs",
            "assertjSwing",
            "edtFreezeThresholdMs")
        .flatMap(RuntimeConfigAppDiagnosticsStore::asInt)
        .map(RuntimeConfigAppDiagnosticsStore::clampAssertjFreezeThresholdMs)
        .orElse(fallback);
  }

  synchronized int readAssertjSwingWatchdogPollMs(int defaultValue) {
    int fallback = clampAssertjWatchdogPollMs(defaultValue);
    return readSetting(
            "ui.appDiagnostics.assertjSwing.edtWatchdogPollMs", "assertjSwing", "edtWatchdogPollMs")
        .flatMap(RuntimeConfigAppDiagnosticsStore::asInt)
        .map(RuntimeConfigAppDiagnosticsStore::clampAssertjWatchdogPollMs)
        .orElse(fallback);
  }

  synchronized int readAssertjSwingFallbackViolationReportMs(int defaultValue) {
    int fallback = clampAssertjFallbackViolationReportMs(defaultValue);
    return readSetting(
            "ui.appDiagnostics.assertjSwing.edtFallbackViolationReportMs",
            "assertjSwing",
            "edtFallbackViolationReportMs")
        .flatMap(RuntimeConfigAppDiagnosticsStore::asInt)
        .map(RuntimeConfigAppDiagnosticsStore::clampAssertjFallbackViolationReportMs)
        .orElse(fallback);
  }

  synchronized boolean readAssertjSwingIssuePlaySound(boolean defaultValue) {
    return readAssertjSwingBoolean("onIssuePlaySound", defaultValue);
  }

  synchronized boolean readAssertjSwingIssueShowNotification(boolean defaultValue) {
    return readAssertjSwingBoolean("onIssueShowNotification", defaultValue);
  }

  synchronized boolean readJhiccupEnabled(boolean defaultValue) {
    return readBooleanSetting(
        defaultValue, "ui.appDiagnostics.jhiccup.enabled", "jhiccup", "enabled");
  }

  synchronized String readJhiccupJarPath(String defaultValue) {
    String fallback = Objects.toString(defaultValue, "").trim();
    String raw =
        readSetting("ui.appDiagnostics.jhiccup.jarPath", "jhiccup", "jarPath")
            .map(value -> Objects.toString(value, "").trim())
            .orElse("");
    return raw.isEmpty() ? fallback : raw;
  }

  synchronized String readJhiccupJavaCommand(String defaultValue) {
    String fallback = Objects.toString(defaultValue, "").trim();
    if (fallback.isEmpty()) fallback = "java";
    String raw =
        readSetting("ui.appDiagnostics.jhiccup.javaCommand", "jhiccup", "javaCommand")
            .map(value -> Objects.toString(value, "").trim())
            .orElse("");
    return raw.isEmpty() ? fallback : raw;
  }

  synchronized List<String> readJhiccupArgs(List<String> defaultValue) {
    List<String> fallback = sanitizeArgs(defaultValue);
    Object argsObj = readSetting("ui.appDiagnostics.jhiccup.args", "jhiccup", "args").orElse(null);
    if (!(argsObj instanceof List<?> raw)) return fallback;
    return sanitizeArgs(raw);
  }

  synchronized void rememberAssertjSwingEnabled(boolean enabled) {
    rememberAssertjSwingSetting("enabled", enabled);
  }

  synchronized void rememberAssertjSwingFreezeWatchdogEnabled(boolean enabled) {
    rememberAssertjSwingSetting("edtFreezeWatchdogEnabled", enabled);
  }

  synchronized void rememberAssertjSwingFreezeThresholdMs(int ms) {
    rememberAssertjSwingSetting("edtFreezeThresholdMs", clampAssertjFreezeThresholdMs(ms));
  }

  synchronized void rememberAssertjSwingWatchdogPollMs(int ms) {
    rememberAssertjSwingSetting("edtWatchdogPollMs", clampAssertjWatchdogPollMs(ms));
  }

  synchronized void rememberAssertjSwingFallbackViolationReportMs(int ms) {
    rememberAssertjSwingSetting(
        "edtFallbackViolationReportMs", clampAssertjFallbackViolationReportMs(ms));
  }

  synchronized void rememberAssertjSwingIssuePlaySound(boolean enabled) {
    rememberAssertjSwingSetting("onIssuePlaySound", enabled);
  }

  synchronized void rememberAssertjSwingIssueShowNotification(boolean enabled) {
    rememberAssertjSwingSetting("onIssueShowNotification", enabled);
  }

  synchronized void rememberJhiccupEnabled(boolean enabled) {
    rememberSectionSetting("jhiccup", "enabled", enabled, false);
  }

  synchronized void rememberJhiccupJarPath(String jarPath) {
    rememberSectionSetting("jhiccup", "jarPath", Objects.toString(jarPath, "").trim(), true);
  }

  synchronized void rememberJhiccupJavaCommand(String javaCommand) {
    rememberSectionSetting(
        "jhiccup", "javaCommand", Objects.toString(javaCommand, "").trim(), true);
  }

  synchronized void rememberJhiccupArgs(List<String> args) {
    rememberSectionSetting("jhiccup", "args", sanitizeArgs(args), true);
  }

  private boolean readAssertjSwingBoolean(String key, boolean defaultValue) {
    return readBooleanSetting(
        defaultValue, "ui.appDiagnostics.assertjSwing." + key, "assertjSwing", key);
  }

  private boolean readBooleanSetting(boolean defaultValue, String description, String... path) {
    return readSetting(description, path)
        .flatMap(RuntimeConfigAppDiagnosticsStore::asBoolean)
        .orElse(defaultValue);
  }

  private Optional<Object> readSetting(String description, String... path) {
    try {
      if (file.toString().isBlank()) return Optional.empty();

      Map<String, Object> doc = documentStore.loadOrEmpty();
      String[] fullPath = new String[path.length + 3];
      fullPath[0] = "ircafe";
      fullPath[1] = "ui";
      fullPath[2] = "appDiagnostics";
      System.arraycopy(path, 0, fullPath, 3, path.length);
      return RuntimeConfigDocumentPathReader.readValue(doc, fullPath);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read {} from '{}'", description, file, e);
      return Optional.empty();
    }
  }

  private void rememberAssertjSwingSetting(String key, Object value) {
    rememberSectionSetting("assertjSwing", key, value, false);
  }

  private void rememberSectionSetting(
      String section, String key, Object value, boolean removeEmpty) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> settings =
          getOrCreateMapPath(doc, "ircafe", "ui", "appDiagnostics", section);

      if (removeEmpty && isEmptySettingValue(value)) {
        settings.remove(key);
      } else {
        settings.put(key, value);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ui.appDiagnostics.{}.{} to '{}'", section, key, file, e);
    }
  }

  private static int clampAssertjFreezeThresholdMs(int value) {
    if (value < 500) return 500;
    if (value > 120_000) return 120_000;
    return value;
  }

  private static int clampAssertjWatchdogPollMs(int value) {
    if (value < 100) return 100;
    if (value > 10_000) return 10_000;
    return value;
  }

  private static int clampAssertjFallbackViolationReportMs(int value) {
    if (value < 250) return 250;
    if (value > 120_000) return 120_000;
    return value;
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

  private static boolean isEmptySettingValue(Object value) {
    if (value == null) return true;
    if (value instanceof CharSequence text) return text.toString().isBlank();
    if (value instanceof Collection<?> collection) return collection.isEmpty();
    return false;
  }

  private static Map<String, Object> getOrCreateMapPath(Map<String, Object> root, String... path) {
    Map<String, Object> current = root;
    for (String segment : path) {
      current = getOrCreateMap(current, segment);
    }
    return current;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }

  private static Optional<Boolean> asBoolean(Object value) {
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
}
