package cafe.woden.ircclient.ui.settings.tray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cafe.woden.ircclient.config.PushyPropertiesTestFixtures;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.config.properties.PushyProperties;
import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.notify.pushy.PushySettingsBus;
import cafe.woden.ircclient.notify.sound.NotificationSoundSettings;
import cafe.woden.ircclient.notify.sound.NotificationSoundSettingsBus;
import cafe.woden.ircclient.ui.settings.NotificationBackendMode;
import cafe.woden.ircclient.ui.shell.LagIndicatorService;
import cafe.woden.ircclient.ui.shell.UpdateNotifierService;
import cafe.woden.ircclient.ui.tray.TrayService;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class TrayControlsSupportTest {

  @Test
  void readSettingsGatesTrayBehaviorsWhenTrayDisabled() {
    TrayControls controls = trayControls();
    controls.enabled.setSelected(false);
    controls.closeToTray.setSelected(true);
    controls.minimizeToTray.setSelected(true);
    controls.startMinimized.setSelected(true);
    controls.notifyHighlights.setSelected(true);
    controls.notifyPrivateMessages.setSelected(true);
    controls.notifyConnectionState.setSelected(true);
    controls.notifyOnlyWhenUnfocused.setSelected(true);
    controls.notifyOnlyWhenMinimizedOrHidden.setSelected(true);
    controls.notifySuppressWhenTargetActive.setSelected(true);
    controls.linuxDbusActions.setSelected(true);
    controls.notificationSoundsEnabled.setSelected(true);
    controls.updateNotifierEnabled.setSelected(true);
    controls.lagIndicatorEnabled.setSelected(true);

    TrayControlsSupport.TraySettings settings = TrayControlsSupport.readSettings(controls);

    assertFalse(settings.trayEnabled());
    assertFalse(settings.trayCloseToTray());
    assertFalse(settings.trayMinimizeToTray());
    assertFalse(settings.trayStartMinimized());
    assertFalse(settings.trayNotifyHighlights());
    assertFalse(settings.trayNotifyPrivateMessages());
    assertFalse(settings.trayNotifyConnectionState());
    assertFalse(settings.trayNotifyOnlyWhenUnfocused());
    assertFalse(settings.trayNotifyOnlyWhenMinimizedOrHidden());
    assertFalse(settings.trayNotifySuppressWhenTargetActive());
    assertFalse(settings.trayLinuxDbusActionsEnabled());
    assertFalse(settings.notificationSoundSettings().enabled());
    assertTrue(settings.updateNotifierEnabled());
    assertTrue(settings.lagIndicatorEnabled());
  }

  @Test
  void readSettingsNormalizesNotificationSoundAndPushyTopic() {
    TrayControls controls = trayControls();
    controls.enabled.setSelected(true);
    controls.notificationBackend.setSelectedItem(NotificationBackendMode.NATIVE_ONLY);
    controls.notificationSoundsEnabled.setSelected(true);
    controls.notificationSound.setSelectedItem(BuiltInSound.NOTIF_2);
    controls.notificationSoundUseCustom.setSelected(true);
    controls.notificationSoundCustomPath.setText(" custom.wav ");
    controls.pushyEnabled.setSelected(true);
    controls.pushyEndpoint.setText(" https://push.example/push ");
    controls.pushyApiKey.setText(" secret ");
    controls.pushyTargetMode.setSelectedItem(PushyTargetMode.TOPIC);
    controls.pushyTargetValue.setText(" topic-name ");
    controls.pushyTitlePrefix.setText(" IRCafe Prod ");
    controls.pushyConnectTimeoutSeconds.setValue(12);
    controls.pushyReadTimeoutSeconds.setValue(34);

    TrayControlsSupport.TraySettings settings = TrayControlsSupport.readSettings(controls);

    assertEquals(NotificationBackendMode.NATIVE_ONLY, settings.trayNotificationBackendMode());
    NotificationSoundSettings sound = settings.notificationSoundSettings();
    assertTrue(sound.enabled());
    assertEquals(BuiltInSound.NOTIF_2.name(), sound.soundId());
    assertTrue(sound.useCustom());
    assertEquals("custom.wav", sound.customPath());

    PushyProperties pushy = settings.pushySettings();
    assertTrue(pushy.enabled());
    assertEquals("https://push.example/push", pushy.endpoint());
    assertEquals("secret", pushy.apiKey());
    assertNull(pushy.deviceToken());
    assertEquals("topic-name", pushy.topic());
    assertEquals("IRCafe Prod", pushy.titlePrefix());
    assertEquals(12, pushy.connectTimeoutSeconds());
    assertEquals(34, pushy.readTimeoutSeconds());
  }

  @Test
  void readSettingsThrowsTitledPushyValidationError() {
    TrayControls controls = trayControls();
    controls.pushyEnabled.setSelected(true);
    controls.pushyApiKey.setText("");
    controls.pushyTargetValue.setText("device-token");

    TrayControlsSupport.TraySettingsException ex =
        assertThrows(
            TrayControlsSupport.TraySettingsException.class,
            () -> TrayControlsSupport.readSettings(controls));

    assertEquals("Invalid Pushy settings", ex.title());
    assertEquals("Pushy API key is required.", ex.getMessage());
  }

  @Test
  void validatePushyInputsTrimsEndpointAndRequiredFields() {
    assertNull(
        TrayControlsSupport.validatePushyInputs(
            true,
            " https://push.example/push ",
            " secret ",
            PushyTargetMode.DEVICE_TOKEN,
            " device-token "));
    assertEquals(
        "Pushy endpoint must be a valid http(s) URL.",
        TrayControlsSupport.validatePushyInputs(
            true, " ftp://push.example/push ", "secret", PushyTargetMode.TOPIC, "topic"));
  }

  @Test
  void rememberSettingsPersistsTraySettingsAndUpdatesServices() {
    RuntimeConfigStore runtimeConfig = mock(RuntimeConfigStore.class);
    NotificationSoundSettingsBus soundBus = mock(NotificationSoundSettingsBus.class);
    PushySettingsBus pushyBus = mock(PushySettingsBus.class);
    UpdateNotifierService updateNotifierService = mock(UpdateNotifierService.class);
    LagIndicatorService lagIndicatorService = mock(LagIndicatorService.class);
    TrayService trayService = mock(TrayService.class);
    NotificationSoundSettings soundSettings =
        new NotificationSoundSettings(true, BuiltInSound.NOTIF_2.name(), false, null);
    PushyProperties pushySettings =
        PushyPropertiesTestFixtures.builder()
            .enabled(true)
            .endpoint("https://push.example/push")
            .apiKey("secret")
            .deviceToken("device-token")
            .titlePrefix("IRCafe")
            .connectTimeoutSeconds(5)
            .readTimeoutSeconds(8)
            .build();
    TrayControlsSupport.TraySettings settings =
        new TrayControlsSupport.TraySettings(
            true,
            true,
            false,
            true,
            true,
            false,
            true,
            true,
            false,
            true,
            true,
            NotificationBackendMode.TWO_SLICES_ONLY,
            true,
            false,
            soundSettings,
            pushySettings);

    TrayControlsSupport.rememberSettings(
        runtimeConfig,
        soundBus,
        pushyBus,
        updateNotifierService,
        lagIndicatorService,
        trayService,
        settings);

    verify(runtimeConfig).rememberTrayEnabled(true);
    verify(runtimeConfig).rememberTrayCloseToTray(true);
    verify(runtimeConfig).rememberTrayMinimizeToTray(false);
    verify(runtimeConfig).rememberTrayStartMinimized(true);
    verify(runtimeConfig).rememberTrayNotifyHighlights(true);
    verify(runtimeConfig).rememberTrayNotifyPrivateMessages(false);
    verify(runtimeConfig).rememberTrayNotifyConnectionState(true);
    verify(runtimeConfig).rememberTrayNotifyOnlyWhenUnfocused(true);
    verify(runtimeConfig).rememberTrayNotifyOnlyWhenMinimizedOrHidden(false);
    verify(runtimeConfig).rememberTrayNotifySuppressWhenTargetActive(true);
    verify(runtimeConfig).rememberTrayLinuxDbusActionsEnabled(true);
    verify(runtimeConfig).rememberTrayNotificationBackend("two-slices-only");
    verify(soundBus).set(soundSettings);
    verify(runtimeConfig).rememberTrayNotificationSoundsEnabled(true);
    verify(runtimeConfig).rememberTrayNotificationSound(BuiltInSound.NOTIF_2.name());
    verify(runtimeConfig).rememberTrayNotificationSoundUseCustom(false);
    verify(runtimeConfig).rememberTrayNotificationSoundCustomPath(null);
    verify(runtimeConfig).rememberUpdateNotifierEnabled(true);
    verify(runtimeConfig).rememberLagIndicatorEnabled(false);
    verify(updateNotifierService).setEnabled(true);
    verify(lagIndicatorService).setEnabled(false);
    verify(pushyBus).set(pushySettings);
    verify(runtimeConfig).rememberPushySettings(pushySettings);
    verify(trayService).applySettings();
  }

  private static TrayControls trayControls() {
    JComboBox<NotificationBackendMode> notificationBackend =
        new JComboBox<>(NotificationBackendMode.values());
    notificationBackend.setSelectedItem(NotificationBackendMode.AUTO);
    JComboBox<BuiltInSound> notificationSound = new JComboBox<>(BuiltInSound.values());
    notificationSound.setSelectedItem(BuiltInSound.NOTIF_1);
    JComboBox<PushyTargetMode> pushyTargetMode = new JComboBox<>(PushyTargetMode.values());
    pushyTargetMode.setSelectedItem(PushyTargetMode.DEVICE_TOKEN);

    return new TrayControls(
        box(false),
        box(false),
        box(false),
        box(false),
        box(false),
        box(false),
        box(false),
        box(false),
        box(false),
        box(false),
        box(false),
        box(false),
        box(false),
        notificationBackend,
        new JButton("Test notification"),
        box(false),
        box(false),
        new JTextField(""),
        new JButton("Browse"),
        new JButton("Clear"),
        notificationSound,
        new JButton("Test sound"),
        box(false),
        new JTextField(""),
        new JPasswordField(""),
        pushyTargetMode,
        new JTextField(""),
        new JTextField(""),
        spinner(5),
        spinner(8),
        new JLabel(" "),
        new JButton("Test Pushy"),
        new JLabel(" "));
  }

  private static JCheckBox box(boolean selected) {
    JCheckBox box = new JCheckBox();
    box.setSelected(selected);
    return box;
  }

  private static JSpinner spinner(int value) {
    return new JSpinner(new SpinnerNumberModel(value, 1, 100, 1));
  }
}
