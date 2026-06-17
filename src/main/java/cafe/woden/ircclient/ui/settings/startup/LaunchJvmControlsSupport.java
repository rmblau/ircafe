package cafe.woden.ircclient.ui.settings.startup;

import cafe.woden.ircclient.config.api.LaunchJvmRuntimeConfigPort;
import cafe.woden.ircclient.config.api.LaunchJvmRuntimeConfigPort.LaunchJvmSnapshot;
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

  public static LaunchJvmControls buildControls(LaunchJvmRuntimeConfigPort runtimeConfig) {
    LaunchJvmSnapshot snapshot =
        runtimeConfig != null ? runtimeConfig.readLaunchJvmSettings() : null;
    if (snapshot == null) {
      snapshot = new LaunchJvmSnapshot("java", 0, 0, "", List.of());
    }
    JTextField javaCommand = new JTextField(snapshot.javaCommand());
    javaCommand.setToolTipText(MESSAGES.text("preferences.startup.javaCommand.tooltip"));

    JSpinner xmsMiB =
        PreferencesUiSupport.numberSpinner(
            SettingsRangeSupport.normalizeLaunchJvmMemoryMiB(snapshot.xmsMiB()), 0, 262_144, 128);
    JSpinner xmxMiB =
        PreferencesUiSupport.numberSpinner(
            SettingsRangeSupport.normalizeLaunchJvmMemoryMiB(snapshot.xmxMiB()), 0, 262_144, 128);

    JComboBox<LaunchGcOption> gc = new JComboBox<>(gcOptions());
    gc.setSelectedItem(gcOptionForId(snapshot.gc()));
    gc.setToolTipText(MESSAGES.text("preferences.startup.gc.tooltip"));

    JTextArea extraArgs = PreferencesUiSupport.textArea(5, 40, false);
    extraArgs.setText(String.join("\n", snapshot.args()));
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

  public static void rememberSettings(
      LaunchJvmRuntimeConfigPort runtimeConfig, LaunchJvmSettings settings) {
    if (settings == null) {
      return;
    }
    runtimeConfig.rememberLaunchJvmSettings(
        new LaunchJvmSnapshot(
            settings.javaCommand(),
            settings.xmsMiB(),
            settings.xmxMiB(),
            settings.gc(),
            settings.args()));
  }

  public record LaunchJvmSettings(
      String javaCommand, int xmsMiB, int xmxMiB, String gc, List<String> args) {}
}
