package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.List;
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
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigAppDiagnosticsStore::clampAssertjFreezeThresholdMs)
        .orElse(fallback);
  }

  synchronized int readAssertjSwingWatchdogPollMs(int defaultValue) {
    int fallback = clampAssertjWatchdogPollMs(defaultValue);
    return readSetting(
            "ui.appDiagnostics.assertjSwing.edtWatchdogPollMs", "assertjSwing", "edtWatchdogPollMs")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigAppDiagnosticsStore::clampAssertjWatchdogPollMs)
        .orElse(fallback);
  }

  synchronized int readAssertjSwingFallbackViolationReportMs(int defaultValue) {
    int fallback = clampAssertjFallbackViolationReportMs(defaultValue);
    return readSetting(
            "ui.appDiagnostics.assertjSwing.edtFallbackViolationReportMs",
            "assertjSwing",
            "edtFallbackViolationReportMs")
        .flatMap(RuntimeConfigYamlSupport::asInt)
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
    List<String> fallback = RuntimeConfigYamlSupport.sanitizeStringList(defaultValue);
    Object argsObj = readSetting("ui.appDiagnostics.jhiccup.args", "jhiccup", "args").orElse(null);
    if (!(argsObj instanceof List<?>)) return fallback;
    return RuntimeConfigYamlSupport.sanitizeStringList(argsObj);
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
    rememberSectionSetting(
        "jhiccup", "args", RuntimeConfigYamlSupport.sanitizeStringList(args), true);
  }

  private boolean readAssertjSwingBoolean(String key, boolean defaultValue) {
    return readBooleanSetting(
        defaultValue, "ui.appDiagnostics.assertjSwing." + key, "assertjSwing", key);
  }

  private boolean readBooleanSetting(boolean defaultValue, String description, String... path) {
    return readSetting(description, path)
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  private Optional<Object> readSetting(String description, String... path) {
    String[] fullPath = new String[path.length + 3];
    fullPath[0] = "ircafe";
    fullPath[1] = "ui";
    fullPath[2] = "appDiagnostics";
    System.arraycopy(path, 0, fullPath, 3, path.length);
    return RuntimeConfigYamlSupport.readValue(file, documentStore, log, description, fullPath);
  }

  private void rememberAssertjSwingSetting(String key, Object value) {
    rememberSectionSetting("assertjSwing", key, value, false);
  }

  private void rememberSectionSetting(
      String section, String key, Object value, boolean removeEmpty) {
    RuntimeConfigYamlSupport.mutateMap(
        file,
        documentStore,
        log,
        "ui.appDiagnostics." + section + "." + key,
        settings -> {
          if (removeEmpty && RuntimeConfigYamlSupport.isEmptySettingValue(value)) {
            settings.remove(key);
          } else {
            settings.put(key, value);
          }
        },
        "ircafe",
        "ui",
        "appDiagnostics",
        section);
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

}
