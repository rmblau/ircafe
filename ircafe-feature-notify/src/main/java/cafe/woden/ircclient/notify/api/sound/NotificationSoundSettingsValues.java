package cafe.woden.ircclient.notify.api.sound;

/** Feature-safe normalized notification sound settings. */
public record NotificationSoundSettingsValues(
    boolean enabled, String soundId, boolean useCustom, String customPath) {}
