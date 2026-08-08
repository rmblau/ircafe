package cafe.woden.ircclient.notify.api.sound;

import java.util.Objects;

/** Feature-safe preview action for notification sound controls. */
public record NotificationSoundPreviewPlan(Action action, String customPath) {

  public enum Action {
    SKIP,
    BUILT_IN_SOUND,
    CUSTOM_FILE
  }

  public NotificationSoundPreviewPlan {
    action = action == null ? Action.SKIP : action;
    customPath = Objects.toString(customPath, "").trim();
    if (action != Action.CUSTOM_FILE || customPath.isEmpty()) {
      customPath = null;
    }
    if (action == Action.CUSTOM_FILE && customPath == null) {
      action = Action.SKIP;
    }
  }

  public static NotificationSoundPreviewPlan skip() {
    return new NotificationSoundPreviewPlan(Action.SKIP, null);
  }

  public static NotificationSoundPreviewPlan builtInSound() {
    return new NotificationSoundPreviewPlan(Action.BUILT_IN_SOUND, null);
  }

  public static NotificationSoundPreviewPlan customFile(String customPath) {
    return new NotificationSoundPreviewPlan(Action.CUSTOM_FILE, customPath);
  }
}
