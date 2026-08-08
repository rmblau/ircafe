package cafe.woden.ircclient.config.runtime.server;

import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigPrivateMessageTargetCodec.containsPrivateMessageTarget;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigPrivateMessageTargetCodec.encodePrivateMessageTarget;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigPrivateMessageTargetCodec.normalizeNick;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigPrivateMessageTargetCodec.privateMessageEntryMatches;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigPrivateMessageTargetCodec.readPrivateMessageTargets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigPrivateMessageTargetCodecTest {

  @Test
  void normalizeAndEncodePrivateMessageTargets() {
    assertEquals("Alice", normalizeNick(" Alice "));
    assertEquals("", normalizeNick(null));
    assertEquals("query:Alice", encodePrivateMessageTarget(" Alice "));
  }

  @Test
  void detectsAndMatchesPrivateMessageEntriesByCaseInsensitiveNick() {
    List<String> autoJoin = List.of("#java", "query:Alice", "QUERY:bob");

    assertTrue(containsPrivateMessageTarget(autoJoin, "alice"));
    assertTrue(privateMessageEntryMatches("query:Alice", "ALICE"));
    assertFalse(containsPrivateMessageTarget(autoJoin, "charlie"));
    assertFalse(privateMessageEntryMatches("#java", "java"));
  }

  @Test
  void readsPrivateMessageTargetsFromServerAutoJoin() {
    assertEquals(
        List.of("Alice", "bob"),
        readPrivateMessageTargets(
            Map.of("autoJoin", List.of("#java", "query:Alice", "QUERY:bob"))));
    assertEquals(List.of(), readPrivateMessageTargets(Map.of()));
  }
}
