package cafe.woden.ircclient.ui.settings.tray;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.settings.DynamicTabbedPane;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.nio.file.Path;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

public final class TrayNotificationsPanelSupport {
  private TrayNotificationsPanelSupport() {}

  public static JPanel buildPanel(TrayControls controls) {
    JPanel form = new JPanel(MigLayouts.singleColumnFill(12, "[]10[]6[grow,fill]"));
    form.add(PreferencesUiSupport.tabTitle("Tray & Notifications"), MigConstraints.growXWrap());
    form.add(PreferencesUiSupport.sectionTitle("Categories"), MigConstraints.growXMinWidth0Wrap());
    form.add(
        PreferencesUiSupport.helpText(
            "Use the sub-tabs below to configure tray behavior, desktop notifications, notification sounds, and Linux integration."),
        MigConstraints.growXMinWidth0Wrap());
    form.add(controls.panel, MigConstraints.growPushMinWidth0());
    return form;
  }

  static JPanel buildTabsPanel(
      TrayControls controls,
      RuntimeConfigStore runtimeConfig,
      boolean linux,
      boolean linuxActionsSupported) {
    JPanel trayTab = new JPanel(MigLayouts.singleColumn());
    trayTab.setOpaque(false);
    JPanel trayBehavior = PreferencesUiSupport.captionPanel("Tray behavior");
    trayBehavior.add(controls.enabled, MigConstraints.growX());
    trayBehavior.add(controls.closeToTray, MigConstraints.growX());
    trayBehavior.add(controls.minimizeToTray, MigConstraints.growX());
    trayBehavior.add(controls.startMinimized, MigConstraints.growXWrap());
    trayTab.add(trayBehavior, MigConstraints.growXMinWidth0Wrap());
    trayTab.add(
        PreferencesUiSupport.helpText(
            "Tray availability depends on your desktop environment. If tray support is unavailable, these options will have no effect."),
        MigConstraints.growX());

    JPanel notificationsTab = new JPanel(MigLayouts.singleColumn());
    notificationsTab.setOpaque(false);
    JPanel notificationEvents = PreferencesUiSupport.captionPanel("Notification events");
    notificationEvents.add(controls.notifyHighlights, MigConstraints.growX());
    notificationEvents.add(controls.notifyPrivateMessages, MigConstraints.growX());
    notificationEvents.add(controls.notifyConnectionState, MigConstraints.growX());
    notificationsTab.add(notificationEvents, MigConstraints.growXMinWidth0Wrap());
    JPanel notificationBackendGroup =
        PreferencesUiSupport.captionPanel("Delivery backend", MigLayouts.twoColumnForm(8, "[]"));
    notificationBackendGroup.add(new JLabel("Mode:"));
    notificationBackendGroup.add(controls.notificationBackend, MigConstraints.widthWrap(260));
    notificationBackendGroup.add(
        PreferencesUiSupport.helpText(
            "Auto tries native OS notifications first and falls back to two-slices.\n"
                + "Native only disables fallback. Two-slices only bypasses OS-native backends."),
        MigConstraints.span2GrowX());
    notificationsTab.add(notificationBackendGroup, MigConstraints.growXMinWidth0Wrap());
    JPanel notificationVisibility =
        PreferencesUiSupport.captionPanel("Suppression and focus rules");
    notificationVisibility.add(controls.updateNotifierEnabled, MigConstraints.growX());
    notificationVisibility.add(controls.lagIndicatorEnabled, MigConstraints.growX());
    notificationVisibility.add(controls.notifyOnlyWhenUnfocused, MigConstraints.growX());
    notificationVisibility.add(controls.notifyOnlyWhenMinimizedOrHidden, MigConstraints.growX());
    notificationVisibility.add(controls.notifySuppressWhenTargetActive, MigConstraints.growXWrap());
    notificationVisibility.add(new JSeparator(), MigConstraints.growXGapTop(4));
    notificationVisibility.add(controls.testNotification, MigConstraints.width(180));
    notificationsTab.add(notificationVisibility, MigConstraints.growXMinWidth0Wrap());
    notificationsTab.add(
        PreferencesUiSupport.helpText(
            "Desktop notifications are shown when your notification rules trigger (or for connection events, if enabled)."),
        MigConstraints.growX());

    JPanel soundsTab = new JPanel(MigLayouts.singleColumn());
    soundsTab.setOpaque(false);
    JPanel soundsBehavior = PreferencesUiSupport.captionPanel("Sound behavior");
    soundsBehavior.add(controls.notificationSoundsEnabled, MigConstraints.growX());
    soundsBehavior.add(controls.notificationSoundUseCustom, MigConstraints.growXWrap());
    soundsTab.add(soundsBehavior, MigConstraints.growXMinWidth0Wrap());
    JPanel customSound =
        PreferencesUiSupport.captionPanel(
            "Custom sound file", MigLayouts.labelFieldActionsForm(8, 2, "[]"));
    customSound.add(new JLabel("File:"));
    customSound.add(controls.notificationSoundCustomPath, MigConstraints.growXPushXMinWidth0());
    customSound.add(controls.browseCustomSound, MigConstraints.width(110));
    customSound.add(controls.clearCustomSound, MigConstraints.widthWrap(80));
    soundsTab.add(customSound, MigConstraints.growXMinWidth0Wrap());
    JPanel builtInSound =
        PreferencesUiSupport.captionPanel(
            "Built-in sound", MigLayouts.labelFieldActionsForm(8, 1, "[]"));
    builtInSound.add(new JLabel("Preset:"));
    builtInSound.add(controls.notificationSound, MigConstraints.width(240));
    builtInSound.add(controls.testSound, MigConstraints.widthWrap(120));
    soundsTab.add(builtInSound, MigConstraints.growXMinWidth0Wrap());

    Path configPath = runtimeConfig != null ? runtimeConfig.runtimeConfigPath() : null;
    Path base = configPath != null ? configPath.getParent() : null;
    if (base != null) {
      soundsTab.add(
          PreferencesUiSupport.helpText(
              "Custom sounds are copied to: "
                  + base.resolve("sounds")
                  + "\nTip: Use small files (short MP3/WAV) for snappy notifications."),
          MigConstraints.growX());
    }

    JPanel pushyTab = new JPanel(MigLayouts.singleColumn());
    pushyTab.setOpaque(false);

    JPanel pushyBasics =
        PreferencesUiSupport.captionPanel("Pushy integration", MigLayouts.twoColumnForm(8, "[]"));
    pushyBasics.add(controls.pushyEnabled, MigConstraints.span2GrowXWrap());
    pushyBasics.add(new JLabel("Endpoint:"));
    pushyBasics.add(controls.pushyEndpoint, MigConstraints.growXPushXMinWidth0Wrap());
    pushyBasics.add(new JLabel("API key:"));
    pushyBasics.add(controls.pushyApiKey, MigConstraints.growXPushXMinWidth0Wrap());
    pushyBasics.add(new JLabel("Title prefix:"));
    pushyBasics.add(controls.pushyTitlePrefix, MigConstraints.growXPushXMinWidth0Wrap());
    pushyTab.add(pushyBasics, MigConstraints.growXMinWidth0Wrap());

    JPanel pushyDestination =
        PreferencesUiSupport.captionPanel("Destination", MigLayouts.twoColumnForm(8, "[]"));
    pushyDestination.add(new JLabel("Target mode:"));
    pushyDestination.add(controls.pushyTargetMode, MigConstraints.widthWrap(180));
    pushyDestination.add(new JLabel("Target value:"));
    pushyDestination.add(controls.pushyTargetValue, MigConstraints.growXPushXMinWidth0Wrap());
    pushyDestination.add(
        PreferencesUiSupport.helpText(
            "Choose a destination type and enter the corresponding value."),
        MigConstraints.span2GrowX());
    pushyTab.add(pushyDestination, MigConstraints.growXMinWidth0Wrap());

    JPanel pushyTimeouts =
        PreferencesUiSupport.captionPanel(
            "Network timeouts",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_4,
            "[right]8[]20[right]8[]",
            "[]");
    pushyTimeouts.add(new JLabel("Connect (s):"));
    pushyTimeouts.add(controls.pushyConnectTimeoutSeconds, MigConstraints.width(90));
    pushyTimeouts.add(new JLabel("Read (s):"));
    pushyTimeouts.add(controls.pushyReadTimeoutSeconds, MigConstraints.widthWrap(90));
    pushyTab.add(pushyTimeouts, MigConstraints.growXMinWidth0Wrap());
    JPanel pushyActions =
        PreferencesUiSupport.captionPanel(
            "Validation & testing",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            "[]12[grow,fill]",
            "[]");
    pushyActions.add(controls.pushyTest, MigConstraints.width(150));
    pushyActions.add(controls.pushyTestStatus, MigConstraints.growXMinWidth0Wrap());
    pushyActions.add(new JLabel(""));
    pushyActions.add(controls.pushyValidationLabel, MigConstraints.growXMinWidth0());
    pushyTab.add(pushyActions, MigConstraints.growXMinWidth0Wrap());
    pushyTab.add(
        PreferencesUiSupport.helpText(
            "Pushy notifications are triggered by matching IRC event rules in Notifications -> IRC Event Rules."),
        MigConstraints.growX());

    JPanel linuxTab = new JPanel(MigLayouts.singleColumn());
    linuxTab.setOpaque(false);
    JPanel linuxGroup = PreferencesUiSupport.captionPanel("Linux integration");
    linuxGroup.add(controls.linuxDbusActions, MigConstraints.growXWrap());
    if (!linux) {
      linuxGroup.add(PreferencesUiSupport.helpText("Linux only."), MigConstraints.growX());
    } else if (!linuxActionsSupported) {
      linuxGroup.add(
          PreferencesUiSupport.helpText(
              "Linux notification actions were not detected for this session.\n"
                  + "IRCafe will fall back to notify-send."),
          MigConstraints.growX());
    } else {
      linuxGroup.add(
          PreferencesUiSupport.helpText(
              "Uses org.freedesktop.Notifications over D-Bus so clicking a notification can open IRCafe."),
          MigConstraints.growX());
    }
    linuxTab.add(linuxGroup, MigConstraints.growXMinWidth0());

    JTabbedPane subTabs = new DynamicTabbedPane();
    subTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    subTabs.addTab("Tray", PreferencesUiSupport.padSubTab(trayTab));
    subTabs.addTab("Desktop notifications", PreferencesUiSupport.padSubTab(notificationsTab));
    subTabs.addTab("Sounds", PreferencesUiSupport.padSubTab(soundsTab));
    subTabs.addTab("Pushy", PreferencesUiSupport.padSubTab(pushyTab));
    subTabs.addTab("Linux / Advanced", PreferencesUiSupport.padSubTab(linuxTab));

    JPanel panel = new JPanel(MigLayouts.singleColumn());
    panel.setOpaque(false);
    panel.add(subTabs, MigConstraints.growXMinWidth0());
    return panel;
  }
}
