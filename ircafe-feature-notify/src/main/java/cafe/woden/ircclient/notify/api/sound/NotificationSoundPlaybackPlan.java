package cafe.woden.ircclient.notify.api.sound;

import java.util.Objects;

/** Feature-safe playback decision for notification sounds. */
public record NotificationSoundPlaybackPlan(Action action, String resourcePath) {

  public enum Action {
    SKIP,
    CUSTOM_FILE,
    BUILT_IN_RESOURCE
  }

  public NotificationSoundPlaybackPlan {
    action = action == null ? Action.SKIP : action;
    resourcePath = Objects.toString(resourcePath, "").trim();
    if (action != Action.BUILT_IN_RESOURCE || resourcePath.isEmpty()) {
      resourcePath = null;
    }
    if (action == Action.BUILT_IN_RESOURCE && resourcePath == null) {
      action = Action.SKIP;
    }
  }

  public static NotificationSoundPlaybackPlan skip() {
    return new NotificationSoundPlaybackPlan(Action.SKIP, null);
  }

  public static NotificationSoundPlaybackPlan customFile() {
    return new NotificationSoundPlaybackPlan(Action.CUSTOM_FILE, null);
  }

  public static NotificationSoundPlaybackPlan builtInResource(String resourcePath) {
    return new NotificationSoundPlaybackPlan(Action.BUILT_IN_RESOURCE, resourcePath);
  }

  public boolean skipPlayback() {
    return action == Action.SKIP;
  }

  public boolean usesCustomFile() {
    return action == Action.CUSTOM_FILE;
  }

  public boolean usesBuiltInResource() {
    return action == Action.BUILT_IN_RESOURCE;
  }
}
