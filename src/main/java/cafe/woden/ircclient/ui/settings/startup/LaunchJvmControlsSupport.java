package cafe.woden.ircclient.ui.settings.startup;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class LaunchJvmControlsSupport {
  private LaunchJvmControlsSupport() {}

  public static LaunchJvmControls buildControls(RuntimeConfigStore runtimeConfig) {
    JTextField javaCommand = new JTextField(runtimeConfig.readLaunchJvmJavaCommand("java"));
    javaCommand.setToolTipText("Java launcher command used for the next app start.");

    int xms = runtimeConfig.readLaunchJvmXmsMiB(0);
    int xmx = runtimeConfig.readLaunchJvmXmxMiB(0);
    JSpinner xmsMiB = PreferencesUiSupport.numberSpinner(Math.max(0, xms), 0, 262_144, 128);
    JSpinner xmxMiB = PreferencesUiSupport.numberSpinner(Math.max(0, xmx), 0, 262_144, 128);

    JComboBox<LaunchGcOption> gc = new JComboBox<>(gcOptions());
    gc.setSelectedItem(gcOptionForId(runtimeConfig.readLaunchJvmGc("")));
    gc.setToolTipText("Garbage collector preference for the next app start.");

    JTextArea extraArgs = PreferencesUiSupport.textArea(5, 40, false);
    extraArgs.setText(String.join("\n", runtimeConfig.readLaunchJvmArgs(List.of())));
    extraArgs.setToolTipText("Additional JVM arguments. One argument per line.");

    return new LaunchJvmControls(javaCommand, xmsMiB, xmxMiB, gc, extraArgs);
  }

  static LaunchGcOption[] gcOptions() {
    return new LaunchGcOption[] {
      new LaunchGcOption("", "Default (JVM chooses)"),
      new LaunchGcOption("g1", "G1GC"),
      new LaunchGcOption("zgc", "ZGC"),
      new LaunchGcOption("shenandoah", "Shenandoah"),
      new LaunchGcOption("parallel", "ParallelGC"),
      new LaunchGcOption("serial", "SerialGC"),
      new LaunchGcOption("epsilon", "EpsilonGC")
    };
  }

  static LaunchGcOption gcOptionForId(String id) {
    String want = Objects.toString(id, "").trim().toLowerCase(Locale.ROOT);
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

    int xmsMiB = clampMemoryMiB(PreferencesUiSupport.spinnerInt(controls.xmsMiB()));
    int xmxMiB = clampMemoryMiB(PreferencesUiSupport.spinnerInt(controls.xmxMiB()));
    if (xmxMiB > 0 && xmsMiB > 0 && xmxMiB < xmsMiB) {
      xmxMiB = xmsMiB;
    }

    return new LaunchJvmSettings(
        javaCommand,
        xmsMiB,
        xmxMiB,
        gcIdValue(
            PreferencesUiSupport.selectedComboItem(controls.gc(), LaunchGcOption.class, null)),
        PreferencesUiSupport.trimmedLines(controls.extraArgs().getText()));
  }

  private static int clampMemoryMiB(int value) {
    if (value < 0) return 0;
    return Math.min(value, 262_144);
  }

  public record LaunchJvmSettings(
      String javaCommand, int xmsMiB, int xmxMiB, String gc, List<String> args) {}
}
