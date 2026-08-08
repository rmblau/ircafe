package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CustomSoundProviderCatalogTest {

  @Test
  void normalizesExtensionProvidersByProviderClass() {
    CustomSoundFileExtensionProvider builtIn = new TestExtensionProvider(List.of("mp3", "wav"));
    CustomSoundFileExtensionProvider duplicate = new TestExtensionProvider(List.of("ogg"));
    CustomSoundFileExtensionProvider installed = new OtherExtensionProvider();

    assertEquals(
        List.of(builtIn, installed),
        CustomSoundProviderCatalog.extensionProviders(
            List.of(builtIn), Arrays.asList(duplicate, null, installed)));
  }

  @Test
  void normalizesPlaybackProvidersByProviderClass() {
    CustomSoundPlaybackProvider first = new TestPlaybackProvider();
    CustomSoundPlaybackProvider duplicate = new TestPlaybackProvider();
    CustomSoundPlaybackProvider other = new OtherPlaybackProvider();

    assertEquals(
        List.of(first, other),
        CustomSoundProviderCatalog.playbackProviders(Arrays.asList(first, duplicate, null, other)));
  }

  @Test
  void normalizesSupportedExtensions() {
    CustomSoundFileExtensionProvider builtIn = new TestExtensionProvider(List.of("mp3", ".WAV"));
    CustomSoundFileExtensionProvider installed =
        new OtherExtensionProvider(List.of(".ogg", " opus ", "bad/ext", "", "."));

    assertEquals(
        Set.of("mp3", "wav", "ogg", "opus"),
        CustomSoundProviderCatalog.supportedExtensions(List.of(builtIn), List.of(installed)));
  }

  @Test
  void formatsSupportedExtensionsForMessagesAndFileChoosers() {
    CustomSoundFileExtensionProvider builtIn = new TestExtensionProvider(List.of("mp3", "wav"));
    CustomSoundFileExtensionProvider installed = new OtherExtensionProvider(List.of("ogg", "flac"));

    assertEquals(
        ".flac, .mp3, .ogg, and .wav",
        CustomSoundProviderCatalog.supportedExtensionSentence(
            List.of(builtIn), List.of(installed)));
    assertEquals(
        "FLAC, MP3, OGG, or WAV",
        CustomSoundProviderCatalog.supportedExtensionTitleList(
            List.of(builtIn), List.of(installed)));
    assertEquals(
        "*.flac, *.mp3, *.ogg, *.wav",
        CustomSoundProviderCatalog.supportedExtensionFilterPattern(
            List.of(builtIn), List.of(installed)));
  }

  @Test
  void formatsBuiltInExtensionsWithoutOxfordComma() {
    CustomSoundFileExtensionProvider builtIn = new TestExtensionProvider(List.of("mp3", "wav"));

    assertEquals(
        ".mp3 and .wav",
        CustomSoundProviderCatalog.supportedExtensionSentence(List.of(builtIn), List.of()));
    assertEquals(
        "MP3 or WAV",
        CustomSoundProviderCatalog.supportedExtensionTitleList(List.of(builtIn), List.of()));
  }

  @Test
  void matchesFileExtensionsCaseInsensitively() {
    CustomSoundFileExtensionProvider builtIn = new TestExtensionProvider(List.of("mp3", "wav"));
    CustomSoundFileExtensionProvider installed = new OtherExtensionProvider(List.of("opus"));

    assertEquals(
        "opus",
        CustomSoundProviderCatalog.extensionFor(
            "ALERT.OPUS", List.of(builtIn), List.of(installed)));
    assertEquals(
        "mp3",
        CustomSoundProviderCatalog.extensionFor(
            "alert.MP3", List.of(builtIn), List.of(installed)));
    assertNull(
        CustomSoundProviderCatalog.extensionFor(
            "alert.txt", List.of(builtIn), List.of(installed)));
  }

  @Test
  void detectsWhetherOnlyBuiltInExtensionsAreAvailable() {
    CustomSoundFileExtensionProvider builtIn = new TestExtensionProvider(List.of("mp3", "wav"));
    CustomSoundFileExtensionProvider duplicateBuiltIn = new TestExtensionProvider(List.of("ogg"));
    CustomSoundFileExtensionProvider installed = new OtherExtensionProvider(List.of("ogg"));

    assertTrue(
        CustomSoundProviderCatalog.hasOnlyBuiltInExtensions(
            List.of(builtIn), List.of(duplicateBuiltIn)));
    assertFalse(
        CustomSoundProviderCatalog.hasOnlyBuiltInExtensions(
            List.of(builtIn), List.of(installed)));
  }

  private record TestExtensionProvider(List<String> extensions)
      implements CustomSoundFileExtensionProvider {
    @Override
    public List<String> soundFileExtensions() {
      return extensions;
    }
  }

  private static final class OtherExtensionProvider implements CustomSoundFileExtensionProvider {
    private final List<String> extensions;

    OtherExtensionProvider() {
      this(List.of("ogg"));
    }

    OtherExtensionProvider(List<String> extensions) {
      this.extensions = List.copyOf(extensions);
    }

    @Override
    public List<String> soundFileExtensions() {
      return extensions;
    }
  }

  private static final class TestPlaybackProvider implements CustomSoundPlaybackProvider {
    @Override
    public boolean playCustomSound(Path path) {
      return false;
    }
  }

  private static final class OtherPlaybackProvider implements CustomSoundPlaybackProvider {
    @Override
    public boolean playCustomSound(Path path) {
      return false;
    }
  }
}
