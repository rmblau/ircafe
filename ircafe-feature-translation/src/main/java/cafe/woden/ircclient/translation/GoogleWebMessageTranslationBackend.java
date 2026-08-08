package cafe.woden.ircclient.translation;

import cafe.woden.ircclient.app.translation.MessageTranslationSettingsProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** Unofficial Google Translate web endpoint backend. */
@Component
@SecondaryAdapter
@InfrastructureLayer
public final class GoogleWebMessageTranslationBackend extends AbstractHttpMessageTranslationBackend {

  public static final String BACKEND_ID = "google-web";

  public GoogleWebMessageTranslationBackend(
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
      URI endpoint = googleEndpointUri(context, request);

      MessageTranslationHttpResponse response =
          getString(endpoint, jsonHeaders(), requestTimeoutMs(context));
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
      MessageTranslationBackendContext context, MessageTranslationRequest request) {
    String base = endpointUri(context).toString();
    String separator = base.contains("?") ? "&" : "?";
    String query =
        "client=gtx"
            + "&sl="
            + encode(sourceLanguageOrAuto(request))
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
