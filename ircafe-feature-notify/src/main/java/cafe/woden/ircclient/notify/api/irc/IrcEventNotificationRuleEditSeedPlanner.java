package cafe.woden.ircclient.notify.api.irc;

import java.util.Locale;
import java.util.Objects;

/** Plans normalized seed values for IRC-event notification rule edit surfaces. */
public final class IrcEventNotificationRuleEditSeedPlanner {
  public static final String DEFAULT_EVENT_TYPE = "INVITE_RECEIVED";

  private IrcEventNotificationRuleEditSeedPlanner() {}

  public static IrcEventNotificationRuleEditSeedPlan defaultSeed() {
    return plan(
        false,
        DEFAULT_EVENT_TYPE,
        null,
        null,
        null,
        null,
        true,
        null,
        true,
        true,
        false,
        null,
        false,
        null,
        false,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  public static IrcEventNotificationRuleEditSeedPlan plan(
      boolean enabled,
      String eventType,
      String sourceMode,
      String sourcePattern,
      String channelScope,
      String channelPatterns,
      boolean toastEnabled,
      String focusScope,
      boolean statusBarEnabled,
      boolean notificationsNodeEnabled,
      boolean soundEnabled,
      String soundId,
      boolean soundUseCustom,
      String soundCustomPath,
      boolean scriptEnabled,
      String scriptPath,
      String scriptArgs,
      String scriptWorkingDirectory,
      String ctcpCommandMode,
      String ctcpCommandPattern,
      String ctcpValueMode,
      String ctcpValuePattern) {
    String normalizedEvent = normalizeNameOrDefault(eventType, DEFAULT_EVENT_TYPE);
    String normalizedSourceMode =
        normalizeNameOrDefault(
            sourceMode, IrcEventNotificationDefaultRuleCatalog.DEFAULT_SOURCE_MODE);
    String normalizedSourcePattern = normalizeTextOrNull(sourcePattern);
    if (!IrcEventNotificationRuleEditPolicy.sourcePatternRequired(normalizedSourceMode)) {
      normalizedSourcePattern = null;
    }

    String normalizedChannelScope =
        normalizeNameOrDefault(
            channelScope, IrcEventNotificationDefaultRuleCatalog.DEFAULT_CHANNEL_SCOPE);
    String normalizedChannelPatterns = normalizeTextOrNull(channelPatterns);
    if (!IrcEventNotificationRuleEditPolicy.channelPatternsRequired(normalizedChannelScope)) {
      normalizedChannelPatterns = null;
    }

    String normalizedFocusScope =
        normalizeNameOrDefault(
            focusScope, IrcEventNotificationDefaultRuleCatalog.DEFAULT_FOCUS_SCOPE);

    String normalizedSoundId = normalizeTextOrNull(soundId);
    if (normalizedSoundId == null) {
      normalizedSoundId =
          IrcEventNotificationDefaultRuleCatalog.defaultBuiltInSoundIdForEvent(normalizedEvent);
    }
    String normalizedSoundCustomPath = normalizeTextOrNull(soundCustomPath);
    boolean normalizedSoundUseCustom = soundUseCustom && normalizedSoundCustomPath != null;

    String normalizedScriptPath = normalizeTextOrNull(scriptPath);
    boolean normalizedScriptEnabled = scriptEnabled && normalizedScriptPath != null;
    String normalizedScriptArgs = normalizeTextOrNull(scriptArgs);
    String normalizedScriptWorkingDirectory = normalizeTextOrNull(scriptWorkingDirectory);

    boolean ctcpActive = IrcEventNotificationRuleEditPolicy.ctcpFiltersActive(normalizedEvent);
    String normalizedCtcpCommandMode =
        ctcpActive
            ? normalizeNameOrDefault(
                ctcpCommandMode, IrcEventNotificationDefaultRuleCatalog.DEFAULT_CTCP_MATCH_MODE)
            : IrcEventNotificationDefaultRuleCatalog.DEFAULT_CTCP_MATCH_MODE;
    String normalizedCtcpValueMode =
        ctcpActive
            ? normalizeNameOrDefault(
                ctcpValueMode, IrcEventNotificationDefaultRuleCatalog.DEFAULT_CTCP_MATCH_MODE)
            : IrcEventNotificationDefaultRuleCatalog.DEFAULT_CTCP_MATCH_MODE;
    String normalizedCtcpCommandPattern =
        ctcpActive ? normalizeTextOrNull(ctcpCommandPattern) : null;
    String normalizedCtcpValuePattern = ctcpActive ? normalizeTextOrNull(ctcpValuePattern) : null;
    if (!IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(normalizedCtcpCommandMode)) {
      normalizedCtcpCommandPattern = null;
    }
    if (!IrcEventNotificationRuleEditPolicy.ctcpPatternRequired(normalizedCtcpValueMode)) {
      normalizedCtcpValuePattern = null;
    }

    return new IrcEventNotificationRuleEditSeedPlan(
        enabled,
        normalizedEvent,
        normalizedSourceMode,
        normalizedSourcePattern,
        normalizedChannelScope,
        normalizedChannelPatterns,
        toastEnabled,
        normalizedFocusScope,
        statusBarEnabled,
        notificationsNodeEnabled,
        soundEnabled,
        normalizedSoundId,
        normalizedSoundUseCustom,
        normalizedSoundCustomPath,
        normalizedScriptEnabled,
        normalizedScriptPath,
        normalizedScriptArgs,
        normalizedScriptWorkingDirectory,
        normalizedCtcpCommandMode,
        normalizedCtcpCommandPattern,
        normalizedCtcpValueMode,
        normalizedCtcpValuePattern);
  }

  private static String normalizeNameOrDefault(String value, String fallback) {
    String normalized = Objects.toString(value, "").trim().toUpperCase(Locale.ROOT);
    return normalized.isEmpty() ? fallback : normalized;
  }

  private static String normalizeTextOrNull(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
