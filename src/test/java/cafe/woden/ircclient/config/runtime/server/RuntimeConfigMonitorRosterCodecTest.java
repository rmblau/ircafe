package cafe.woden.ircclient.config.runtime.server;

import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigMonitorRosterCodec.normalizeMonitorNick;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigMonitorRosterCodec.sanitizeMonitorNickList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class RuntimeConfigMonitorRosterCodecTest {

  @Test
  void normalizeMonitorNickExtractsNickAndRejectsInvalidTargets() {
    assertEquals("alice", normalizeMonitorNick(" :alice,extra "));
    assertEquals("bob", normalizeMonitorNick("bob!ident@host"));
    assertEquals("", normalizeMonitorNick("#channel"));
    assertEquals("", normalizeMonitorNick("&ops"));
    assertEquals("", normalizeMonitorNick("two words"));
    assertEquals("", normalizeMonitorNick(null));
  }

  @Test
  void sanitizeMonitorNickListNormalizesAndDeduplicatesByCase() {
    assertEquals(
        List.of("Alice", "bob"),
        sanitizeMonitorNickList(List.of(" Alice ", "alice", "bob!ident@host", "#channel", "")));
    assertEquals(List.of(), sanitizeMonitorNickList("not-a-list"));
  }
}
