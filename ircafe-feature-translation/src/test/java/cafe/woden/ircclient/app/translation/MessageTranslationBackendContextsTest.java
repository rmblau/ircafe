package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageTranslationBackendContextsTest {

  @Test
  void createsContextFromSettingsSnapshot() {
    MessageTranslationSettingsSnapshot settings =
        new MessageTranslationSettingsSnapshot(
            true,
            MessageTranslationSettingsSnapshot.Mode.MANUAL,
            "deepl",
            "https://example.test/translate",
            "secret",
            "auto",
            "fr",
            true,
            true,
            List.of(),
            2_500,
            4_000,
            2);

    MessageTranslationBackendContext context = MessageTranslationBackendContexts.from(settings);

    assertEquals("https://example.test/translate", context.endpoint());
    assertEquals("secret", context.apiKey());
    assertEquals(2_500, context.requestTimeoutMs());
  }

  @Test
  void overrideTimeoutPreservesEndpointAndApiKey() {
    MessageTranslationSettingsSnapshot settings =
        new MessageTranslationSettingsSnapshot(
            true,
            MessageTranslationSettingsSnapshot.Mode.AUTO,
            "libretranslate",
            "https://example.test/libre",
            "token",
            "auto",
            "es",
            true,
            true,
            List.of(),
            2_500,
            4_000,
            2);

    MessageTranslationBackendContext context =
        MessageTranslationBackendContexts.from(settings, 900);

    assertEquals("https://example.test/libre", context.endpoint());
    assertEquals("token", context.apiKey());
    assertEquals(900, context.requestTimeoutMs());
  }

  @Test
  void nullSettingsUseBlankEndpointAndDefaultTimeout() {
    MessageTranslationBackendContext context = MessageTranslationBackendContexts.from(null);

    assertEquals("", context.endpoint());
    assertEquals("", context.apiKey());
    assertEquals(10_000, context.requestTimeoutMs());
  }
}
