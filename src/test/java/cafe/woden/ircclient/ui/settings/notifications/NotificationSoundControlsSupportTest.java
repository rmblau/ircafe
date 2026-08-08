package cafe.woden.ircclient.ui.settings.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.notify.api.NotificationSoundPort;
import org.junit.jupiter.api.Test;

class NotificationSoundControlsSupportTest {

  @Test
  void refreshDisablesControlsWhenParentIsUnavailable() {
    NotificationSoundControlsSupport.Controls controls =
        controls(mock(NotificationSoundPort.class), false, true, true, false);

    controls.refresh();

    assertFalse(controls.enabled().isEnabled());
    assertFalse(controls.useCustom().isEnabled());
    assertFalse(controls.customPath().isEnabled());
    assertFalse(controls.browseCustom().isEnabled());
    assertFalse(controls.clearCustom().isEnabled());
    assertFalse(controls.builtInSound().isEnabled());
    assertFalse(controls.testSound().isEnabled());
  }

  @Test
  void previewsBuiltInAndCustomSounds() {
    NotificationSoundPort soundPort = mock(NotificationSoundPort.class);
    NotificationSoundControlsSupport.Controls controls =
        controls(soundPort, true, true, false, false);

    controls.testSound().doClick();

    verify(soundPort).preview(BuiltInSound.NOTIF_1);

    controls.useCustom().setSelected(true);
    controls.customPath().setText("sounds/custom.wav");
    controls.refresh();
    controls.testSound().doClick();

    verify(soundPort).previewCustom("sounds/custom.wav");
  }

  @Test
  void skipsCustomPreviewWhenCustomPathIsBlank() {
    NotificationSoundPort soundPort = mock(NotificationSoundPort.class);
    NotificationSoundControlsSupport.Controls controls =
        controls(soundPort, true, true, true, false);

    controls.testSound().doClick();

    verifyNoInteractions(soundPort);
  }

  @Test
  void clearCustomSoundResetsSelectionState() {
    NotificationSoundControlsSupport.Controls controls =
        controls(mock(NotificationSoundPort.class), true, true, true, true);
    controls.customPath().setText("sounds/custom.wav");
    controls.refresh();

    controls.clearCustom().doClick();

    assertFalse(controls.useCustom().isSelected());
    assertEquals("", controls.customPath().getText());
    assertFalse(controls.customPath().isEnabled());
    assertFalse(controls.browseCustom().isEnabled());
  }

  @Test
  void canRequireCustomModeBeforeEnablingCustomFileControls() {
    NotificationSoundControlsSupport.Controls controls =
        controls(mock(NotificationSoundPort.class), true, true, false, true);

    assertFalse(controls.customPath().isEnabled());
    assertFalse(controls.browseCustom().isEnabled());

    controls.useCustom().setSelected(true);
    controls.refresh();

    assertTrue(controls.customPath().isEnabled());
    assertTrue(controls.browseCustom().isEnabled());
  }

  private static NotificationSoundControlsSupport.Controls controls(
      NotificationSoundPort soundPort,
      boolean parentAvailable,
      boolean enabled,
      boolean useCustom,
      boolean customControlsRequireUseCustom) {
    return NotificationSoundControlsSupport.buildControls(
        NotificationSoundControlsSupport.Request.builder()
            .enabledLabel("Play sound")
            .enabledSelected(enabled)
            .useCustomLabel("Use custom")
            .useCustomSelected(useCustom)
            .soundId(BuiltInSound.NOTIF_1.name())
            .customPath("")
            .browseButtonText("Browse")
            .clearButtonText("Clear")
            .testButtonText("Test")
            .buttonStyle(NotificationSoundControlsSupport.ButtonStyle.TEXT)
            .notificationSoundService(soundPort)
            .soundFileImporter(source -> "sounds/" + source.getName())
            .availableSupplier(() -> parentAvailable)
            .customPathEditableWhenEnabled(true)
            .customFileControlsRequireUseCustom(customControlsRequireUseCustom)
            .build());
  }
}
