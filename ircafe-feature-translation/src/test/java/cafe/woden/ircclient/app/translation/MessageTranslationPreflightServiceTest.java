package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MessageTranslationPreflightServiceTest {

  private static final MessageTranslationTargetView TARGET =
      new MessageTranslationTargetView("libera", "#ircafe");
  private static final Instant AT = Instant.parse("2026-06-01T12:00:00Z");

  @Test
  void buildRequestUsesTargetOverrideBeforeDefaultTargetLanguage() {
    MessageTranslationPreflightService service = service(Optional.empty());

    MessageTranslationPreflightService.PreflightResult result =
        service.buildRequest(
            new MessageTranslationPreflightService.TranslationRequestInput(
                TARGET, AT, "alice", " msg-1 ", "hello", "auto", "fr", "es", 4_000));

    assertTrue(result.accepted());
    assertEquals("msg-1", result.request().messageId());
    assertEquals("fr", result.request().targetLanguage());
    assertEquals("hello", result.request().text());
  }

  @Test
  void buildRequestSkipsSameLanguageBasePair() {
    MessageTranslationPreflightService service = service(Optional.empty());

    MessageTranslationPreflightService.PreflightResult result =
        service.buildRequest(
            new MessageTranslationPreflightService.TranslationRequestInput(
                TARGET, AT, "alice", "msg-1", "hello", "en-us", "", "en", 4_000));

    assertFalse(result.accepted());
    assertEquals(
        "source and target languages do not require translation (source={}, target={})",
        result.skipReason());
  }

  @Test
  void prepareBackendRequestUsesDetectedAutomaticSource() {
    MessageTranslationPreflightService service = service(Optional.of("en"));
    MessageTranslationRequest request =
        new MessageTranslationRequest(TARGET, AT, "alice", "msg-1", "hello", "auto", "es");

    MessageTranslationPreflightService.PreflightResult result =
        service.prepareBackendRequest(
            new MessageTranslationPreflightService.AutomaticPreflightInput(
                request, true, false, List.of("en", "es")));

    assertTrue(result.accepted());
    assertEquals("en", result.request().sourceLanguage());
    assertEquals("es", result.request().targetLanguage());
  }

  @Test
  void prepareBackendRequestSkipsWhenDetectedSourceMatchesTarget() {
    MessageTranslationPreflightService service = service(Optional.of("es"));
    MessageTranslationRequest request =
        new MessageTranslationRequest(TARGET, AT, "alice", "msg-1", "hola", "auto", "es");

    MessageTranslationPreflightService.PreflightResult result =
        service.prepareBackendRequest(
            new MessageTranslationPreflightService.AutomaticPreflightInput(
                request, true, false, List.of("en", "es")));

    assertFalse(result.accepted());
    assertEquals("detected source language matches target language", result.skipReason());
  }

  @Test
  void suppressesSameLanguageAutomaticBackendResults() {
    MessageTranslationPreflightService service = service(Optional.empty());
    MessageTranslationRequest request =
        new MessageTranslationRequest(TARGET, AT, "alice", "msg-1", "hola", "auto", "es");
    MessageTranslationResult result = new MessageTranslationResult("hola", "es", "es", "test");

    assertTrue(service.shouldSuppressTranslationResult(request, result, true));
    assertFalse(service.shouldSuppressTranslationResult(request, result, false));
  }

  private static MessageTranslationPreflightService service(Optional<String> detectedLanguage) {
    return new MessageTranslationPreflightService(new FixedDetector(detectedLanguage));
  }

  private record FixedDetector(Optional<String> detectedLanguage)
      implements MessageLanguageDetector {

    @Override
    public Optional<String> detectLanguageCode(String text) {
      return detectedLanguage;
    }

    @Override
    public Optional<String> detectLanguageCode(String text, Collection<String> languageCodes) {
      return detectedLanguage;
    }
  }
}
