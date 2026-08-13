package cafe.woden.ircclient.notify.api.sound;

import java.util.Objects;

/** Feature-owned normalization rules for notification sound settings. */
public final class NotificationSoundSettingsPolicy {
  private NotificationSoundSettingsPolicy() {}

  public static NotificationSoundSettingsValues seed(
      Boolean enabled,
      String soundId,
      Boolean useCustom,
      String customPath,
      String defaultSoundId) {
    return normalize(
        enabled == null || Boolean.TRUE.equals(enabled),
        soundId,
        Boolean.TRUE.equals(useCustom),
        customPath,
        defaultSoundId);
  }

  public static NotificationSoundSettingsValues normalize(
      boolean enabled,
      String soundId,
      boolean useCustom,
      String customPath,
      String defaultSoundId) {
    String normalizedSoundId = normalizeSoundId(soundId, defaultSoundId);
    String normalizedCustomPath = normalizeCustomPath(customPath);
    boolean normalizedUseCustom = useCustom && normalizedCustomPath != null;
    return new NotificationSoundSettingsValues(
        enabled, normalizedSoundId, normalizedUseCustom, normalizedCustomPath);
  }

  public static String normalizeSoundId(String soundId, String defaultSoundId) {
    String fallback = Objects.toString(defaultSoundId, "").trim();
    if (fallback.isEmpty()) {
      fallback = "NOTIF_1";
    }
    String normalized = Objects.toString(soundId, "").trim();
    return normalized.isEmpty() ? fallback : normalized;
  }

  public static String normalizeCustomPath(String customPath) {
    String normalized = Objects.toString(customPath, "").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
