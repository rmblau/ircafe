package cafe.woden.ircclient.notify.api.pushy;

import cafe.woden.ircclient.notify.api.pushy.PushyNotificationSettingsValidator.TargetMode;
import java.util.Objects;

/** Plans Pushy device-token/topic selections from feature-safe scalar values. */
public final class PushyNotificationTargetSelectionPlanner {
  private PushyNotificationTargetSelectionPlanner() {}

  public static PushyNotificationTargetSelectionPlan planInitial(String deviceToken, String topic) {
    String token = trimToNull(deviceToken);
    if (token != null) {
      return new PushyNotificationTargetSelectionPlan(TargetMode.DEVICE_TOKEN, token, token, null);
    }

    String topicValue = trimToNull(topic);
    return new PushyNotificationTargetSelectionPlan(TargetMode.TOPIC, topicValue, null, topicValue);
  }

  public static PushyNotificationTargetSelectionPlan planSelected(
      TargetMode targetMode, String targetValue) {
    TargetMode safeMode = targetMode != null ? targetMode : TargetMode.DEVICE_TOKEN;
    String target = trim(targetValue);
    String deviceToken = safeMode == TargetMode.DEVICE_TOKEN ? trimToNull(target) : null;
    String topic = safeMode == TargetMode.TOPIC ? trimToNull(target) : null;
    return new PushyNotificationTargetSelectionPlan(safeMode, target, deviceToken, topic);
  }

  private static String trim(String raw) {
    return Objects.toString(raw, "").trim();
  }

  private static String trimToNull(String raw) {
    String value = trim(raw);
    return value.isEmpty() ? null : value;
  }
}
