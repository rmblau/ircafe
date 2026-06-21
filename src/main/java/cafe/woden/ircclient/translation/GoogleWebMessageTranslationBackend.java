package cafe.woden.ircclient.translation;

import cafe.woden.ircclient.app.translation.MessageTranslationSettingsBus;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.net.HttpLite;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** Unofficial Google Translate web endpoint backend. */
@Component
@SecondaryAdapter
@InfrastructureLayer
public final class GoogleWebMessageTranslationBackend extends AbstractHttpMessageTranslationBackend
    implements MessageTranslationBackendProvider {

  public static final String BACKEND_ID = "google-web";

  private final MessageTranslationSettingsBus settingsBus;

  public GoogleWebMessageTranslationBackend(MessageTranslationSettingsBus settingsBus) {
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
      URI endpoint = googleEndpointUri(settings, request);

      HttpLite.Response<String> response =
          getString(endpoint, jsonHeaders(), settings.requestTimeoutMs());
      require2xx(response, "Google Web");

      JsonNode root = json(response.body());
      String translatedText = translatedText(root);
      String detectedSource = root.path(2).asText("");
      return new MessageTranslationResult(
          translatedText,
          lower(detectedSource.isBlank() ? request.sourceLanguage() : detectedSource),
          lower(request.targetLanguage()),
          "Google Web");
    } catch (Exception ex) {
      throw new IllegalStateException("Google Web translation failed", ex);
    }
  }

  private static URI googleEndpointUri(
      IrcProperties.Client.Translation settings, MessageTranslationRequest request) {
    String base = endpointUri(settings).toString();
    String separator = base.contains("?") ? "&" : "?";
    String source =
        lower(request.sourceLanguage()).isBlank() ? "auto" : lower(request.sourceLanguage());
    String query =
        "client=gtx"
            + "&sl="
            + encode(source)
            + "&tl="
            + encode(lower(request.targetLanguage()))
            + "&dt=t"
            + "&ie=UTF-8"
            + "&oe=UTF-8"
            + "&q="
            + encode(Objects.toString(request.text(), ""));
    return URI.create(base + separator + query);
  }

  private static String translatedText(JsonNode root) {
    StringBuilder out = new StringBuilder();
    for (JsonNode segment : root.path(0)) {
      String text = segment.path(0).asText("");
      if (!text.isEmpty()) {
        out.append(text);
      }
    }
    return out.toString();
  }

  private static String encode(String value) {
    return URLEncoder.encode(Objects.toString(value, ""), StandardCharsets.UTF_8);
  }
}
