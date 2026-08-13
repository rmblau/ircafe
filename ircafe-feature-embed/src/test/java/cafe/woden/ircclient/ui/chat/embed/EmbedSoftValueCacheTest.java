package cafe.woden.ircclient.ui.chat.embed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmbedSoftValueCacheTest {

  @Test
  void returnsStoredValuesByKey() {
    EmbedSoftValueCache<String> cache = new EmbedSoftValueCache<>(4, 4);

    cache.put("one", "alpha");

    assertThat(cache.get("one")).isEqualTo("alpha");
    assertThat(cache.get("missing")).isNull();
  }

  @Test
  void prunesOverflowWithoutEvictingNewestValue() {
    EmbedSoftValueCache<String> cache = new EmbedSoftValueCache<>(2, 4);

    cache.put("one", "alpha");
    cache.put("two", "bravo");
    cache.put("three", "charlie");

    assertThat(cache.size()).isLessThanOrEqualTo(2);
    assertThat(cache.get("three")).isEqualTo("charlie");
  }

  @Test
  void rejectsInvalidArguments() {
    assertThatThrownBy(() -> new EmbedSoftValueCache<String>(0, 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("maxKeys");
    assertThatThrownBy(() -> new EmbedSoftValueCache<String>(1, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("pruneMaxRemovals");

    EmbedSoftValueCache<String> cache = new EmbedSoftValueCache<>(1, 1);
    assertThatThrownBy(() -> cache.get(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> cache.put(null, "value")).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> cache.put("key", null)).isInstanceOf(NullPointerException.class);
  }
}
