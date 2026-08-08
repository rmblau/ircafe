package cafe.woden.ircclient.ui.settings.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NotificationSoundFileImportSupportTest {

  @Test
  void importsSupportedSoundFileUnderRuntimeSoundsDirectory(@TempDir Path tempDir)
      throws Exception {
    Path runtimeConfig = tempDir.resolve("ircafe.yml");
    Path source = tempDir.resolve("My Sound!.mp3");
    Files.writeString(source, "audio");

    String relative =
        NotificationSoundFileImportSupport.importToRuntimeDir(runtimeConfig, source.toFile());

    assertEquals("sounds/My_Sound_.mp3", relative);
    assertEquals("audio", Files.readString(tempDir.resolve(relative)));
  }

  @Test
  void avoidsOverwritingExistingImportedSound(@TempDir Path tempDir) throws Exception {
    Path runtimeConfig = tempDir.resolve("ircafe.yml");
    Path source = tempDir.resolve("notice.wav");
    Files.writeString(source, "audio");

    assertEquals(
        "sounds/notice.wav",
        NotificationSoundFileImportSupport.importToRuntimeDir(runtimeConfig, source.toFile()));
    assertEquals(
        "sounds/notice-2.wav",
        NotificationSoundFileImportSupport.importToRuntimeDir(runtimeConfig, source.toFile()));
  }

  @Test
  void rejectsUnsupportedFileTypes(@TempDir Path tempDir) throws Exception {
    Path source = tempDir.resolve("notice.ogg");
    Files.writeString(source, "audio");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                NotificationSoundFileImportSupport.importToRuntimeDir(
                    tempDir.resolve("ircafe.yml"), source.toFile()));

    assertEquals("Only .mp3 and .wav are supported", ex.getMessage());
  }

  @Test
  void unsupportedTypeMessageIncludesPluginExtensions(@TempDir Path tempDir) throws Exception {
    Path source = tempDir.resolve("notice.txt");
    Files.writeString(source, "audio");
    List<CustomSoundFileExtensionProvider> extensionProviders =
        List.of(() -> List.of("ogg", ".flac"));

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                NotificationSoundFileImportSupport.importToRuntimeDir(
                    tempDir.resolve("ircafe.yml"), source.toFile(), extensionProviders));

    assertEquals("Only .flac, .mp3, .ogg, and .wav are supported", ex.getMessage());
  }

  @Test
  void returnsNullWhenSourceIsNull(@TempDir Path tempDir) throws Exception {
    assertNull(
        NotificationSoundFileImportSupport.importToRuntimeDir(tempDir.resolve("ircafe.yml"), null));
  }
}
