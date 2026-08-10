package cafe.woden.ircclient.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.translation.MessageTranslationSettingsProvider;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsSnapshot;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class HttpMessageTranslationBackendTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  private HttpServer server;

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void deepLSendsExpectedJsonAndParsesTranslation() throws Exception {
    AtomicReference<CapturedRequest> captured = new AtomicReference<>();
    String endpoint =
        startServer(
            "/v2/translate",
            captured,
            """
            {"translations":[{"detected_source_language":"EN","text":"Hola a todos"}]}
            """);
    DeepLMessageTranslationBackend backend =
        new DeepLMessageTranslationBackend(
            settings("deepl", endpoint, "secret-key"), new JdkMessageTranslationHttpClient());

    MessageTranslationResult result =
        backend.translate(request("Hello everyone")).toCompletableFuture().get();

    assertEquals("Hola a todos", result.translatedText());
    assertEquals("en", result.sourceLanguage());
    assertEquals("es", result.targetLanguage());
    assertEquals("DeepL", result.provider());

    CapturedRequest request = captured.get();
    assertEquals("POST", request.method());
    assertEquals("DeepL-Auth-Key secret-key", request.headers().get("Authorization"));
    JsonNode payload = JSON.readTree(request.body());
    assertEquals("Hello everyone", payload.path("text").path(0).asText());
    assertEquals("ES", payload.path("target_lang").asText());
    assertTrue(payload.path("source_lang").isMissingNode());
  }

  @Test
  void libreTranslateSendsExpectedJsonAndParsesTranslation() throws Exception {
    AtomicReference<CapturedRequest> captured = new AtomicReference<>();
    String endpoint =
        startServer(
            "/translate",
            captured,
            """
            {"detectedLanguage":{"language":"en","confidence":90.0},"translatedText":"Hola"}
            """);
    LibreTranslateMessageTranslationBackend backend =
        new LibreTranslateMessageTranslationBackend(
            settings("libretranslate", endpoint, "optional-key"),
            new JdkMessageTranslationHttpClient());

    MessageTranslationResult result =
        backend.translate(request("Hello")).toCompletableFuture().get();

    assertEquals("Hola", result.translatedText());
    assertEquals("en", result.sourceLanguage());
    assertEquals("es", result.targetLanguage());
    assertEquals("LibreTranslate", result.provider());

    CapturedRequest request = captured.get();
    assertEquals("POST", request.method());
    JsonNode payload = JSON.readTree(request.body());
    assertEquals("Hello", payload.path("q").asText());
    assertEquals("auto", payload.path("source").asText());
    assertEquals("es", payload.path("target").asText());
    assertEquals("text", payload.path("format").asText());
    assertEquals("optional-key", payload.path("api_key").asText());
  }

  @Test
  void libreTranslateOmitsBlankApiKeyAndFallsBackToRequestSourceLanguage() throws Exception {
    AtomicReference<CapturedRequest> captured = new AtomicReference<>();
    String endpoint =
        startServer(
            "/translate",
            captured,
            """
            {"translatedText":"Hola"}
            """);
    LibreTranslateMessageTranslationBackend backend =
        new LibreTranslateMessageTranslationBackend(
            settings("libretranslate", endpoint, ""), new JdkMessageTranslationHttpClient());

    MessageTranslationResult result =
        backend.translate(request("Bonjour", "fr", "es")).toCompletableFuture().get();

    assertEquals("Hola", result.translatedText());
    assertEquals("fr", result.sourceLanguage());
    assertEquals("es", result.targetLanguage());
    assertEquals("LibreTranslate", result.provider());

    JsonNode payload = JSON.readTree(captured.get().body());
    assertEquals("Bonjour", payload.path("q").asText());
    assertEquals("fr", payload.path("source").asText());
    assertEquals("es", payload.path("target").asText());
    assertFalse(payload.has("api_key"));
  }

  @Test
  void libreTranslateIncludesProviderAndResponseBodyWhenRequestFails() throws Exception {
    AtomicReference<CapturedRequest> captured = new AtomicReference<>();
    String endpoint =
        startServer(
            "/translate",
            captured,
            """
            {"error":"rate limited"}
            """,
            429);
    LibreTranslateMessageTranslationBackend backend =
        new LibreTranslateMessageTranslationBackend(
            settings("libretranslate", endpoint, ""), new JdkMessageTranslationHttpClient());

    IllegalStateException failure =
        assertThrows(IllegalStateException.class, () -> backend.translate(request("Hello")));

    assertEquals("LibreTranslate translation failed", failure.getMessage());
    assertTrue(failure.getCause().getMessage().contains("LibreTranslate"));
    assertTrue(failure.getCause().getMessage().contains("429"));
    assertTrue(failure.getCause().getMessage().contains("rate limited"));
    assertEquals("POST", captured.get().method());
  }

  @Test
  void googleWebSendsExpectedQueryAndParsesTranslation() throws Exception {
    AtomicReference<CapturedRequest> captured = new AtomicReference<>();
    String endpoint =
        startServer(
            "/translate_a/single",
            captured,
            """
            [[["Hola ","Hello ",null,null,10],["a todos","everyone",null,null,10]],null,"en"]
            """);
    GoogleWebMessageTranslationBackend backend =
        new GoogleWebMessageTranslationBackend(
            settings("google-web", endpoint, ""), new JdkMessageTranslationHttpClient());

    MessageTranslationResult result =
        backend.translate(request("Hello everyone")).toCompletableFuture().get();

    assertEquals("Hola a todos", result.translatedText());
    assertEquals("en", result.sourceLanguage());
    assertEquals("es", result.targetLanguage());
    assertEquals("Google Web", result.provider());

    CapturedRequest request = captured.get();
    assertEquals("GET", request.method());
    Map<String, String> query = decodedQuery(request.query());
    assertEquals("gtx", query.get("client"));
    assertEquals("auto", query.get("sl"));
    assertEquals("es", query.get("tl"));
    assertEquals("t", query.get("dt"));
    assertEquals("UTF-8", query.get("ie"));
    assertEquals("UTF-8", query.get("oe"));
    assertEquals("Hello everyone", query.get("q"));
  }

  private String startServer(
      String path, AtomicReference<CapturedRequest> captured, String responseBody)
      throws IOException {
    return startServer(path, captured, responseBody, 200);
  }

  private String startServer(
      String path, AtomicReference<CapturedRequest> captured, String responseBody, int statusCode)
      throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext(
        path,
        exchange -> {
          captured.set(capture(exchange));
          byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(statusCode, response.length);
          exchange.getResponseBody().write(response);
          exchange.close();
        });
    server.start();
    return "http://"
        + server.getAddress().getHostString()
        + ":"
        + server.getAddress().getPort()
        + path;
  }

  private static CapturedRequest capture(HttpExchange exchange) throws IOException {
    Map<String, String> headers =
        exchange.getRequestHeaders().entrySet().stream()
            .filter(entry -> !entry.getValue().isEmpty())
            .collect(
                java.util.stream.Collectors.toMap(
                    Map.Entry::getKey, entry -> entry.getValue().getFirst(), (a, b) -> a));
    return new CapturedRequest(
        exchange.getRequestMethod(),
        exchange.getRequestURI().getRawQuery(),
        headers,
        new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
  }

  private static Map<String, String> decodedQuery(String rawQuery) {
    Map<String, String> values = new LinkedHashMap<>();
    for (String pair : rawQuery.split("&")) {
      String[] parts = pair.split("=", 2);
      String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
      String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
      values.put(key, value);
    }
    return values;
  }

  private static MessageTranslationRequest request(String text) {
    return request(text, "auto", "es");
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

  private static MessageTranslationSettingsProvider settings(
      String backend, String endpoint, String apiKey) {
    MessageTranslationSettingsSnapshot snapshot =
        new MessageTranslationSettingsSnapshot(
            true,
            MessageTranslationSettingsSnapshot.Mode.AUTO,
            backend,
            endpoint,
            apiKey,
            "auto",
            "es",
            true,
            true,
            List.of(),
            10_000,
            4_000,
            2);
    return () -> snapshot;
  }

  private record CapturedRequest(
      String method, String query, Map<String, String> headers, String body) {}

  private static final class JdkMessageTranslationHttpClient
      implements MessageTranslationHttpClient {
    @Override
    public MessageTranslationHttpResponse getString(
        URI endpoint, Map<String, String> headers, long timeoutMs) throws IOException {
      HttpURLConnection connection = open(endpoint, headers, timeoutMs);
      connection.setRequestMethod("GET");
      return read(connection);
    }

    @Override
    public MessageTranslationHttpResponse postString(
        URI endpoint, Map<String, String> headers, String body, long timeoutMs) throws IOException {
      HttpURLConnection connection = open(endpoint, headers, timeoutMs);
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
      return read(connection);
    }

    private static HttpURLConnection open(URI endpoint, Map<String, String> headers, long timeoutMs)
        throws IOException {
      HttpURLConnection connection = (HttpURLConnection) endpoint.toURL().openConnection();
      int timeout = Math.toIntExact(Math.max(1L, Math.min(timeoutMs, 120_000L)));
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      headers.forEach(connection::setRequestProperty);
      return connection;
    }

    private static MessageTranslationHttpResponse read(HttpURLConnection connection)
        throws IOException {
      int statusCode = connection.getResponseCode();
      InputStream stream =
          statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
      byte[] body = stream == null ? new byte[0] : stream.readAllBytes();
      return new MessageTranslationHttpResponse(
          statusCode, new String(body, StandardCharsets.UTF_8));
    }
  }
}
