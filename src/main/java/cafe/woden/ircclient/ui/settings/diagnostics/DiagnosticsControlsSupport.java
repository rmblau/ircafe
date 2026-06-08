package cafe.woden.ircclient.ui.settings.diagnostics;

import cafe.woden.ircclient.config.api.DiagnosticsRuntimeConfigPort;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import java.util.List;
import java.util.Objects;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class DiagnosticsControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private DiagnosticsControlsSupport() {}

  public static DiagnosticsControls buildControls(DiagnosticsRuntimeConfigPort runtimeConfig) {
    JCheckBox assertjSwingEnabled =
        new JCheckBox(MESSAGES.text("preferences.diagnostics.assertj.enabled"));
    assertjSwingEnabled.setSelected(runtimeConfig.readAppDiagnosticsAssertjSwingEnabled(true));
    assertjSwingEnabled.setToolTipText(
        MESSAGES.text("preferences.diagnostics.assertj.enabled.tooltip"));

    JCheckBox assertjSwingFreezeWatchdogEnabled =
        new JCheckBox(MESSAGES.text("preferences.diagnostics.assertj.freezeWatchdog"));
    assertjSwingFreezeWatchdogEnabled.setSelected(
        runtimeConfig.readAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(true));
    assertjSwingFreezeWatchdogEnabled.setToolTipText(
        MESSAGES.text("preferences.diagnostics.assertj.freezeWatchdog.tooltip"));

    int freezeThresholdMs = runtimeConfig.readAppDiagnosticsAssertjSwingFreezeThresholdMs(2500);
    JSpinner assertjSwingFreezeThresholdMs =
        PreferencesUiSupport.numberSpinner(freezeThresholdMs, 500, 120_000, 100);

    int watchdogPollMs = runtimeConfig.readAppDiagnosticsAssertjSwingWatchdogPollMs(500);
    JSpinner assertjSwingWatchdogPollMs =
        PreferencesUiSupport.numberSpinner(watchdogPollMs, 100, 10_000, 100);

    int fallbackViolationReportMs =
        runtimeConfig.readAppDiagnosticsAssertjSwingFallbackViolationReportMs(5000);
    JSpinner assertjSwingFallbackViolationReportMs =
        PreferencesUiSupport.numberSpinner(fallbackViolationReportMs, 250, 120_000, 250);

    JCheckBox assertjSwingOnIssuePlaySound =
        new JCheckBox(MESSAGES.text("preferences.diagnostics.assertj.issueSound"));
    assertjSwingOnIssuePlaySound.setSelected(
        runtimeConfig.readAppDiagnosticsAssertjSwingIssuePlaySound(false));
    assertjSwingOnIssuePlaySound.setToolTipText(
        MESSAGES.text("preferences.diagnostics.assertj.issueSound.tooltip"));

    JCheckBox assertjSwingOnIssueShowNotification =
        new JCheckBox(MESSAGES.text("preferences.diagnostics.assertj.issueNotification"));
    assertjSwingOnIssueShowNotification.setSelected(
        runtimeConfig.readAppDiagnosticsAssertjSwingIssueShowNotification(false));
    assertjSwingOnIssueShowNotification.setToolTipText(
        MESSAGES.text("preferences.diagnostics.assertj.issueNotification.tooltip"));

    JCheckBox jhiccupEnabled =
        new JCheckBox(MESSAGES.text("preferences.diagnostics.jhiccup.enabled"));
    jhiccupEnabled.setSelected(runtimeConfig.readAppDiagnosticsJhiccupEnabled(false));
    jhiccupEnabled.setToolTipText(MESSAGES.text("preferences.diagnostics.jhiccup.enabled.tooltip"));

    JTextField jhiccupJarPath = new JTextField(runtimeConfig.readAppDiagnosticsJhiccupJarPath(""));
    jhiccupJarPath.setToolTipText(MESSAGES.text("preferences.diagnostics.jhiccup.jarPath.tooltip"));

    JTextField jhiccupJavaCommand =
        new JTextField(runtimeConfig.readAppDiagnosticsJhiccupJavaCommand("java"));
    jhiccupJavaCommand.setToolTipText(
        MESSAGES.text("preferences.diagnostics.jhiccup.javaCommand.tooltip"));

    JTextArea jhiccupArgs = PreferencesUiSupport.textArea(5, 40, false);
    jhiccupArgs.setText(String.join("\n", runtimeConfig.readAppDiagnosticsJhiccupArgs(List.of())));
    jhiccupArgs.setToolTipText(MESSAGES.text("preferences.diagnostics.jhiccup.args.tooltip"));

    Runnable syncEnabledState =
        () -> {
          boolean assertjEnabled = assertjSwingEnabled.isSelected();
          assertjSwingFreezeWatchdogEnabled.setEnabled(assertjEnabled);
          boolean watchdogEnabled =
              assertjEnabled && assertjSwingFreezeWatchdogEnabled.isSelected();
          assertjSwingFreezeThresholdMs.setEnabled(watchdogEnabled);
          assertjSwingWatchdogPollMs.setEnabled(watchdogEnabled);
          assertjSwingFallbackViolationReportMs.setEnabled(assertjEnabled);
          assertjSwingOnIssuePlaySound.setEnabled(assertjEnabled);
          assertjSwingOnIssueShowNotification.setEnabled(assertjEnabled);
        };
    assertjSwingEnabled.addActionListener(e -> syncEnabledState.run());
    assertjSwingFreezeWatchdogEnabled.addActionListener(e -> syncEnabledState.run());
    syncEnabledState.run();

    return new DiagnosticsControls(
        assertjSwingEnabled,
        assertjSwingFreezeWatchdogEnabled,
        assertjSwingFreezeThresholdMs,
        assertjSwingWatchdogPollMs,
        assertjSwingFallbackViolationReportMs,
        assertjSwingOnIssuePlaySound,
        assertjSwingOnIssueShowNotification,
        jhiccupEnabled,
        jhiccupJarPath,
        jhiccupJavaCommand,
        jhiccupArgs);
  }

  public static DiagnosticsSettings readSettings(DiagnosticsControls controls) {
    String jhiccupJavaCommandRaw = PreferencesUiSupport.trimmedText(controls.jhiccupJavaCommand());
    String jhiccupJavaCommandEffective =
        jhiccupJavaCommandRaw.isEmpty() ? "java" : jhiccupJavaCommandRaw;

    return new DiagnosticsSettings(
        controls.assertjSwingEnabled().isSelected(),
        controls.assertjSwingFreezeWatchdogEnabled().isSelected(),
        SettingsRangeSupport.normalizeAssertjSwingFreezeThresholdMs(
            PreferencesUiSupport.spinnerInt(controls.assertjSwingFreezeThresholdMs())),
        SettingsRangeSupport.normalizeAssertjSwingWatchdogPollMs(
            PreferencesUiSupport.spinnerInt(controls.assertjSwingWatchdogPollMs())),
        SettingsRangeSupport.normalizeAssertjSwingFallbackViolationReportMs(
            PreferencesUiSupport.spinnerInt(controls.assertjSwingFallbackViolationReportMs())),
        controls.assertjSwingOnIssuePlaySound().isSelected(),
        controls.assertjSwingOnIssueShowNotification().isSelected(),
        controls.jhiccupEnabled().isSelected(),
        PreferencesUiSupport.trimmedText(controls.jhiccupJarPath()),
        jhiccupJavaCommandRaw,
        jhiccupJavaCommandEffective,
        SettingsValueSupport.trimmedLines(controls.jhiccupArgs().getText()));
  }

  public static boolean settingsChanged(
      DiagnosticsRuntimeConfigPort runtimeConfig, DiagnosticsSettings settings) {
    return runtimeConfig.readAppDiagnosticsAssertjSwingEnabled(true)
            != settings.assertjSwingEnabled()
        || runtimeConfig.readAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(true)
            != settings.assertjSwingFreezeWatchdogEnabled()
        || runtimeConfig.readAppDiagnosticsAssertjSwingFreezeThresholdMs(2500)
            != settings.assertjSwingFreezeThresholdMs()
        || runtimeConfig.readAppDiagnosticsAssertjSwingWatchdogPollMs(500)
            != settings.assertjSwingWatchdogPollMs()
        || runtimeConfig.readAppDiagnosticsAssertjSwingFallbackViolationReportMs(5000)
            != settings.assertjSwingFallbackViolationReportMs()
        || runtimeConfig.readAppDiagnosticsAssertjSwingIssuePlaySound(false)
            != settings.assertjSwingOnIssuePlaySound()
        || runtimeConfig.readAppDiagnosticsAssertjSwingIssueShowNotification(false)
            != settings.assertjSwingOnIssueShowNotification()
        || runtimeConfig.readAppDiagnosticsJhiccupEnabled(false) != settings.jhiccupEnabled()
        || !Objects.equals(
            runtimeConfig.readAppDiagnosticsJhiccupJarPath(""), settings.jhiccupJarPath())
        || !Objects.equals(
            runtimeConfig.readAppDiagnosticsJhiccupJavaCommand("java"),
            settings.jhiccupJavaCommandEffective())
        || !Objects.equals(
            runtimeConfig.readAppDiagnosticsJhiccupArgs(List.of()), settings.jhiccupArgs());
  }

  public static void rememberSettings(
      DiagnosticsRuntimeConfigPort runtimeConfig, DiagnosticsSettings settings) {
    runtimeConfig.rememberAppDiagnosticsAssertjSwingEnabled(settings.assertjSwingEnabled());
    runtimeConfig.rememberAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(
        settings.assertjSwingFreezeWatchdogEnabled());
    runtimeConfig.rememberAppDiagnosticsAssertjSwingFreezeThresholdMs(
        settings.assertjSwingFreezeThresholdMs());
    runtimeConfig.rememberAppDiagnosticsAssertjSwingWatchdogPollMs(
        settings.assertjSwingWatchdogPollMs());
    runtimeConfig.rememberAppDiagnosticsAssertjSwingFallbackViolationReportMs(
        settings.assertjSwingFallbackViolationReportMs());
    runtimeConfig.rememberAppDiagnosticsAssertjSwingIssuePlaySound(
        settings.assertjSwingOnIssuePlaySound());
    runtimeConfig.rememberAppDiagnosticsAssertjSwingIssueShowNotification(
        settings.assertjSwingOnIssueShowNotification());
    runtimeConfig.rememberAppDiagnosticsJhiccupEnabled(settings.jhiccupEnabled());
    runtimeConfig.rememberAppDiagnosticsJhiccupJarPath(settings.jhiccupJarPath());
    runtimeConfig.rememberAppDiagnosticsJhiccupJavaCommand(settings.jhiccupJavaCommandRaw());
    runtimeConfig.rememberAppDiagnosticsJhiccupArgs(settings.jhiccupArgs());
  }

  public record DiagnosticsSettings(
      boolean assertjSwingEnabled,
      boolean assertjSwingFreezeWatchdogEnabled,
      int assertjSwingFreezeThresholdMs,
      int assertjSwingWatchdogPollMs,
      int assertjSwingFallbackViolationReportMs,
      boolean assertjSwingOnIssuePlaySound,
      boolean assertjSwingOnIssueShowNotification,
      boolean jhiccupEnabled,
      String jhiccupJarPath,
      String jhiccupJavaCommandRaw,
      String jhiccupJavaCommandEffective,
      List<String> jhiccupArgs) {}
}
