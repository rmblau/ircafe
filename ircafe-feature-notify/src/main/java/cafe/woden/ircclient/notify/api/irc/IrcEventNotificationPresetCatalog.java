package cafe.woden.ircclient.notify.api.irc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Feature-owned preset catalog for IRC-event notification rules. */
public final class IrcEventNotificationPresetCatalog {
  public static final String ESSENTIAL = "ESSENTIAL";
  public static final String MODERATION = "MODERATION";
  public static final String ALL_EVENTS = "ALL_EVENTS";

  private IrcEventNotificationPresetCatalog() {}

  /** Builds a named preset using the supplied event names as the available event catalog. */
  public static List<IrcEventNotificationDefaultRule> buildPreset(
      String preset, List<String> eventTypes) {
    return switch (normalizePreset(preset)) {
      case ESSENTIAL ->
          List.of(
              eventDefaultRule("PRIVATE_MESSAGE_RECEIVED", "OTHERS", false),
              eventDefaultRule("INVITE_RECEIVED", "OTHERS", false),
              eventDefaultRule("YOU_KICKED", "ANY", false),
              eventDefaultRule("YOU_BANNED", "ANY", false),
              eventDefaultRule("YOU_KLINED", "ANY", false));
      case MODERATION ->
          List.of(
              eventDefaultRule("KICKED", "OTHERS", false),
              eventDefaultRule("BANNED", "OTHERS", false),
              eventDefaultRule("OPPED", "OTHERS", false),
              eventDefaultRule("DEOPPED", "OTHERS", false),
              eventDefaultRule("VOICED", "OTHERS", false),
              eventDefaultRule("DEVOICED", "OTHERS", false),
              eventDefaultRule("HALF_OPPED", "OTHERS", false),
              eventDefaultRule("DEHALF_OPPED", "OTHERS", false),
              eventDefaultRule("INVITE_RECEIVED", "ANY", false));
      case ALL_EVENTS -> allEventsPreset(eventTypes);
      default -> List.of();
    };
  }

  private static List<IrcEventNotificationDefaultRule> allEventsPreset(List<String> eventTypes) {
    List<String> events = normalizeEventTypes(eventTypes);
    if (events.isEmpty()) return List.of();

    List<IrcEventNotificationDefaultRule> out = new ArrayList<>();
    for (String eventType : events) {
      out.add(eventDefaultRule(eventType, "ANY", false));
    }
    return List.copyOf(out);
  }

  private static IrcEventNotificationDefaultRule eventDefaultRule(
      String eventType, String sourceMode, boolean soundEnabled) {
    return new IrcEventNotificationDefaultRule(
        true,
        eventType,
        sourceMode,
        null,
        IrcEventNotificationDefaultRuleCatalog.DEFAULT_CHANNEL_SCOPE,
        null,
        true,
        IrcEventNotificationDefaultRuleCatalog.DEFAULT_FOCUS_SCOPE,
        true,
        true,
        soundEnabled,
        IrcEventNotificationDefaultRuleCatalog.defaultBuiltInSoundIdForEvent(eventType),
        false,
        null,
        false,
        null,
        null,
        null,
        IrcEventNotificationDefaultRuleCatalog.DEFAULT_CTCP_MATCH_MODE,
        null,
        IrcEventNotificationDefaultRuleCatalog.DEFAULT_CTCP_MATCH_MODE,
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

  private static String normalizePreset(String preset) {
    return Objects.toString(preset, "").trim().toUpperCase(Locale.ROOT);
  }
}
