package cafe.woden.ircclient.notify.api.pushy;

import cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.TargetMode;

/** Plans normalized Pushy settings values from feature-safe scalar preferences input. */
public final class PushyNotificationSettingsSelectionPlanner {
  private PushyNotificationSettingsSelectionPlanner() {}

  public static PushyNotificationSettingsSelectionPlan plan(
      boolean enabled,
      String endpoint,
      String apiKey,
      TargetMode targetMode,
      String targetValue,
      String titlePrefix,
      int connectTimeoutSeconds,
      int readTimeoutSeconds) {
    PushyNotificationTargetSelectionPlan targetPlan =
        PushyNotificationTargetSelectionPlanner.planSelected(targetMode, targetValue);
    return new PushyNotificationSettingsSelectionPlan(
        enabled,
        endpoint,
        apiKey,
        targetPlan.deviceToken(),
        targetPlan.topic(),
        titlePrefix,
        connectTimeoutSeconds,
        readTimeoutSeconds);
  }
}
