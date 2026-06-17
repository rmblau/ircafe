package cafe.woden.ircclient.app.translation.spi;

import cafe.woden.ircclient.app.translation.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.MessageTranslationResult;
import java.util.concurrent.CompletionStage;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/**
 * ServiceLoader-backed contribution point for translation service backends.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider}.
 */
@SecondaryPort
@ApplicationLayer
public interface MessageTranslationBackendProvider {

  /**
   * Stable configuration identifier, for example {@code deepl}, {@code libretranslate}, or {@code
   * google-web}.
   */
  String backendId();

  CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request);
}
