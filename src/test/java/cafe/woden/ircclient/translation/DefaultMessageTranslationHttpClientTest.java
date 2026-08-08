package cafe.woden.ircclient.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DefaultMessageTranslationHttpClientTest {

  private HttpServer server;

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void getStringDelegatesToAppHttpPolicyAndReturnsResponse() throws Exception {
    AtomicReference<CapturedRequest> captured = new AtomicReference<>();
    URI endpoint = startServer("/translate", captured, "translated");
    DefaultMessageTranslationHttpClient client = new DefaultMessageTranslationHttpClient();

    MessageTranslationHttpResponse response =
        client.getString(endpoint, Map.of("X-Test", "true"), 10_000);

    assertEquals(200, response.statusCode());
    assertEquals("translated", response.body());
    assertEquals("GET", captured.get().method());
    assertEquals("true", header(captured.get(), "X-Test"));
  }

  @Test
  void postStringDelegatesToAppHttpPolicyAndReturnsResponse() throws Exception {
    AtomicReference<CapturedRequest> captured = new AtomicReference<>();
    URI endpoint = startServer("/translate", captured, "ok");
    DefaultMessageTranslationHttpClient client = new DefaultMessageTranslationHttpClient();

    MessageTranslationHttpResponse response =
        client.postString(
            endpoint, Map.of("Content-Type", "application/json"), "{\"q\":\"hello\"}", 10_000);

    assertEquals(200, response.statusCode());
    assertEquals("ok", response.body());
    assertEquals("POST", captured.get().method());
    assertEquals("{\"q\":\"hello\"}", captured.get().body());
  }

  private URI startServer(
      String path, AtomicReference<CapturedRequest> captured, String responseBody)
      throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext(
        path,
        exchange -> {
          captured.set(capture(exchange));
          byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, response.length);
          exchange.getResponseBody().write(response);
          exchange.close();
        });
    server.start();
    return URI.create(
        "http://"
            + server.getAddress().getHostString()
            + ":"
            + server.getAddress().getPort()
            + path);
  }

  private static String header(CapturedRequest request, String name) {
    return request.headers().entrySet().stream()
        .filter(entry -> entry.getKey().equalsIgnoreCase(name))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse("");
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
        headers,
        new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
  }

  private record CapturedRequest(String method, Map<String, String> headers, String body) {}
}
