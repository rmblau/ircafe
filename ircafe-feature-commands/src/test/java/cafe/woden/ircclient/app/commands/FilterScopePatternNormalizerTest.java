package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FilterScopePatternNormalizerTest {

  @Test
  void normalizesServerTargetAndStatusShorthand() {
    assertEquals("libera/*", FilterScopePatternNormalizer.normalize("libera"));
    assertEquals("*/#llamas", FilterScopePatternNormalizer.normalize("#LLaMas"));
    assertEquals("*/status", FilterScopePatternNormalizer.normalize("status"));
    assertEquals("libera/#irc", FilterScopePatternNormalizer.normalize("libera/#IRC"));
  }

  @Test
  void defaultsBlankScopesToWildcard() {
    assertEquals("*", FilterScopePatternNormalizer.normalize(null));
    assertEquals("*", FilterScopePatternNormalizer.normalize("  "));
    assertEquals("*", FilterScopePatternNormalizer.normalize("*"));
  }
}
