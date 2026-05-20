package cafe.woden.ircclient.ui.settings.diagnostics;

import cafe.woden.ircclient.config.RuntimeConfigStore;
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
  private DiagnosticsControlsSupport() {}

  public static DiagnosticsControls buildControls(RuntimeConfigStore runtimeConfig) {
    JCheckBox assertjSwingEnabled = new JCheckBox("Enable AssertJ Swing diagnostics");
    assertjSwingEnabled.setSelected(runtimeConfig.readAppDiagnosticsAssertjSwingEnabled(true));
    assertjSwingEnabled.setToolTipText(
        "Installs AssertJ Swing (or a fallback detector) for EDT thread violation checks.");

    JCheckBox assertjSwingFreezeWatchdogEnabled = new JCheckBox("Enable EDT freeze watchdog");
    assertjSwingFreezeWatchdogEnabled.setSelected(
        runtimeConfig.readAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(true));
    assertjSwingFreezeWatchdogEnabled.setToolTipText(
        "Reports prolonged Event Dispatch Thread stalls into Application -> AssertJ Swing.");

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

    JCheckBox assertjSwingOnIssuePlaySound = new JCheckBox("Play sound when an issue is detected");
    assertjSwingOnIssuePlaySound.setSelected(
        runtimeConfig.readAppDiagnosticsAssertjSwingIssuePlaySound(false));
    assertjSwingOnIssuePlaySound.setToolTipText(
        "Uses the configured tray notification sound when EDT freeze/violation issues are detected.");

    JCheckBox assertjSwingOnIssueShowNotification =
        new JCheckBox("Show desktop notification when an issue is detected");
    assertjSwingOnIssueShowNotification.setSelected(
        runtimeConfig.readAppDiagnosticsAssertjSwingIssueShowNotification(false));
    assertjSwingOnIssueShowNotification.setToolTipText(
        "Uses the tray notification pipeline; desktop-notification delivery still follows tray settings.");

    JCheckBox jhiccupEnabled = new JCheckBox("Enable jHiccup process integration");
    jhiccupEnabled.setSelected(runtimeConfig.readAppDiagnosticsJhiccupEnabled(false));
    jhiccupEnabled.setToolTipText(
        "Runs an external jHiccup process and mirrors output into Application -> jHiccup.");

    JTextField jhiccupJarPath = new JTextField(runtimeConfig.readAppDiagnosticsJhiccupJarPath(""));
    jhiccupJarPath.setToolTipText(
        "Path to jHiccup jar file. Relative paths are resolved from the runtime-config directory.");

    JTextField jhiccupJavaCommand =
        new JTextField(runtimeConfig.readAppDiagnosticsJhiccupJavaCommand("java"));
    jhiccupJavaCommand.setToolTipText("Java launcher command used to start jHiccup.");

    JTextArea jhiccupArgs = PreferencesUiSupport.textArea(5, 40, false);
    jhiccupArgs.setText(String.join("\n", runtimeConfig.readAppDiagnosticsJhiccupArgs(List.of())));
    jhiccupArgs.setToolTipText("One argument per line.");

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
      RuntimeConfigStore runtimeConfig, DiagnosticsSettings settings) {
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
      RuntimeConfigStore runtimeConfig, DiagnosticsSettings settings) {
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
