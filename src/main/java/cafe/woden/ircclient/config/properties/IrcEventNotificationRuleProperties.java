package cafe.woden.ircclient.config.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Config-backed rule definition for IRC event notifications. */
public record IrcEventNotificationRuleProperties(
    Boolean enabled,
    EventType eventType,
    SourceMode sourceMode,
    String sourcePattern,
    ChannelScope channelScope,
    String channelPatterns,
    Boolean toastEnabled,
    Boolean toastWhenFocused,
    FocusScope focusScope,
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
    CtcpMatchMode ctcpCommandMode,
    String ctcpCommandPattern,
    CtcpMatchMode ctcpValueMode,
    String ctcpValuePattern,
    String channelWhitelist,
    String channelBlacklist) {
  private static final List<EventType> STATUS_BAR_ANY_COMPANION_EVENTS =
      List.of(EventType.KICKED, EventType.BANNED, EventType.KLINED);

  public enum EventType {
    KICKED,
    YOU_KICKED,
    BANNED,
    YOU_BANNED,
    VOICED,
    DEVOICED,
    OPPED,
    DEOPPED,
    HALF_OPPED,
    DEHALF_OPPED,
    YOU_OPPED,
    YOU_DEOPPED,
    YOU_VOICED,
    YOU_DEVOICED,
    YOU_HALF_OPPED,
    YOU_DEHALF_OPPED,
    PRIVATE_MESSAGE_RECEIVED,
    CTCP_RECEIVED,
    NOTICE_RECEIVED,
    WALLOPS_RECEIVED,
    INVITE_RECEIVED,
    USER_JOINED,
    USER_PARTED,
    USER_QUIT,
    USER_NICK_CHANGED,
    TOPIC_CHANGED,
    NETSPLIT_DETECTED,
    KLINED,
    YOU_KLINED
  }

  public enum SourceMode {
    ANY,
    SELF,
    OTHERS,
    NICK_LIST,
    GLOB,
    REGEX
  }

  public enum ChannelScope {
    ALL,
    ACTIVE_TARGET_ONLY,
    ONLY,
    ALL_EXCEPT
  }

  public enum FocusScope {
    ANY,
    FOREGROUND_ONLY,
    BACKGROUND_ONLY
  }

  public enum CtcpMatchMode {
    ANY,
    LIKE,
    GLOB,
    REGEX
  }

  public IrcEventNotificationRuleProperties {
    EventType normalizedEvent = eventType != null ? eventType : EventType.INVITE_RECEIVED;

    SourceMode normalizedSourceMode = sourceMode != null ? sourceMode : SourceMode.ANY;
    String normalizedSourcePattern = normalizeTextOrNull(sourcePattern);
    if (!sourcePatternRequired(normalizedSourceMode)) {
      normalizedSourcePattern = null;
    }

    String includeLegacy = normalizeTextOrNull(channelWhitelist);
    String excludeLegacy = normalizeTextOrNull(channelBlacklist);
    ChannelScope normalizedChannelScope = channelScope;
    if (normalizedChannelScope == null) {
      if (includeLegacy != null) {
        normalizedChannelScope = ChannelScope.ONLY;
      } else if (excludeLegacy != null) {
        normalizedChannelScope = ChannelScope.ALL_EXCEPT;
      } else {
        normalizedChannelScope = ChannelScope.ALL;
      }
    }

    String normalizedChannelPatterns = normalizeTextOrNull(channelPatterns);
    if (normalizedChannelPatterns == null) {
      if (normalizedChannelScope == ChannelScope.ONLY) {
        normalizedChannelPatterns = includeLegacy;
      } else if (normalizedChannelScope == ChannelScope.ALL_EXCEPT) {
        normalizedChannelPatterns = excludeLegacy;
      }
    }
    if (!channelPatternsRequired(normalizedChannelScope)) {
      normalizedChannelPatterns = null;
    }

    boolean normalizedToastEnabled = booleanOrDefault(toastEnabled, true);
    FocusScope normalizedFocusScope = focusScope;
    if (normalizedFocusScope == null) {
      normalizedFocusScope =
          Boolean.TRUE.equals(toastWhenFocused) ? FocusScope.ANY : FocusScope.BACKGROUND_ONLY;
    }
    boolean normalizedToastWhenFocused =
        toastWhenFocused != null
            ? toastWhenFocused
            : normalizedFocusScope != FocusScope.BACKGROUND_ONLY;

    boolean normalizedSoundEnabled = booleanOrDefault(soundEnabled, false);
    boolean normalizedStatusBarEnabled =
        statusBarEnabled != null
            ? statusBarEnabled
            : normalizedToastEnabled || normalizedSoundEnabled;
    boolean normalizedNotificationsNodeEnabled = booleanOrDefault(notificationsNodeEnabled, true);

    String normalizedSoundId = normalizeTextOrNull(soundId);
    if (normalizedSoundId == null) {
      normalizedSoundId = defaultBuiltInSoundIdForEvent(normalizedEvent);
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

    boolean ctcpActive = normalizedEvent == EventType.CTCP_RECEIVED;
    CtcpMatchMode normalizedCtcpCommandMode =
        ctcpActive && ctcpCommandMode != null ? ctcpCommandMode : CtcpMatchMode.ANY;
    CtcpMatchMode normalizedCtcpValueMode =
        ctcpActive && ctcpValueMode != null ? ctcpValueMode : CtcpMatchMode.ANY;
    String normalizedCtcpCommandPattern =
        ctcpActive ? normalizeTextOrNull(ctcpCommandPattern) : null;
    String normalizedCtcpValuePattern = ctcpActive ? normalizeTextOrNull(ctcpValuePattern) : null;
    if (!ctcpPatternRequired(normalizedCtcpCommandMode)) {
      normalizedCtcpCommandPattern = null;
    }
    if (!ctcpPatternRequired(normalizedCtcpValueMode)) {
      normalizedCtcpValuePattern = null;
    }

    enabled = Boolean.TRUE.equals(enabled);
    eventType = normalizedEvent;
    sourceMode = normalizedSourceMode;
    sourcePattern = normalizedSourcePattern;
    channelScope = normalizedChannelScope;
    channelPatterns = normalizedChannelPatterns;
    toastEnabled = normalizedToastEnabled;
    toastWhenFocused = normalizedToastWhenFocused;
    focusScope = normalizedFocusScope;
    statusBarEnabled = normalizedStatusBarEnabled;
    notificationsNodeEnabled = normalizedNotificationsNodeEnabled;
    soundEnabled = normalizedSoundEnabled;
    soundId = normalizedSoundId;
    soundUseCustom = normalizedSoundUseCustom;
    soundCustomPath = normalizedSoundCustomPath;
    scriptEnabled = normalizedScriptEnabled;
    scriptPath = normalizedScriptPath;
    scriptArgs = normalizedScriptArgs;
    scriptWorkingDirectory = normalizedScriptWorkingDirectory;
    ctcpCommandMode = normalizedCtcpCommandMode;
    ctcpCommandPattern = normalizedCtcpCommandPattern;
    ctcpValueMode = normalizedCtcpValueMode;
    ctcpValuePattern = normalizedCtcpValuePattern;
    channelWhitelist = includeLegacy;
    channelBlacklist = excludeLegacy;
  }

  public static List<IrcEventNotificationRuleProperties> defaultRules() {
    List<IrcEventNotificationRuleProperties> out =
        Arrays.stream(EventType.values())
            .map(
                eventType ->
                    defaultRule(
                        defaultEnabledForEvent(eventType),
                        eventType,
                        defaultSourceModeForEvent(eventType)))
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

    for (EventType eventType : STATUS_BAR_ANY_COMPANION_EVENTS) {
      out.add(statusBarAnyCompanionRule(eventType));
    }
    return List.copyOf(out);
  }

  private static IrcEventNotificationRuleProperties defaultRule(
      boolean enabled, EventType eventType, SourceMode sourceMode) {
    return new IrcEventNotificationRuleProperties(
        enabled,
        eventType,
        sourceMode,
        null,
        ChannelScope.ALL,
        null,
        true,
        false,
        FocusScope.BACKGROUND_ONLY,
        true,
        true,
        false,
        defaultBuiltInSoundIdForEvent(eventType),
        false,
        null,
        false,
        null,
        null,
        null,
        CtcpMatchMode.ANY,
        null,
        CtcpMatchMode.ANY,
        null,
        null,
        null);
  }

  private static IrcEventNotificationRuleProperties statusBarAnyCompanionRule(EventType eventType) {
    return new IrcEventNotificationRuleProperties(
        true,
        eventType,
        defaultSourceModeForEvent(eventType),
        null,
        ChannelScope.ALL,
        null,
        false,
        true,
        FocusScope.ANY,
        true,
        true,
        false,
        defaultBuiltInSoundIdForEvent(eventType),
        false,
        null,
        false,
        null,
        null,
        null,
        CtcpMatchMode.ANY,
        null,
        CtcpMatchMode.ANY,
        null,
        null,
        null);
  }

  private static String defaultBuiltInSoundIdForEvent(EventType eventType) {
    return switch (eventTypeOrDefault(enumName(eventType))) {
      case KICKED -> "SOMEBODY_GOT_KICKED";
      case YOU_KICKED -> "YOU_KICKED_1";
      case BANNED -> "SOMEBODY_BANNED";
      case YOU_BANNED -> "YOU_BANNED_1";
      case VOICED -> "SOMEBODY_GAVE_SOMEBODY_VOICE";
      case DEVOICED -> "SOMEONE_ELSE_TOOK_VOICE";
      case OPPED -> "SOMEBODY_OPPED";
      case DEOPPED -> "SOMEBODY_DEOPPED";
      case HALF_OPPED -> "SOMEBODY_HALF_OPPED";
      case DEHALF_OPPED -> "SOMEBODY_LOST_HALFOPS";
      case YOU_HALF_OPPED -> "YOU_HALF_OPS";
      case YOU_DEHALF_OPPED -> "YOU_HALF_OPS_REMOVED";
      case YOU_OPPED -> "YOU_OPS_1";
      case YOU_DEOPPED -> "YOU_DEOPPED";
      case YOU_VOICED -> "YOU_VOICE_1";
      case YOU_DEVOICED -> "YOU_LOST_VOICE_1";
      case PRIVATE_MESSAGE_RECEIVED -> "PM_RECEIVED_1";
      case CTCP_RECEIVED -> "SOMEBODY_SENT_CTCP_1";
      case NOTICE_RECEIVED -> "NOTICE_RECEIVED_1";
      case WALLOPS_RECEIVED -> "WALLOPS_1";
      case INVITE_RECEIVED -> "CHANNEL_INVITE_1";
      case USER_JOINED -> "USER_JOINED";
      case USER_PARTED -> "USER_LEFT_CHANNEL";
      case USER_QUIT -> "USER_DISCONNECTED_SERVER";
      case USER_NICK_CHANGED -> "SOMEBODY_NICK_CHANGED";
      case TOPIC_CHANGED -> "TOPIC_CHANGED_1";
      case NETSPLIT_DETECTED -> "NETSPLIT_1";
      case KLINED -> "USER_KLINED_1";
      case YOU_KLINED -> "YOU_KLINED";
    };
  }

  private static boolean defaultEnabledForEvent(EventType eventType) {
    return switch (eventTypeOrDefault(enumName(eventType))) {
      case PRIVATE_MESSAGE_RECEIVED, INVITE_RECEIVED, YOU_KICKED, YOU_BANNED, YOU_KLINED -> true;
      default -> false;
    };
  }

  private static SourceMode defaultSourceModeForEvent(EventType eventType) {
    return switch (eventTypeOrDefault(enumName(eventType))) {
      case KICKED,
          BANNED,
          VOICED,
          DEVOICED,
          OPPED,
          DEOPPED,
          HALF_OPPED,
          DEHALF_OPPED,
          PRIVATE_MESSAGE_RECEIVED,
          CTCP_RECEIVED,
          NOTICE_RECEIVED,
          WALLOPS_RECEIVED,
          INVITE_RECEIVED,
          USER_JOINED,
          USER_PARTED,
          USER_QUIT,
          USER_NICK_CHANGED,
          NETSPLIT_DETECTED,
          KLINED ->
          SourceMode.OTHERS;
      default -> SourceMode.ANY;
    };
  }

  private static boolean booleanOrDefault(Boolean value, boolean fallback) {
    return value != null ? value : fallback;
  }

  private static boolean sourcePatternRequired(SourceMode sourceMode) {
    SourceMode mode = sourceMode != null ? sourceMode : SourceMode.ANY;
    return mode == SourceMode.NICK_LIST || mode == SourceMode.GLOB || mode == SourceMode.REGEX;
  }

  private static boolean channelPatternsRequired(ChannelScope channelScope) {
    ChannelScope scope = channelScope != null ? channelScope : ChannelScope.ALL;
    return scope == ChannelScope.ONLY || scope == ChannelScope.ALL_EXCEPT;
  }

  private static boolean ctcpPatternRequired(CtcpMatchMode mode) {
    return mode != null && mode != CtcpMatchMode.ANY;
  }

  private static EventType eventTypeOrDefault(String value) {
    if (value == null || value.isBlank()) return EventType.INVITE_RECEIVED;
    try {
      return EventType.valueOf(value.trim());
    } catch (Exception ignored) {
      return EventType.INVITE_RECEIVED;
    }
  }

  private static String enumName(Enum<?> value) {
    return value == null ? null : value.name();
  }

  private static String normalizeTextOrNull(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
