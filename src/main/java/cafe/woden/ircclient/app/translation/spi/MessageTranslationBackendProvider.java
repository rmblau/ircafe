package cafe.woden.ircclient.app.translation.spi;

import cafe.woden.ircclient.app.translation.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.MessageTranslationResult;
import java.util.concurrent.CompletionStage;

/**
 * ServiceLoader-backed contribution point for translation service backends.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider}.
 */
public interface MessageTranslationBackendProvider {

  /**
   * Stable configuration identifier, for example {@code deepl}, {@code libretranslate}, or {@code
   * google-web}.
   */
  String backendId();

  CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request);
}
