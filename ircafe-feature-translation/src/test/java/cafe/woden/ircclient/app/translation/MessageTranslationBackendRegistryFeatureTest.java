package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;

class MessageTranslationBackendRegistryFeatureTest {

  @Test
  void resolvesBackendsByNormalizedId() {
    MessageTranslationBackendProvider backend = new StubBackend(" DeepL ");
    MessageTranslationBackendRegistry registry =
        new MessageTranslationBackendRegistry(List.of(backend));

    assertSame(backend, registry.find("deepl").orElseThrow());
    assertSame(backend, registry.find("  DEEPL ").orElseThrow());
    assertEquals(Set.of("deepl"), registry.backendIds());
  }

  @Test
  void returnsEmptyForUnknownBackend() {
    MessageTranslationBackendRegistry registry = new MessageTranslationBackendRegistry(List.of());

    assertEquals(Optional.empty(), registry.find("missing"));
  }

  @Test
  void rejectsDuplicateNormalizedIds() {
    List<MessageTranslationBackendProvider> backends =
        List.of(new StubBackend("DeepL"), new StubBackend(" deepl "));

    assertThrows(
        IllegalStateException.class, () -> new MessageTranslationBackendRegistry(backends));
  }

  @Test
  void rejectsBlankBackendIds() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new MessageTranslationBackendRegistry(List.of(new StubBackend(" "))));
  }

  private record StubBackend(String backendId) implements MessageTranslationBackendProvider {
    @Override
    public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
      return CompletableFuture.completedFuture(new MessageTranslationResult("", "", "", ""));
    }
  }
}
