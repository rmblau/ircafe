package cafe.woden.ircclient.notifications.api;

import static cafe.woden.ircclient.notifications.IrcEventNotificationRuleTestFixtures.rule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationActionRule;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationMatchRule;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleAdaptersTest {

  @Test
  void convertsNullRuleListsToEmptyMatchRuleLists() {
    assertTrue(IrcEventNotificationRuleAdapters.toMatchRules(null).isEmpty());
  }

  @Test
  void skipsNullRulesWhenConvertingMatchRuleLists() {
    IrcEventNotificationRule source =
        rule().eventType(IrcEventNotificationRule.EventType.CTCP_RECEIVED).enabled(true).build();

    List<IrcEventNotificationMatchRule> rules =
        IrcEventNotificationRuleAdapters.toMatchRules(Arrays.asList(null, source));

    assertEquals(1, rules.size());
    assertEquals("CTCP_RECEIVED", rules.get(0).eventType());
  }

  @Test
  void adaptsRootRuleToFeatureMatchRule() {
    IrcEventNotificationRule source =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.CTCP_RECEIVED)
            .sourceMode(IrcEventNotificationRule.SourceMode.GLOB)
            .sourcePattern("bot*")
            .channelScope(IrcEventNotificationRule.ChannelScope.ONLY)
            .channelPatterns("#ops,#help")
            .ctcpCommandMode(IrcEventNotificationRule.CtcpMatchMode.LIKE)
            .ctcpCommandPattern("VERSION")
            .ctcpValueMode(IrcEventNotificationRule.CtcpMatchMode.REGEX)
            .ctcpValuePattern(".*irc.*")
            .build();

    IrcEventNotificationMatchRule rule = IrcEventNotificationRuleAdapters.toMatchRule(source);

    assertTrue(rule.enabled());
    assertEquals("CTCP_RECEIVED", rule.eventType());
    assertEquals("GLOB", rule.sourceMode());
    assertEquals("bot*", rule.sourcePattern());
    assertEquals("ONLY", rule.channelScope());
    assertEquals("#ops,#help", rule.channelPatterns());
    assertEquals("LIKE", rule.ctcpCommandMode());
    assertEquals("VERSION", rule.ctcpCommandPattern());
    assertEquals("REGEX", rule.ctcpValueMode());
    assertEquals(".*irc.*", rule.ctcpValuePattern());
  }

  @Test
  void adaptsRootRuleToFeatureActionRule() {
    IrcEventNotificationRule source =
        rule()
            .notificationsNodeEnabled(true)
            .toastEnabled(true)
            .focusScope(IrcEventNotificationRule.FocusScope.FOREGROUND_ONLY)
            .statusBarEnabled(false)
            .soundEnabled(true)
            .soundId("NOTIF_3")
            .soundUseCustom(true)
            .soundCustomPath("/tmp/ping.wav")
            .scriptEnabled(true)
            .scriptPath("/usr/local/bin/hook")
            .scriptArgs("--quiet")
            .scriptWorkingDirectory("/tmp")
            .build();

    IrcEventNotificationActionRule rule = IrcEventNotificationRuleAdapters.toActionRule(source);

    assertTrue(rule.notificationsNodeEnabled());
    assertTrue(rule.toastEnabled());
    assertEquals("FOREGROUND_ONLY", rule.focusScope());
    assertFalse(rule.statusBarEnabled());
    assertTrue(rule.soundEnabled());
    assertEquals("NOTIF_3", rule.soundId());
    assertTrue(rule.soundUseCustom());
    assertEquals("/tmp/ping.wav", rule.soundCustomPath());
    assertTrue(rule.scriptEnabled());
    assertEquals("/usr/local/bin/hook", rule.scriptPath());
    assertEquals("--quiet", rule.scriptArgs());
    assertEquals("/tmp", rule.scriptWorkingDirectory());
  }

  @Test
  void nullRulesConvertToNullFeatureRules() {
    assertNull(IrcEventNotificationRuleAdapters.toMatchRule(null));
    assertNull(IrcEventNotificationRuleAdapters.toActionRule(null));
  }

  @Test
  void adaptsFocusScopeWithFallbackForBlankOrInvalidValues() {
    assertEquals(
        IrcEventNotificationRule.FocusScope.ANY,
        IrcEventNotificationRuleAdapters.toFocusScope(
            "ANY", IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY));
    assertEquals(
        IrcEventNotificationRule.FocusScope.FOREGROUND_ONLY,
        IrcEventNotificationRuleAdapters.toFocusScope(
            " ", IrcEventNotificationRule.FocusScope.FOREGROUND_ONLY));
    assertEquals(
        IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY,
        IrcEventNotificationRuleAdapters.toFocusScope("nope", null));
  }
}
