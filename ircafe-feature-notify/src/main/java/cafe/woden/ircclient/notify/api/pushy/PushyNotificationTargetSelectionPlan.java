package cafe.woden.ircclient.notify.api.pushy;

import cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.TargetMode;
import java.util.Objects;

/** Feature-owned target-mode/value normalization for Pushy settings forms. */
public record PushyNotificationTargetSelectionPlan(
    TargetMode targetMode, String targetValue, String deviceToken, String topic) {

  public PushyNotificationTargetSelectionPlan {
    if (targetMode == null) targetMode = TargetMode.DEVICE_TOKEN;
    targetValue = trim(targetValue);
    deviceToken = trimToNull(deviceToken);
    topic = trimToNull(topic);
  }

  private static String trim(String raw) {
    return Objects.toString(raw, "").trim();
  }

  private static String trimToNull(String raw) {
    String value = trim(raw);
    return value.isEmpty() ? null : value;
  }
}
