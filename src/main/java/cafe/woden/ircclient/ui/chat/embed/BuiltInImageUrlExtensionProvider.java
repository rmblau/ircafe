package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
import com.google.auto.service.AutoService;
import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Built-in direct image URL extensions recognized by chat embeds. */
@InterfaceLayer
@AutoService(ImageUrlExtensionProvider.class)
public final class BuiltInImageUrlExtensionProvider implements ImageUrlExtensionProvider {
  private static final List<String> EXTENSIONS = List.of(".png", ".jpg", ".jpeg", ".gif", ".webp");

  @Override
  public List<String> imageFileExtensions() {
    return EXTENSIONS;
  }
}
