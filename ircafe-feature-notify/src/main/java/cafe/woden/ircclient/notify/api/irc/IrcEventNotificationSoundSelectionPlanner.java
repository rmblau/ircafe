package cafe.woden.ircclient.notify.api.irc;

import java.util.Locale;
import java.util.Objects;

/**
 * Feature-owned policy for preserving or updating IRC-event built-in sound selections while
 * editing.
 */
public final class IrcEventNotificationSoundSelectionPlanner {
  private IrcEventNotificationSoundSelectionPlanner() {}

  /**
   * Returns a plan to update the built-in sound when the current selection still equals the
   * previous event's default. Manual built-in selections and custom-sound selections are preserved.
   */
  public static IrcEventNotificationSoundSelectionPlan planDefaultSoundForEventChange(
      String previousEventType,
      String selectedEventType,
      String currentSoundId,
      boolean customSoundSelected) {
    if (customSoundSelected) return IrcEventNotificationSoundSelectionPlan.keepCurrent();

    String currentSound = normalize(currentSoundId);
    if (currentSound.isEmpty()) return IrcEventNotificationSoundSelectionPlan.keepCurrent();

    String previousDefault =
        normalize(
            IrcEventNotificationDefaultRuleCatalog.defaultBuiltInSoundIdForEvent(
                previousEventType));
    if (!currentSound.equals(previousDefault)) {
      return IrcEventNotificationSoundSelectionPlan.keepCurrent();
    }

    String selectedDefault =
        normalize(
            IrcEventNotificationDefaultRuleCatalog.defaultBuiltInSoundIdForEvent(
                selectedEventType));
    if (selectedDefault.isEmpty() || selectedDefault.equals(currentSound)) {
      return IrcEventNotificationSoundSelectionPlan.keepCurrent();
    }
    return IrcEventNotificationSoundSelectionPlan.updateTo(selectedDefault);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim().toUpperCase(Locale.ROOT);
  }
}
