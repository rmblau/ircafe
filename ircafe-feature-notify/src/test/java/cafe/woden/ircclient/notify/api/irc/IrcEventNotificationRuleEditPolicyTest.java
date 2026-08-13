package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditValidationError.Field;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationRuleEditValidationError.Reason;
import org.junit.jupiter.api.Test;

class IrcEventNotificationRuleEditPolicyTest {

  @Test
  void requiresSourcePatternForPatternBasedSourceModes() {
    IrcEventNotificationRuleEditValidationError error =
        IrcEventNotificationRuleEditPolicy.validate(
            values().sourceMode("REGEX").sourcePattern(" ").build());

    assertEquals(Field.SOURCE_PATTERN, error.field());
    assertEquals(Reason.REQUIRED, error.reason());
  }

  @Test
  void reportsInvalidSourceRegex() {
    IrcEventNotificationRuleEditValidationError error =
        IrcEventNotificationRuleEditPolicy.validate(
            values().sourceMode("REGEX").sourcePattern("[").build());

    assertEquals(Field.SOURCE_PATTERN, error.field());
    assertEquals(Reason.INVALID_REGEX, error.reason());
  }

  @Test
  void requiresChannelPatternForScopedChannelModes() {
    IrcEventNotificationRuleEditValidationError error =
        IrcEventNotificationRuleEditPolicy.validate(
            values().channelScope("ONLY").channelPatterns("").build());

    assertEquals(Field.CHANNEL_PATTERNS, error.field());
    assertEquals(Reason.REQUIRED, error.reason());
  }

  @Test
  void validatesCtcpFiltersOnlyForCtcpEvents() {
    assertNull(
        IrcEventNotificationRuleEditPolicy.validate(
            values()
                .eventType("INVITE_RECEIVED")
                .ctcpCommandMode("REGEX")
                .ctcpCommandPattern("[")
                .build()));

    IrcEventNotificationRuleEditValidationError error =
        IrcEventNotificationRuleEditPolicy.validate(
            values()
                .eventType("CTCP_RECEIVED")
                .ctcpCommandMode("REGEX")
                .ctcpCommandPattern("[")
                .build());

    assertEquals(Field.CTCP_COMMAND_PATTERN, error.field());
    assertEquals(Reason.INVALID_REGEX, error.reason());
  }

  @Test
  void requiresCtcpValuePatternWhenModeIsNotAny() {
    IrcEventNotificationRuleEditValidationError error =
        IrcEventNotificationRuleEditPolicy.validate(
            values()
                .eventType("CTCP_RECEIVED")
                .ctcpValueMode("GLOB")
                .ctcpValuePattern(" ")
                .build());

    assertEquals(Field.CTCP_VALUE_PATTERN, error.field());
    assertEquals(Reason.REQUIRED, error.reason());
  }

  @Test
  void requiresScriptPathWhenScriptExecutionIsEnabled() {
    IrcEventNotificationRuleEditValidationError error =
        IrcEventNotificationRuleEditPolicy.validate(
            values().scriptEnabled(true).scriptPath(" ").build());

    assertEquals(Field.SCRIPT_PATH, error.field());
    assertEquals(Reason.REQUIRED, error.reason());
  }

  @Test
  void exposesFieldActivationPolicyForUiAdapters() {
    assertTrue(IrcEventNotificationRuleEditPolicy.sourcePatternRequired(" nick_list "));
    assertTrue(IrcEventNotificationRuleEditPolicy.sourcePatternRequired("glob"));
    assertFalse(IrcEventNotificationRuleEditPolicy.sourcePatternRequired("ANY"));

    assertTrue(IrcEventNotificationRuleEditPolicy.channelPatternsRequired(" all_except "));
    assertTrue(IrcEventNotificationRuleEditPolicy.channelPatternsRequired("ONLY"));
    assertFalse(IrcEventNotificationRuleEditPolicy.channelPatternsRequired(null));

    assertTrue(IrcEventNotificationRuleEditPolicy.ctcpFiltersActive("ctcp_received"));
    assertFalse(IrcEventNotificationRuleEditPolicy.ctcpFiltersActive("INVITE_RECEIVED"));

    assertTrue(IrcEventNotificationRuleEditPolicy.ctcpPatternRequired("LIKE"));
    assertFalse(IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(" any "));

    assertTrue(IrcEventNotificationRuleEditPolicy.scriptPathRequired(true));
    assertFalse(IrcEventNotificationRuleEditPolicy.scriptPathRequired(false));
  }

  @Test
  void validRuleHasNoError() {
    assertNull(
        IrcEventNotificationRuleEditPolicy.validate(
            values()
                .eventType("CTCP_RECEIVED")
                .sourceMode("NICK_LIST")
                .sourcePattern("alice bob")
                .channelScope("ALL_EXCEPT")
                .channelPatterns("#ops")
                .ctcpCommandMode("LIKE")
                .ctcpCommandPattern("VERSION")
                .ctcpValueMode("REGEX")
                .ctcpValuePattern(".*")
                .build()));
  }

  private static Builder values() {
    return new Builder();
  }

  private static final class Builder {
    private String eventType = "INVITE_RECEIVED";
    private String sourceMode = "ANY";
    private String sourcePattern = "";
    private String channelScope = "ALL";
    private String channelPatterns = "";
    private String ctcpCommandMode = "ANY";
    private String ctcpCommandPattern = "";
    private String ctcpValueMode = "ANY";
    private String ctcpValuePattern = "";
    private boolean scriptEnabled;
    private String scriptPath = "";

    Builder eventType(String value) {
      this.eventType = value;
      return this;
    }

    Builder sourceMode(String value) {
      this.sourceMode = value;
      return this;
    }

    Builder sourcePattern(String value) {
      this.sourcePattern = value;
      return this;
    }

    Builder channelScope(String value) {
      this.channelScope = value;
      return this;
    }

    Builder channelPatterns(String value) {
      this.channelPatterns = value;
      return this;
    }

    Builder ctcpCommandMode(String value) {
      this.ctcpCommandMode = value;
      return this;
    }

    Builder ctcpCommandPattern(String value) {
      this.ctcpCommandPattern = value;
      return this;
    }

    Builder ctcpValueMode(String value) {
      this.ctcpValueMode = value;
      return this;
    }

    Builder ctcpValuePattern(String value) {
      this.ctcpValuePattern = value;
      return this;
    }

    Builder scriptEnabled(boolean value) {
      this.scriptEnabled = value;
      return this;
    }

    Builder scriptPath(String value) {
      this.scriptPath = value;
      return this;
    }

    IrcEventNotificationRuleEditValues build() {
      return new IrcEventNotificationRuleEditValues(
          eventType,
          sourceMode,
          sourcePattern,
          channelScope,
          channelPatterns,
          ctcpCommandMode,
          ctcpCommandPattern,
          ctcpValueMode,
          ctcpValuePattern,
          scriptEnabled,
          scriptPath);
    }
  }
}
