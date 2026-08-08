package cafe.woden.ircclient.notify.api.sound;

import static cafe.woden.ircclient.notify.api.sound.NotificationSoundPlaybackPlan.Action.BUILT_IN_RESOURCE;
import static cafe.woden.ircclient.notify.api.sound.NotificationSoundPlaybackPlan.Action.CUSTOM_FILE;
import static cafe.woden.ircclient.notify.api.sound.NotificationSoundPlaybackPlan.Action.SKIP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationSoundPlaybackPlannerTest {

  @Test
  void skipsRegularPlaybackWhenSoundsAreDisabled() {
    NotificationSoundPlaybackPlan disabled =
        NotificationSoundPlaybackPlanner.planSelected(false, true, true, "sounds/default.mp3");

    assertEquals(SKIP, disabled.action());
    assertTrue(disabled.skipPlayback());
    assertFalse(disabled.usesCustomFile());
    assertFalse(disabled.usesBuiltInResource());
  }

  @Test
  void prefersAvailableCustomSoundForSelectedAndOverridePlayback() {
    NotificationSoundPlaybackPlan selected =
        NotificationSoundPlaybackPlanner.planSelected(true, true, true, "sounds/default.mp3");
    NotificationSoundPlaybackPlan override =
        NotificationSoundPlaybackPlanner.planOverride(true, true, true, "sounds/override.mp3");

    assertEquals(CUSTOM_FILE, selected.action());
    assertEquals(CUSTOM_FILE, override.action());
    assertNull(selected.resourcePath());
    assertNull(override.resourcePath());
    assertTrue(selected.usesCustomFile());
    assertTrue(override.usesCustomFile());
    assertFalse(selected.usesBuiltInResource());
  }

  @Test
  void fallsBackToBuiltInResourceWhenCustomSoundIsMissing() {
    NotificationSoundPlaybackPlan plan =
        NotificationSoundPlaybackPlanner.planSelected(true, true, false, " sounds/default.mp3 ");

    assertEquals(BUILT_IN_RESOURCE, plan.action());
    assertEquals("sounds/default.mp3", plan.resourcePath());
    assertFalse(plan.skipPlayback());
    assertFalse(plan.usesCustomFile());
    assertTrue(plan.usesBuiltInResource());
  }

  @Test
  void skipsWhenNoBuiltInResourceIsAvailable() {
    NotificationSoundPlaybackPlan plan =
        NotificationSoundPlaybackPlanner.planSelected(true, false, false, " ");

    assertEquals(SKIP, plan.action());
    assertNull(plan.resourcePath());
  }

  @Test
  void previewsBypassGlobalEnabledButRequireAvailableTargets() {
    NotificationSoundPlaybackPlan builtIn =
        NotificationSoundPlaybackPlanner.planBuiltInPreview("sounds/preview.mp3");
    NotificationSoundPlaybackPlan custom = NotificationSoundPlaybackPlanner.planCustomPreview(true);
    NotificationSoundPlaybackPlan missingCustom =
        NotificationSoundPlaybackPlanner.planCustomPreview(false);

    assertEquals(BUILT_IN_RESOURCE, builtIn.action());
    assertEquals("sounds/preview.mp3", builtIn.resourcePath());
    assertEquals(CUSTOM_FILE, custom.action());
    assertEquals(SKIP, missingCustom.action());
  }
}
