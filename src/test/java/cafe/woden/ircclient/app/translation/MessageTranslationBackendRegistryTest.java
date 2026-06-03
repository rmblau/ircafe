package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;

class MessageTranslationBackendRegistryTest {

  @Test
  void resolvesBackendsByNormalizedId() {
    MessageTranslationBackend backend = new StubBackend(" DeepL ");
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
    List<MessageTranslationBackend> backends =
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

  private record StubBackend(String backendId) implements MessageTranslationBackend {
    @Override
    public CompletionStage<MessageTranslationResult> translate(MessageTranslationRequest request) {
      return CompletableFuture.completedFuture(new MessageTranslationResult("", "", "", ""));
    }
  }
}
