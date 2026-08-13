package cafe.woden.ircclient.config.runtime.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicyScope;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigEmbedLoadPolicyCodecTest {

  @Test
  void parsesAndNormalizesPersistedPolicy() {
    EmbedLoadPolicySnapshot snapshot =
        RuntimeConfigEmbedLoadPolicyCodec.parseSnapshot(
            Map.of(
                "global",
                Map.of("userWhitelist", List.of(" alice ", ""), "minAccountAgeDays", -3),
                "byServer",
                Map.of(" libera ", Map.of("requireLoggedIn", true), "oftc", "invalid")));

    assertEquals(List.of("alice"), snapshot.global().userWhitelist());
    assertEquals(0, snapshot.global().minAccountAgeDays());
    assertEquals(Map.of("libera", scope(true)), snapshot.byServer());
  }

  @Test
  void serializesOnlyNonDefaultValuesAndEntries() {
    EmbedLoadPolicySnapshot snapshot =
        new EmbedLoadPolicySnapshot(
            EmbedLoadPolicyScope.defaults(),
            Map.of(" libera ", scope(true), "oftc", EmbedLoadPolicyScope.defaults()));

    assertEquals(
        Map.of("byServer", Map.of("libera", Map.of("requireLoggedIn", true))),
        RuntimeConfigEmbedLoadPolicyCodec.serializeSnapshot(snapshot));
    assertEquals(Map.of(), RuntimeConfigEmbedLoadPolicyCodec.serializeSnapshot(null));
  }

  private static EmbedLoadPolicyScope scope(boolean requireLoggedIn) {
    return new EmbedLoadPolicyScope(
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        false,
        requireLoggedIn,
        0,
        List.of(),
        List.of(),
        List.of(),
        List.of());
  }
}
