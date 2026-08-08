package cafe.woden.ircclient.notify.api.pushy;

/** Plans Pushy settings control availability from feature-safe state. */
public final class PushyNotificationControlAvailabilityPlanner {
  private PushyNotificationControlAvailabilityPlanner() {}

  public static PushyNotificationControlAvailabilityPlan plan(
      boolean enabled, PushyNotificationSettingsValidator.Error validationError) {
    boolean valid =
        validationError == null || validationError == PushyNotificationSettingsValidator.Error.NONE;
    return new PushyNotificationControlAvailabilityPlan(
        enabled, enabled, enabled, enabled, enabled, enabled, enabled, enabled && valid);
  }
}
