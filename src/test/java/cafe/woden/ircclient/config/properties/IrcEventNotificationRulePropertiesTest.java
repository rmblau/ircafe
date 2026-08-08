package cafe.woden.ircclient.config.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class IrcEventNotificationRulePropertiesTest {

  @Test
  void defaultsMirrorFeatureCatalogShape() {
    List<IrcEventNotificationRuleProperties> defaults =
        IrcEventNotificationRuleProperties.defaultRules();

    assertEquals(IrcEventNotificationRuleProperties.EventType.values().length + 3, defaults.size());

    IrcEventNotificationRuleProperties privateMessage =
        findFirstForEvent(
            defaults, IrcEventNotificationRuleProperties.EventType.PRIVATE_MESSAGE_RECEIVED);
    assertTrue(privateMessage.enabled());
    assertEquals(IrcEventNotificationRuleProperties.SourceMode.OTHERS, privateMessage.sourceMode());
    assertEquals("PM_RECEIVED_1", privateMessage.soundId());
    assertFalse(privateMessage.soundEnabled());

    IrcEventNotificationRuleProperties kickedCompanion =
        defaults.get(IrcEventNotificationRuleProperties.EventType.values().length);
    assertEquals(IrcEventNotificationRuleProperties.EventType.KICKED, kickedCompanion.eventType());
    assertFalse(kickedCompanion.toastEnabled());
    assertEquals(IrcEventNotificationRuleProperties.FocusScope.ANY, kickedCompanion.focusScope());
  }

  @Test
  void defaultsIncludeStatusBarAnyCompanionsForCoreModerationEvents() {
    List<IrcEventNotificationRuleProperties> defaults =
        IrcEventNotificationRuleProperties.defaultRules();
    assertHasStatusBarAnyCompanion(defaults, IrcEventNotificationRuleProperties.EventType.KICKED);
    assertHasStatusBarAnyCompanion(defaults, IrcEventNotificationRuleProperties.EventType.BANNED);
    assertHasStatusBarAnyCompanion(defaults, IrcEventNotificationRuleProperties.EventType.KLINED);
  }

  @Test
  void constructorDelegatesPersistedNormalizationPolicy() {
    IrcEventNotificationRuleProperties rule =
        new IrcEventNotificationRuleProperties(
            true,
            IrcEventNotificationRuleProperties.EventType.CTCP_RECEIVED,
            IrcEventNotificationRuleProperties.SourceMode.REGEX,
            "  Alice.*  ",
            IrcEventNotificationRuleProperties.ChannelScope.ONLY,
            "  #ops  ",
            null,
            null,
            null,
            null,
            null,
            true,
            "  ",
            true,
            "  ",
            true,
            "  ",
            " --flag ",
            " /tmp ",
            IrcEventNotificationRuleProperties.CtcpMatchMode.LIKE,
            " VERSION ",
            IrcEventNotificationRuleProperties.CtcpMatchMode.ANY,
            " ignored ",
            null,
            null);

    assertTrue(rule.enabled());
    assertEquals(IrcEventNotificationRuleProperties.SourceMode.REGEX, rule.sourceMode());
    assertEquals("Alice.*", rule.sourcePattern());
    assertEquals("#ops", rule.channelPatterns());
    assertTrue(rule.toastEnabled());
    assertFalse(rule.toastWhenFocused());
    assertEquals(IrcEventNotificationRuleProperties.FocusScope.BACKGROUND_ONLY, rule.focusScope());
    assertTrue(rule.statusBarEnabled());
    assertTrue(rule.notificationsNodeEnabled());
    assertTrue(rule.soundEnabled());
    assertEquals("SOMEBODY_SENT_CTCP_1", rule.soundId());
    assertFalse(rule.soundUseCustom());
    assertFalse(rule.scriptEnabled());
    assertEquals("--flag", rule.scriptArgs());
    assertEquals("/tmp", rule.scriptWorkingDirectory());
    assertEquals(IrcEventNotificationRuleProperties.CtcpMatchMode.LIKE, rule.ctcpCommandMode());
    assertEquals("VERSION", rule.ctcpCommandPattern());
    assertEquals(IrcEventNotificationRuleProperties.CtcpMatchMode.ANY, rule.ctcpValueMode());
    assertEquals(null, rule.ctcpValuePattern());
  }

  @Test
  void constructorMigratesLegacyChannelListsThroughFeaturePolicy() {
    IrcEventNotificationRuleProperties include =
        new IrcEventNotificationRuleProperties(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, " #ops ", null);
    IrcEventNotificationRuleProperties exclude =
        new IrcEventNotificationRuleProperties(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            " #noise ");

    assertEquals(IrcEventNotificationRuleProperties.ChannelScope.ONLY, include.channelScope());
    assertEquals("#ops", include.channelPatterns());
    assertEquals("#ops", include.channelWhitelist());

    assertEquals(
        IrcEventNotificationRuleProperties.ChannelScope.ALL_EXCEPT, exclude.channelScope());
    assertEquals("#noise", exclude.channelPatterns());
    assertEquals("#noise", exclude.channelBlacklist());
  }

  private static IrcEventNotificationRuleProperties findFirstForEvent(
      List<IrcEventNotificationRuleProperties> rules,
      IrcEventNotificationRuleProperties.EventType eventType) {
    return rules.stream()
        .filter(r -> r != null && r.eventType() == eventType)
        .findFirst()
        .orElseThrow();
  }

  private static void assertHasStatusBarAnyCompanion(
      List<IrcEventNotificationRuleProperties> rules,
      IrcEventNotificationRuleProperties.EventType eventType) {
    assertTrue(
        rules.stream()
            .anyMatch(
                r ->
                    r != null
                        && r.eventType() == eventType
                        && Boolean.TRUE.equals(r.enabled())
                        && Boolean.FALSE.equals(r.toastEnabled())
                        && r.focusScope() == IrcEventNotificationRuleProperties.FocusScope.ANY
                        && Boolean.TRUE.equals(r.statusBarEnabled())
                        && Boolean.FALSE.equals(r.soundEnabled())));
  }
}
