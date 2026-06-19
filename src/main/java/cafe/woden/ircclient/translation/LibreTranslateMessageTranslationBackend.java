package cafe.woden.ircclient.translation;

import cafe.woden.ircclient.app.translation.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsBus;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.net.HttpLite;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** LibreTranslate text translation backend. */
@Component
@SecondaryAdapter
@InfrastructureLayer
public final class LibreTranslateMessageTranslationBackend
    extends AbstractHttpMessageTranslationBackend implements MessageTranslationBackendProvider {

  public static final String BACKEND_ID = "libretranslate";

  private final MessageTranslationSettingsBus settingsBus;

  public LibreTranslateMessageTranslationBackend(MessageTranslationSettingsBus settingsBus) {
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
      URI endpoint = endpointUri(settings);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("q", Objects.toString(request.text(), ""));
      payload.put(
          "source",
          lower(request.sourceLanguage()).isBlank() ? "auto" : lower(request.sourceLanguage()));
      payload.put("target", lower(request.targetLanguage()));
      payload.put("format", "text");
      String apiKey = Objects.toString(settings.apiKey(), "").trim();
      if (!apiKey.isBlank()) {
        payload.put("api_key", apiKey);
      }

      HttpLite.Response<String> response =
          postJson(endpoint, jsonHeaders(), jsonFor(payload), settings.requestTimeoutMs());
      require2xx(response, "LibreTranslate");

      JsonNode root = json(response.body());
      String translatedText = root.path("translatedText").asText("");
      String detectedSource = root.path("detectedLanguage").path("language").asText("");
      return new MessageTranslationResult(
          translatedText,
          lower(detectedSource.isBlank() ? request.sourceLanguage() : detectedSource),
          lower(request.targetLanguage()),
          "LibreTranslate");
    } catch (Exception ex) {
      throw new IllegalStateException("LibreTranslate translation failed", ex);
    }
  }
}
