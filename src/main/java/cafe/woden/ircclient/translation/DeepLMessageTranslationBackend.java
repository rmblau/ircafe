package cafe.woden.ircclient.translation;

import cafe.woden.ircclient.app.translation.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsBus;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.net.HttpHeaderNames;
import cafe.woden.ircclient.net.HttpLite;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** DeepL text translation backend. */
@Component
@SecondaryAdapter
@InfrastructureLayer
public final class DeepLMessageTranslationBackend extends AbstractHttpMessageTranslationBackend
    implements MessageTranslationBackendProvider {

  public static final String BACKEND_ID = "deepl";

  private final MessageTranslationSettingsBus settingsBus;

  public DeepLMessageTranslationBackend(MessageTranslationSettingsBus settingsBus) {
    this.settingsBus = settingsBus;
  }

  @Override
  public String backendId() {
    return BACKEND_ID;
  }

  @Override
  public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
    return CompletableFuture.completedFuture(translateNow(request));
  }

  private MessageTranslationResult translateNow(MessageTranslationRequest request) {
    try {
      IrcProperties.Client.Translation settings = settingsBus.get();
      String apiKey = Objects.toString(settings.apiKey(), "").trim();
      if (apiKey.isBlank()) {
        throw new IllegalArgumentException("DeepL API key is required.");
      }

      URI endpoint = endpointUri(settings);
      Map<String, String> headers = new LinkedHashMap<>(jsonHeaders());
      headers.put(HttpHeaderNames.AUTHORIZATION, "DeepL-Auth-Key " + apiKey);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("text", List.of(Objects.toString(request.text(), "")));
      payload.put("target_lang", upper(request.targetLanguage()));
      String source = upper(request.sourceLanguage());
      if (!source.isBlank() && !"AUTO".equals(source)) {
        payload.put("source_lang", source);
      }

      HttpLite.Response<String> response =
          postJson(endpoint, headers, jsonFor(payload), settings.requestTimeoutMs());
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
