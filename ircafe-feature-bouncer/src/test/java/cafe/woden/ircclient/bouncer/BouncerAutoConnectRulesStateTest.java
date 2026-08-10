package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BouncerAutoConnectRulesStateTest {

  private final BouncerAutoConnectNetworkKeyNormalizer normalizer =
      new BouncerAutoConnectNetworkKeyNormalizer();

  @Test
  void replacesSeedWithEnabledNormalizedRulesOnly() {
    Map<String, Map<String, Boolean>> seed = new LinkedHashMap<>();
    seed.put(" ", Map.of("ignored", true));
    seed.put(
        " bouncer-main ", new LinkedHashMap<>(Map.of("Lib Era", true, "OFTC", false, "!!!", true)));

    BouncerAutoConnectRulesState state = new BouncerAutoConnectRulesState();
    state.replace(seed, normalizer::normalize);

    assertEquals(Map.of("bouncer-main", Map.of("lib_era", true)), state.snapshot());
    assertTrue(state.isEnabled("BOUNCER-MAIN", "LIB ERA", normalizer::normalize));
    assertFalse(state.isEnabled("bouncer-main", "oftc", normalizer::normalize));
  }

  @Test
  void mutatesNormalizedRulesAndReturnsPersistenceTarget() {
    BouncerAutoConnectRulesState state = new BouncerAutoConnectRulesState();
    state.replace(Map.of(), normalizer::normalize);

    BouncerAutoConnectRulesState.NormalizedRule enabled =
        state.setEnabled(" bouncer-main ", "Lib Era", true, normalizer::normalize).orElseThrow();

    assertEquals("bouncer-main", enabled.bouncerServerId());
    assertEquals("lib_era", enabled.networkKey());
    assertTrue(enabled.enabled());
    assertEquals(Map.of("lib_era", true), state.networksForBouncer("BOUNCER-MAIN"));

    BouncerAutoConnectRulesState.NormalizedRule disabled =
        state.setEnabled("bouncer-main", "LIB ERA", false, normalizer::normalize).orElseThrow();

    assertFalse(disabled.enabled());
    assertTrue(state.snapshot().isEmpty());
  }

  @Test
  void rejectsInvalidMutationWithoutChangingState() {
    BouncerAutoConnectRulesState state = new BouncerAutoConnectRulesState();
    state.replace(Map.of("bouncer", Map.of("libera", true)), normalizer::normalize);

    assertTrue(state.setEnabled(" ", "libera", false, normalizer::normalize).isEmpty());
    assertTrue(state.setEnabled("bouncer", "!!!", true, normalizer::normalize).isEmpty());
    assertEquals(Map.of("bouncer", Map.of("libera", true)), state.snapshot());
  }

  @Test
  void snapshotsAndPerBouncerViewsAreImmutable() {
    BouncerAutoConnectRulesState state = new BouncerAutoConnectRulesState();
    state.replace(Map.of("bouncer", Map.of("libera", true)), normalizer::normalize);

    Map<String, Map<String, Boolean>> snapshot = state.snapshot();
    Map<String, Boolean> networks = state.networksForBouncer("bouncer");

    assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
    assertThrows(UnsupportedOperationException.class, () -> networks.clear());
  }

  @Test
  void writesPreserveCallerBouncerCasingWhileReadsRemainCaseInsensitive() {
    BouncerAutoConnectRulesState state = new BouncerAutoConnectRulesState();
    state.replace(Map.of("Bouncer", Map.of("libera", true)), normalizer::normalize);

    state.setEnabled("bouncer", "oftc", true, normalizer::normalize).orElseThrow();

    assertEquals(2, state.snapshot().size());
    assertTrue(state.isEnabled("BOUNCER", "libera", normalizer::normalize));
    assertTrue(state.isEnabled("bouncer", "oftc", normalizer::normalize));
  }
}
