package cafe.woden.ircclient.notify.api.irc;

import java.util.Locale;
import java.util.Objects;

/** Feature-owned edit-time plan for updating an IRC-event rule's built-in sound selection. */
public record IrcEventNotificationSoundSelectionPlan(boolean updateBuiltInSound, String soundId) {
  public IrcEventNotificationSoundSelectionPlan {
    soundId = updateBuiltInSound ? normalize(soundId) : null;
    if (soundId == null || soundId.isEmpty()) {
      updateBuiltInSound = false;
      soundId = null;
    }
  }

  public static IrcEventNotificationSoundSelectionPlan keepCurrent() {
    return new IrcEventNotificationSoundSelectionPlan(false, null);
  }

  public static IrcEventNotificationSoundSelectionPlan updateTo(String soundId) {
    return new IrcEventNotificationSoundSelectionPlan(true, soundId);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim().toUpperCase(Locale.ROOT);
  }
}
