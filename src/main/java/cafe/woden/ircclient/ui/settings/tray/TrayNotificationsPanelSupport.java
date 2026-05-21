package cafe.woden.ircclient.ui.settings.tray;

import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.ui.settings.DynamicTabbedPane;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import java.nio.file.Path;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import net.miginfocom.swing.MigLayout;

public final class TrayNotificationsPanelSupport {
  private TrayNotificationsPanelSupport() {}

  public static JPanel buildPanel(TrayControls controls) {
    JPanel form =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_12_FILL_WRAP_1,
                MigLayoutConstraints.GROW_FILL,
                "[]10[]6[grow,fill]"));
    form.add(
        PreferencesUiSupport.tabTitle("Tray & Notifications"), MigLayoutConstraints.GROW_X_WRAP);
    form.add(
        PreferencesUiSupport.sectionTitle("Categories"), MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    form.add(
        PreferencesUiSupport.helpText(
            "Use the sub-tabs below to configure tray behavior, desktop notifications, notification sounds, and Linux integration."),
        MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    form.add(controls.panel, MigLayoutConstraints.GROW_PUSH_WMIN_0);
    return form;
  }

  static JPanel buildTabsPanel(
      TrayControls controls,
      RuntimeConfigStore runtimeConfig,
      boolean linux,
      boolean linuxActionsSupported) {
    JPanel trayTab =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1, MigLayoutConstraints.GROW_FILL));
    trayTab.setOpaque(false);
    JPanel trayBehavior =
        PreferencesUiSupport.captionPanel(
            "Tray behavior",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    trayBehavior.add(controls.enabled, MigLayoutConstraints.GROW_X);
    trayBehavior.add(controls.closeToTray, MigLayoutConstraints.GROW_X);
    trayBehavior.add(controls.minimizeToTray, MigLayoutConstraints.GROW_X);
    trayBehavior.add(controls.startMinimized, MigLayoutConstraints.GROW_X_WRAP);
    trayTab.add(trayBehavior, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    trayTab.add(
        PreferencesUiSupport.helpText(
            "Tray availability depends on your desktop environment. If tray support is unavailable, these options will have no effect."),
        MigLayoutConstraints.GROW_X);

    JPanel notificationsTab =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1, MigLayoutConstraints.GROW_FILL));
    notificationsTab.setOpaque(false);
    JPanel notificationEvents =
        PreferencesUiSupport.captionPanel(
            "Notification events",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    notificationEvents.add(controls.notifyHighlights, MigLayoutConstraints.GROW_X);
    notificationEvents.add(controls.notifyPrivateMessages, MigLayoutConstraints.GROW_X);
    notificationEvents.add(controls.notifyConnectionState, MigLayoutConstraints.GROW_X);
    notificationsTab.add(notificationEvents, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    JPanel notificationBackendGroup =
        PreferencesUiSupport.captionPanel(
            "Delivery backend",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_8_GROW_FILL,
            "[]");
    notificationBackendGroup.add(new JLabel("Mode:"));
    notificationBackendGroup.add(controls.notificationBackend, "w 260!, wrap");
    notificationBackendGroup.add(
        PreferencesUiSupport.helpText(
            "Auto tries native OS notifications first and falls back to two-slices.\n"
                + "Native only disables fallback. Two-slices only bypasses OS-native backends."),
        MigLayoutConstraints.SPAN_2_GROW_X);
    notificationsTab.add(notificationBackendGroup, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    JPanel notificationVisibility =
        PreferencesUiSupport.captionPanel(
            "Suppression and focus rules",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    notificationVisibility.add(controls.updateNotifierEnabled, MigLayoutConstraints.GROW_X);
    notificationVisibility.add(controls.lagIndicatorEnabled, MigLayoutConstraints.GROW_X);
    notificationVisibility.add(controls.notifyOnlyWhenUnfocused, MigLayoutConstraints.GROW_X);
    notificationVisibility.add(
        controls.notifyOnlyWhenMinimizedOrHidden, MigLayoutConstraints.GROW_X);
    notificationVisibility.add(
        controls.notifySuppressWhenTargetActive, MigLayoutConstraints.GROW_X_WRAP);
    notificationVisibility.add(new JSeparator(), "growx, gaptop 4");
    notificationVisibility.add(controls.testNotification, "w 180!");
    notificationsTab.add(notificationVisibility, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    notificationsTab.add(
        PreferencesUiSupport.helpText(
            "Desktop notifications are shown when your notification rules trigger (or for connection events, if enabled)."),
        MigLayoutConstraints.GROW_X);

    JPanel soundsTab =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1, MigLayoutConstraints.GROW_FILL));
    soundsTab.setOpaque(false);
    JPanel soundsBehavior =
        PreferencesUiSupport.captionPanel(
            "Sound behavior",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    soundsBehavior.add(controls.notificationSoundsEnabled, MigLayoutConstraints.GROW_X);
    soundsBehavior.add(controls.notificationSoundUseCustom, MigLayoutConstraints.GROW_X_WRAP);
    soundsTab.add(soundsBehavior, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    JPanel customSound =
        PreferencesUiSupport.captionPanel(
            "Custom sound file",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_4,
            MigLayoutConstraints.RIGHT_8_GROW_FILL_8_TRAILING_8_TRAILING,
            "[]");
    customSound.add(new JLabel("File:"));
    customSound.add(
        controls.notificationSoundCustomPath, MigLayoutConstraints.GROW_X_PUSH_X_WMIN_0);
    customSound.add(controls.browseCustomSound, MigLayoutConstraints.WIDTH_110);
    customSound.add(controls.clearCustomSound, "w 80!, wrap");
    soundsTab.add(customSound, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    JPanel builtInSound =
        PreferencesUiSupport.captionPanel(
            "Built-in sound",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_3,
            "[right]8[grow,fill]8[]",
            "[]");
    builtInSound.add(new JLabel("Preset:"));
    builtInSound.add(controls.notificationSound, "w 240!");
    builtInSound.add(controls.testSound, "w 120!, wrap");
    soundsTab.add(builtInSound, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    Path configPath = runtimeConfig != null ? runtimeConfig.runtimeConfigPath() : null;
    Path base = configPath != null ? configPath.getParent() : null;
    if (base != null) {
      soundsTab.add(
          PreferencesUiSupport.helpText(
              "Custom sounds are copied to: "
                  + base.resolve("sounds")
                  + "\nTip: Use small files (short MP3/WAV) for snappy notifications."),
          MigLayoutConstraints.GROW_X);
    }

    JPanel pushyTab =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1, MigLayoutConstraints.GROW_FILL));
    pushyTab.setOpaque(false);

    JPanel pushyBasics =
        PreferencesUiSupport.captionPanel(
            "Pushy integration",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_8_GROW_FILL,
            "[]");
    pushyBasics.add(controls.pushyEnabled, MigLayoutConstraints.SPAN_2_GROW_X_WRAP);
    pushyBasics.add(new JLabel("Endpoint:"));
    pushyBasics.add(controls.pushyEndpoint, MigLayoutConstraints.GROW_X_PUSH_X_WMIN_0_WRAP);
    pushyBasics.add(new JLabel("API key:"));
    pushyBasics.add(controls.pushyApiKey, MigLayoutConstraints.GROW_X_PUSH_X_WMIN_0_WRAP);
    pushyBasics.add(new JLabel("Title prefix:"));
    pushyBasics.add(controls.pushyTitlePrefix, MigLayoutConstraints.GROW_X_PUSH_X_WMIN_0_WRAP);
    pushyTab.add(pushyBasics, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel pushyDestination =
        PreferencesUiSupport.captionPanel(
            "Destination",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            MigLayoutConstraints.RIGHT_8_GROW_FILL,
            "[]");
    pushyDestination.add(new JLabel("Target mode:"));
    pushyDestination.add(controls.pushyTargetMode, "w 180!, wrap");
    pushyDestination.add(new JLabel("Target value:"));
    pushyDestination.add(controls.pushyTargetValue, MigLayoutConstraints.GROW_X_PUSH_X_WMIN_0_WRAP);
    pushyDestination.add(
        PreferencesUiSupport.helpText(
            "Choose a destination type and enter the corresponding value."),
        MigLayoutConstraints.SPAN_2_GROW_X);
    pushyTab.add(pushyDestination, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);

    JPanel pushyTimeouts =
        PreferencesUiSupport.captionPanel(
            "Network timeouts",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_4,
            "[right]8[]20[right]8[]",
            "[]");
    pushyTimeouts.add(new JLabel("Connect (s):"));
    pushyTimeouts.add(controls.pushyConnectTimeoutSeconds, "w 90!");
    pushyTimeouts.add(new JLabel("Read (s):"));
    pushyTimeouts.add(controls.pushyReadTimeoutSeconds, "w 90!, wrap");
    pushyTab.add(pushyTimeouts, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    JPanel pushyActions =
        PreferencesUiSupport.captionPanel(
            "Validation & testing",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            "[]12[grow,fill]",
            "[]");
    pushyActions.add(controls.pushyTest, "w 150!");
    pushyActions.add(controls.pushyTestStatus, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    pushyActions.add(new JLabel(""));
    pushyActions.add(controls.pushyValidationLabel, MigLayoutConstraints.GROW_X_WMIN_0);
    pushyTab.add(pushyActions, MigLayoutConstraints.GROW_X_WMIN_0_WRAP);
    pushyTab.add(
        PreferencesUiSupport.helpText(
            "Pushy notifications are triggered by matching IRC event rules in Notifications -> IRC Event Rules."),
        MigLayoutConstraints.GROW_X);

    JPanel linuxTab =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1, MigLayoutConstraints.GROW_FILL));
    linuxTab.setOpaque(false);
    JPanel linuxGroup =
        PreferencesUiSupport.captionPanel(
            "Linux integration",
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1,
            MigLayoutConstraints.GROW_FILL,
            "");
    linuxGroup.add(controls.linuxDbusActions, MigLayoutConstraints.GROW_X_WRAP);
    if (!linux) {
      linuxGroup.add(PreferencesUiSupport.helpText("Linux only."), MigLayoutConstraints.GROW_X);
    } else if (!linuxActionsSupported) {
      linuxGroup.add(
          PreferencesUiSupport.helpText(
              "Linux notification actions were not detected for this session.\n"
                  + "IRCafe will fall back to notify-send."),
          MigLayoutConstraints.GROW_X);
    } else {
      linuxGroup.add(
          PreferencesUiSupport.helpText(
              "Uses org.freedesktop.Notifications over D-Bus so clicking a notification can open IRCafe."),
          MigLayoutConstraints.GROW_X);
    }
    linuxTab.add(linuxGroup, MigLayoutConstraints.GROW_X_WMIN_0);

    JTabbedPane subTabs = new DynamicTabbedPane();
    subTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    subTabs.addTab("Tray", PreferencesUiSupport.padSubTab(trayTab));
    subTabs.addTab("Desktop notifications", PreferencesUiSupport.padSubTab(notificationsTab));
    subTabs.addTab("Sounds", PreferencesUiSupport.padSubTab(soundsTab));
    subTabs.addTab("Pushy", PreferencesUiSupport.padSubTab(pushyTab));
    subTabs.addTab("Linux / Advanced", PreferencesUiSupport.padSubTab(linuxTab));

    JPanel panel =
        new JPanel(
            new MigLayout(
                MigLayoutConstraints.INSETS_0_FILL_X_WRAP_1, MigLayoutConstraints.GROW_FILL));
    panel.setOpaque(false);
    panel.add(subTabs, MigLayoutConstraints.GROW_X_WMIN_0);
    return panel;
  }
}
