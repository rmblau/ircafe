package cafe.woden.ircclient.notifications;

import static cafe.woden.ircclient.notifications.IrcEventNotificationRuleTestFixtures.rule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.model.IrcEventNotificationRule;
import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleTest {

  @Test
  void defaultsIncludeStatusBarAnyCompanionsForCoreModerationEvents() {
    java.util.List<IrcEventNotificationRule> defaults = IrcEventNotificationRule.defaults();
    assertHasStatusBarAnyCompanion(defaults, IrcEventNotificationRule.EventType.KICKED);
    assertHasStatusBarAnyCompanion(defaults, IrcEventNotificationRule.EventType.BANNED);
    assertHasStatusBarAnyCompanion(defaults, IrcEventNotificationRule.EventType.KLINED);
  }

  @Test
  void defaultsUseEventSpecificBuiltInSoundMappings() {
    assertEquals(
        BuiltInSound.YOU_DEOPPED,
        IrcEventNotificationRule.defaultBuiltInSoundForEvent(
            IrcEventNotificationRule.EventType.YOU_DEOPPED));
    assertEquals(
        BuiltInSound.SOMEBODY_DEOPPED,
        IrcEventNotificationRule.defaultBuiltInSoundForEvent(
            IrcEventNotificationRule.EventType.DEOPPED));
    assertEquals(
        BuiltInSound.CHANNEL_INVITE_1,
        IrcEventNotificationRule.defaultBuiltInSoundForEvent(
            IrcEventNotificationRule.EventType.INVITE_RECEIVED));
    assertEquals(
        BuiltInSound.SOMEBODY_SENT_CTCP_1,
        IrcEventNotificationRule.defaultBuiltInSoundForEvent(
            IrcEventNotificationRule.EventType.CTCP_RECEIVED));
    assertEquals(
        BuiltInSound.NETSPLIT_1,
        IrcEventNotificationRule.defaultBuiltInSoundForEvent(
            IrcEventNotificationRule.EventType.NETSPLIT_DETECTED));
    assertEquals(
        BuiltInSound.WALLOPS_1,
        IrcEventNotificationRule.defaultBuiltInSoundForEvent(
            IrcEventNotificationRule.EventType.WALLOPS_RECEIVED));
  }

  @Test
  void presetAdapterBuildsFeatureOwnedPresetRules() {
    java.util.List<IrcEventNotificationRule> essential =
        IrcEventNotificationRule.preset("ESSENTIAL");
    java.util.List<IrcEventNotificationRule> allEvents =
        IrcEventNotificationRule.preset("ALL_EVENTS");

    assertEquals(5, essential.size());
    assertEquals(
        IrcEventNotificationRule.EventType.PRIVATE_MESSAGE_RECEIVED, essential.get(0).eventType());
    assertEquals(IrcEventNotificationRule.SourceMode.OTHERS, essential.get(0).sourceMode());
    assertEquals(BuiltInSound.PM_RECEIVED_1.name(), essential.get(0).soundId());
    assertFalse(essential.get(0).soundEnabled());

    assertEquals(IrcEventNotificationRule.EventType.values().length, allEvents.size());
    assertTrue(allEvents.stream().allMatch(IrcEventNotificationRule::enabled));
    assertTrue(
        allEvents.stream()
            .allMatch(rule -> rule.sourceMode() == IrcEventNotificationRule.SourceMode.ANY));
  }

  @Test
  void sourceAndChannelFiltersAreApplied() {
    IrcEventNotificationRule rule =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.INVITE_RECEIVED)
            .sourceMode(IrcEventNotificationRule.SourceMode.OTHERS)
            .sourcePattern(null)
            .channelScope(IrcEventNotificationRule.ChannelScope.ONLY)
            .channelPatterns("#staff*")
            .toastEnabled(true)
            .focusScope(IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY)
            .statusBarEnabled(true)
            .notificationsNodeEnabled(true)
            .soundEnabled(false)
            .soundId(BuiltInSound.NOTIF_1.name())
            .soundUseCustom(false)
            .soundCustomPath(null)
            .scriptEnabled(false)
            .scriptPath(null)
            .scriptArgs(null)
            .scriptWorkingDirectory(null)
            .build();

    assertTrue(
        rule.matches(
            IrcEventNotificationRule.EventType.INVITE_RECEIVED,
            "alice",
            Boolean.FALSE,
            "#staff-chat"));
    assertFalse(
        rule.matches(
            IrcEventNotificationRule.EventType.INVITE_RECEIVED,
            "alice",
            Boolean.TRUE,
            "#staff-chat"));
    assertFalse(
        rule.matches(
            IrcEventNotificationRule.EventType.INVITE_RECEIVED,
            "alice",
            Boolean.FALSE,
            "#general"));
    assertFalse(
        rule.matches(
            IrcEventNotificationRule.EventType.KICKED, "alice", Boolean.FALSE, "#staff-chat"));
  }

  @Test
  void sourceMatcherSupportsNickListGlobAndRegex() {
    IrcEventNotificationRule nickList =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.USER_JOINED)
            .sourceMode(IrcEventNotificationRule.SourceMode.NICK_LIST)
            .sourcePattern("alice bob")
            .channelScope(IrcEventNotificationRule.ChannelScope.ALL)
            .channelPatterns(null)
            .toastEnabled(true)
            .focusScope(IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY)
            .statusBarEnabled(true)
            .notificationsNodeEnabled(true)
            .soundEnabled(false)
            .soundId(BuiltInSound.NOTIF_1.name())
            .soundUseCustom(false)
            .soundCustomPath(null)
            .scriptEnabled(false)
            .scriptPath(null)
            .scriptArgs(null)
            .scriptWorkingDirectory(null)
            .build();

    IrcEventNotificationRule glob =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.USER_JOINED)
            .sourceMode(IrcEventNotificationRule.SourceMode.GLOB)
            .sourcePattern("mod*")
            .channelScope(IrcEventNotificationRule.ChannelScope.ALL)
            .channelPatterns(null)
            .toastEnabled(true)
            .focusScope(IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY)
            .statusBarEnabled(true)
            .notificationsNodeEnabled(true)
            .soundEnabled(false)
            .soundId(BuiltInSound.NOTIF_1.name())
            .soundUseCustom(false)
            .soundCustomPath(null)
            .scriptEnabled(false)
            .scriptPath(null)
            .scriptArgs(null)
            .scriptWorkingDirectory(null)
            .build();

    IrcEventNotificationRule regex =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.USER_JOINED)
            .sourceMode(IrcEventNotificationRule.SourceMode.REGEX)
            .sourcePattern("^op[0-9]+$")
            .channelScope(IrcEventNotificationRule.ChannelScope.ALL)
            .channelPatterns(null)
            .toastEnabled(true)
            .focusScope(IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY)
            .statusBarEnabled(true)
            .notificationsNodeEnabled(true)
            .soundEnabled(false)
            .soundId(BuiltInSound.NOTIF_1.name())
            .soundUseCustom(false)
            .soundCustomPath(null)
            .scriptEnabled(false)
            .scriptPath(null)
            .scriptArgs(null)
            .scriptWorkingDirectory(null)
            .build();

    assertTrue(
        nickList.matches(
            IrcEventNotificationRule.EventType.USER_JOINED, "Alice", Boolean.FALSE, "#chan"));
    assertFalse(
        nickList.matches(
            IrcEventNotificationRule.EventType.USER_JOINED, "charlie", Boolean.FALSE, "#chan"));

    assertTrue(
        glob.matches(
            IrcEventNotificationRule.EventType.USER_JOINED, "Moderator", Boolean.FALSE, "#chan"));
    assertFalse(
        glob.matches(
            IrcEventNotificationRule.EventType.USER_JOINED, "user", Boolean.FALSE, "#chan"));

    assertTrue(
        regex.matches(
            IrcEventNotificationRule.EventType.USER_JOINED, "op42", Boolean.FALSE, "#chan"));
    assertFalse(
        regex.matches(
            IrcEventNotificationRule.EventType.USER_JOINED, "opx", Boolean.FALSE, "#chan"));
  }

  @Test
  void allExceptScopeAllowsServerWideEventsWithoutChannel() {
    IrcEventNotificationRule rule =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.KLINED)
            .sourceMode(IrcEventNotificationRule.SourceMode.ANY)
            .sourcePattern(null)
            .channelScope(IrcEventNotificationRule.ChannelScope.ALL_EXCEPT)
            .channelPatterns("#ops*")
            .toastEnabled(true)
            .focusScope(IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY)
            .statusBarEnabled(true)
            .notificationsNodeEnabled(true)
            .soundEnabled(true)
            .soundId(BuiltInSound.NOTIF_2.name())
            .soundUseCustom(false)
            .soundCustomPath(null)
            .scriptEnabled(false)
            .scriptPath(null)
            .scriptArgs(null)
            .scriptWorkingDirectory(null)
            .build();

    assertTrue(rule.matches(IrcEventNotificationRule.EventType.KLINED, null, null, null));
    assertTrue(rule.matches(IrcEventNotificationRule.EventType.KLINED, null, null, "#general"));
    assertFalse(rule.matches(IrcEventNotificationRule.EventType.KLINED, null, null, "#ops"));
  }

  @Test
  void activeTargetOnlyScopeRequiresSameServerAndMatchingActiveChannel() {
    IrcEventNotificationRule rule =
        rule()
            .enabled(true)
            .eventType(IrcEventNotificationRule.EventType.TOPIC_CHANGED)
            .sourceMode(IrcEventNotificationRule.SourceMode.ANY)
            .sourcePattern(null)
            .channelScope(IrcEventNotificationRule.ChannelScope.ACTIVE_TARGET_ONLY)
            .channelPatterns("#ignored")
            .toastEnabled(true)
            .focusScope(IrcEventNotificationRule.FocusScope.ANY)
            .statusBarEnabled(true)
            .notificationsNodeEnabled(true)
            .soundEnabled(false)
            .soundId(BuiltInSound.TOPIC_CHANGED_1.name())
            .soundUseCustom(false)
            .soundCustomPath(null)
            .scriptEnabled(false)
            .scriptPath(null)
            .scriptArgs(null)
            .scriptWorkingDirectory(null)
            .build();

    assertFalse(
        rule.matches(IrcEventNotificationRule.EventType.TOPIC_CHANGED, null, null, "#chat"));
    assertFalse(
        rule.matches(
            IrcEventNotificationRule.EventType.TOPIC_CHANGED, null, null, "#chat", false, "#chat"));
    assertFalse(
        rule.matches(
            IrcEventNotificationRule.EventType.TOPIC_CHANGED, null, null, "#chat", true, "#other"));
    assertTrue(
        rule.matches(
            IrcEventNotificationRule.EventType.TOPIC_CHANGED, null, null, "#chat", true, "#chat"));
  }

  @Test
  void ctcpCommandAndValueFiltersAreAppliedForCtcpEvents() {
    IrcEventNotificationRule rule =
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
            .soundId(BuiltInSound.SOMEBODY_SENT_CTCP_1.name())
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

    assertTrue(
        rule.matches(
            IrcEventNotificationRule.EventType.CTCP_RECEIVED,
            "alice",
            Boolean.FALSE,
            "#irc",
            true,
            "#irc",
            "VERSION",
            "HexChat 2.16.2"));
    assertFalse(
        rule.matches(
            IrcEventNotificationRule.EventType.CTCP_RECEIVED,
            "alice",
            Boolean.FALSE,
            "#irc",
            true,
            "#irc",
            "PING",
            "HexChat 2.16.2"));
    assertFalse(
        rule.matches(
            IrcEventNotificationRule.EventType.CTCP_RECEIVED,
            "alice",
            Boolean.FALSE,
            "#irc",
            true,
            "#irc",
            "VERSION",
            "mIRC"));
  }

  private static void assertHasStatusBarAnyCompanion(
      java.util.List<IrcEventNotificationRule> rules,
      IrcEventNotificationRule.EventType eventType) {
    assertTrue(
        rules.stream()
            .anyMatch(
                r ->
                    r != null
                        && r.eventType() == eventType
                        && r.enabled()
                        && !r.toastEnabled()
                        && r.focusScope() == IrcEventNotificationRule.FocusScope.ANY
                        && r.statusBarEnabled()
                        && !r.soundEnabled()));
  }
}
