package cafe.woden.ircclient.notify.api.irc;

import java.util.Locale;
import java.util.Objects;

/** Feature-owned normalization for persisted IRC-event notification rule settings. */
public final class IrcEventNotificationPersistedRulePlanner {
  private IrcEventNotificationPersistedRulePlanner() {}

  public static IrcEventNotificationPersistedRulePlan plan(
      Boolean enabled,
      String eventType,
      String sourceMode,
      String sourcePattern,
      String channelScope,
      String channelPatterns,
      Boolean toastEnabled,
      Boolean toastWhenFocused,
      String focusScope,
      Boolean statusBarEnabled,
      Boolean notificationsNodeEnabled,
      Boolean soundEnabled,
      String soundId,
      Boolean soundUseCustom,
      String soundCustomPath,
      Boolean scriptEnabled,
      String scriptPath,
      String scriptArgs,
      String scriptWorkingDirectory,
      String ctcpCommandMode,
      String ctcpCommandPattern,
      String ctcpValueMode,
      String ctcpValuePattern,
      String channelWhitelist,
      String channelBlacklist) {
    String normalizedEvent =
        normalizeNameOrDefault(
            eventType, IrcEventNotificationRuleEditSeedPlanner.DEFAULT_EVENT_TYPE);

    String normalizedSourceMode =
        normalizeNameOrDefault(
            sourceMode, IrcEventNotificationDefaultRuleCatalog.DEFAULT_SOURCE_MODE);
    String normalizedSourcePattern = normalizeTextOrNull(sourcePattern);
    if (!IrcEventNotificationRuleEditPolicy.sourcePatternRequired(normalizedSourceMode)) {
      normalizedSourcePattern = null;
    }

    String includeLegacy = normalizeTextOrNull(channelWhitelist);
    String excludeLegacy = normalizeTextOrNull(channelBlacklist);
    String normalizedChannelScope = normalizeNameOrNull(channelScope);
    if (normalizedChannelScope == null) {
      if (includeLegacy != null) {
        normalizedChannelScope = "ONLY";
      } else if (excludeLegacy != null) {
        normalizedChannelScope = "ALL_EXCEPT";
      } else {
        normalizedChannelScope = IrcEventNotificationDefaultRuleCatalog.DEFAULT_CHANNEL_SCOPE;
      }
    }

    String normalizedChannelPatterns = normalizeTextOrNull(channelPatterns);
    if (normalizedChannelPatterns == null) {
      if ("ONLY".equals(normalizedChannelScope)) {
        normalizedChannelPatterns = includeLegacy;
      } else if ("ALL_EXCEPT".equals(normalizedChannelScope)) {
        normalizedChannelPatterns = excludeLegacy;
      }
    }
    if (!IrcEventNotificationRuleEditPolicy.channelPatternsRequired(normalizedChannelScope)) {
      normalizedChannelPatterns = null;
    }

    boolean normalizedToastEnabled = booleanOrDefault(toastEnabled, true);
    String normalizedFocusScope = normalizeNameOrNull(focusScope);
    if (normalizedFocusScope == null) {
      normalizedFocusScope =
          Boolean.TRUE.equals(toastWhenFocused)
              ? "ANY"
              : IrcEventNotificationDefaultRuleCatalog.DEFAULT_FOCUS_SCOPE;
    }
    boolean normalizedToastWhenFocused =
        toastWhenFocused != null
            ? toastWhenFocused
            : !"BACKGROUND_ONLY".equals(normalizedFocusScope);

    boolean normalizedSoundEnabled = booleanOrDefault(soundEnabled, false);
    boolean normalizedStatusBarEnabled =
        statusBarEnabled != null
            ? statusBarEnabled
            : normalizedToastEnabled || normalizedSoundEnabled;
    boolean normalizedNotificationsNodeEnabled = booleanOrDefault(notificationsNodeEnabled, true);

    String normalizedSoundId = normalizeTextOrNull(soundId);
    if (normalizedSoundId == null) {
      normalizedSoundId =
          IrcEventNotificationDefaultRuleCatalog.defaultBuiltInSoundIdForEvent(normalizedEvent);
    }
    String normalizedSoundCustomPath = normalizeTextOrNull(soundCustomPath);
    boolean normalizedSoundUseCustom = Boolean.TRUE.equals(soundUseCustom);
    if (normalizedSoundUseCustom && normalizedSoundCustomPath == null) {
      normalizedSoundUseCustom = false;
    }

    String normalizedScriptPath = normalizeTextOrNull(scriptPath);
    boolean normalizedScriptEnabled = Boolean.TRUE.equals(scriptEnabled);
    if (normalizedScriptEnabled && normalizedScriptPath == null) {
      normalizedScriptEnabled = false;
    }
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

    return new IrcEventNotificationPersistedRulePlan(
        Boolean.TRUE.equals(enabled),
        normalizedEvent,
        normalizedSourceMode,
        normalizedSourcePattern,
        normalizedChannelScope,
        normalizedChannelPatterns,
        normalizedToastEnabled,
        normalizedToastWhenFocused,
        normalizedFocusScope,
        normalizedStatusBarEnabled,
        normalizedNotificationsNodeEnabled,
        normalizedSoundEnabled,
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
        normalizedCtcpValuePattern,
        includeLegacy,
        excludeLegacy);
  }

  private static boolean booleanOrDefault(Boolean value, boolean fallback) {
    return value != null ? value : fallback;
  }

  private static String normalizeNameOrDefault(String value, String fallback) {
    String normalized = normalizeNameOrNull(value);
    return normalized == null ? fallback : normalized;
  }

  private static String normalizeNameOrNull(String value) {
    String normalized = Objects.toString(value, "").trim().toUpperCase(Locale.ROOT);
    return normalized.isEmpty() ? null : normalized;
  }

  private static String normalizeTextOrNull(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
