package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleEvaluatorTest {

  @Test
  void returnsInvalidWhenNoRulesOrServerIdIsBlank() {
    assertFalse(
        IrcEventNotificationRuleEvaluator.evaluate(
                List.of(),
                "INVITE_RECEIVED",
                "Invite Received",
                "libera",
                "#ircafe",
                "alice",
                Boolean.FALSE,
                "Invite",
                "body",
                "libera",
                "#ircafe",
                null,
                null)
            .valid());

    IrcEventNotificationRuleEvaluation evaluation =
        IrcEventNotificationRuleEvaluator.evaluate(
            List.of(rule("INVITE_RECEIVED", "ANY", null, "ALL", null)),
            "INVITE_RECEIVED",
            "Invite Received",
            " ",
            "#ircafe",
            "alice",
            Boolean.FALSE,
            "Invite",
            "body",
            "libera",
            "#ircafe",
            null,
            null);

    assertFalse(evaluation.valid());
    assertFalse(evaluation.anyMatched());
  }

  @Test
  void plansDispatchContextAndMatchedIndexes() {
    IrcEventNotificationMatchRule disabledMatch =
        rule("INVITE_RECEIVED", "ANY", null, "ALL", null, false);
    IrcEventNotificationMatchRule sourceMismatch =
        rule("INVITE_RECEIVED", "SELF", null, "ALL", null);
    IrcEventNotificationMatchRule channelMatch =
        rule("INVITE_RECEIVED", "OTHERS", null, "ONLY", "#staff*");
    IrcEventNotificationMatchRule eventMismatch = rule("KICKED", "ANY", null, "ALL", null);

    IrcEventNotificationRuleEvaluation evaluation =
        IrcEventNotificationRuleEvaluator.evaluate(
            List.of(disabledMatch, sourceMismatch, channelMatch, eventMismatch),
            "INVITE_RECEIVED",
            "Invite Received",
            " libera ",
            " #staff-chat ",
            " alice ",
            Boolean.FALSE,
            " ",
            " body ",
            "LIBERA",
            " #staff-chat ",
            null,
            null);

    assertTrue(evaluation.valid());
    assertTrue(evaluation.anyMatched());
    assertEquals(List.of(2), evaluation.matchedRuleIndexes());
    assertEquals("libera", evaluation.context().serverId());
    assertEquals("#staff-chat", evaluation.context().target());
    assertEquals("alice", evaluation.context().sourceNick());
    assertEquals("Invite Received", evaluation.context().title());
    assertEquals("body", evaluation.context().body());
    assertTrue(evaluation.context().activeTargetOnSameServer());
  }

  @Test
  void carriesCtcpValuesIntoMatching() {
    IrcEventNotificationMatchRule ctcp =
        new IrcEventNotificationMatchRule(
            true,
            "CTCP_RECEIVED",
            "OTHERS",
            null,
            "ALL",
            null,
            "LIKE",
            "VERSION",
            "GLOB",
            "*hexchat*");

    IrcEventNotificationRuleEvaluation noMatch =
        IrcEventNotificationRuleEvaluator.evaluate(
            List.of(ctcp),
            "CTCP_RECEIVED",
            "CTCP Request Received",
            "libera",
            "#ircafe",
            "alice",
            Boolean.FALSE,
            "CTCP",
            "VERSION mIRC",
            "libera",
            "#ircafe",
            "VERSION",
            "mIRC");
    IrcEventNotificationRuleEvaluation match =
        IrcEventNotificationRuleEvaluator.evaluate(
            List.of(ctcp),
            "CTCP_RECEIVED",
            "CTCP Request Received",
            "libera",
            "#ircafe",
            "alice",
            Boolean.FALSE,
            "CTCP",
            "VERSION HexChat",
            "libera",
            "#ircafe",
            "VERSION",
            "HexChat 2.16.2");

    assertFalse(noMatch.anyMatched());
    assertTrue(match.anyMatched());
    assertEquals(List.of(0), match.matchedRuleIndexes());
  }

  private static IrcEventNotificationMatchRule rule(
      String eventType,
      String sourceMode,
      String sourcePattern,
      String channelScope,
      String channelPatterns) {
    return rule(eventType, sourceMode, sourcePattern, channelScope, channelPatterns, true);
  }

  private static IrcEventNotificationMatchRule rule(
      String eventType,
      String sourceMode,
      String sourcePattern,
      String channelScope,
      String channelPatterns,
      boolean enabled) {
    return new IrcEventNotificationMatchRule(
        enabled,
        eventType,
        sourceMode,
        sourcePattern,
        channelScope,
        channelPatterns,
        "ANY",
        null,
        "ANY",
        null);
  }
}
