package cafe.woden.ircclient.notifications;

import static cafe.woden.ircclient.notifications.IrcEventNotificationRuleTestFixtures.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.app.api.TrayNotificationsPort;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notify.api.PushyNotificationPort;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class IrcEventNotificationServiceTest {

  @Test
  void appliesAllMatchingRulesForSameEventType() {
    IrcEventNotificationRulesBus rulesBus = mock(IrcEventNotificationRulesBus.class);
    TrayNotificationsPort tray = mock(TrayNotificationsPort.class);
    NotificationStore store = mock(NotificationStore.class);
    PushyNotificationPort pushy = null;

    IrcEventNotificationRule statusRule =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.KLINED)
            .sourceMode(IrcEventNotificationRule.SourceMode.ANY)
            .sourcePattern(null)
            .channelScope(IrcEventNotificationRule.ChannelScope.ALL)
            .channelPatterns(null)
            .toastEnabled(false)
            .focusScope(IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY)
            .statusBarEnabled(true)
            .notificationsNodeEnabled(true)
            .soundEnabled(false)
            .soundId("NOTIF_1")
            .soundUseCustom(false)
            .soundCustomPath(null)
            .scriptEnabled(false)
            .scriptPath(null)
            .scriptArgs(null)
            .scriptWorkingDirectory(null)
            .build();

    IrcEventNotificationRule soundRule =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.KLINED)
            .sourceMode(IrcEventNotificationRule.SourceMode.ANY)
            .sourcePattern(null)
            .channelScope(IrcEventNotificationRule.ChannelScope.ALL)
            .channelPatterns(null)
            .toastEnabled(false)
            .focusScope(IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY)
            .statusBarEnabled(false)
            .notificationsNodeEnabled(false)
            .soundEnabled(true)
            .soundId("NOTIF_3")
            .soundUseCustom(false)
            .soundCustomPath(null)
            .scriptEnabled(false)
            .scriptPath(null)
            .scriptArgs(null)
            .scriptWorkingDirectory(null)
            .build();

    when(rulesBus.get()).thenReturn(List.of(statusRule, soundRule));
    ExecutorService exec = Executors.newSingleThreadExecutor();
    try {
      IrcEventNotificationService service =
          new IrcEventNotificationService(rulesBus, tray, store, pushy, exec);

      boolean matched =
          service.notifyConfigured(
              IrcEventNotificationRule.EventType.KLINED,
              "libera",
              null,
              "alice",
              Boolean.FALSE,
              "User restricted",
              "alice appears restricted",
              "libera",
              "#general");

      assertTrue(matched);
      verify(tray, times(2))
          .notifyCustom(
              eq("libera"),
              eq("status"),
              anyString(),
              anyString(),
              org.mockito.ArgumentMatchers.anyBoolean(),
              org.mockito.ArgumentMatchers.anyBoolean(),
              org.mockito.ArgumentMatchers.any(IrcEventNotificationRule.FocusScope.class),
              org.mockito.ArgumentMatchers.anyBoolean(),
              anyString(),
              org.mockito.ArgumentMatchers.anyBoolean(),
              org.mockito.ArgumentMatchers.isNull());
      verify(store, times(1))
          .recordIrcEvent(eq("libera"), eq("status"), eq("alice"), anyString(), anyString());
    } finally {
      exec.shutdownNow();
    }
  }

  @Test
  void activeChannelOnlyScopeRequiresActiveTargetChannelOnSameServer() {
    IrcEventNotificationRulesBus rulesBus = mock(IrcEventNotificationRulesBus.class);
    TrayNotificationsPort tray = mock(TrayNotificationsPort.class);
    NotificationStore store = mock(NotificationStore.class);
    PushyNotificationPort pushy = null;

    IrcEventNotificationRule activeOnlyRule =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.TOPIC_CHANGED)
            .sourceMode(IrcEventNotificationRule.SourceMode.ANY)
            .sourcePattern(null)
            .channelScope(IrcEventNotificationRule.ChannelScope.ACTIVE_TARGET_ONLY)
            .channelPatterns(null)
            .toastEnabled(true)
            .focusScope(IrcEventNotificationRule.FocusScope.ANY)
            .statusBarEnabled(true)
            .notificationsNodeEnabled(true)
            .soundEnabled(false)
            .soundId("NOTIF_1")
            .soundUseCustom(false)
            .soundCustomPath(null)
            .scriptEnabled(false)
            .scriptPath(null)
            .scriptArgs(null)
            .scriptWorkingDirectory(null)
            .build();
    when(rulesBus.get()).thenReturn(List.of(activeOnlyRule));

    ExecutorService exec = Executors.newSingleThreadExecutor();
    try {
      IrcEventNotificationService service =
          new IrcEventNotificationService(rulesBus, tray, store, pushy, exec);

      boolean noMatchDifferentChannel =
          service.notifyConfigured(
              IrcEventNotificationRule.EventType.TOPIC_CHANGED,
              "libera",
              "#chat",
              "alice",
              Boolean.FALSE,
              "Topic changed",
              "changed",
              "libera",
              "#other");
      boolean noMatchDifferentServer =
          service.notifyConfigured(
              IrcEventNotificationRule.EventType.TOPIC_CHANGED,
              "libera",
              "#chat",
              "alice",
              Boolean.FALSE,
              "Topic changed",
              "changed",
              "oftc",
              "#chat");
      boolean matchesActiveChannel =
          service.notifyConfigured(
              IrcEventNotificationRule.EventType.TOPIC_CHANGED,
              "libera",
              "#chat",
              "alice",
              Boolean.FALSE,
              "Topic changed",
              "changed",
              "libera",
              "#chat");

      org.junit.jupiter.api.Assertions.assertFalse(noMatchDifferentChannel);
      org.junit.jupiter.api.Assertions.assertFalse(noMatchDifferentServer);
      assertTrue(matchesActiveChannel);
    } finally {
      exec.shutdownNow();
    }
  }

  @Test
  void ctcpRuleMatchesOnlyWhenCommandAndValueFiltersMatch() {
    IrcEventNotificationRulesBus rulesBus = mock(IrcEventNotificationRulesBus.class);
    TrayNotificationsPort tray = mock(TrayNotificationsPort.class);
    NotificationStore store = mock(NotificationStore.class);
    PushyNotificationPort pushy = null;

    IrcEventNotificationRule ctcpRule =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.CTCP_RECEIVED)
            .sourceMode(IrcEventNotificationRule.SourceMode.OTHERS)
            .sourcePattern(null)
            .channelScope(IrcEventNotificationRule.ChannelScope.ALL)
            .channelPatterns(null)
            .toastEnabled(true)
            .focusScope(IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY)
            .statusBarEnabled(true)
            .notificationsNodeEnabled(true)
            .soundEnabled(false)
            .soundId("SOMEBODY_SENT_CTCP_1")
            .soundUseCustom(false)
            .soundCustomPath(null)
            .scriptEnabled(false)
            .scriptPath(null)
            .scriptArgs(null)
            .scriptWorkingDirectory(null)
            .ctcpCommandMode(IrcEventNotificationRule.CtcpMatchMode.LIKE)
            .ctcpCommandPattern("VERSION")
            .ctcpValueMode(IrcEventNotificationRule.CtcpMatchMode.GLOB)
            .ctcpValuePattern("*hexchat*")
            .build();
    when(rulesBus.get()).thenReturn(List.of(ctcpRule));

    ExecutorService exec = Executors.newSingleThreadExecutor();
    try {
      IrcEventNotificationService service =
          new IrcEventNotificationService(rulesBus, tray, store, pushy, exec);

      boolean noMatchWrongCommand =
          service.notifyConfigured(
              IrcEventNotificationRule.EventType.CTCP_RECEIVED,
              "libera",
              "#ircafe",
              "alice",
              Boolean.FALSE,
              "CTCP request",
              "PING 12345",
              "libera",
              "#ircafe",
              "PING",
              "12345");
      boolean noMatchWrongValue =
          service.notifyConfigured(
              IrcEventNotificationRule.EventType.CTCP_RECEIVED,
              "libera",
              "#ircafe",
              "alice",
              Boolean.FALSE,
              "CTCP request",
              "VERSION mIRC",
              "libera",
              "#ircafe",
              "VERSION",
              "mIRC");
      boolean match =
          service.notifyConfigured(
              IrcEventNotificationRule.EventType.CTCP_RECEIVED,
              "libera",
              "#ircafe",
              "alice",
              Boolean.FALSE,
              "CTCP request",
              "VERSION HexChat",
              "libera",
              "#ircafe",
              "VERSION",
              "HexChat 2.16.2");

      assertFalse(noMatchWrongCommand);
      assertFalse(noMatchWrongValue);
      assertTrue(match);
    } finally {
      exec.shutdownNow();
    }
  }
}
