package cafe.woden.ircclient.ui.settings.startup;

import cafe.woden.ircclient.ui.localization.UiMessages;
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
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private StartupPanelSupport() {}

  public static JCheckBox buildAutoConnectCheckbox(UiSettings current) {
    JCheckBox autoConnectOnStart =
        new JCheckBox(MESSAGES.text("preferences.startup.autoConnect.enabled"));
    autoConnectOnStart.setSelected(current.autoConnectOnStart());
    autoConnectOnStart.setToolTipText(MESSAGES.text("preferences.startup.autoConnect.tooltip"));
    return autoConnectOnStart;
  }

  public static JPanel buildPanel(JCheckBox autoConnectOnStart, LaunchJvmControls launchJvm) {
    JPanel form = new JPanel(MigLayouts.singleColumn(12, MigLayouts.rows(3, 10)));
    form.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.startup.title")),
        MigConstraints.growXWrap());

    form.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.startup.section.onLaunch")),
        MigConstraints.growXWrap());
    form.add(autoConnectOnStart, MigConstraints.growXWrap());
    form.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.startup.autoConnect.help")),
        MigConstraints.growXWrap());

    JScrollPane extraArgsScroll = new JScrollPane(launchJvm.extraArgs());
    extraArgsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    extraArgsScroll.setHorizontalScrollBarPolicy(
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel jvm =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.startup.section.jvmNextLaunch"),
            MigLayouts.twoColumnForm(10, MigLayouts.rows(5, 4)));
    jvm.add(new JLabel(MESSAGES.text("preferences.startup.field.javaCommand")));
    jvm.add(launchJvm.javaCommand(), MigConstraints.growXMinWidth0Wrap());
    jvm.add(new JLabel(MESSAGES.text("preferences.startup.field.initialHeapMiB")));
    jvm.add(launchJvm.xmsMiB(), MigConstraints.widthWrap(140));
    jvm.add(new JLabel(MESSAGES.text("preferences.startup.field.maxHeapMiB")));
    jvm.add(launchJvm.xmxMiB(), MigConstraints.widthWrap(140));
    jvm.add(new JLabel(MESSAGES.text("preferences.startup.field.gc")));
    jvm.add(launchJvm.gc(), MigConstraints.growXMinWidth0Wrap());
    jvm.add(
        new JLabel(MESSAGES.text("preferences.startup.field.extraJvmArgs")),
        MigConstraints.alignYTop());
    jvm.add(extraArgsScroll, MigConstraints.growXMinWidthHeightWrap(0, 100));
    jvm.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.startup.jvm.help")),
        MigConstraints.span2GrowXMinWidth0Wrap());
    form.add(jvm, MigConstraints.growXMinWidth0Wrap());

    return form;
  }
}
