package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import java.util.List;
import org.junit.jupiter.api.Test;

class CustomSoundFileImportPlannerTest {

  @Test
  void rejectsBlankAndUnsupportedNames() {
    CustomSoundFileExtensionProvider builtIn = () -> List.of("mp3", "wav");

    CustomSoundFileImportPlan invalid =
        CustomSoundFileImportPlanner.plan(" ", "notification", List.of(builtIn), List.of());
    CustomSoundFileImportPlan unsupported =
        CustomSoundFileImportPlanner.plan("notice.ogg", "notification", List.of(builtIn), List.of());

    assertFalse(invalid.validFileName());
    assertFalse(invalid.importable());
    assertTrue(unsupported.validFileName());
    assertFalse(unsupported.supportedType());
    assertFalse(unsupported.importable());
  }

  @Test
  void sanitizesBaseNameAndPreservesNormalizedExtension() {
    CustomSoundFileExtensionProvider builtIn = () -> List.of("mp3", "wav");

    CustomSoundFileImportPlan plan =
        CustomSoundFileImportPlanner.plan(
            " My Sound!.MP3 ", "notification", List.of(builtIn), List.of());

    assertTrue(plan.importable());
    assertEquals("My_Sound_", plan.baseName());
    assertEquals("mp3", plan.extension());
    assertEquals("My_Sound_.mp3", plan.fileName(1));
    assertEquals("My_Sound_-2.mp3", plan.fileName(2));
  }

  @Test
  void acceptsPluginProvidedExtensions() {
    CustomSoundFileExtensionProvider builtIn = () -> List.of("mp3", "wav");
    CustomSoundFileExtensionProvider plugin = () -> List.of(".opus");

    CustomSoundFileImportPlan plan =
        CustomSoundFileImportPlanner.plan(
            " Alert.OPUS ", "notification", List.of(builtIn), List.of(plugin));

    assertTrue(plan.importable());
    assertEquals("Alert", plan.baseName());
    assertEquals("opus", plan.extension());
    assertEquals("Alert.opus", plan.fileName(1));
  }

  @Test
  void treatsNonPositiveCollisionSequenceAsFirstFileName() {
    CustomSoundFileExtensionProvider builtIn = () -> List.of("mp3");

    CustomSoundFileImportPlan plan =
        CustomSoundFileImportPlanner.plan("alert.mp3", "notification", List.of(builtIn), List.of());

    assertTrue(plan.importable());
    assertEquals("alert.mp3", plan.fileName(0));
  }
}
