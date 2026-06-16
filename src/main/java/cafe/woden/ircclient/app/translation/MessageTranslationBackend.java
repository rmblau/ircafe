package cafe.woden.ircclient.app.translation;

import java.util.concurrent.CompletionStage;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/**
 * ServiceLoader-backed secondary port implemented by translation service backends.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.translation.MessageTranslationBackend}.
 */
@SecondaryPort
@ApplicationLayer
public interface MessageTranslationBackend {

  /**
   * Stable configuration identifier, for example {@code deepl}, {@code libretranslate}, or {@code
   * google-web}.
   */
  String backendId();

  CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request);
}
