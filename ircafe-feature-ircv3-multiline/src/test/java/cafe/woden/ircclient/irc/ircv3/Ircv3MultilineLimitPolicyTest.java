package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3MultilineLimitPolicyTest {

  @Test
  void reportsLineLimitBeforeByteLimit() {
    assertEquals(
        "Message has 3 lines; negotiated multiline max-lines is 2.",
        Ircv3MultilineLimitPolicy.limitReason(3, 100L, 2L, 50L));
  }

  @Test
  void reportsByteLimitAndAcceptsUnboundedValues() {
    assertEquals(
        "Message is 11 UTF-8 bytes; negotiated multiline max-bytes is 5.",
        Ircv3MultilineLimitPolicy.limitReason(1, 11L, 0L, 5L));
    assertEquals("", Ircv3MultilineLimitPolicy.limitReason(5, 100L, 0L, 0L));
  }
}
