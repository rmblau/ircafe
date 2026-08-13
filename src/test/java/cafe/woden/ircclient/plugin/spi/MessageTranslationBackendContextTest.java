package cafe.woden.ircclient.plugin.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;

class MessageTranslationBackendContextTest {

  @Test
  void normalizesRuntimeContextValues() {
    MessageTranslationBackendContext context =
        new MessageTranslationBackendContext(
            " https://translation.example/api ", " secret-token ", 2_500);

    assertEquals("https://translation.example/api", context.endpoint());
    assertEquals("secret-token", context.apiKey());
    assertEquals(2_500, context.requestTimeoutMs());
  }

  @Test
  void appliesTimeoutDefaultsAndUpperBound() {
    assertEquals(10_000, MessageTranslationBackendContext.empty().requestTimeoutMs());
    assertEquals(10_000, new MessageTranslationBackendContext("", "", -1).requestTimeoutMs());
    assertEquals(120_000, new MessageTranslationBackendContext("", "", 180_000).requestTimeoutMs());
  }

  @Test
  void supportsContextOnlyProviders() throws Exception {
    MessageTranslationBackendProvider provider =
        new MessageTranslationBackendProvider() {
          @Override
          public String backendId() {
            return "context-only";
          }

          @Override
          public CompletionStage<MessageTranslationResult> translate(
              MessageTranslationRequest request, MessageTranslationBackendContext context) {
            return CompletableFuture.completedFuture(
                new MessageTranslationResult(
                    "timeout=" + context.requestTimeoutMs(), "en", "es", backendId()));
          }
        };

    MessageTranslationResult result =
        provider
            .translate(null, new MessageTranslationBackendContext("", "", 2_500))
            .toCompletableFuture()
            .get();

    assertEquals("timeout=2500", result.translatedText());
  }

  @Test
  void adaptsLegacyRequestOnlyProvidersToContextCalls() throws Exception {
    MessageTranslationBackendProvider provider =
        new MessageTranslationBackendProvider() {
          @Override
          public String backendId() {
            return "legacy";
          }

          @Override
          public CompletionStage<MessageTranslationResult> translate(
              MessageTranslationRequest request) {
            return CompletableFuture.completedFuture(
                new MessageTranslationResult("legacy", "en", "es", backendId()));
          }
        };

    MessageTranslationResult result =
        provider
            .translate(null, new MessageTranslationBackendContext("", "", 2_500))
            .toCompletableFuture()
            .get();

    assertEquals("legacy", result.translatedText());
  }
}
