package cafe.woden.ircclient.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LibreTranslateMessageTranslationMapperTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Test
  void mapsPayloadWithApiKey() {
    MessageTranslationRequest request = request("Bonjour", "FR", "ES");
    MessageTranslationBackendContext context =
        new MessageTranslationBackendContext("https://example.invalid/translate", "secret", 4_000);

    Map<String, Object> payload =
        LibreTranslateMessageTranslationMapper.requestPayload(request, context);

    assertEquals("Bonjour", payload.get("q"));
    assertEquals("fr", payload.get("source"));
    assertEquals("es", payload.get("target"));
    assertEquals("text", payload.get("format"));
    assertEquals("secret", payload.get("api_key"));
  }

  @Test
  void mapsPayloadWithoutBlankApiKey() {
    MessageTranslationRequest request = request("Bonjour", "fr", "es");
    MessageTranslationBackendContext context =
        new MessageTranslationBackendContext("https://example.invalid/translate", " ", 4_000);

    Map<String, Object> payload =
        LibreTranslateMessageTranslationMapper.requestPayload(request, context);

    assertEquals("Bonjour", payload.get("q"));
    assertEquals("fr", payload.get("source"));
    assertEquals("es", payload.get("target"));
    assertFalse(payload.containsKey("api_key"));
  }

  @Test
  void mapsBlankSourceToAutoAndAllowsNullContext() {
    MessageTranslationRequest request = request("Bonjour", " ", "es");

    Map<String, Object> payload =
        LibreTranslateMessageTranslationMapper.requestPayload(request, null);

    assertEquals("Bonjour", payload.get("q"));
    assertEquals("auto", payload.get("source"));
    assertEquals("es", payload.get("target"));
    assertFalse(payload.containsKey("api_key"));
  }

  @Test
  void mapsObjectDetectedLanguageResponse() throws Exception {
    MessageTranslationResult result =
        LibreTranslateMessageTranslationMapper.resultFrom(
            JSON.readTree(
                """
                {"detectedLanguage":{"language":"FR"},"translatedText":"Hola"}
                """),
            request("Bonjour", "auto", "es"));

    assertEquals("Hola", result.translatedText());
    assertEquals("fr", result.sourceLanguage());
    assertEquals("es", result.targetLanguage());
    assertEquals("LibreTranslate", result.provider());
  }

  @Test
  void mapsTextualDetectedLanguageResponse() throws Exception {
    MessageTranslationResult result =
        LibreTranslateMessageTranslationMapper.resultFrom(
            JSON.readTree(
                """
                {"detectedLanguage":"PT","translatedText":"Hola"}
                """),
            request("Ola", "auto", "es"));

    assertEquals("Hola", result.translatedText());
    assertEquals("pt", result.sourceLanguage());
    assertEquals("es", result.targetLanguage());
    assertEquals("LibreTranslate", result.provider());
  }

  @Test
  void fallsBackToRequestSourceLanguageWhenDetectedLanguageIsAbsent() throws Exception {
    MessageTranslationResult result =
        LibreTranslateMessageTranslationMapper.resultFrom(
            JSON.readTree(
                """
                {"translatedText":"Hola"}
                """),
            request("Bonjour", "fr", "es"));

    assertEquals("Hola", result.translatedText());
    assertEquals("fr", result.sourceLanguage());
    assertEquals("es", result.targetLanguage());
    assertEquals("LibreTranslate", result.provider());
  }

  private static MessageTranslationRequest request(
      String text, String sourceLanguage, String targetLanguage) {
    return new MessageTranslationRequest(
        new MessageTranslationTargetView("libera", "#ircafe"),
        Instant.parse("2026-06-01T12:00:00Z"),
        "alice",
        "m1",
        text,
        sourceLanguage,
        targetLanguage);
  }
}
