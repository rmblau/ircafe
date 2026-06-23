package cafe.woden.ircclient.notify.builtins;

import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import com.google.auto.service.AutoService;
import java.util.List;

/** Built-in custom sound file extensions accepted by IRCafe importers. */
@AutoService(CustomSoundFileExtensionProvider.class)
public final class BuiltInCustomSoundFileExtensionProvider
    implements CustomSoundFileExtensionProvider {
  private static final List<String> EXTENSIONS = List.of("mp3", "wav");

  @Override
  public List<String> soundFileExtensions() {
    return EXTENSIONS;
  }
}
