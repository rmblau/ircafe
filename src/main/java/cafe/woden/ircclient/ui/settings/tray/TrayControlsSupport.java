package cafe.woden.ircclient.ui.settings.tray;

import cafe.woden.ircclient.config.api.TrayRuntimeConfigPort;
import cafe.woden.ircclient.config.properties.PushyProperties;
import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.notify.api.NotificationSoundPort;
import cafe.woden.ircclient.notify.api.PushyNotificationPort;
import cafe.woden.ircclient.notify.pushy.PushySettingsBus;
import cafe.woden.ircclient.notify.sound.NotificationSoundSettings;
import cafe.woden.ircclient.notify.sound.NotificationSoundSettingsBus;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.settings.NotificationBackendMode;
import cafe.woden.ircclient.ui.settings.PreferencesUiSupport;
import cafe.woden.ircclient.ui.settings.SettingsDocumentListener;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import cafe.woden.ircclient.ui.settings.notifications.NotificationSoundControlsSupport;
import cafe.woden.ircclient.ui.shell.LagIndicatorService;
import cafe.woden.ircclient.ui.shell.UpdateNotifierService;
import cafe.woden.ircclient.ui.tray.TrayNotificationService;
import cafe.woden.ircclient.ui.tray.TrayService;
import cafe.woden.ircclient.ui.tray.dbus.GnomeDbusNotificationBackend;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class TrayControlsSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private TrayControlsSupport() {}

  public static TrayControls buildControls(
      UiSettings current,
      NotificationSoundSettings soundSettings,
      PushyProperties pushySettings,
      TrayRuntimeConfigPort runtimeConfig,
      GnomeDbusNotificationBackend gnomeDbusBackend,
      TrayNotificationService trayNotificationService,
      NotificationSoundPort notificationSoundService,
      PushyNotificationPort pushyNotificationService,
      ExecutorService pushyTestExecutor,
      NotificationSoundControlsSupport.SoundFileImporter notificationSoundImporter) {
    NotificationSoundSettings effectiveSoundSettings =
        soundSettings != null
            ? soundSettings
            : new NotificationSoundSettings(true, BuiltInSound.NOTIF_1.name(), false, null);
    PushyProperties effectivePushySettings =
        pushySettings != null
            ? pushySettings
            : new PushyProperties(false, null, null, null, null, null, null, null);

    JCheckBox enabled =
        new JCheckBox(MESSAGES.text("preferences.tray.controls.enabled"), current.trayEnabled());
    JCheckBox closeToTray =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.closeToTray"), current.trayCloseToTray());
    JCheckBox minimizeToTray =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.minimizeToTray"),
            current.trayMinimizeToTray());
    JCheckBox startMinimized =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.startMinimized"),
            current.trayStartMinimized());

    JCheckBox notifyHighlights =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.notifyHighlights"),
            current.trayNotifyHighlights());
    JCheckBox notifyPrivateMessages =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.notifyPrivateMessages"),
            current.trayNotifyPrivateMessages());
    JCheckBox notifyConnectionState =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.notifyConnectionState"),
            current.trayNotifyConnectionState());

    JCheckBox notifyOnlyWhenUnfocused =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.notifyOnlyWhenUnfocused"),
            current.trayNotifyOnlyWhenUnfocused());
    JCheckBox notifyOnlyWhenMinimizedOrHidden =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.notifyOnlyWhenMinimizedOrHidden"),
            current.trayNotifyOnlyWhenMinimizedOrHidden());
    JCheckBox notifySuppressWhenTargetActive =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.notifySuppressWhenTargetActive"),
            current.trayNotifySuppressWhenTargetActive());
    JCheckBox updateNotifierEnabled =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.updateNotifier.enabled"),
            runtimeConfig == null || runtimeConfig.readUpdateNotifierEnabled(true));
    updateNotifierEnabled.setToolTipText(
        MESSAGES.text("preferences.tray.controls.updateNotifier.tooltip"));
    JCheckBox lagIndicatorEnabled =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.lagIndicator.enabled"),
            runtimeConfig == null || runtimeConfig.readLagIndicatorEnabled(true));
    lagIndicatorEnabled.setToolTipText(
        MESSAGES.text("preferences.tray.controls.lagIndicator.tooltip"));

    boolean linuxTmp = false;
    boolean linuxActionsSupportedTmp = false;
    try {
      linuxTmp = gnomeDbusBackend != null && gnomeDbusBackend.isLinux();
      if (linuxTmp) {
        GnomeDbusNotificationBackend.ProbeResult probeResult = gnomeDbusBackend.probe();
        linuxActionsSupportedTmp =
            probeResult != null
                && probeResult.sessionBusReachable()
                && probeResult.actionsSupported();
      }
    } catch (Exception ignored) {
    }

    final boolean linux = linuxTmp;
    final boolean linuxActionsSupported = linuxActionsSupportedTmp;

    JCheckBox linuxDbusActions =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.linuxDbusActions"),
            linux && linuxActionsSupported && current.trayLinuxDbusActionsEnabled());
    linuxDbusActions.setToolTipText(
        linux
            ? (linuxActionsSupported
                ? MESSAGES.text("preferences.tray.controls.linuxDbusActions.tooltip.supported")
                : MESSAGES.text("preferences.tray.controls.linuxDbusActions.tooltip.unsupported"))
            : MESSAGES.text("preferences.tray.controls.linuxOnly.tooltip"));

    JComboBox<NotificationBackendMode> notificationBackend =
        new JComboBox<>(NotificationBackendMode.values());
    notificationBackend.setSelectedItem(current.trayNotificationBackendMode());
    notificationBackend.setToolTipText(
        MESSAGES.text("preferences.tray.controls.notificationBackend.tooltip"));

    JButton testNotification =
        new JButton(MESSAGES.text("preferences.tray.controls.testNotification"));
    testNotification.setToolTipText(
        MESSAGES.text("preferences.tray.controls.testNotification.tooltip"));
    testNotification.addActionListener(
        e -> {
          try {
            if (trayNotificationService != null) {
              trayNotificationService.notifyTest();
            }
          } catch (Throwable ignored) {
          }
        });

    notifyHighlights.setToolTipText(
        MESSAGES.text("preferences.tray.controls.notifyHighlights.tooltip"));
    notifyPrivateMessages.setToolTipText(
        MESSAGES.text("preferences.tray.controls.notifyPrivateMessages.tooltip"));
    notifyConnectionState.setToolTipText(
        MESSAGES.text("preferences.tray.controls.notifyConnectionState.tooltip"));
    notifyOnlyWhenUnfocused.setToolTipText(
        MESSAGES.text("preferences.tray.controls.notifyOnlyWhenUnfocused.tooltip"));
    notifyOnlyWhenMinimizedOrHidden.setToolTipText(
        MESSAGES.text("preferences.tray.controls.notifyOnlyWhenMinimizedOrHidden.tooltip"));
    notifySuppressWhenTargetActive.setToolTipText(
        MESSAGES.text("preferences.tray.controls.notifySuppressWhenTargetActive.tooltip"));

    NotificationSoundControlsSupport.Controls soundControls =
        NotificationSoundControlsSupport.buildControls(
            NotificationSoundControlsSupport.Request.builder()
                .enabledLabel(MESSAGES.text("preferences.tray.controls.sound.enabled"))
                .enabledSelected(effectiveSoundSettings.enabled())
                .useCustomLabel(MESSAGES.text("preferences.tray.controls.sound.useCustom"))
                .useCustomSelected(effectiveSoundSettings.useCustom())
                .soundId(effectiveSoundSettings.soundId())
                .customPath(effectiveSoundSettings.customPath())
                .browseButtonText(MESSAGES.text("common.button.browse.ellipsis"))
                .clearButtonText(MESSAGES.text("common.button.clear"))
                .testButtonText(MESSAGES.text("preferences.tray.controls.sound.test"))
                .buttonStyle(NotificationSoundControlsSupport.ButtonStyle.TEXT)
                .notificationSoundService(notificationSoundService)
                .soundFileImporter(notificationSoundImporter)
                .availableSupplier(enabled::isSelected)
                .build());
    JCheckBox notificationSoundsEnabled = soundControls.enabled();
    notificationSoundsEnabled.setToolTipText(
        MESSAGES.text("preferences.tray.controls.sound.enabled.tooltip"));
    JCheckBox notificationSoundUseCustom = soundControls.useCustom();
    notificationSoundUseCustom.setToolTipText(
        MESSAGES.text("preferences.tray.controls.sound.useCustom.tooltip"));
    JTextField notificationSoundCustomPath = soundControls.customPath();
    notificationSoundCustomPath.setToolTipText(
        MESSAGES.text("preferences.tray.controls.sound.customPath.tooltip"));
    JComboBox<BuiltInSound> notificationSound = soundControls.builtInSound();
    notificationSound.setToolTipText(
        MESSAGES.text("preferences.tray.controls.sound.builtIn.tooltip"));
    JButton browseCustomSound = soundControls.browseCustom();
    JButton clearCustomSound = soundControls.clearCustom();
    JButton testSound = soundControls.testSound();

    JCheckBox pushyEnabled =
        new JCheckBox(
            MESSAGES.text("preferences.tray.controls.pushy.enabled"),
            Boolean.TRUE.equals(effectivePushySettings.enabled()));
    pushyEnabled.setToolTipText(
        MESSAGES.text("preferences.tray.controls.pushy.enabled.tooltip"));

    JTextField pushyEndpoint =
        new JTextField(
            Objects.toString(effectivePushySettings.endpoint(), "https://api.pushy.me/push"));
    pushyEndpoint.setToolTipText(
        MESSAGES.text("preferences.tray.controls.pushy.endpoint.tooltip"));

    JPasswordField pushyApiKey =
        new JPasswordField(Objects.toString(effectivePushySettings.apiKey(), ""));
    pushyApiKey.setToolTipText(MESSAGES.text("preferences.tray.controls.pushy.apiKey.tooltip"));

    PushyTargetMode pushyInitialTargetMode =
        effectivePushySettings.deviceToken() != null
                && !effectivePushySettings.deviceToken().isBlank()
            ? PushyTargetMode.DEVICE_TOKEN
            : PushyTargetMode.TOPIC;
    String pushyInitialTargetValue =
        pushyInitialTargetMode == PushyTargetMode.DEVICE_TOKEN
            ? Objects.toString(effectivePushySettings.deviceToken(), "")
            : Objects.toString(effectivePushySettings.topic(), "");

    JComboBox<PushyTargetMode> pushyTargetMode = new JComboBox<>(PushyTargetMode.values());
    pushyTargetMode.setSelectedItem(pushyInitialTargetMode);
    pushyTargetMode.setToolTipText(
        MESSAGES.text("preferences.tray.controls.pushy.targetMode.tooltip"));

    JTextField pushyTargetValue = new JTextField(pushyInitialTargetValue);
    pushyTargetValue.setToolTipText(
        MESSAGES.text("preferences.tray.controls.pushy.targetValue.tooltip"));

    JTextField pushyTitlePrefix =
        new JTextField(Objects.toString(effectivePushySettings.titlePrefix(), "IRCafe"));
    pushyTitlePrefix.setToolTipText(
        MESSAGES.text("preferences.tray.controls.pushy.titlePrefix.tooltip"));

    JSpinner pushyConnectTimeoutSeconds =
        PreferencesUiSupport.numberSpinner(
            effectivePushySettings.connectTimeoutSeconds(), 1, 30, 1);
    JSpinner pushyReadTimeoutSeconds =
        PreferencesUiSupport.numberSpinner(effectivePushySettings.readTimeoutSeconds(), 1, 60, 1);

    JButton pushyTest = new JButton(MESSAGES.text("preferences.tray.controls.pushy.test"));
    pushyTest.setToolTipText(MESSAGES.text("preferences.tray.controls.pushy.test.tooltip"));
    JLabel pushyValidationLabel = new JLabel(" ");
    pushyValidationLabel.setForeground(PreferencesUiSupport.errorForeground());
    JLabel pushyTestStatus = new JLabel(" ");

    Runnable refreshPushyValidation =
        () -> {
          PushyTargetMode mode =
              PreferencesUiSupport.selectedComboItem(
                  pushyTargetMode, PushyTargetMode.class, PushyTargetMode.DEVICE_TOKEN);
          String endpoint = PreferencesUiSupport.trimmedText(pushyEndpoint);
          String apiKey = PreferencesUiSupport.trimmedPasswordText(pushyApiKey);
          String target = PreferencesUiSupport.trimmedText(pushyTargetValue);
          String error =
              validatePushyInputs(pushyEnabled.isSelected(), endpoint, apiKey, mode, target);
          if (error == null) {
            pushyValidationLabel.setText(" ");
            pushyValidationLabel.setVisible(false);
            pushyTest.setEnabled(pushyEnabled.isSelected());
          } else {
            pushyValidationLabel.setText(error);
            pushyValidationLabel.setVisible(true);
            pushyTest.setEnabled(false);
          }
        };

    pushyTest.addActionListener(
        e -> {
          PushyTargetMode mode =
              PreferencesUiSupport.selectedComboItem(
                  pushyTargetMode, PushyTargetMode.class, PushyTargetMode.DEVICE_TOKEN);
          String endpoint = PreferencesUiSupport.trimmedText(pushyEndpoint);
          String apiKey = PreferencesUiSupport.trimmedPasswordText(pushyApiKey);
          String target = PreferencesUiSupport.trimmedText(pushyTargetValue);
          String titlePrefix = PreferencesUiSupport.trimmedText(pushyTitlePrefix);
          int connectSeconds = PreferencesUiSupport.spinnerInt(pushyConnectTimeoutSeconds);
          int readSeconds = PreferencesUiSupport.spinnerInt(pushyReadTimeoutSeconds);

          String error =
              validatePushyInputs(pushyEnabled.isSelected(), endpoint, apiKey, mode, target);
          if (error != null) {
            pushyTestStatus.setText(error);
            pushyTestStatus.setForeground(PreferencesUiSupport.errorForeground());
            return;
          }

          String deviceToken = mode == PushyTargetMode.DEVICE_TOKEN ? target : null;
          String topic = mode == PushyTargetMode.TOPIC ? target : null;
          PushyProperties draft =
              new PushyProperties(
                  pushyEnabled.isSelected(),
                  endpoint.isBlank() ? null : endpoint,
                  apiKey.isBlank() ? null : apiKey,
                  deviceToken,
                  topic,
                  titlePrefix.isBlank() ? null : titlePrefix,
                  connectSeconds,
                  readSeconds);

          pushyTest.setEnabled(false);
          pushyTestStatus.setText(MESSAGES.text("preferences.tray.controls.pushy.status.sending"));
          pushyTestStatus.setForeground(UIManager.getColor(UiColorKeys.LABEL_FOREGROUND));

          pushyTestExecutor.submit(
              () -> {
                PushyNotificationPort.PushResult result =
                    pushyNotificationService != null
                        ? pushyNotificationService.sendTestNotification(
                            draft,
                            MESSAGES.text("preferences.tray.controls.pushy.test.title"),
                            MESSAGES.text("preferences.tray.controls.pushy.test.body"))
                        : PushyNotificationPort.PushResult.failed(
                            MESSAGES.text(
                                "preferences.tray.controls.pushy.status.serviceUnavailable"));
                SwingUtilities.invokeLater(
                    () -> {
                      pushyTestStatus.setText(
                          result.message() == null || result.message().isBlank()
                              ? (result.success()
                                  ? MESSAGES.text("preferences.tray.controls.pushy.status.sent")
                                  : MESSAGES.text("preferences.tray.controls.pushy.status.failed"))
                              : result.message());
                      pushyTestStatus.setForeground(
                          result.success()
                              ? UIManager.getColor(UiColorKeys.LABEL_FOREGROUND)
                              : PreferencesUiSupport.errorForeground());
                      refreshPushyValidation.run();
                    });
              });
        });

    Runnable refreshPushyDestinationState =
        () -> {
          PushyTargetMode mode =
              PreferencesUiSupport.selectedComboItem(
                  pushyTargetMode, PushyTargetMode.class, PushyTargetMode.DEVICE_TOKEN);
          if (mode == PushyTargetMode.DEVICE_TOKEN) {
            pushyTargetValue.setToolTipText(
                MESSAGES.text(
                    "preferences.tray.controls.pushy.targetValue.deviceToken.tooltip"));
          } else {
            pushyTargetValue.setToolTipText(
                MESSAGES.text("preferences.tray.controls.pushy.targetValue.topic.tooltip"));
          }
        };

    Runnable refreshPushyState =
        () -> {
          boolean enabledState = pushyEnabled.isSelected();
          pushyEndpoint.setEnabled(enabledState);
          pushyApiKey.setEnabled(enabledState);
          pushyTargetMode.setEnabled(enabledState);
          pushyTargetValue.setEnabled(enabledState);
          pushyTitlePrefix.setEnabled(enabledState);
          pushyConnectTimeoutSeconds.setEnabled(enabledState);
          pushyReadTimeoutSeconds.setEnabled(enabledState);
          refreshPushyDestinationState.run();
          refreshPushyValidation.run();
        };
    pushyEnabled.addActionListener(e -> refreshPushyState.run());
    pushyTargetMode.addActionListener(e -> refreshPushyState.run());
    pushyEndpoint
        .getDocument()
        .addDocumentListener(new SettingsDocumentListener(refreshPushyValidation));
    pushyApiKey
        .getDocument()
        .addDocumentListener(new SettingsDocumentListener(refreshPushyValidation));
    pushyTargetValue
        .getDocument()
        .addDocumentListener(new SettingsDocumentListener(refreshPushyValidation));
    refreshPushyState.run();

    Runnable refreshEnabled =
        () -> {
          boolean enabledState = enabled.isSelected();
          closeToTray.setEnabled(enabledState);
          minimizeToTray.setEnabled(enabledState);
          startMinimized.setEnabled(enabledState);
          notifyHighlights.setEnabled(enabledState);
          notifyPrivateMessages.setEnabled(enabledState);
          notifyConnectionState.setEnabled(enabledState);
          notifyOnlyWhenUnfocused.setEnabled(enabledState);
          notifyOnlyWhenMinimizedOrHidden.setEnabled(enabledState);
          notifySuppressWhenTargetActive.setEnabled(enabledState);
          linuxDbusActions.setEnabled(enabledState && linux && linuxActionsSupported);
          notificationBackend.setEnabled(enabledState);
          testNotification.setEnabled(enabledState);

          if (!enabledState) {
            closeToTray.setSelected(false);
            minimizeToTray.setSelected(false);
            startMinimized.setSelected(false);
            notifyHighlights.setSelected(false);
            notifyPrivateMessages.setSelected(false);
            notifyConnectionState.setSelected(false);
            notifyOnlyWhenUnfocused.setSelected(false);
            notifyOnlyWhenMinimizedOrHidden.setSelected(false);
            notifySuppressWhenTargetActive.setSelected(false);
            linuxDbusActions.setSelected(false);
            notificationSoundsEnabled.setSelected(false);
          }

          if (!(linux && linuxActionsSupported)) {
            linuxDbusActions.setSelected(false);
          }
          soundControls.refresh();
        };

    enabled.addActionListener(e -> refreshEnabled.run());
    notificationSoundsEnabled.addActionListener(e -> refreshEnabled.run());
    notificationSoundUseCustom.addActionListener(e -> refreshEnabled.run());
    refreshEnabled.run();

    TrayControls controls =
        new TrayControls(
            enabled,
            closeToTray,
            minimizeToTray,
            startMinimized,
            notifyHighlights,
            notifyPrivateMessages,
            notifyConnectionState,
            notifyOnlyWhenUnfocused,
            notifyOnlyWhenMinimizedOrHidden,
            notifySuppressWhenTargetActive,
            updateNotifierEnabled,
            lagIndicatorEnabled,
            linuxDbusActions,
            notificationBackend,
            testNotification,
            notificationSoundsEnabled,
            notificationSoundUseCustom,
            notificationSoundCustomPath,
            browseCustomSound,
            clearCustomSound,
            notificationSound,
            testSound,
            pushyEnabled,
            pushyEndpoint,
            pushyApiKey,
            pushyTargetMode,
            pushyTargetValue,
            pushyTitlePrefix,
            pushyConnectTimeoutSeconds,
            pushyReadTimeoutSeconds,
            pushyValidationLabel,
            pushyTest,
            pushyTestStatus);
    controls.panel =
        TrayNotificationsPanelSupport.buildTabsPanel(
            controls, runtimeConfig, linux, linuxActionsSupported);
    return controls;
  }

  public static TraySettings readSettings(TrayControls controls) {
    boolean trayEnabled = controls.enabled.isSelected();
    NotificationBackendMode notificationBackendMode =
        PreferencesUiSupport.selectedComboItem(
            controls.notificationBackend,
            NotificationBackendMode.class,
            NotificationBackendMode.AUTO);

    return new TraySettings(
        trayEnabled,
        trayEnabled && controls.closeToTray.isSelected(),
        trayEnabled && controls.minimizeToTray.isSelected(),
        trayEnabled && controls.startMinimized.isSelected(),
        trayEnabled && controls.notifyHighlights.isSelected(),
        trayEnabled && controls.notifyPrivateMessages.isSelected(),
        trayEnabled && controls.notifyConnectionState.isSelected(),
        trayEnabled && controls.notifyOnlyWhenUnfocused.isSelected(),
        trayEnabled && controls.notifyOnlyWhenMinimizedOrHidden.isSelected(),
        trayEnabled && controls.notifySuppressWhenTargetActive.isSelected(),
        trayEnabled && controls.linuxDbusActions.isSelected(),
        notificationBackendMode,
        controls.updateNotifierEnabled.isSelected(),
        controls.lagIndicatorEnabled.isSelected(),
        readNotificationSoundSettings(controls, trayEnabled),
        readPushySettings(controls));
  }

  public static void rememberSettings(
      TrayRuntimeConfigPort runtimeConfig,
      NotificationSoundSettingsBus notificationSoundSettingsBus,
      PushySettingsBus pushySettingsBus,
      UpdateNotifierService updateNotifierService,
      LagIndicatorService lagIndicatorService,
      TrayService trayService,
      TraySettings settings) {
    runtimeConfig.rememberTrayEnabled(settings.trayEnabled());
    runtimeConfig.rememberTrayCloseToTray(settings.trayCloseToTray());
    runtimeConfig.rememberTrayMinimizeToTray(settings.trayMinimizeToTray());
    runtimeConfig.rememberTrayStartMinimized(settings.trayStartMinimized());
    runtimeConfig.rememberTrayNotifyHighlights(settings.trayNotifyHighlights());
    runtimeConfig.rememberTrayNotifyPrivateMessages(settings.trayNotifyPrivateMessages());
    runtimeConfig.rememberTrayNotifyConnectionState(settings.trayNotifyConnectionState());
    runtimeConfig.rememberTrayNotifyOnlyWhenUnfocused(settings.trayNotifyOnlyWhenUnfocused());
    runtimeConfig.rememberTrayNotifyOnlyWhenMinimizedOrHidden(
        settings.trayNotifyOnlyWhenMinimizedOrHidden());
    runtimeConfig.rememberTrayNotifySuppressWhenTargetActive(
        settings.trayNotifySuppressWhenTargetActive());
    runtimeConfig.rememberTrayLinuxDbusActionsEnabled(settings.trayLinuxDbusActionsEnabled());
    runtimeConfig.rememberTrayNotificationBackend(settings.trayNotificationBackendMode().token());

    NotificationSoundSettings soundSettings = settings.notificationSoundSettings();
    if (notificationSoundSettingsBus != null) {
      notificationSoundSettingsBus.set(soundSettings);
    }
    runtimeConfig.rememberTrayNotificationSoundsEnabled(soundSettings.enabled());
    runtimeConfig.rememberTrayNotificationSound(soundSettings.soundId());
    runtimeConfig.rememberTrayNotificationSoundUseCustom(soundSettings.useCustom());
    runtimeConfig.rememberTrayNotificationSoundCustomPath(soundSettings.customPath());

    runtimeConfig.rememberUpdateNotifierEnabled(settings.updateNotifierEnabled());
    runtimeConfig.rememberLagIndicatorEnabled(settings.lagIndicatorEnabled());
    if (updateNotifierService != null) {
      updateNotifierService.setEnabled(settings.updateNotifierEnabled());
    }
    if (lagIndicatorService != null) {
      lagIndicatorService.setEnabled(settings.lagIndicatorEnabled());
    }

    if (pushySettingsBus != null) {
      pushySettingsBus.set(settings.pushySettings());
    }
    runtimeConfig.rememberPushySettings(settings.pushySettings());

    if (trayService != null) {
      trayService.applySettings();
    }
  }

  static String validatePushyInputs(
      boolean enabled,
      String endpoint,
      String apiKey,
      PushyTargetMode targetMode,
      String targetValue) {
    if (!enabled) return null;

    String key = SettingsValueSupport.trimmedString(apiKey);
    if (key.isEmpty()) {
      return MESSAGES.text("preferences.tray.controls.pushy.validation.apiKeyRequired");
    }

    String target = SettingsValueSupport.trimmedString(targetValue);
    if (target.isEmpty()) {
      return switch (targetMode) {
        case TOPIC -> MESSAGES.text("preferences.tray.controls.pushy.validation.topicRequired");
        case DEVICE_TOKEN ->
            MESSAGES.text("preferences.tray.controls.pushy.validation.deviceTokenRequired");
      };
    }

    String trimmedEndpoint = SettingsValueSupport.trimmedString(endpoint);
    if (!trimmedEndpoint.isEmpty() && !isValidPushyEndpoint(trimmedEndpoint)) {
      return MESSAGES.text("preferences.tray.controls.pushy.validation.endpointInvalid");
    }

    return null;
  }

  private static NotificationSoundSettings readNotificationSoundSettings(
      TrayControls controls, boolean trayEnabled) {
    boolean enabled = trayEnabled && controls.notificationSoundsEnabled.isSelected();
    BuiltInSound selectedSound =
        PreferencesUiSupport.selectedComboItem(
            controls.notificationSound, BuiltInSound.class, BuiltInSound.NOTIF_1);
    String soundId = selectedSound != null ? selectedSound.name() : BuiltInSound.NOTIF_1.name();
    boolean useCustom = controls.notificationSoundUseCustom.isSelected();
    String customPath = PreferencesUiSupport.trimmedText(controls.notificationSoundCustomPath);
    if (customPath.isBlank()) customPath = null;
    if (useCustom && customPath == null) useCustom = false;
    return new NotificationSoundSettings(enabled, soundId, useCustom, customPath);
  }

  private static PushyProperties readPushySettings(TrayControls controls) {
    boolean enabled = controls.pushyEnabled.isSelected();
    String endpoint = PreferencesUiSupport.trimmedText(controls.pushyEndpoint);
    String apiKey = PreferencesUiSupport.trimmedPasswordText(controls.pushyApiKey);
    PushyTargetMode targetMode =
        PreferencesUiSupport.selectedComboItem(
            controls.pushyTargetMode, PushyTargetMode.class, PushyTargetMode.DEVICE_TOKEN);
    String targetValue = PreferencesUiSupport.trimmedText(controls.pushyTargetValue);
    String titlePrefix = PreferencesUiSupport.trimmedText(controls.pushyTitlePrefix);
    int connectTimeoutSeconds =
        PreferencesUiSupport.spinnerInt(controls.pushyConnectTimeoutSeconds);
    int readTimeoutSeconds = PreferencesUiSupport.spinnerInt(controls.pushyReadTimeoutSeconds);

    String validationError =
        validatePushyInputs(enabled, endpoint, apiKey, targetMode, targetValue);
    if (validationError != null) {
      throw new TraySettingsException(
          MESSAGES.text("preferences.tray.controls.pushy.validation.title"), validationError);
    }

    String deviceToken =
        targetMode == PushyTargetMode.DEVICE_TOKEN && !targetValue.isBlank() ? targetValue : null;
    String topic =
        targetMode == PushyTargetMode.TOPIC && !targetValue.isBlank() ? targetValue : null;

    return new PushyProperties(
        enabled,
        endpoint.isBlank() ? null : endpoint,
        apiKey.isBlank() ? null : apiKey,
        deviceToken,
        topic,
        titlePrefix.isBlank() ? null : titlePrefix,
        connectTimeoutSeconds,
        readTimeoutSeconds);
  }

  public record TraySettings(
      boolean trayEnabled,
      boolean trayCloseToTray,
      boolean trayMinimizeToTray,
      boolean trayStartMinimized,
      boolean trayNotifyHighlights,
      boolean trayNotifyPrivateMessages,
      boolean trayNotifyConnectionState,
      boolean trayNotifyOnlyWhenUnfocused,
      boolean trayNotifyOnlyWhenMinimizedOrHidden,
      boolean trayNotifySuppressWhenTargetActive,
      boolean trayLinuxDbusActionsEnabled,
      NotificationBackendMode trayNotificationBackendMode,
      boolean updateNotifierEnabled,
      boolean lagIndicatorEnabled,
      NotificationSoundSettings notificationSoundSettings,
      PushyProperties pushySettings) {
    public TraySettings {
      if (trayNotificationBackendMode == null) {
        trayNotificationBackendMode = NotificationBackendMode.AUTO;
      }
      if (notificationSoundSettings == null) {
        notificationSoundSettings =
            new NotificationSoundSettings(false, BuiltInSound.NOTIF_1.name(), false, null);
      }
      if (pushySettings == null) {
        pushySettings = new PushyProperties(false, null, null, null, null, null, null, null);
      }
    }
  }

  public static final class TraySettingsException extends IllegalArgumentException {
    private final String title;

    private TraySettingsException(String title, String message) {
      super(message);
      this.title = title;
    }

    public String title() {
      return title;
    }
  }

  private static boolean isValidPushyEndpoint(String endpoint) {
    try {
      URI uri = URI.create(SettingsValueSupport.trimmedString(endpoint));
      String scheme = SettingsValueSupport.lowerTrimmedString(uri.getScheme());
      String host = SettingsValueSupport.trimmedString(uri.getHost());
      return ("https".equals(scheme) || "http".equals(scheme)) && !host.isBlank();
    } catch (Exception ignored) {
      return false;
    }
  }
}
