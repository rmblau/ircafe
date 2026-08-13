package cafe.woden.ircclient.notify.builtins;

import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import com.google.auto.service.AutoService;
import java.util.List;

/**
 * Built-in custom sound file extensions with decoder support in the root application.
 *
 * <p>Additional formats should contribute both a file-extension provider and, when Java Sound
 * cannot decode the format, a playback provider.
 */
@AutoService(CustomSoundFileExtensionProvider.class)
public final class BuiltInCustomSoundFileExtensionProvider
    implements CustomSoundFileExtensionProvider {
  private static final List<String> EXTENSIONS = List.of("mp3", "wav");

  @Override
  public List<String> soundFileExtensions() {
    return EXTENSIONS;
  }
}
