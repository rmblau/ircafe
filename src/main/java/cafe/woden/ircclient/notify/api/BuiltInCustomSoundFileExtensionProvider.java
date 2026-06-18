package cafe.woden.ircclient.notify.api;

import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import com.google.auto.service.AutoService;
import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Built-in custom sound file extensions accepted by IRCafe importers. */
@SecondaryAdapter
@ApplicationLayer
@AutoService(CustomSoundFileExtensionProvider.class)
public final class BuiltInCustomSoundFileExtensionProvider
    implements CustomSoundFileExtensionProvider {
  private static final List<String> EXTENSIONS = List.of("mp3", "wav");

  @Override
  public List<String> soundFileExtensions() {
    return EXTENSIONS;
  }
}
