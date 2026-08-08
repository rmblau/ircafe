package cafe.woden.ircclient.config.runtime.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuntimeConfigServerIdentityCodecTest {

  @Test
  void normalizeNickTrimsNullToBlank() {
    assertEquals("alice", RuntimeConfigServerIdentityCodec.normalizeNick(" alice "));
    assertEquals("", RuntimeConfigServerIdentityCodec.normalizeNick(null));
  }
}
