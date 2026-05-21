package cafe.woden.ircclient.ui.settings.startup;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.miginfocom.swing.MigLayout;

public final class StartupPanelSupport {
  private StartupPanelSupport() {}

  public static JCheckBox buildAutoConnectCheckbox(UiSettings current) {
    JCheckBox autoConnectOnStart = new JCheckBox("Auto-connect to servers on startup");
    autoConnectOnStart.setSelected(current.autoConnectOnStart());
    autoConnectOnStart.setToolTipText(
        "If enabled, IRCafe will connect to all configured servers automatically after the UI loads.\n"
            + "If disabled, IRCafe starts disconnected and you can connect manually using the Connect button.");
    return autoConnectOnStart;
  }

  public static JPanel buildPanel(JCheckBox autoConnectOnStart, LaunchJvmControls launchJvm) {
    JPanel form =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_X_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]10[]10[]"));
    form.add(PreferencesUiSupport.tabTitle("Startup"), MigLayoutConstraints.GROW_X_WRAP);

    form.add(PreferencesUiSupport.sectionTitle("On launch"), MigLayoutConstraints.GROW_X_WRAP);
    form.add(autoConnectOnStart, MigLayoutConstraints.GROW_X_WRAP);
    form.add(
        PreferencesUiSupport.helpText(
            "If enabled, IRCafe will connect to all configured servers automatically after the UI loads."),
        MigLayoutConstraints.GROW_X_WRAP);

    JScrollPane extraArgsScroll = new JScrollPane(launchJvm.extraArgs());
    extraArgsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    extraArgsScroll.setHorizontalScrollBarPolicy(
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel jvm =
        PreferencesUiSupport.captionPanel(
            "JVM on next launch",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_10_GROW_FILL,
            "[]4[]4[]4[]4[]");
    jvm.add(new JLabel("Java command"));
    jvm.add(launchJvm.javaCommand(), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    jvm.add(new JLabel("Initial heap (MiB)"));
    jvm.add(launchJvm.xmsMiB(), "w 140!, wrap");
    jvm.add(new JLabel("Max heap (MiB)"));
    jvm.add(launchJvm.xmxMiB(), "w 140!, wrap");
    jvm.add(new JLabel("GC"));
    jvm.add(launchJvm.gc(), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    jvm.add(new JLabel("Extra JVM args"), MigLayoutConstraints.ALIGN_Y_TOP);
    jvm.add(extraArgsScroll, "growx, h 100!, wmin 0, wrap");
    jvm.add(
        PreferencesUiSupport.helpText(
            "These settings are stored in runtime config and applied on a future restart by launcher scripts.\n"
                + "Use 0 for heap values to leave them unset."),
        MigLayoutConstraints.SPAN_2_GROW_X_WMIN_0_WRAP);
    form.add(jvm, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    return form;
  }
}
