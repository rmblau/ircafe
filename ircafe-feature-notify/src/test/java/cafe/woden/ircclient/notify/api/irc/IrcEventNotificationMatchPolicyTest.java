package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationMatchPolicyTest {

  @Test
  void appliesSourceAndChannelFilters() {
    IrcEventNotificationMatchRule rule =
        rule("INVITE_RECEIVED", "OTHERS", null, "ONLY", "#staff*", "ANY", null, "ANY", null);

    assertTrue(
        IrcEventNotificationMatchPolicy.matches(
            rule, event("INVITE_RECEIVED", "alice", Boolean.FALSE, "#staff-chat")));
    assertFalse(
        IrcEventNotificationMatchPolicy.matches(
            rule, event("INVITE_RECEIVED", "alice", Boolean.TRUE, "#staff-chat")));
    assertFalse(
        IrcEventNotificationMatchPolicy.matches(
            rule, event("INVITE_RECEIVED", "alice", Boolean.FALSE, "#general")));
    assertFalse(
        IrcEventNotificationMatchPolicy.matches(
            rule, event("KICKED", "alice", Boolean.FALSE, "#staff-chat")));
  }

  @Test
  void sourceMatcherSupportsNickListGlobAndRegex() {
    IrcEventNotificationMatchRule nickList =
        rule("USER_JOINED", "NICK_LIST", "alice bob", "ALL", null, "ANY", null, "ANY", null);
    IrcEventNotificationMatchRule glob =
        rule("USER_JOINED", "GLOB", "mod*", "ALL", null, "ANY", null, "ANY", null);
    IrcEventNotificationMatchRule regex =
        rule("USER_JOINED", "REGEX", "^op[0-9]+$", "ALL", null, "ANY", null, "ANY", null);

    assertTrue(
        IrcEventNotificationMatchPolicy.matches(
            nickList, event("USER_JOINED", "Alice", Boolean.FALSE, "#chan")));
    assertFalse(
        IrcEventNotificationMatchPolicy.matches(
            nickList, event("USER_JOINED", "charlie", Boolean.FALSE, "#chan")));

    assertTrue(
        IrcEventNotificationMatchPolicy.matches(
            glob, event("USER_JOINED", "Moderator", Boolean.FALSE, "#chan")));
    assertFalse(
        IrcEventNotificationMatchPolicy.matches(
            glob, event("USER_JOINED", "user", Boolean.FALSE, "#chan")));

    assertTrue(
        IrcEventNotificationMatchPolicy.matches(
            regex, event("USER_JOINED", "op42", Boolean.FALSE, "#chan")));
    assertFalse(
        IrcEventNotificationMatchPolicy.matches(
            regex, event("USER_JOINED", "opx", Boolean.FALSE, "#chan")));
  }

  @Test
  void allExceptScopeAllowsServerWideEventsWithoutChannel() {
    IrcEventNotificationMatchRule rule =
        rule("KLINED", "ANY", null, "ALL_EXCEPT", "#ops*", "ANY", null, "ANY", null);

    assertTrue(IrcEventNotificationMatchPolicy.matches(rule, event("KLINED", null, null, null)));
    assertTrue(
        IrcEventNotificationMatchPolicy.matches(rule, event("KLINED", null, null, "#general")));
    assertFalse(
        IrcEventNotificationMatchPolicy.matches(rule, event("KLINED", null, null, "#ops")));
  }

  @Test
  void activeTargetOnlyScopeRequiresSameServerAndMatchingActiveChannel() {
    IrcEventNotificationMatchRule rule =
        rule("TOPIC_CHANGED", "ANY", null, "ACTIVE_TARGET_ONLY", "#ignored", "ANY", null, "ANY", null);

    assertFalse(
        IrcEventNotificationMatchPolicy.matches(
            rule,
            event("TOPIC_CHANGED", null, null, "#chat", false, "#chat", null, null)));
    assertFalse(
        IrcEventNotificationMatchPolicy.matches(
            rule,
            event("TOPIC_CHANGED", null, null, "#chat", true, "#other", null, null)));
    assertTrue(
        IrcEventNotificationMatchPolicy.matches(
            rule,
            event("TOPIC_CHANGED", null, null, "#chat", true, "#chat", null, null)));
  }

  @Test
  void hasEnabledRuleForRequiresEnabledRuleWithMatchingEventType() {
    IrcEventNotificationMatchRule disabledInvite =
        rule("INVITE_RECEIVED", "ANY", null, "ALL", null, "ANY", null, "ANY", null, false);
    IrcEventNotificationMatchRule enabledKick =
        rule("KICKED", "ANY", null, "ALL", null, "ANY", null, "ANY", null);
    IrcEventNotificationMatchRule enabledInvite =
        rule("INVITE_RECEIVED", "ANY", null, "ALL", null, "ANY", null, "ANY", null);

    assertFalse(IrcEventNotificationMatchPolicy.hasEnabledRuleFor(null, "INVITE_RECEIVED"));
    assertFalse(
        IrcEventNotificationMatchPolicy.hasEnabledRuleFor(
            java.util.List.of(disabledInvite, enabledKick), "INVITE_RECEIVED"));
    assertTrue(
        IrcEventNotificationMatchPolicy.hasEnabledRuleFor(
            java.util.List.of(disabledInvite, enabledKick, enabledInvite), "INVITE_RECEIVED"));
  }

  @Test
  void ctcpCommandAndValueFiltersAreAppliedOnlyForCtcpEvents() {
    IrcEventNotificationMatchRule ctcp =
        rule("CTCP_RECEIVED", "OTHERS", null, "ALL", null, "LIKE", "VERSION", "GLOB", "*hexchat*");
    IrcEventNotificationMatchRule nonCtcp =
        rule("NOTICE_RECEIVED", "ANY", null, "ALL", null, "LIKE", "VERSION", "GLOB", "*hexchat*");

    assertFalse(
        IrcEventNotificationMatchPolicy.matches(
            ctcp,
            event(
                "CTCP_RECEIVED", "alice", Boolean.FALSE, "#chan", false, null, "PING", "123")));
    assertFalse(
        IrcEventNotificationMatchPolicy.matches(
            ctcp,
            event(
                "CTCP_RECEIVED",
                "alice",
                Boolean.FALSE,
                "#chan",
                false,
                null,
                "VERSION",
                "mIRC")));
    assertTrue(
        IrcEventNotificationMatchPolicy.matches(
            ctcp,
            event(
                "CTCP_RECEIVED",
                "alice",
                Boolean.FALSE,
                "#chan",
                false,
                null,
                "VERSION",
                "HexChat 2.16.2")));
    assertTrue(
        IrcEventNotificationMatchPolicy.matches(
            nonCtcp,
            event("NOTICE_RECEIVED", "server", null, null, false, null, "PING", "123")));
  }

  private static IrcEventNotificationMatchRule rule(
      String eventType,
      String sourceMode,
      String sourcePattern,
      String channelScope,
      String channelPatterns,
      String ctcpCommandMode,
      String ctcpCommandPattern,
      String ctcpValueMode,
      String ctcpValuePattern) {
    return rule(
        eventType,
        sourceMode,
        sourcePattern,
        channelScope,
        channelPatterns,
        ctcpCommandMode,
        ctcpCommandPattern,
        ctcpValueMode,
        ctcpValuePattern,
        true);
  }

  private static IrcEventNotificationMatchRule rule(
      String eventType,
      String sourceMode,
      String sourcePattern,
      String channelScope,
      String channelPatterns,
      String ctcpCommandMode,
      String ctcpCommandPattern,
      String ctcpValueMode,
      String ctcpValuePattern,
      boolean enabled) {
    return new IrcEventNotificationMatchRule(
        enabled,
        eventType,
        sourceMode,
        sourcePattern,
        channelScope,
        channelPatterns,
        ctcpCommandMode,
        ctcpCommandPattern,
        ctcpValueMode,
        ctcpValuePattern);
  }

  private static IrcEventNotificationMatchEvent event(
      String eventType, String sourceNick, Boolean sourceIsSelf, String channel) {
    return event(eventType, sourceNick, sourceIsSelf, channel, false, null, null, null);
  }

  private static IrcEventNotificationMatchEvent event(
      String eventType,
      String sourceNick,
      Boolean sourceIsSelf,
      String channel,
      boolean activeTargetOnSameServer,
      String activeTarget,
      String ctcpCommand,
      String ctcpValue) {
    return new IrcEventNotificationMatchEvent(
        eventType,
        sourceNick,
        sourceIsSelf,
        channel,
        activeTargetOnSameServer,
        activeTarget,
        ctcpCommand,
        ctcpValue);
  }
}
