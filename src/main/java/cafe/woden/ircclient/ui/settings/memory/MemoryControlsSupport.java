package cafe.woden.ircclient.ui.settings.memory;

import cafe.woden.ircclient.config.api.UiShellRuntimeConfigPort;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;

public final class MemoryControlsSupport {
  private MemoryControlsSupport() {}

  public static JComboBox<MemoryUsageDisplayMode> buildMemoryUsageDisplayModeCombo(
      UiSettings current) {
    JComboBox<MemoryUsageDisplayMode> combo = new JComboBox<>(MemoryUsageDisplayMode.values());
    MemoryUsageDisplayMode selected =
        current != null && current.memoryUsageDisplayMode() != null
            ? current.memoryUsageDisplayMode()
            : MemoryUsageDisplayMode.LONG;
    combo.setSelectedItem(selected);
    combo.setToolTipText("Controls the memory widget shown on the far right side of the menu bar.");
    return combo;
  }

  public static JSpinner buildMemoryUsageRefreshIntervalSpinner(
      UiSettings current, List<AutoCloseable> closeables) {
    int refreshIntervalMs =
        current != null && current.memoryUsageRefreshIntervalMs() > 0
            ? current.memoryUsageRefreshIntervalMs()
            : 1000;
    JSpinner spinner =
        PreferencesUiSupport.numberSpinner(refreshIntervalMs, 250, 60_000, 250, closeables);
    spinner.setToolTipText(
        "How often the memory widget refreshes (milliseconds). Lower values cost more CPU/wakeups.");
    return spinner;
  }

  public static MemoryWarningControls buildMemoryWarningControls(
      UiSettings current, List<AutoCloseable> closeables) {
    int nearMaxPercent =
        current != null && current.memoryUsageWarningNearMaxPercent() > 0
            ? current.memoryUsageWarningNearMaxPercent()
            : 5;

    JSpinner nearMaxPercentSpinner =
        PreferencesUiSupport.numberSpinner(nearMaxPercent, 1, 50, 1, closeables);
    nearMaxPercentSpinner.setToolTipText(
        "Trigger warning actions when heap usage is within this percent of the JVM max.");

    JCheckBox tooltipEnabled = new JCheckBox("Show warning tooltip near memory widget");
    tooltipEnabled.setSelected(current == null || current.memoryUsageWarningTooltipEnabled());
    tooltipEnabled.setToolTipText(
        "Shows a transient warning tooltip near the memory widget when threshold is crossed.");

    JCheckBox toastEnabled = new JCheckBox("Show desktop toast warning");
    toastEnabled.setSelected(current != null && current.memoryUsageWarningToastEnabled());
    toastEnabled.setToolTipText(
        "Uses the existing tray notification pipeline for memory threshold alerts.");

    JCheckBox pushyEnabled = new JCheckBox("Send Pushy warning");
    pushyEnabled.setSelected(current != null && current.memoryUsageWarningPushyEnabled());
    pushyEnabled.setToolTipText(
        "Sends a Pushy notification when configured and the warning threshold is crossed.");

    JCheckBox soundEnabled = new JCheckBox("Play warning sound");
    soundEnabled.setSelected(current != null && current.memoryUsageWarningSoundEnabled());
    soundEnabled.setToolTipText("Plays the configured notification sound on memory warning.");

    return new MemoryWarningControls(
        nearMaxPercentSpinner, tooltipEnabled, toastEnabled, pushyEnabled, soundEnabled);
  }

  public static MemorySettings readSettings(
      JComboBox<MemoryUsageDisplayMode> displayMode,
      JSpinner refreshIntervalMs,
      MemoryWarningControls warnings) {
    MemoryUsageDisplayMode mode =
        PreferencesUiSupport.selectedComboItem(
            displayMode, MemoryUsageDisplayMode.class, MemoryUsageDisplayMode.LONG);
    return new MemorySettings(
        mode,
        SettingsRangeSupport.normalizeMemoryUsageRefreshIntervalMs(
            PreferencesUiSupport.spinnerInt(refreshIntervalMs)),
        PreferencesUiSupport.spinnerInt(warnings.nearMaxPercent),
        warnings.tooltipEnabled.isSelected(),
        warnings.toastEnabled.isSelected(),
        warnings.pushyEnabled.isSelected(),
        warnings.soundEnabled.isSelected());
  }

  public static void rememberSettings(
      UiShellRuntimeConfigPort runtimeConfig, MemorySettings settings) {
    runtimeConfig.rememberMemoryUsageDisplayMode(settings.displayMode().token());
    runtimeConfig.rememberMemoryUsageRefreshIntervalMs(settings.refreshIntervalMs());
    runtimeConfig.rememberMemoryUsageWarningNearMaxPercent(settings.warningNearMaxPercent());
    runtimeConfig.rememberMemoryUsageWarningTooltipEnabled(settings.warningTooltipEnabled());
    runtimeConfig.rememberMemoryUsageWarningToastEnabled(settings.warningToastEnabled());
    runtimeConfig.rememberMemoryUsageWarningPushyEnabled(settings.warningPushyEnabled());
    runtimeConfig.rememberMemoryUsageWarningSoundEnabled(settings.warningSoundEnabled());
  }

  public record MemorySettings(
      MemoryUsageDisplayMode displayMode,
      int refreshIntervalMs,
      int warningNearMaxPercent,
      boolean warningTooltipEnabled,
      boolean warningToastEnabled,
      boolean warningPushyEnabled,
      boolean warningSoundEnabled) {
    public MemorySettings {
      if (displayMode == null) displayMode = MemoryUsageDisplayMode.LONG;
      refreshIntervalMs =
          SettingsRangeSupport.normalizeMemoryUsageRefreshIntervalMs(refreshIntervalMs);
      warningNearMaxPercent =
          SettingsRangeSupport.normalizeMemoryUsageWarningNearMaxPercent(warningNearMaxPercent);
    }
  }
}
