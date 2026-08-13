package cafe.woden.ircclient.notify.api.irc;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Feature-owned default IRC-event notification rule catalog and sound-selection policy. */
public final class IrcEventNotificationDefaultRuleCatalog {
  public static final String DEFAULT_SOUND_ID = "NOTIF_1";
  public static final String DEFAULT_SOURCE_MODE = "ANY";
  public static final String DEFAULT_CHANNEL_SCOPE = "ALL";
  public static final String DEFAULT_FOCUS_SCOPE = "BACKGROUND_ONLY";
  public static final String DEFAULT_CTCP_MATCH_MODE = "ANY";

  private static final List<String> STATUS_BAR_ANY_COMPANION_EVENTS =
      List.of("KICKED", "BANNED", "KLINED");

  private IrcEventNotificationDefaultRuleCatalog() {}

  /** Builds the complete default catalog in the same order as the supplied event names. */
  public static List<IrcEventNotificationDefaultRule> defaults(List<String> eventTypes) {
    List<String> events = normalizeEventTypes(eventTypes);
    if (events.isEmpty()) return List.of();

    List<IrcEventNotificationDefaultRule> out = new ArrayList<>();
    for (String eventType : events) {
      out.add(
          defaultRule(
              defaultEnabledForEvent(eventType), eventType, defaultSourceModeForEvent(eventType)));
    }

    Set<String> available = new LinkedHashSet<>(events);
    for (String eventType : STATUS_BAR_ANY_COMPANION_EVENTS) {
      if (available.contains(eventType)) {
        out.add(statusBarAnyCompanionRule(eventType));
      }
    }
    return List.copyOf(out);
  }

  /** Selects the built-in sound id for an IRC event name. */
  public static String defaultBuiltInSoundIdForEvent(String eventType) {
    return switch (normalizeEventType(eventType)) {
      case "KICKED" -> "SOMEBODY_GOT_KICKED";
      case "YOU_KICKED" -> "YOU_KICKED_1";
      case "BANNED" -> "SOMEBODY_BANNED";
      case "YOU_BANNED" -> "YOU_BANNED_1";
      case "VOICED" -> "SOMEBODY_GAVE_SOMEBODY_VOICE";
      case "DEVOICED" -> "SOMEONE_ELSE_TOOK_VOICE";
      case "OPPED" -> "SOMEBODY_OPPED";
      case "DEOPPED" -> "SOMEBODY_DEOPPED";
      case "HALF_OPPED" -> "SOMEBODY_HALF_OPPED";
      case "DEHALF_OPPED" -> "SOMEBODY_LOST_HALFOPS";
      case "YOU_HALF_OPPED" -> "YOU_HALF_OPS";
      case "YOU_DEHALF_OPPED" -> "YOU_HALF_OPS_REMOVED";
      case "YOU_OPPED" -> "YOU_OPS_1";
      case "YOU_DEOPPED" -> "YOU_DEOPPED";
      case "YOU_VOICED" -> "YOU_VOICE_1";
      case "YOU_DEVOICED" -> "YOU_LOST_VOICE_1";
      case "PRIVATE_MESSAGE_RECEIVED" -> "PM_RECEIVED_1";
      case "CTCP_RECEIVED" -> "SOMEBODY_SENT_CTCP_1";
      case "NOTICE_RECEIVED" -> "NOTICE_RECEIVED_1";
      case "WALLOPS_RECEIVED" -> "WALLOPS_1";
      case "INVITE_RECEIVED" -> "CHANNEL_INVITE_1";
      case "USER_JOINED" -> "USER_JOINED";
      case "USER_PARTED" -> "USER_LEFT_CHANNEL";
      case "USER_QUIT" -> "USER_DISCONNECTED_SERVER";
      case "USER_NICK_CHANGED" -> "SOMEBODY_NICK_CHANGED";
      case "TOPIC_CHANGED" -> "TOPIC_CHANGED_1";
      case "NETSPLIT_DETECTED" -> "NETSPLIT_1";
      case "KLINED" -> "USER_KLINED_1";
      case "YOU_KLINED" -> "YOU_KLINED";
      default -> DEFAULT_SOUND_ID;
    };
  }

  public static boolean defaultEnabledForEvent(String eventType) {
    return switch (normalizeEventType(eventType)) {
      case "PRIVATE_MESSAGE_RECEIVED",
          "INVITE_RECEIVED",
          "YOU_KICKED",
          "YOU_BANNED",
          "YOU_KLINED" ->
          true;
      default -> false;
    };
  }

  public static String defaultSourceModeForEvent(String eventType) {
    return switch (normalizeEventType(eventType)) {
      case "KICKED",
          "BANNED",
          "VOICED",
          "DEVOICED",
          "OPPED",
          "DEOPPED",
          "HALF_OPPED",
          "DEHALF_OPPED",
          "PRIVATE_MESSAGE_RECEIVED",
          "CTCP_RECEIVED",
          "NOTICE_RECEIVED",
          "WALLOPS_RECEIVED",
          "INVITE_RECEIVED",
          "USER_JOINED",
          "USER_PARTED",
          "USER_QUIT",
          "USER_NICK_CHANGED",
          "NETSPLIT_DETECTED",
          "KLINED" ->
          "OTHERS";
      default -> DEFAULT_SOURCE_MODE;
    };
  }

  private static IrcEventNotificationDefaultRule defaultRule(
      boolean enabled, String eventType, String sourceMode) {
    return new IrcEventNotificationDefaultRule(
        enabled,
        eventType,
        sourceMode,
        null,
        DEFAULT_CHANNEL_SCOPE,
        null,
        true,
        DEFAULT_FOCUS_SCOPE,
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
        DEFAULT_CTCP_MATCH_MODE,
        null,
        DEFAULT_CTCP_MATCH_MODE,
        null);
  }

  private static IrcEventNotificationDefaultRule statusBarAnyCompanionRule(String eventType) {
    return new IrcEventNotificationDefaultRule(
        true,
        eventType,
        defaultSourceModeForEvent(eventType),
        null,
        DEFAULT_CHANNEL_SCOPE,
        null,
        false,
        "ANY",
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
        DEFAULT_CTCP_MATCH_MODE,
        null,
        DEFAULT_CTCP_MATCH_MODE,
        null);
  }

  private static List<String> normalizeEventTypes(List<String> eventTypes) {
    if (eventTypes == null || eventTypes.isEmpty()) return List.of();
    List<String> out = new ArrayList<>();
    for (String eventType : eventTypes) {
      String normalized = normalizeEventType(eventType);
      if (!normalized.isEmpty()) out.add(normalized);
    }
    return List.copyOf(out);
  }

  private static String normalizeEventType(String eventType) {
    return Objects.toString(eventType, "").trim().toUpperCase(Locale.ROOT);
  }
}
