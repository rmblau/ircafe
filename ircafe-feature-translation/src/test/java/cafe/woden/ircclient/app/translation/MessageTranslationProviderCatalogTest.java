package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;

class MessageTranslationProviderCatalogTest {

  @Test
  void dedupesBackendsByProviderClassAndNormalizedBackendId() {
    MessageTranslationBackendProvider builtIn = new RecordingBackend(" guide-echo ");
    MessageTranslationBackendProvider duplicate = new RecordingBackend("GUIDE-ECHO");
    MessageTranslationBackendProvider sameClassDifferentId = new RecordingBackend("other-echo");

    List<MessageTranslationBackendProvider> providers =
        MessageTranslationProviderCatalog.translationBackends(
            List.of(builtIn), Arrays.asList(null, duplicate, sameClassDifferentId));

    assertEquals(2, providers.size());
    assertSame(builtIn, providers.get(0));
    assertSame(sameClassDifferentId, providers.get(1));
  }

  @Test
  void dedupesLanguageProvidersByProviderClass() {
    MessageTranslationLanguageProvider builtIn =
        new RecordingLanguageProvider(List.of(new MessageTranslationLanguage("en", "English")));
    MessageTranslationLanguageProvider duplicate =
        new RecordingLanguageProvider(List.of(new MessageTranslationLanguage("fr", "French")));

    List<MessageTranslationLanguageProvider> providers =
        MessageTranslationProviderCatalog.languageProviders(List.of(builtIn), List.of(duplicate));

    assertEquals(List.of(builtIn), providers);
  }

  @Test
  void copiesOnlyNonNullProviders() {
    MessageTranslationBackendProvider backend = new RecordingBackend("echo");

    assertEquals(
        List.of(backend),
        MessageTranslationProviderCatalog.copyNonNullProviders(Arrays.asList(null, backend)));
  }

  private record RecordingBackend(String backendId) implements MessageTranslationBackendProvider {
    @Override
    public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
      return CompletableFuture.completedFuture(new MessageTranslationResult("", "", "", ""));
    }
  }

  private record RecordingLanguageProvider(List<MessageTranslationLanguage> languages)
      implements MessageTranslationLanguageProvider {}
}
