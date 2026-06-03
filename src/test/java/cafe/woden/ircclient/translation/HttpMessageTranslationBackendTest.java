package cafe.woden.ircclient.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.translation.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.MessageTranslationSettingsBus;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.model.TargetRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    MessageTranslationSettingsBus settingsBus =
        new MessageTranslationSettingsBus(
            props(true, "deepl", endpoint, "secret-key", "auto", "es", 10_000));
    DeepLMessageTranslationBackend backend = new DeepLMessageTranslationBackend(settingsBus);

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
    MessageTranslationSettingsBus settingsBus =
        new MessageTranslationSettingsBus(
            props(true, "libretranslate", endpoint, "optional-key", "auto", "es", 10_000));
    LibreTranslateMessageTranslationBackend backend =
        new LibreTranslateMessageTranslationBackend(settingsBus);

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
  void googleWebSendsExpectedQueryAndParsesTranslation() throws Exception {
    AtomicReference<CapturedRequest> captured = new AtomicReference<>();
    String endpoint =
        startServer(
            "/translate_a/single",
            captured,
            """
            [[["Hola ","Hello ",null,null,10],["a todos","everyone",null,null,10]],null,"en"]
            """);
    MessageTranslationSettingsBus settingsBus =
        new MessageTranslationSettingsBus(
            props(true, "google-web", endpoint, "", "auto", "es", 10_000));
    GoogleWebMessageTranslationBackend backend =
        new GoogleWebMessageTranslationBackend(settingsBus);

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
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext(
        path,
        exchange -> {
          captured.set(capture(exchange));
          byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, response.length);
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
    return new MessageTranslationRequest(
        new TargetRef("libera", "#ircafe"),
        Instant.parse("2026-06-01T12:00:00Z"),
        "alice",
        "m1",
        text,
        "auto",
        "es");
  }

  private static IrcProperties props(
      boolean enabled,
      String backend,
      String endpoint,
      String apiKey,
      String sourceLanguage,
      String targetLanguage,
      long timeoutMs) {
    return new IrcProperties(
        new IrcProperties.Client(
            "IRCafe",
            null,
            null,
            null,
            null,
            new IrcProperties.Client.Translation(
                enabled,
                IrcProperties.Client.Translation.Mode.AUTO,
                backend,
                endpoint,
                apiKey,
                sourceLanguage,
                targetLanguage,
                null,
                timeoutMs,
                4_000,
                2)),
        List.of());
  }

  private record CapturedRequest(
      String method, String query, Map<String, String> headers, String body) {}
}
