package cafe.woden.ircclient.translation;

import cafe.woden.ircclient.app.translation.MessageTranslationSettingsProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import java.net.URI;
import java.util.Map;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.stereotype.Component;

/** LibreTranslate text translation backend. */
@Component
@SecondaryAdapter
@InfrastructureLayer
public final class LibreTranslateMessageTranslationBackend
    extends AbstractHttpMessageTranslationBackend {

  public static final String BACKEND_ID = "libretranslate";

  public LibreTranslateMessageTranslationBackend(
      MessageTranslationSettingsProvider settingsProvider,
      MessageTranslationHttpClient httpClient) {
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
      URI endpoint = endpointUri(context);
      Map<String, Object> payload =
          LibreTranslateMessageTranslationMapper.requestPayload(request, context);
      MessageTranslationHttpResponse response =
          postJson(endpoint, jsonHeaders(), jsonFor(payload), requestTimeoutMs(context));
      require2xx(response, LibreTranslateMessageTranslationMapper.PROVIDER_NAME);

      return LibreTranslateMessageTranslationMapper.resultFrom(json(response.body()), request);
    } catch (Exception ex) {
      throw new IllegalStateException("LibreTranslate translation failed", ex);
    }
  }
}
