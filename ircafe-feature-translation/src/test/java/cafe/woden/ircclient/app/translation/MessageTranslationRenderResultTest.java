package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;

class MessageTranslationRenderResultTest {

  @Test
  void fillsDisplayFieldsFromBackendResultAndRequestFallbacks() {
    MessageTranslationRequest request =
        new MessageTranslationRequest(
            new MessageTranslationTargetView("libera", "#ircafe"),
            Instant.parse("2026-06-01T12:00:00Z"),
            "alice",
            "msg-1",
            "hello",
            "en",
            "es");

    MessageTranslationRenderResult result =
        MessageTranslationRenderResult.from(
            new Backend("deepl"), request, new MessageTranslationResult("hola", "", "", ""));

    assertEquals(request.target(), result.target());
    assertEquals(request.at(), result.at());
    assertEquals("msg-1", result.targetMessageId());
    assertEquals("hola", result.translatedText());
    assertEquals("en", result.sourceLanguage());
    assertEquals("es", result.targetLanguage());
    assertEquals("deepl", result.provider());
  }

  @Test
  void preservesBackendResultLanguagesAndProviderWhenPresent() {
    MessageTranslationRequest request =
        new MessageTranslationRequest(
            new MessageTranslationTargetView("libera", "#ircafe"),
            Instant.parse("2026-06-01T12:00:00Z"),
            "alice",
            "msg-1",
            "hello",
            "auto",
            "es");

    MessageTranslationRenderResult result =
        MessageTranslationRenderResult.from(
            new Backend("deepl"),
            request,
            new MessageTranslationResult("hola", "EN", "ES", "deepl-api"));

    assertEquals("EN", result.sourceLanguage());
    assertEquals("ES", result.targetLanguage());
    assertEquals("deepl-api", result.provider());
  }

  @Test
  void normalizesBlankFields() {
    MessageTranslationRenderResult result =
        new MessageTranslationRenderResult(
            new MessageTranslationTargetView(" libera ", " #ircafe "),
            Instant.parse("2026-06-01T12:00:00Z"),
            " msg-1 ",
            null,
            " en ",
            " es ",
            " deepl ");

    assertEquals("msg-1", result.targetMessageId());
    assertEquals("", result.translatedText());
    assertEquals("en", result.sourceLanguage());
    assertEquals("es", result.targetLanguage());
    assertEquals("deepl", result.provider());
  }

  private record Backend(String backendId) implements MessageTranslationBackendProvider {
    @Override
    public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
      return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }
  }
}
