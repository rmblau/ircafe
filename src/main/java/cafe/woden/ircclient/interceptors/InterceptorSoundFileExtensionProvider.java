package cafe.woden.ircclient.interceptors;

import java.util.List;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** ServiceLoader-backed contribution point for interceptor custom sound file extensions. */
@SecondaryPort
@ApplicationLayer
public interface InterceptorSoundFileExtensionProvider {

  /**
   * Returns additional file extensions, without a leading dot, accepted by the interceptor
   * importer.
   */
  List<String> soundFileExtensions();
}
