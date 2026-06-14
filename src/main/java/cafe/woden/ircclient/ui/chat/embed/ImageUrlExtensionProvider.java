package cafe.woden.ircclient.ui.chat.embed;

import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** ServiceLoader-backed provider for direct image URL extensions recognized by chat embeds. */
@InterfaceLayer
public interface ImageUrlExtensionProvider {

  /**
   * Returns additional file extensions that should be treated as direct image URLs.
   *
   * <p>Values may be supplied with or without the leading dot and are matched case-insensitively.
   */
  List<String> imageFileExtensions();
}
