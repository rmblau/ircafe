package cafe.woden.ircclient.translation;

import cafe.woden.ircclient.app.translation.MessageTranslationBackendContexts;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

abstract class AbstractHttpMessageTranslationBackend implements MessageTranslationBackendProvider {

  static final String CONTENT_TYPE_JSON = "application/json";
  static final String USER_AGENT = "IRCafe";
  static final String HEADER_ACCEPT = "Accept";
  static final String HEADER_AUTHORIZATION = "Authorization";
  static final String HEADER_CONTENT_TYPE = "Content-Type";
  static final String HEADER_USER_AGENT = "User-Agent";

  private static final ObjectMapper JSON = new ObjectMapper();

  private final MessageTranslationSettingsProvider settingsProvider;
  private final MessageTranslationHttpClient httpClient;

  AbstractHttpMessageTranslationBackend(
      MessageTranslationSettingsProvider settingsProvider,
      MessageTranslationHttpClient httpClient) {
    this.settingsProvider = Objects.requireNonNull(settingsProvider, "settingsProvider");
    this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
  }

  @Override
  public final CompletionStage<MessageTranslationResult> translate(
      MessageTranslationRequest request) {
    return translate(request, MessageTranslationBackendContexts.from(settingsProvider.snapshot()));
  }

  @Override
  public final CompletionStage<MessageTranslationResult> translate(
      MessageTranslationRequest request, MessageTranslationBackendContext context) {
    return CompletableFuture.completedFuture(translateNow(request, context));
  }

  abstract MessageTranslationResult translateNow(
      MessageTranslationRequest request, MessageTranslationBackendContext context);

  MessageTranslationHttpResponse postJson(
      URI endpoint, Map<String, String> headers, String payload, long timeoutMs)
      throws IOException {
    return httpClient.postString(endpoint, headers, payload, timeoutMs);
  }

  MessageTranslationHttpResponse getString(
      URI endpoint, Map<String, String> headers, long timeoutMs) throws IOException {
    return httpClient.getString(endpoint, headers, timeoutMs);
  }

  String jsonFor(Object payload) throws IOException {
    return JSON.writeValueAsString(payload);
  }

  JsonNode json(String body) throws IOException {
    return JSON.readTree(Objects.toString(body, "{}"));
  }

  static URI endpointUri(MessageTranslationBackendContext context) {
    return endpointUri(context == null ? "" : context.endpoint());
  }

  static String apiKey(MessageTranslationBackendContext context) {
    return Objects.toString(context == null ? "" : context.apiKey(), "").trim();
  }

  static long requestTimeoutMs(MessageTranslationBackendContext context) {
    return context == null ? 0L : context.requestTimeoutMs();
  }

  static String sourceLanguageOrAuto(MessageTranslationRequest request) {
    String source = lower(request == null ? "" : request.sourceLanguage());
    return source.isBlank() ? "auto" : source;
  }

  private static URI endpointUri(String endpoint) {
    endpoint = Objects.toString(endpoint, "").trim();
    if (endpoint.isBlank()) {
      throw new IllegalArgumentException("Translation endpoint is required.");
    }
    return URI.create(endpoint);
  }

  static void require2xx(MessageTranslationHttpResponse response, String provider)
      throws IOException {
    int status = response != null ? response.statusCode() : 0;
    if (status >= 200 && status < 300) {
      return;
    }
    String body = response != null ? Objects.toString(response.body(), "").trim() : "";
    if (body.length() > 240) {
      body = body.substring(0, 240) + "...";
    }
    throw new IOException(provider + " translation request failed (" + status + "): " + body);
  }

  static Map<String, String> jsonHeaders() {
    return Map.of(
        HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON,
        HEADER_ACCEPT, CONTENT_TYPE_JSON,
        HEADER_USER_AGENT, USER_AGENT);
  }

  static String lower(String value) {
    return Objects.toString(value, "").trim().toLowerCase(java.util.Locale.ROOT);
  }

  static String upper(String value) {
    return Objects.toString(value, "").trim().toUpperCase(java.util.Locale.ROOT);
  }
}
