package cafe.woden.ircclient.ui.settings.notifications;

import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** ServiceLoader-backed contribution point for notification sound file extensions. */
@InterfaceLayer
public interface NotificationSoundFileExtensionProvider {

  /**
   * Returns additional file extensions, without a leading dot, accepted by the notification sound
   * importer.
   */
  List<String> soundFileExtensions();
}
