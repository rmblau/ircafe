package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

final class LaunchJvmControlsSupport {
  private LaunchJvmControlsSupport() {}

  static LaunchJvmControls buildControls(RuntimeConfigStore runtimeConfig) {
    JTextField javaCommand = new JTextField(runtimeConfig.readLaunchJvmJavaCommand("java"));
    javaCommand.setToolTipText("Java launcher command used for the next app start.");

    int xms = runtimeConfig.readLaunchJvmXmsMiB(0);
    int xmx = runtimeConfig.readLaunchJvmXmxMiB(0);
    JSpinner xmsMiB = new JSpinner(new SpinnerNumberModel(Math.max(0, xms), 0, 262_144, 128));
    JSpinner xmxMiB = new JSpinner(new SpinnerNumberModel(Math.max(0, xmx), 0, 262_144, 128));

    JComboBox<LaunchGcOption> gc = new JComboBox<>(gcOptions());
    gc.setSelectedItem(gcOptionForId(runtimeConfig.readLaunchJvmGc("")));
    gc.setToolTipText("Garbage collector preference for the next app start.");

    JTextArea extraArgs = new JTextArea(5, 40);
    extraArgs.setLineWrap(false);
    extraArgs.setWrapStyleWord(false);
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

  static LaunchJvmSettings readSettings(LaunchJvmControls controls) {
    String javaCommand = Objects.toString(controls.javaCommand().getText(), "").trim();
    if (javaCommand.isBlank()) javaCommand = "java";

    int xmsMiB = clampMemoryMiB(((Number) controls.xmsMiB().getValue()).intValue());
    int xmxMiB = clampMemoryMiB(((Number) controls.xmxMiB().getValue()).intValue());
    if (xmxMiB > 0 && xmsMiB > 0 && xmxMiB < xmsMiB) {
      xmxMiB = xmsMiB;
    }

    return new LaunchJvmSettings(
        javaCommand,
        xmsMiB,
        xmxMiB,
        gcIdValue((LaunchGcOption) controls.gc().getSelectedItem()),
        parseArgs(controls.extraArgs().getText()));
  }

  private static int clampMemoryMiB(int value) {
    if (value < 0) return 0;
    return Math.min(value, 262_144);
  }

  private static List<String> parseArgs(String text) {
    String raw = Objects.toString(text, "");
    if (raw.isBlank()) return List.of();

    List<String> args = new ArrayList<>();
    for (String line : raw.split("\\R")) {
      String arg = Objects.toString(line, "").trim();
      if (!arg.isEmpty()) args.add(arg);
    }
    return List.copyOf(args);
  }

  record LaunchJvmSettings(
      String javaCommand, int xmsMiB, int xmxMiB, String gc, List<String> args) {}
}
