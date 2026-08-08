package cafe.woden.ircclient.translation;

import cafe.woden.ircclient.app.translation.MessageTranslationSettingsProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** DeepL text translation backend. */
@Component
@SecondaryAdapter
@InfrastructureLayer
public final class DeepLMessageTranslationBackend extends AbstractHttpMessageTranslationBackend {

  public static final String BACKEND_ID = "deepl";

  public DeepLMessageTranslationBackend(
      MessageTranslationSettingsProvider settingsProvider, MessageTranslationHttpClient httpClient) {
    super(settingsProvider, httpClient);
  }

  @Override
  public String backendId() {
    return BACKEND_ID;
  }

  @Override
  MessageTranslationResult translateNow(
      MessageTranslationRequest request, MessageTranslationBackendContext context) {
    try {
      String apiKey = apiKey(context);
      if (apiKey.isBlank()) {
        throw new IllegalArgumentException("DeepL API key is required.");
      }

      URI endpoint = endpointUri(context);
      Map<String, String> headers = new LinkedHashMap<>(jsonHeaders());
      headers.put(HEADER_AUTHORIZATION, "DeepL-Auth-Key " + apiKey);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("text", List.of(Objects.toString(request.text(), "")));
      payload.put("target_lang", upper(request.targetLanguage()));
      String source = upper(request.sourceLanguage());
      if (!source.isBlank() && !"AUTO".equals(source)) {
        payload.put("source_lang", source);
      }

      MessageTranslationHttpResponse response =
          postJson(endpoint, headers, jsonFor(payload), requestTimeoutMs(context));
      require2xx(response, "DeepL");

      JsonNode translation = json(response.body()).path("translations").path(0);
      String translatedText = translation.path("text").asText("");
      String detectedSource = translation.path("detected_source_language").asText("");
      return new MessageTranslationResult(
          translatedText,
          lower(detectedSource.isBlank() ? request.sourceLanguage() : detectedSource),
          lower(request.targetLanguage()),
          "DeepL");
    } catch (Exception ex) {
      throw new IllegalStateException("DeepL translation failed", ex);
    }
  }
}
