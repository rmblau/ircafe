package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.notifications.IrcEventNotificationRuleTestFixtures.rule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import cafe.woden.ircclient.config.api.NotificationRule;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigNotificationSettingsCodecTest {

  @Test
  void notificationCooldownIsClampedForPersistence() {
    assertEquals(15, RuntimeConfigNotificationSettingsCodec.normalizeRuleCooldownSeconds(-1));
    assertEquals(0, RuntimeConfigNotificationSettingsCodec.normalizeRuleCooldownSeconds(0));
    assertEquals(3600, RuntimeConfigNotificationSettingsCodec.normalizeRuleCooldownSeconds(5000));
  }

  @Test
  void notificationRulesSerializeNormalizedValues() {
    Map<String, Object> rule =
        RuntimeConfigNotificationSettingsCodec.toRuleMaps(
                Arrays.asList(
                    new NotificationRule(
                        " Important ping ",
                        NotificationRule.Type.REGEX,
                        " ping|alert ",
                        true,
                        true,
                        false,
                        "ffaa00"),
                    null))
            .getFirst();

    assertEquals(true, rule.get("enabled"));
    assertEquals("Important ping", rule.get("label"));
    assertEquals("REGEX", rule.get("type"));
    assertEquals("ping|alert", rule.get("pattern"));
    assertEquals(true, rule.get("caseSensitive"));
    assertEquals(false, rule.get("wholeWord"));
    assertEquals("#FFAA00", rule.get("highlightFg"));
  }

  @Test
  void notificationRulesOmitBlankHighlightColor() {
    Map<String, Object> rule =
        RuntimeConfigNotificationSettingsCodec.toRuleMaps(
                List.of(new NotificationRule("", null, " ping ", true, false, true, "")))
            .getFirst();

    assertEquals("ping", rule.get("label"));
    assertEquals("WORD", rule.get("type"));
    assertFalse(rule.containsKey("highlightFg"));
  }

  @Test
  void ircEventRulesSerializeCtcpSpecificFieldsAndLegacyFocusFlag() {
    Map<String, Object> rule =
        RuntimeConfigNotificationSettingsCodec.toIrcEventRuleMaps(
                List.of(
                    rule()
                        .enabled(true)
                        .eventType(IrcEventNotificationRule.EventType.CTCP_RECEIVED)
                        .sourceMode(IrcEventNotificationRule.SourceMode.OTHERS)
                        .channelScope(IrcEventNotificationRule.ChannelScope.ONLY)
                        .channelPatterns(" #general ")
                        .toastEnabled(true)
                        .focusScope(IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY)
                        .statusBarEnabled(true)
                        .notificationsNodeEnabled(true)
                        .soundEnabled(true)
                        .soundId(" NOTIF_3 ")
                        .soundUseCustom(false)
                        .scriptEnabled(true)
                        .scriptPath(" /tmp/ircafe-event-hook.sh ")
                        .scriptArgs(" --flag ")
                        .scriptWorkingDirectory(" /tmp ")
                        .ctcpCommandMode(IrcEventNotificationRule.CtcpMatchMode.LIKE)
                        .ctcpCommandPattern(" VERSION ")
                        .ctcpValueMode(IrcEventNotificationRule.CtcpMatchMode.GLOB)
                        .ctcpValuePattern(" *hexchat* ")
                        .build()))
            .getFirst();

    assertEquals("CTCP_RECEIVED", rule.get("eventType"));
    assertEquals("OTHERS", rule.get("sourceMode"));
    assertEquals("ONLY", rule.get("channelScope"));
    assertEquals("#general", rule.get("channelPatterns"));
    assertEquals("BACKGROUND_ONLY", rule.get("focusScope"));
    assertEquals(false, rule.get("toastWhenFocused"));
    assertEquals("NOTIF_3", rule.get("soundId"));
    assertEquals("/tmp/ircafe-event-hook.sh", rule.get("scriptPath"));
    assertEquals("--flag", rule.get("scriptArgs"));
    assertEquals("/tmp", rule.get("scriptWorkingDirectory"));
    assertEquals("LIKE", rule.get("ctcpCommandMode"));
    assertEquals("VERSION", rule.get("ctcpCommandPattern"));
    assertEquals("GLOB", rule.get("ctcpValueMode"));
    assertEquals("*hexchat*", rule.get("ctcpValuePattern"));
  }

  @Test
  void ircEventRulesOmitCtcpFieldsForNonCtcpEvents() {
    Map<String, Object> rule =
        RuntimeConfigNotificationSettingsCodec.toIrcEventRuleMaps(
                List.of(
                    rule()
                        .eventType(IrcEventNotificationRule.EventType.INVITE_RECEIVED)
                        .ctcpCommandMode(IrcEventNotificationRule.CtcpMatchMode.LIKE)
                        .ctcpCommandPattern("VERSION")
                        .build()))
            .getFirst();

    assertEquals("INVITE_RECEIVED", rule.get("eventType"));
    assertFalse(rule.containsKey("ctcpCommandMode"));
    assertFalse(rule.containsKey("ctcpCommandPattern"));
  }
}
