package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class IrcEventNotificationScriptPlannerTest {

  @Test
  void parsesShellLikeArgumentStringsWithoutShellExpansion() {
    assertEquals(
        List.of("--flag", "value with spaces", "literal quote", "path with spaces"),
        IrcEventNotificationScriptPlanner.parseCommandArgs(
            "--flag \"value with spaces\" 'literal quote' path\\ with\\ spaces"));
  }

  @Test
  void rejectsUnterminatedQuotedArguments() {
    assertThrows(
        IllegalArgumentException.class,
        () -> IrcEventNotificationScriptPlanner.parseCommandArgs("\"unterminated"));
  }

  @Test
  void plansCommandWorkingDirectoryAndEnvironment() {
    IrcEventNotificationScriptPlan plan =
        IrcEventNotificationScriptPlanner.plan(
            " /usr/local/bin/notify-event ",
            "--event \"$IRCAFE_EVENT_TYPE\"",
            " /tmp ",
            "CTCP_RECEIVED",
            "libera",
            "#ircafe",
            "alice",
            Boolean.FALSE,
            "CTCP request",
            "VERSION HexChat",
            "VERSION",
            "HexChat 2.16.2",
            12345L);

    assertEquals(
        List.of("/usr/local/bin/notify-event", "--event", "$IRCAFE_EVENT_TYPE"), plan.command());
    assertEquals("/tmp", plan.workingDirectory());
    assertEquals("CTCP_RECEIVED", plan.environment().get("IRCAFE_EVENT_TYPE"));
    assertEquals("libera", plan.environment().get("IRCAFE_SERVER_ID"));
    assertEquals("#ircafe", plan.environment().get("IRCAFE_CHANNEL"));
    assertEquals("alice", plan.environment().get("IRCAFE_SOURCE_NICK"));
    assertEquals("false", plan.environment().get("IRCAFE_SOURCE_IS_SELF"));
    assertEquals("CTCP request", plan.environment().get("IRCAFE_TITLE"));
    assertEquals("VERSION HexChat", plan.environment().get("IRCAFE_BODY"));
    assertEquals("VERSION", plan.environment().get("IRCAFE_CTCP_COMMAND"));
    assertEquals("HexChat 2.16.2", plan.environment().get("IRCAFE_CTCP_VALUE"));
    assertEquals("12345", plan.environment().get("IRCAFE_TIMESTAMP_MS"));
  }

  @Test
  void skipsBlankScriptAndUsesUnknownSelfState() {
    IrcEventNotificationScriptPlan skipped =
        IrcEventNotificationScriptPlanner.plan(" ", "", "", "", "", "", "", null, "", "", "", "", 1L);
    IrcEventNotificationScriptPlan plan =
        IrcEventNotificationScriptPlanner.plan("script", null, null, null, null, null, null, null, null, null, null, null, 2L);

    assertEquals(List.of(), skipped.command());
    assertEquals("unknown", plan.environment().get("IRCAFE_SOURCE_IS_SELF"));
    assertEquals("", plan.environment().get("IRCAFE_EVENT_TYPE"));
  }
}
