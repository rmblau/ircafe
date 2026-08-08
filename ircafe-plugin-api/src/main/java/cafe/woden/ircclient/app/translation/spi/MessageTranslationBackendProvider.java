package cafe.woden.ircclient.app.translation.spi;

import java.util.concurrent.CompletionStage;

/**
 * ServiceLoader-backed contribution point for translation service backends.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider}.
 * Provider classes must be public, stateless, and expose public no-argument constructors.
 */
public interface MessageTranslationBackendProvider {

  /**
   * Stable configuration identifier, for example {@code deepl}, {@code libretranslate}, or {@code
   * google-web}.
   *
   * <p>IRCafe trims identifiers and matches them case-insensitively. The normalized identifier must
   * be non-blank and unique across resolved providers; duplicate normalized identifiers invalidate
   * the backend registry rather than replacing an earlier provider.
   */
  String backendId();

  /**
   * Translates a request without app-provided runtime context.
   *
   * <p>Older stateless providers may continue to override this method. New providers that need
   * endpoint, secret, or timeout values should override {@link #translate(MessageTranslationRequest,
   * MessageTranslationBackendContext)} instead.
   *
   * <p>Return a non-null completion stage. Complete it exceptionally when translation fails. IRCafe
   * owns request scheduling, timeout enforcement, concurrency limits, result suppression, and UI
   * rendering.
   */
  default CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
    throw new UnsupportedOperationException(
        "Translation backends must implement translate(request) or translate(request, context).");
  }

  /**
   * Translates a request with app-owned runtime context.
   *
   * <p>The default keeps existing stateless providers source-compatible. Providers that need
   * endpoint, secret, or timeout values should override this method instead of depending on app
   * config objects. The request and context are immutable request-scoped values and must not be
   * retained after the call returns.
   *
   * <p>Return a non-null completion stage. A null stage, null result, or blank translated text is
   * treated as an unusable backend result by the app.
   */
  default CompletionStage<MessageTranslationResult> translate(
      MessageTranslationRequest request, MessageTranslationBackendContext context) {
    return translate(request);
  }
}
