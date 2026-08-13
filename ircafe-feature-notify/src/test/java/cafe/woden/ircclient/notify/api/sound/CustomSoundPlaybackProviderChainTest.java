package cafe.woden.ircclient.notify.api.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CustomSoundPlaybackProviderChainTest {

  private static final Path SOUND_PATH = Path.of("alert.ogg");

  @Test
  void returnsUnhandledWhenNoProviderHandlesPlayback() {
    CustomSoundPlaybackProviderResult result =
        CustomSoundPlaybackProviderChain.play(
            SOUND_PATH, List.of(new CountingProvider(false), new CountingProvider(false)), null);

    assertFalse(result.handled());
    assertFalse(result.handledWhileFresh());
    assertTrue(result.failures().isEmpty());
  }

  @Test
  void stopsAtFirstProviderThatHandlesPlayback() {
    CountingProvider first = new CountingProvider(false);
    CountingProvider second = new CountingProvider(true);
    CountingProvider third = new CountingProvider(true);

    CustomSoundPlaybackProviderResult result =
        CustomSoundPlaybackProviderChain.play(SOUND_PATH, List.of(first, second, third), null);

    assertTrue(result.handled());
    assertTrue(result.handledWhileFresh());
    assertEquals(1, first.calls());
    assertEquals(1, second.calls());
    assertEquals(0, third.calls());
  }

  @Test
  void handledProviderCanBecomeStaleBeforeResultIsReported() {
    AtomicBoolean stale = new AtomicBoolean(false);
    CustomSoundPlaybackProvider provider =
        path -> {
          stale.set(true);
          return true;
        };

    CustomSoundPlaybackProviderResult result =
        CustomSoundPlaybackProviderChain.play(SOUND_PATH, List.of(provider), stale::get);

    assertTrue(result.handled());
    assertFalse(result.handledWhileFresh());
  }

  @Test
  void capturesProviderFailuresAndContinues() {
    IllegalStateException failure = new IllegalStateException("cannot decode");
    CountingProvider second = new CountingProvider(true);

    CustomSoundPlaybackProviderResult result =
        CustomSoundPlaybackProviderChain.play(
            SOUND_PATH, List.of(new FailingProvider(failure), second), null);

    assertTrue(result.handled());
    assertTrue(result.handledWhileFresh());
    assertEquals(1, second.calls());
    assertEquals(1, result.failures().size());
    assertEquals(FailingProvider.class.getName(), result.failures().getFirst().providerClassName());
    assertSame(failure, result.failures().getFirst().exception());
  }

  @Test
  void skipsProvidersWhenPlaybackIsAlreadyStale() {
    AtomicBoolean stale = new AtomicBoolean(true);
    CountingProvider provider = new CountingProvider(true);

    CustomSoundPlaybackProviderResult result =
        CustomSoundPlaybackProviderChain.play(SOUND_PATH, List.of(provider), stale::get);

    assertFalse(result.handled());
    assertEquals(0, provider.calls());
  }

  private static final class CountingProvider implements CustomSoundPlaybackProvider {
    private final AtomicInteger calls = new AtomicInteger();
    private final boolean handled;

    private CountingProvider(boolean handled) {
      this.handled = handled;
    }

    @Override
    public boolean playCustomSound(Path path) {
      calls.incrementAndGet();
      return handled;
    }

    private int calls() {
      return calls.get();
    }
  }

  private record FailingProvider(Exception failure) implements CustomSoundPlaybackProvider {
    @Override
    public boolean playCustomSound(Path path) throws Exception {
      throw failure;
    }
  }
}
