package cafe.woden.ircclient.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

final class LibreTranslateMessageTranslationMapper {

  static final String PROVIDER_NAME = "LibreTranslate";

  private static final String FORMAT_TEXT = "text";
  private static final String FIELD_API_KEY = "api_key";
  private static final String FIELD_DETECTED_LANGUAGE = "detectedLanguage";
  private static final String FIELD_FORMAT = "format";
  private static final String FIELD_LANGUAGE = "language";
  private static final String FIELD_QUERY = "q";
  private static final String FIELD_SOURCE = "source";
  private static final String FIELD_TARGET = "target";
  private static final String FIELD_TRANSLATED_TEXT = "translatedText";

  private LibreTranslateMessageTranslationMapper() {}

  static Map<String, Object> requestPayload(
      MessageTranslationRequest request, MessageTranslationBackendContext context) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put(FIELD_QUERY, Objects.toString(request.text(), ""));
    payload.put(FIELD_SOURCE, sourceLanguageOrAuto(request));
    payload.put(FIELD_TARGET, lower(request.targetLanguage()));
    payload.put(FIELD_FORMAT, FORMAT_TEXT);
    String apiKey = apiKey(context);
    if (!apiKey.isBlank()) {
      payload.put(FIELD_API_KEY, apiKey);
    }
    return payload;
  }

  static MessageTranslationResult resultFrom(JsonNode root, MessageTranslationRequest request) {
    String translatedText = root.path(FIELD_TRANSLATED_TEXT).asText("");
    String detectedSource = detectedSourceLanguage(root);
    return new MessageTranslationResult(
        translatedText,
        lower(detectedSource.isBlank() ? request.sourceLanguage() : detectedSource),
        lower(request.targetLanguage()),
        PROVIDER_NAME);
  }

  private static String detectedSourceLanguage(JsonNode root) {
    JsonNode detectedLanguage = root.path(FIELD_DETECTED_LANGUAGE);
    if (detectedLanguage.isTextual()) {
      return detectedLanguage.asText("");
    }
    return detectedLanguage.path(FIELD_LANGUAGE).asText("");
  }

  private static String apiKey(MessageTranslationBackendContext context) {
    return Objects.toString(context == null ? "" : context.apiKey(), "").trim();
  }

  private static String sourceLanguageOrAuto(MessageTranslationRequest request) {
    String source = lower(request == null ? "" : request.sourceLanguage());
    return source.isBlank() ? "auto" : source;
  }

  private static String lower(String value) {
    return Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
  }
}
