package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import java.time.Instant;
import java.util.Objects;

/** Feature-owned translation value ready for the root UI adapter to render. */
public record MessageTranslationRenderResult(
    MessageTranslationTargetView target,
    Instant at,
    String targetMessageId,
    String translatedText,
    String sourceLanguage,
    String targetLanguage,
    String provider) {

  public MessageTranslationRenderResult {
    Objects.requireNonNull(target, "target");
    at = at == null ? Instant.now() : at;
    targetMessageId = Objects.toString(targetMessageId, "").trim();
    translatedText = Objects.toString(translatedText, "");
    sourceLanguage = Objects.toString(sourceLanguage, "").trim();
    targetLanguage = Objects.toString(targetLanguage, "").trim();
    provider = Objects.toString(provider, "").trim();
  }

  public static MessageTranslationRenderResult from(
      MessageTranslationBackendProvider backend,
      MessageTranslationRequest request,
      MessageTranslationResult result) {
    Objects.requireNonNull(backend, "backend");
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(result, "result");
    return new MessageTranslationRenderResult(
        request.target(),
        request.at(),
        request.messageId(),
        result.translatedText(),
        firstNonBlank(result.sourceLanguage(), request.sourceLanguage()),
        firstNonBlank(result.targetLanguage(), request.targetLanguage()),
        firstNonBlank(result.provider(), backend.backendId()));
  }

  private static String firstNonBlank(String first, String fallback) {
    return first == null || first.isBlank() ? Objects.toString(fallback, "") : first;
  }
}
