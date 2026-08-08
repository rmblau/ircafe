package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class NotificationSoundPathResolverTest {

  @Test
  void resolvesRelativeSoundPathInsideRuntimeConfigDirectory() {
    Path runtimeConfig = Path.of("config-home", "ircafe", "ircafe.yml");

    assertEquals(
        Path.of("config-home", "ircafe", "sounds", "custom.wav"),
        NotificationSoundPathResolver.resolveCustomSoundPath(
            runtimeConfig, " sounds/custom.wav "));
  }

  @Test
  void rejectsBlankPathsAndMissingRuntimeConfigParents() {
    assertNull(
        NotificationSoundPathResolver.resolveCustomSoundPath(
            Path.of("config-home", "ircafe", "ircafe.yml"), " "));
    assertNull(NotificationSoundPathResolver.resolveCustomSoundPath(Path.of("ircafe.yml"), "x.wav"));
  }

  @Test
  void rejectsTraversalOutsideRuntimeConfigDirectory() {
    Path runtimeConfig = Path.of("config-home", "ircafe", "ircafe.yml");

    assertNull(
        NotificationSoundPathResolver.resolveCustomSoundPath(
            runtimeConfig, "../outside/custom.wav"));
  }

  @Test
  void rejectsAbsolutePathOutsideRuntimeConfigDirectory() {
    Path runtimeConfig = Path.of("/tmp/ircafe/config/ircafe.yml");

    assertNull(
        NotificationSoundPathResolver.resolveCustomSoundPath(runtimeConfig, "/tmp/outside.wav"));
  }
}
