package cafe.woden.ircclient.ui.settings.startup;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsRangeSupport;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class LaunchJvmControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private LaunchJvmControlsSupport() {}

  public static LaunchJvmControls buildControls(RuntimeConfigStore runtimeConfig) {
    JTextField javaCommand = new JTextField(runtimeConfig.readLaunchJvmJavaCommand("java"));
    javaCommand.setToolTipText(MESSAGES.text("preferences.startup.javaCommand.tooltip"));

    int xms = runtimeConfig.readLaunchJvmXmsMiB(0);
    int xmx = runtimeConfig.readLaunchJvmXmxMiB(0);
    JSpinner xmsMiB =
        PreferencesUiSupport.numberSpinner(
            SettingsRangeSupport.normalizeLaunchJvmMemoryMiB(xms), 0, 262_144, 128);
    JSpinner xmxMiB =
        PreferencesUiSupport.numberSpinner(
            SettingsRangeSupport.normalizeLaunchJvmMemoryMiB(xmx), 0, 262_144, 128);

    JComboBox<LaunchGcOption> gc = new JComboBox<>(gcOptions());
    gc.setSelectedItem(gcOptionForId(runtimeConfig.readLaunchJvmGc("")));
    gc.setToolTipText(MESSAGES.text("preferences.startup.gc.tooltip"));

    JTextArea extraArgs = PreferencesUiSupport.textArea(5, 40, false);
    extraArgs.setText(String.join("\n", runtimeConfig.readLaunchJvmArgs(List.of())));
    extraArgs.setToolTipText(MESSAGES.text("preferences.startup.extraArgs.tooltip"));

    return new LaunchJvmControls(javaCommand, xmsMiB, xmxMiB, gc, extraArgs);
  }

  static LaunchGcOption[] gcOptions() {
    return new LaunchGcOption[] {
      new LaunchGcOption("", MESSAGES.text("preferences.startup.gc.default")),
      new LaunchGcOption("g1", "G1GC"),
      new LaunchGcOption("zgc", "ZGC"),
      new LaunchGcOption("shenandoah", "Shenandoah"),
      new LaunchGcOption("parallel", "ParallelGC"),
      new LaunchGcOption("serial", "SerialGC"),
      new LaunchGcOption("epsilon", "EpsilonGC")
    };
  }

  static LaunchGcOption gcOptionForId(String id) {
    String want = SettingsValueSupport.lowerTrimmedString(id);
    for (LaunchGcOption option : gcOptions()) {
      if (option.id().equalsIgnoreCase(want)) return option;
    }
    return gcOptions()[0];
  }

  static String gcIdValue(LaunchGcOption option) {
    return option != null ? option.id() : "";
  }

  public static LaunchJvmSettings readSettings(LaunchJvmControls controls) {
    String javaCommand = PreferencesUiSupport.trimmedText(controls.javaCommand());
    if (javaCommand.isBlank()) javaCommand = "java";

    int xmsMiB =
        SettingsRangeSupport.normalizeLaunchJvmMemoryMiB(
            PreferencesUiSupport.spinnerInt(controls.xmsMiB()));
    int xmxMiB =
        SettingsRangeSupport.normalizeLaunchJvmMemoryMiB(
            PreferencesUiSupport.spinnerInt(controls.xmxMiB()));
    if (xmxMiB > 0 && xmsMiB > 0 && xmxMiB < xmsMiB) {
      xmxMiB = xmsMiB;
    }

    return new LaunchJvmSettings(
        javaCommand,
        xmsMiB,
        xmxMiB,
        gcIdValue(
            PreferencesUiSupport.selectedComboItem(controls.gc(), LaunchGcOption.class, null)),
        SettingsValueSupport.trimmedLines(controls.extraArgs().getText()));
  }

  public record LaunchJvmSettings(
      String javaCommand, int xmsMiB, int xmxMiB, String gc, List<String> args) {}
}
