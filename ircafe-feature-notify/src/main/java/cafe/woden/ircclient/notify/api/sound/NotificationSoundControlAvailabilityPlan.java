package cafe.woden.ircclient.notify.api.sound;

/** Feature-owned enablement plan for notification sound controls. */
public record NotificationSoundControlAvailabilityPlan(
    boolean enabledControlEnabled,
    boolean useCustomControlEnabled,
    boolean builtInSoundControlEnabled,
    boolean customPathEnabled,
    boolean customPathEditable,
    boolean browseCustomEnabled,
    boolean clearCustomEnabled,
    boolean testSoundEnabled) {}
