package cafe.woden.ircclient.ui.settings.startup;

import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

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
    JPanel form = new JPanel(MigLayouts.singleColumn(12, "[]10[]10[]"));
    form.add(PreferencesUiSupport.tabTitle("Startup"), MigConstraints.growXWrap());

    form.add(PreferencesUiSupport.sectionTitle("On launch"), MigConstraints.growXWrap());
    form.add(autoConnectOnStart, MigConstraints.growXWrap());
    form.add(
        PreferencesUiSupport.helpText(
            "If enabled, IRCafe will connect to all configured servers automatically after the UI loads."),
        MigConstraints.growXWrap());

    JScrollPane extraArgsScroll = new JScrollPane(launchJvm.extraArgs());
    extraArgsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    extraArgsScroll.setHorizontalScrollBarPolicy(
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel jvm =
        PreferencesUiSupport.captionPanel(
            "JVM on next launch", MigLayouts.twoColumnForm(10, "[]4[]4[]4[]4[]"));
    jvm.add(new JLabel("Java command"));
    jvm.add(launchJvm.javaCommand(), MigConstraints.growXMinWidth0Wrap());
    jvm.add(new JLabel("Initial heap (MiB)"));
    jvm.add(launchJvm.xmsMiB(), "w 140!, wrap");
    jvm.add(new JLabel("Max heap (MiB)"));
    jvm.add(launchJvm.xmxMiB(), "w 140!, wrap");
    jvm.add(new JLabel("GC"));
    jvm.add(launchJvm.gc(), MigConstraints.growXMinWidth0Wrap());
    jvm.add(new JLabel("Extra JVM args"), MigConstraints.alignYTop());
    jvm.add(extraArgsScroll, "growx, h 100!, wmin 0, wrap");
    jvm.add(
        PreferencesUiSupport.helpText(
            "These settings are stored in runtime config and applied on a future restart by launcher scripts.\n"
                + "Use 0 for heap values to leave them unset."),
        MigConstraints.span2GrowXMinWidth0Wrap());
    form.add(jvm, MigConstraints.growXMinWidth0Wrap());

    return form;
  }
}
