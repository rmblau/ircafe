package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns application diagnostics settings under {@code ircafe.ui.appDiagnostics}. */
public class RuntimeConfigAppDiagnosticsStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigAppDiagnosticsStore.class);

  private final RuntimeConfigYamlSection diagnosticsSection;

  public RuntimeConfigAppDiagnosticsStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.diagnosticsSection =
        RuntimeConfigYamlSection.ircafeUi(file, documentStore, log, "appDiagnostics");
  }

  public synchronized boolean readApplicationJfrEnabled(boolean defaultValue) {
    return readBooleanSetting(defaultValue, "ui.appDiagnostics.jfr.enabled", "jfr", "enabled");
  }

  public synchronized void rememberApplicationJfrEnabled(boolean enabled) {
    rememberSectionSetting("jfr", "enabled", enabled, false);
  }

  public synchronized boolean readAssertjSwingEnabled(boolean defaultValue) {
    return readAssertjSwingBoolean("enabled", defaultValue);
  }

  public synchronized boolean readAssertjSwingFreezeWatchdogEnabled(boolean defaultValue) {
    return readAssertjSwingBoolean("edtFreezeWatchdogEnabled", defaultValue);
  }

  public synchronized int readAssertjSwingFreezeThresholdMs(int defaultValue) {
    int fallback = clampAssertjFreezeThresholdMs(defaultValue);
    return readSetting(
            "ui.appDiagnostics.assertjSwing.edtFreezeThresholdMs",
            "assertjSwing",
            "edtFreezeThresholdMs")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigAppDiagnosticsStore::clampAssertjFreezeThresholdMs)
        .orElse(fallback);
  }

  public synchronized int readAssertjSwingWatchdogPollMs(int defaultValue) {
    int fallback = clampAssertjWatchdogPollMs(defaultValue);
    return readSetting(
            "ui.appDiagnostics.assertjSwing.edtWatchdogPollMs", "assertjSwing", "edtWatchdogPollMs")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigAppDiagnosticsStore::clampAssertjWatchdogPollMs)
        .orElse(fallback);
  }

  public synchronized int readAssertjSwingFallbackViolationReportMs(int defaultValue) {
    int fallback = clampAssertjFallbackViolationReportMs(defaultValue);
    return readSetting(
            "ui.appDiagnostics.assertjSwing.edtFallbackViolationReportMs",
            "assertjSwing",
            "edtFallbackViolationReportMs")
        .flatMap(RuntimeConfigYamlSupport::asInt)
        .map(RuntimeConfigAppDiagnosticsStore::clampAssertjFallbackViolationReportMs)
        .orElse(fallback);
  }

  public synchronized boolean readAssertjSwingIssuePlaySound(boolean defaultValue) {
    return readAssertjSwingBoolean("onIssuePlaySound", defaultValue);
  }

  public synchronized boolean readAssertjSwingIssueShowNotification(boolean defaultValue) {
    return readAssertjSwingBoolean("onIssueShowNotification", defaultValue);
  }

  public synchronized boolean readJhiccupEnabled(boolean defaultValue) {
    return readBooleanSetting(
        defaultValue, "ui.appDiagnostics.jhiccup.enabled", "jhiccup", "enabled");
  }

  public synchronized String readJhiccupJarPath(String defaultValue) {
    String fallback = Objects.toString(defaultValue, "").trim();
    String raw =
        readSetting("ui.appDiagnostics.jhiccup.jarPath", "jhiccup", "jarPath")
            .map(value -> Objects.toString(value, "").trim())
            .orElse("");
    return raw.isEmpty() ? fallback : raw;
  }

  public synchronized String readJhiccupJavaCommand(String defaultValue) {
    String fallback = Objects.toString(defaultValue, "").trim();
    if (fallback.isEmpty()) fallback = "java";
    String raw =
        readSetting("ui.appDiagnostics.jhiccup.javaCommand", "jhiccup", "javaCommand")
            .map(value -> Objects.toString(value, "").trim())
            .orElse("");
    return raw.isEmpty() ? fallback : raw;
  }

  public synchronized List<String> readJhiccupArgs(List<String> defaultValue) {
    List<String> fallback = RuntimeConfigYamlSupport.sanitizeStringList(defaultValue);
    Object argsObj = readSetting("ui.appDiagnostics.jhiccup.args", "jhiccup", "args").orElse(null);
    if (!(argsObj instanceof List<?>)) return fallback;
    return RuntimeConfigYamlSupport.sanitizeStringList(argsObj);
  }

  public synchronized void rememberAssertjSwingEnabled(boolean enabled) {
    rememberAssertjSwingSetting("enabled", enabled);
  }

  public synchronized void rememberAssertjSwingFreezeWatchdogEnabled(boolean enabled) {
    rememberAssertjSwingSetting("edtFreezeWatchdogEnabled", enabled);
  }

  public synchronized void rememberAssertjSwingFreezeThresholdMs(int ms) {
    rememberAssertjSwingSetting("edtFreezeThresholdMs", clampAssertjFreezeThresholdMs(ms));
  }

  public synchronized void rememberAssertjSwingWatchdogPollMs(int ms) {
    rememberAssertjSwingSetting("edtWatchdogPollMs", clampAssertjWatchdogPollMs(ms));
  }

  public synchronized void rememberAssertjSwingFallbackViolationReportMs(int ms) {
    rememberAssertjSwingSetting(
        "edtFallbackViolationReportMs", clampAssertjFallbackViolationReportMs(ms));
  }

  public synchronized void rememberAssertjSwingIssuePlaySound(boolean enabled) {
    rememberAssertjSwingSetting("onIssuePlaySound", enabled);
  }

  public synchronized void rememberAssertjSwingIssueShowNotification(boolean enabled) {
    rememberAssertjSwingSetting("onIssueShowNotification", enabled);
  }

  public synchronized void rememberJhiccupEnabled(boolean enabled) {
    rememberSectionSetting("jhiccup", "enabled", enabled, false);
  }

  public synchronized void rememberJhiccupJarPath(String jarPath) {
    rememberSectionSetting("jhiccup", "jarPath", Objects.toString(jarPath, "").trim(), true);
  }

  public synchronized void rememberJhiccupJavaCommand(String javaCommand) {
    rememberSectionSetting(
        "jhiccup", "javaCommand", Objects.toString(javaCommand, "").trim(), true);
  }

  public synchronized void rememberJhiccupArgs(List<String> args) {
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
    return diagnosticsSection.readValue(description, path);
  }

  private void rememberAssertjSwingSetting(String key, Object value) {
    rememberSectionSetting("assertjSwing", key, value, false);
  }

  private void rememberSectionSetting(
      String section, String key, Object value, boolean removeEmpty) {
    diagnosticsSection.mutateMap(
        "ui.appDiagnostics." + section + "." + key,
        settings -> {
          if (removeEmpty && RuntimeConfigYamlSupport.isEmptySettingValue(value)) {
            settings.remove(key);
          } else {
            settings.put(key, value);
          }
        },
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
