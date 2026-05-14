package cafe.woden.ircclient.ui.settings.startup;

import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public record LaunchJvmControls(
    JTextField javaCommand,
    JSpinner xmsMiB,
    JSpinner xmxMiB,
    JComboBox<LaunchGcOption> gc,
    JTextArea extraArgs) {}
