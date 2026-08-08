package cafe.woden.ircclient.notify.api.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IrcEventNotificationPersistedRulePlannerTest {

  @Test
  void appliesPersistedDefaultsForMissingValues() {
    IrcEventNotificationPersistedRulePlan plan = plan(null, null, null, null, null, null);

    assertFalse(plan.enabled());
    assertEquals("INVITE_RECEIVED", plan.eventType());
    assertEquals("ANY", plan.sourceMode());
    assertEquals("ALL", plan.channelScope());
    assertTrue(plan.toastEnabled());
    assertFalse(plan.toastWhenFocused());
    assertEquals("BACKGROUND_ONLY", plan.focusScope());
    assertTrue(plan.statusBarEnabled());
    assertTrue(plan.notificationsNodeEnabled());
    assertFalse(plan.soundEnabled());
    assertEquals("CHANNEL_INVITE_1", plan.soundId());
  }

  @Test
  void migratesLegacyChannelWhitelistAndBlacklist() {
    IrcEventNotificationPersistedRulePlan whitelist =
        plan(null, null, null, null, null, " #ops , #dev ", null);
    IrcEventNotificationPersistedRulePlan blacklist =
        plan(null, null, null, null, null, null, " #noise ");

    assertEquals("ONLY", whitelist.channelScope());
    assertEquals("#ops , #dev", whitelist.channelPatterns());
    assertEquals("#ops , #dev", whitelist.channelWhitelist());

    assertEquals("ALL_EXCEPT", blacklist.channelScope());
    assertEquals("#noise", blacklist.channelPatterns());
    assertEquals("#noise", blacklist.channelBlacklist());
  }

  @Test
  void explicitChannelPatternsWinOverLegacyLists() {
    IrcEventNotificationPersistedRulePlan plan =
        plan(null, null, null, "only", " #current ", "#legacy", null);

    assertEquals("ONLY", plan.channelScope());
    assertEquals("#current", plan.channelPatterns());
    assertEquals("#legacy", plan.channelWhitelist());
  }

  @Test
  void focusScopeFallsBackFromLegacyToastWhenFocused() {
    IrcEventNotificationPersistedRulePlan focused =
        plan(null, null, null, null, null, Boolean.TRUE);
    IrcEventNotificationPersistedRulePlan background =
        plan(null, null, null, null, null, Boolean.FALSE);

    assertEquals("ANY", focused.focusScope());
    assertTrue(focused.toastWhenFocused());
    assertEquals("BACKGROUND_ONLY", background.focusScope());
    assertFalse(background.toastWhenFocused());
  }

  @Test
  void missingStatusBarUsesToastOrSoundCompatibilityDefault() {
    IrcEventNotificationPersistedRulePlan quiet = plan(false, false, null);
    IrcEventNotificationPersistedRulePlan toast = plan(true, false, null);
    IrcEventNotificationPersistedRulePlan sound = plan(false, true, null);
    IrcEventNotificationPersistedRulePlan explicit = plan(true, true, Boolean.FALSE);

    assertFalse(quiet.statusBarEnabled());
    assertTrue(toast.statusBarEnabled());
    assertTrue(sound.statusBarEnabled());
    assertFalse(explicit.statusBarEnabled());
  }

  @Test
  void dependentSoundAndScriptValuesRequirePaths() {
    IrcEventNotificationPersistedRulePlan plan =
        IrcEventNotificationPersistedRulePlanner.plan(
            true,
            "invite_received",
            null,
            null,
            null,
            null,
            true,
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
            " --arg ",
            " /tmp ",
            null,
            null,
            null,
            null,
            null,
            null);

    assertEquals("CHANNEL_INVITE_1", plan.soundId());
    assertFalse(plan.soundUseCustom());
    assertNull(plan.soundCustomPath());
    assertFalse(plan.scriptEnabled());
    assertNull(plan.scriptPath());
    assertEquals("--arg", plan.scriptArgs());
    assertEquals("/tmp", plan.scriptWorkingDirectory());
  }

  @Test
  void ctcpPatternsAreOnlyKeptForCtcpEventsAndRequiredModes() {
    IrcEventNotificationPersistedRulePlan nonCtcp =
        plan("INVITE_RECEIVED", "REGEX", " VERSION.* ", "LIKE", " 123 ");
    IrcEventNotificationPersistedRulePlan ctcp =
        plan("CTCP_RECEIVED", "REGEX", " VERSION.* ", "ANY", " 123 ");

    assertEquals("ANY", nonCtcp.ctcpCommandMode());
    assertNull(nonCtcp.ctcpCommandPattern());
    assertEquals("ANY", nonCtcp.ctcpValueMode());
    assertNull(nonCtcp.ctcpValuePattern());

    assertEquals("REGEX", ctcp.ctcpCommandMode());
    assertEquals("VERSION.*", ctcp.ctcpCommandPattern());
    assertEquals("ANY", ctcp.ctcpValueMode());
    assertNull(ctcp.ctcpValuePattern());
  }

  private static IrcEventNotificationPersistedRulePlan plan(
      String eventType,
      String sourceMode,
      String sourcePattern,
      String channelScope,
      String channelPatterns,
      Boolean toastWhenFocused) {
    return IrcEventNotificationPersistedRulePlanner.plan(
        null,
        eventType,
        sourceMode,
        sourcePattern,
        channelScope,
        channelPatterns,
        null,
        toastWhenFocused,
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
        null);
  }

  private static IrcEventNotificationPersistedRulePlan plan(
      String eventType,
      String sourceMode,
      String sourcePattern,
      String channelScope,
      String channelPatterns,
      String channelWhitelist,
      String channelBlacklist) {
    return IrcEventNotificationPersistedRulePlanner.plan(
        null,
        eventType,
        sourceMode,
        sourcePattern,
        channelScope,
        channelPatterns,
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
        channelWhitelist,
        channelBlacklist);
  }

  private static IrcEventNotificationPersistedRulePlan plan(
      boolean toastEnabled, boolean soundEnabled, Boolean statusBarEnabled) {
    return IrcEventNotificationPersistedRulePlanner.plan(
        null,
        null,
        null,
        null,
        null,
        null,
        toastEnabled,
        null,
        null,
        statusBarEnabled,
        null,
        soundEnabled,
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
        null);
  }

  private static IrcEventNotificationPersistedRulePlan plan(
      String eventType,
      String ctcpCommandMode,
      String ctcpCommandPattern,
      String ctcpValueMode,
      String ctcpValuePattern) {
    return IrcEventNotificationPersistedRulePlanner.plan(
        null,
        eventType,
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
        ctcpCommandMode,
        ctcpCommandPattern,
        ctcpValueMode,
        ctcpValuePattern,
        null,
        null);
  }
}
