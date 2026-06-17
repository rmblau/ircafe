package cafe.woden.ircclient.ui.settings.startup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.config.api.LaunchJvmRuntimeConfigPort;
import cafe.woden.ircclient.config.api.LaunchJvmRuntimeConfigPort.LaunchJvmSnapshot;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class LaunchJvmControlsSupportTest {

  @Test
  void buildControlsReadsLaunchJvmSnapshot() {
    LaunchJvmRuntimeConfigPort runtimeConfig = mock(LaunchJvmRuntimeConfigPort.class);
    when(runtimeConfig.readLaunchJvmSettings())
        .thenReturn(new LaunchJvmSnapshot("java25", 512, 2048, "zgc", List.of("-Dfoo=bar")));

    LaunchJvmControls controls = LaunchJvmControlsSupport.buildControls(runtimeConfig);

    assertEquals("java25", controls.javaCommand().getText());
    assertEquals(512, controls.xmsMiB().getValue());
    assertEquals(2048, controls.xmxMiB().getValue());
    assertEquals(
        "zgc",
        LaunchJvmControlsSupport.gcIdValue((LaunchGcOption) controls.gc().getSelectedItem()));
    assertEquals("-Dfoo=bar", controls.extraArgs().getText());
  }

  @Test
  void rememberSettingsWritesLaunchJvmSnapshot() {
    LaunchJvmRuntimeConfigPort runtimeConfig = mock(LaunchJvmRuntimeConfigPort.class);
    LaunchJvmControlsSupport.LaunchJvmSettings settings =
        new LaunchJvmControlsSupport.LaunchJvmSettings(
            "java25", 512, 2048, "zgc", List.of("-Dfoo=bar"));

    LaunchJvmControlsSupport.rememberSettings(runtimeConfig, settings);

    verify(runtimeConfig)
        .rememberLaunchJvmSettings(
            new LaunchJvmSnapshot("java25", 512, 2048, "zgc", List.of("-Dfoo=bar")));
  }

  @Test
  void readSettingsDefaultsBlankJavaCommandAndTrimsArgs() {
    LaunchJvmControls controls =
        controls("  ", 512, 1024, new LaunchGcOption("zgc", "ZGC"), " -Xfoo \n\n -Dbar=baz ");

    LaunchJvmControlsSupport.LaunchJvmSettings settings =
        LaunchJvmControlsSupport.readSettings(controls);

    assertEquals("java", settings.javaCommand());
    assertEquals(512, settings.xmsMiB());
    assertEquals(1024, settings.xmxMiB());
    assertEquals("zgc", settings.gc());
    assertEquals(List.of("-Xfoo", "-Dbar=baz"), settings.args());
  }

  @Test
  void readSettingsClampsMemoryAndRaisesXmxToXmsWhenBothAreSet() {
    LaunchJvmControls controls =
        controls("java25", 262_145, 128, new LaunchGcOption("g1", "G1GC"), "");

    LaunchJvmControlsSupport.LaunchJvmSettings settings =
        LaunchJvmControlsSupport.readSettings(controls);

    assertEquals(262_144, settings.xmsMiB());
    assertEquals(262_144, settings.xmxMiB());
    assertEquals(List.of(), settings.args());
  }

  @Test
  void readSettingsClampsNegativeMemoryToZero() {
    LaunchJvmControls controls = controls("java", -10, -1, new LaunchGcOption("", "Default"), "");

    LaunchJvmControlsSupport.LaunchJvmSettings settings =
        LaunchJvmControlsSupport.readSettings(controls);

    assertEquals(0, settings.xmsMiB());
    assertEquals(0, settings.xmxMiB());
  }

  private static LaunchJvmControls controls(
      String javaCommand, int xmsMiB, int xmxMiB, LaunchGcOption gcOption, String args) {
    JComboBox<LaunchGcOption> gc = new JComboBox<>(LaunchJvmControlsSupport.gcOptions());
    gc.setSelectedItem(gcOption);
    return new LaunchJvmControls(
        new JTextField(javaCommand), spinner(xmsMiB), spinner(xmxMiB), gc, new JTextArea(args));
  }

  private static JSpinner spinner(int value) {
    return new JSpinner(new SpinnerNumberModel(value, -1000, 300_000, 1));
  }
}
