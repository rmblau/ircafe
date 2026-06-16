package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.app.api.ActiveTargetPort;
import cafe.woden.ircclient.app.commands.UserCommandAliasesPort;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsBus;
import cafe.woden.ircclient.config.RuntimeConfigStore;
import cafe.woden.ircclient.config.api.ChatLoggingRuntimeConfigPort;
import cafe.woden.ircclient.config.api.CtcpReplyRuntimeConfigPort;
import cafe.woden.ircclient.config.api.DiagnosticsRuntimeConfigPort;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort;
import cafe.woden.ircclient.config.api.FilterSettingsConfigPort;
import cafe.woden.ircclient.config.api.NickColorRuntimeConfigPort;
import cafe.woden.ircclient.config.api.NotificationRuntimeConfigPort;
import cafe.woden.ircclient.config.api.OutgoingMessageRuntimeConfigPort;
import cafe.woden.ircclient.config.api.SpellcheckRuntimeConfigPort;
import cafe.woden.ircclient.config.api.TimestampRuntimeConfigPort;
import cafe.woden.ircclient.config.api.UserCommandAliasesConfigPort;
import cafe.woden.ircclient.config.properties.LogProperties;
import cafe.woden.ircclient.irc.backend.IrcHeartbeatMaintenanceService;
import cafe.woden.ircclient.notifications.api.IrcEventNotificationRulesPort;
import cafe.woden.ircclient.notify.api.NotificationSoundPort;
import cafe.woden.ircclient.notify.api.PushyNotificationPort;
import cafe.woden.ircclient.notify.pushy.PushySettingsBus;
import cafe.woden.ircclient.notify.sound.NotificationSoundSettingsBus;
import cafe.woden.ircclient.ui.chat.NickColorService;
import cafe.woden.ircclient.ui.chat.NickColorSettingsBus;
import cafe.woden.ircclient.ui.chat.embed.EmbedLoadPolicyBus;
import cafe.woden.ircclient.ui.chat.transcript.rebuild.TranscriptRebuildService;
import cafe.woden.ircclient.ui.filter.FilterSettingsBus;
import cafe.woden.ircclient.ui.nickcolors.NickColorOverridesDialog;
import cafe.woden.ircclient.ui.servers.ServerDialogs;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ChatThemeSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeAccentSettingsBus;
import cafe.woden.ircclient.ui.settings.theme.ThemeManager;
import cafe.woden.ircclient.ui.settings.theme.ThemeTweakSettingsBus;
import cafe.woden.ircclient.ui.shell.LagIndicatorService;
import cafe.woden.ircclient.ui.shell.UpdateNotifierService;
import cafe.woden.ircclient.ui.tray.TrayNotificationService;
import cafe.woden.ircclient.ui.tray.TrayService;
import cafe.woden.ircclient.ui.tray.dbus.GnomeDbusNotificationBackend;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.Test;

class PreferencesDialogConstructorTest {

  @Test
  void acceptsActiveExecutors() {
    ExecutorService pushyExec = mock(ExecutorService.class);
    ExecutorService ruleExec = mock(ExecutorService.class);
    when(pushyExec.isShutdown()).thenReturn(false);
    when(ruleExec.isShutdown()).thenReturn(false);

    PreferencesDialog dialog = newDialog(pushyExec, ruleExec);
    assertNotNull(dialog);
  }

  @Test
  void rejectsShutdownPushyExecutor() {
    ExecutorService pushyExec = mock(ExecutorService.class);
    ExecutorService ruleExec = mock(ExecutorService.class);
    when(pushyExec.isShutdown()).thenReturn(true);
    when(ruleExec.isShutdown()).thenReturn(false);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> newDialog(pushyExec, ruleExec));
    assertEquals("pushyTestExecutor must be active", ex.getMessage());
  }

  @Test
  void rejectsShutdownRuleExecutor() {
    ExecutorService pushyExec = mock(ExecutorService.class);
    ExecutorService ruleExec = mock(ExecutorService.class);
    when(pushyExec.isShutdown()).thenReturn(false);
    when(ruleExec.isShutdown()).thenReturn(true);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> newDialog(pushyExec, ruleExec));
    assertEquals("notificationRuleTestExecutor must be active", ex.getMessage());
  }

  private static PreferencesDialog newDialog(
      ExecutorService pushyTestExecutor, ExecutorService notificationRuleTestExecutor) {
    return new PreferencesDialog(
        mock(UiSettingsBus.class),
        mock(EmbedCardStyleBus.class),
        mock(ThemeManager.class),
        mock(ThemeAccentSettingsBus.class),
        mock(ThemeTweakSettingsBus.class),
        mock(ChatThemeSettingsBus.class),
        mock(SpellcheckSettingsBus.class),
        mock(RuntimeConfigStore.class),
        mock(ChatLoggingRuntimeConfigPort.class),
        mock(DiagnosticsRuntimeConfigPort.class),
        mock(FilterSettingsConfigPort.class),
        mock(EmbedLoadPolicyConfigPort.class),
        mock(CtcpReplyRuntimeConfigPort.class),
        mock(OutgoingMessageRuntimeConfigPort.class),
        mock(TimestampRuntimeConfigPort.class),
        mock(SpellcheckRuntimeConfigPort.class),
        mock(NickColorRuntimeConfigPort.class),
        mock(UserCommandAliasesConfigPort.class),
        mock(NotificationRuntimeConfigPort.class),
        mock(LogProperties.class),
        mock(NickColorSettingsBus.class),
        mock(NickColorService.class),
        mock(NickColorOverridesDialog.class),
        mock(EmbedLoadPolicyDialog.class),
        mock(EmbedLoadPolicyBus.class),
        mock(IrcHeartbeatMaintenanceService.class),
        mock(FilterSettingsBus.class),
        mock(TranscriptRebuildService.class),
        mock(ActiveTargetPort.class),
        mock(TrayService.class),
        mock(TrayNotificationService.class),
        mock(UpdateNotifierService.class),
        mock(LagIndicatorService.class),
        mock(GnomeDbusNotificationBackend.class),
        mock(NotificationSoundSettingsBus.class),
        mock(PushySettingsBus.class),
        mock(PushyNotificationPort.class),
        mock(IrcEventNotificationRulesPort.class),
        mock(UserCommandAliasesPort.class),
        mock(NotificationSoundPort.class),
        mock(ServerDialogs.class),
        mock(MessageTranslationSettingsBus.class),
        pushyTestExecutor,
        notificationRuleTestExecutor);
  }
}
