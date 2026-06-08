package cafe.woden.ircclient.ui.settings.tray;

import cafe.woden.ircclient.config.api.TrayRuntimeConfigPort;
import cafe.woden.ircclient.ui.localization.UiMessages;
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
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private TrayNotificationsPanelSupport() {}

  public static JPanel buildPanel(TrayControls controls) {
    JPanel form = new JPanel(MigLayouts.singleColumnFill(12, "[]10[]6[grow,fill]"));
    form.add(
        PreferencesUiSupport.tabTitle(MESSAGES.text("preferences.tray.title")),
        MigConstraints.growXWrap());
    form.add(
        PreferencesUiSupport.sectionTitle(MESSAGES.text("preferences.tray.section.categories")),
        MigConstraints.growXMinWidth0Wrap());
    form.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.tray.subtitle")),
        MigConstraints.growXMinWidth0Wrap());
    form.add(controls.panel, MigConstraints.growPushMinWidth0());
    return form;
  }

  static JPanel buildTabsPanel(
      TrayControls controls,
      TrayRuntimeConfigPort runtimeConfig,
      boolean linux,
      boolean linuxActionsSupported) {
    JPanel trayTab = new JPanel(MigLayouts.singleColumn());
    trayTab.setOpaque(false);
    JPanel trayBehavior =
        PreferencesUiSupport.captionPanel(MESSAGES.text("preferences.tray.section.trayBehavior"));
    trayBehavior.add(controls.enabled, MigConstraints.growX());
    trayBehavior.add(controls.closeToTray, MigConstraints.growX());
    trayBehavior.add(controls.minimizeToTray, MigConstraints.growX());
    trayBehavior.add(controls.startMinimized, MigConstraints.growXWrap());
    trayTab.add(trayBehavior, MigConstraints.growXMinWidth0Wrap());
    trayTab.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.tray.trayAvailability.help")),
        MigConstraints.growX());

    JPanel notificationsTab = new JPanel(MigLayouts.singleColumn());
    notificationsTab.setOpaque(false);
    JPanel notificationEvents =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.tray.section.notificationEvents"));
    notificationEvents.add(controls.notifyHighlights, MigConstraints.growX());
    notificationEvents.add(controls.notifyPrivateMessages, MigConstraints.growX());
    notificationEvents.add(controls.notifyConnectionState, MigConstraints.growX());
    notificationsTab.add(notificationEvents, MigConstraints.growXMinWidth0Wrap());
    JPanel notificationBackendGroup =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.tray.section.deliveryBackend"),
            MigLayouts.twoColumnForm(8, "[]"));
    notificationBackendGroup.add(new JLabel(MESSAGES.text("preferences.tray.field.mode")));
    notificationBackendGroup.add(controls.notificationBackend, MigConstraints.widthWrap(260));
    notificationBackendGroup.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.tray.deliveryBackend.help")),
        MigConstraints.span2GrowX());
    notificationsTab.add(notificationBackendGroup, MigConstraints.growXMinWidth0Wrap());
    JPanel notificationVisibility =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.tray.section.suppressionFocusRules"));
    notificationVisibility.add(controls.updateNotifierEnabled, MigConstraints.growX());
    notificationVisibility.add(controls.lagIndicatorEnabled, MigConstraints.growX());
    notificationVisibility.add(controls.notifyOnlyWhenUnfocused, MigConstraints.growX());
    notificationVisibility.add(controls.notifyOnlyWhenMinimizedOrHidden, MigConstraints.growX());
    notificationVisibility.add(controls.notifySuppressWhenTargetActive, MigConstraints.growXWrap());
    notificationVisibility.add(new JSeparator(), MigConstraints.growXGapTop(4));
    notificationVisibility.add(controls.testNotification, MigConstraints.width(180));
    notificationsTab.add(notificationVisibility, MigConstraints.growXMinWidth0Wrap());
    notificationsTab.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.tray.desktopNotifications.help")),
        MigConstraints.growX());

    JPanel soundsTab = new JPanel(MigLayouts.singleColumn());
    soundsTab.setOpaque(false);
    JPanel soundsBehavior =
        PreferencesUiSupport.captionPanel(MESSAGES.text("preferences.tray.section.soundBehavior"));
    soundsBehavior.add(controls.notificationSoundsEnabled, MigConstraints.growX());
    soundsBehavior.add(controls.notificationSoundUseCustom, MigConstraints.growXWrap());
    soundsTab.add(soundsBehavior, MigConstraints.growXMinWidth0Wrap());
    JPanel customSound =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.tray.section.customSoundFile"),
            MigLayouts.labelFieldActionsForm(8, 2, "[]"));
    customSound.add(new JLabel(MESSAGES.text("preferences.tray.field.file")));
    customSound.add(controls.notificationSoundCustomPath, MigConstraints.growXPushXMinWidth0());
    customSound.add(controls.browseCustomSound, MigConstraints.width(110));
    customSound.add(controls.clearCustomSound, MigConstraints.widthWrap(80));
    soundsTab.add(customSound, MigConstraints.growXMinWidth0Wrap());
    JPanel builtInSound =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.tray.section.builtInSound"),
            MigLayouts.labelFieldActionsForm(8, 1, "[]"));
    builtInSound.add(new JLabel(MESSAGES.text("preferences.tray.field.preset")));
    builtInSound.add(controls.notificationSound, MigConstraints.width(240));
    builtInSound.add(controls.testSound, MigConstraints.widthWrap(120));
    soundsTab.add(builtInSound, MigConstraints.growXMinWidth0Wrap());

    Path configPath = runtimeConfig != null ? runtimeConfig.runtimeConfigPath() : null;
    Path base = configPath != null ? configPath.getParent() : null;
    if (base != null) {
      soundsTab.add(
          PreferencesUiSupport.helpText(
              MESSAGES.text("preferences.tray.customSounds.copiedTo.help", base.resolve("sounds"))),
          MigConstraints.growX());
    }

    JPanel pushyTab = new JPanel(MigLayouts.singleColumn());
    pushyTab.setOpaque(false);

    JPanel pushyBasics =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.tray.section.pushyIntegration"),
            MigLayouts.twoColumnForm(8, "[]"));
    pushyBasics.add(controls.pushyEnabled, MigConstraints.span2GrowXWrap());
    pushyBasics.add(new JLabel(MESSAGES.text("preferences.tray.field.endpoint")));
    pushyBasics.add(controls.pushyEndpoint, MigConstraints.growXPushXMinWidth0Wrap());
    pushyBasics.add(new JLabel(MESSAGES.text("preferences.tray.field.apiKey")));
    pushyBasics.add(controls.pushyApiKey, MigConstraints.growXPushXMinWidth0Wrap());
    pushyBasics.add(new JLabel(MESSAGES.text("preferences.tray.field.titlePrefix")));
    pushyBasics.add(controls.pushyTitlePrefix, MigConstraints.growXPushXMinWidth0Wrap());
    pushyTab.add(pushyBasics, MigConstraints.growXMinWidth0Wrap());

    JPanel pushyDestination =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.tray.section.destination"),
            MigLayouts.twoColumnForm(8, "[]"));
    pushyDestination.add(new JLabel(MESSAGES.text("preferences.tray.field.targetMode")));
    pushyDestination.add(controls.pushyTargetMode, MigConstraints.widthWrap(180));
    pushyDestination.add(new JLabel(MESSAGES.text("preferences.tray.field.targetValue")));
    pushyDestination.add(controls.pushyTargetValue, MigConstraints.growXPushXMinWidth0Wrap());
    pushyDestination.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.tray.pushyDestination.help")),
        MigConstraints.span2GrowX());
    pushyTab.add(pushyDestination, MigConstraints.growXMinWidth0Wrap());

    JPanel pushyTimeouts =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.tray.section.networkTimeouts"),
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_4,
            "[right]8[]20[right]8[]",
            "[]");
    pushyTimeouts.add(new JLabel(MESSAGES.text("preferences.tray.field.connectSeconds")));
    pushyTimeouts.add(controls.pushyConnectTimeoutSeconds, MigConstraints.width(90));
    pushyTimeouts.add(new JLabel(MESSAGES.text("preferences.tray.field.readSeconds")));
    pushyTimeouts.add(controls.pushyReadTimeoutSeconds, MigConstraints.widthWrap(90));
    pushyTab.add(pushyTimeouts, MigConstraints.growXMinWidth0Wrap());
    JPanel pushyActions =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.tray.section.validationTesting"),
            MigLayoutConstraints.INSETS_0_FILL_X_WRAP_2,
            "[]12[grow,fill]",
            "[]");
    pushyActions.add(controls.pushyTest, MigConstraints.width(150));
    pushyActions.add(controls.pushyTestStatus, MigConstraints.growXMinWidth0Wrap());
    pushyActions.add(new JLabel(""));
    pushyActions.add(controls.pushyValidationLabel, MigConstraints.growXMinWidth0());
    pushyTab.add(pushyActions, MigConstraints.growXMinWidth0Wrap());
    pushyTab.add(
        PreferencesUiSupport.helpText(MESSAGES.text("preferences.tray.pushyRules.help")),
        MigConstraints.growX());

    JPanel linuxTab = new JPanel(MigLayouts.singleColumn());
    linuxTab.setOpaque(false);
    JPanel linuxGroup =
        PreferencesUiSupport.captionPanel(
            MESSAGES.text("preferences.tray.section.linuxIntegration"));
    linuxGroup.add(controls.linuxDbusActions, MigConstraints.growXWrap());
    if (!linux) {
      linuxGroup.add(
          PreferencesUiSupport.helpText(MESSAGES.text("preferences.tray.linuxOnly.help")),
          MigConstraints.growX());
    } else if (!linuxActionsSupported) {
      linuxGroup.add(
          PreferencesUiSupport.helpText(
              MESSAGES.text("preferences.tray.linuxActionsUnavailable.help")),
          MigConstraints.growX());
    } else {
      linuxGroup.add(
          PreferencesUiSupport.helpText(
              MESSAGES.text("preferences.tray.linuxActionsAvailable.help")),
          MigConstraints.growX());
    }
    linuxTab.add(linuxGroup, MigConstraints.growXMinWidth0());

    JTabbedPane subTabs = new DynamicTabbedPane();
    subTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    subTabs.addTab(
        MESSAGES.text("preferences.tray.tab.tray"), PreferencesUiSupport.padSubTab(trayTab));
    subTabs.addTab(
        MESSAGES.text("preferences.tray.tab.desktopNotifications"),
        PreferencesUiSupport.padSubTab(notificationsTab));
    subTabs.addTab(
        MESSAGES.text("preferences.tray.tab.sounds"), PreferencesUiSupport.padSubTab(soundsTab));
    subTabs.addTab(
        MESSAGES.text("preferences.tray.tab.pushy"), PreferencesUiSupport.padSubTab(pushyTab));
    subTabs.addTab(
        MESSAGES.text("preferences.tray.tab.linuxAdvanced"),
        PreferencesUiSupport.padSubTab(linuxTab));

    JPanel panel = new JPanel(MigLayouts.singleColumn());
    panel.setOpaque(false);
    panel.add(subTabs, MigConstraints.growXMinWidth0());
    return panel;
  }
}
