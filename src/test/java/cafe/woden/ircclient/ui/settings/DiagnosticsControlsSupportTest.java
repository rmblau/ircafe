package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DiagnosticsControlsSupportTest {

  @Test
  void readSettingsTrimsTextDefaultsBlankJavaCommandAndTrimsArgs() {
    DiagnosticsControls controls =
        controls(
            true,
            false,
            2500,
            500,
            5000,
            true,
            false,
            true,
            " /tmp/jhiccup.jar ",
            " ",
            " -d 1000 \n\n -c ");

    DiagnosticsControlsSupport.DiagnosticsSettings settings =
        DiagnosticsControlsSupport.readSettings(controls);

    assertTrue(settings.assertjSwingEnabled());
    assertFalse(settings.assertjSwingFreezeWatchdogEnabled());
    assertEquals(2500, settings.assertjSwingFreezeThresholdMs());
    assertEquals(500, settings.assertjSwingWatchdogPollMs());
    assertEquals(5000, settings.assertjSwingFallbackViolationReportMs());
    assertTrue(settings.assertjSwingOnIssuePlaySound());
    assertFalse(settings.assertjSwingOnIssueShowNotification());
    assertTrue(settings.jhiccupEnabled());
    assertEquals("/tmp/jhiccup.jar", settings.jhiccupJarPath());
    assertEquals("", settings.jhiccupJavaCommandRaw());
    assertEquals("java", settings.jhiccupJavaCommandEffective());
    assertEquals(List.of("-d 1000", "-c"), settings.jhiccupArgs());
  }

  @Test
  void readSettingsClampsDiagnosticsIntervals() {
    DiagnosticsControls controls =
        controls(false, true, 100, 50, 200_000, false, true, false, "", "java25", "");

    DiagnosticsControlsSupport.DiagnosticsSettings settings =
        DiagnosticsControlsSupport.readSettings(controls);

    assertEquals(500, settings.assertjSwingFreezeThresholdMs());
    assertEquals(100, settings.assertjSwingWatchdogPollMs());
    assertEquals(120_000, settings.assertjSwingFallbackViolationReportMs());
    assertEquals("java25", settings.jhiccupJavaCommandRaw());
    assertEquals("java25", settings.jhiccupJavaCommandEffective());
    assertEquals(List.of(), settings.jhiccupArgs());
  }

  @Test
  void rememberSettingsPersistsValuesThatNoLongerCompareAsChanged(@TempDir Path tempDir) {
    RuntimeConfigStore runtimeConfig =
        new RuntimeConfigStore(
            tempDir.resolve("ircafe.yml").toString(), new IrcProperties(null, List.of()));
    DiagnosticsControlsSupport.DiagnosticsSettings settings =
        DiagnosticsControlsSupport.readSettings(
            controls(
                false,
                true,
                1500,
                750,
                2500,
                true,
                true,
                true,
                "/tmp/jhiccup.jar",
                "java25",
                "-d 500\n-c"));

    assertTrue(DiagnosticsControlsSupport.settingsChanged(runtimeConfig, settings));

    DiagnosticsControlsSupport.rememberSettings(runtimeConfig, settings);

    assertFalse(DiagnosticsControlsSupport.settingsChanged(runtimeConfig, settings));
    assertFalse(runtimeConfig.readAppDiagnosticsAssertjSwingEnabled(true));
    assertTrue(runtimeConfig.readAppDiagnosticsAssertjSwingFreezeWatchdogEnabled(false));
    assertEquals(1500, runtimeConfig.readAppDiagnosticsAssertjSwingFreezeThresholdMs(2500));
    assertEquals(750, runtimeConfig.readAppDiagnosticsAssertjSwingWatchdogPollMs(500));
    assertEquals(2500, runtimeConfig.readAppDiagnosticsAssertjSwingFallbackViolationReportMs(5000));
    assertTrue(runtimeConfig.readAppDiagnosticsAssertjSwingIssuePlaySound(false));
    assertTrue(runtimeConfig.readAppDiagnosticsAssertjSwingIssueShowNotification(false));
    assertTrue(runtimeConfig.readAppDiagnosticsJhiccupEnabled(false));
    assertEquals("/tmp/jhiccup.jar", runtimeConfig.readAppDiagnosticsJhiccupJarPath(""));
    assertEquals("java25", runtimeConfig.readAppDiagnosticsJhiccupJavaCommand("java"));
    assertEquals(List.of("-d 500", "-c"), runtimeConfig.readAppDiagnosticsJhiccupArgs(List.of()));
  }

  private static DiagnosticsControls controls(
      boolean assertjSwingEnabled,
      boolean assertjSwingFreezeWatchdogEnabled,
      int freezeThresholdMs,
      int watchdogPollMs,
      int fallbackViolationReportMs,
      boolean playSound,
      boolean showNotification,
      boolean jhiccupEnabled,
      String jhiccupJarPath,
      String jhiccupJavaCommand,
      String jhiccupArgs) {
    return new DiagnosticsControls(
        checkbox(assertjSwingEnabled),
        checkbox(assertjSwingFreezeWatchdogEnabled),
        spinner(freezeThresholdMs),
        spinner(watchdogPollMs),
        spinner(fallbackViolationReportMs),
        checkbox(playSound),
        checkbox(showNotification),
        checkbox(jhiccupEnabled),
        new JTextField(jhiccupJarPath),
        new JTextField(jhiccupJavaCommand),
        new JTextArea(jhiccupArgs));
  }

  private static JCheckBox checkbox(boolean selected) {
    JCheckBox checkbox = new JCheckBox();
    checkbox.setSelected(selected);
    return checkbox;
  }

  private static JSpinner spinner(int value) {
    return new JSpinner(new SpinnerNumberModel(value, -1_000, 300_000, 1));
  }
}
