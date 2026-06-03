package cafe.woden.ircclient.app.translation;

import java.util.concurrent.CompletionStage;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Secondary port implemented by translation service backends. */
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
