package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationSoundSettingsPolicyTest {

  @Test
  void normalizesBlankSoundIdToDefault() {
    NotificationSoundSettingsValues values =
        NotificationSoundSettingsPolicy.normalize(true, "  ", false, null, "DEFAULT_SOUND");

    assertEquals("DEFAULT_SOUND", values.soundId());
  }

  @Test
  void fallsBackWhenDefaultSoundIdIsBlank() {
    NotificationSoundSettingsValues values =
        NotificationSoundSettingsPolicy.normalize(true, null, false, null, " ");

    assertEquals("NOTIF_1", values.soundId());
  }

  @Test
  void trimsCustomPathAndPreservesCustomModeWhenPresent() {
    NotificationSoundSettingsValues values =
        NotificationSoundSettingsPolicy.normalize(true, "SOUND", true, " sounds/custom.wav ", "");

    assertTrue(values.useCustom());
    assertEquals("sounds/custom.wav", values.customPath());
  }

  @Test
  void disablesCustomModeWhenPathIsBlank() {
    NotificationSoundSettingsValues values =
        NotificationSoundSettingsPolicy.normalize(true, "SOUND", true, " ", "");

    assertFalse(values.useCustom());
    assertNull(values.customPath());
  }

  @Test
  void seedsMissingEnabledAsEnabledAndBlankSoundIdAsDefault() {
    NotificationSoundSettingsValues values =
        NotificationSoundSettingsPolicy.seed(null, " ", null, null, "DEFAULT_SOUND");

    assertTrue(values.enabled());
    assertEquals("DEFAULT_SOUND", values.soundId());
    assertFalse(values.useCustom());
  }

  @Test
  void seedsExplicitDisabledAndCustomPath() {
    NotificationSoundSettingsValues values =
        NotificationSoundSettingsPolicy.seed(
            Boolean.FALSE, " SOUND ", Boolean.TRUE, " sounds/custom.wav ", "DEFAULT_SOUND");

    assertFalse(values.enabled());
    assertEquals("SOUND", values.soundId());
    assertTrue(values.useCustom());
    assertEquals("sounds/custom.wav", values.customPath());
  }
}
