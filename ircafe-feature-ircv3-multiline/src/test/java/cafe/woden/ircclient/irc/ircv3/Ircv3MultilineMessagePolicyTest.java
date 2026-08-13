package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3MultilineMessagePolicyTest {

  @Test
  void normalizesLineEndingsAndPreservesTrailingEmptyLines() {
    List<String> lines = Ircv3MultilineMessagePolicy.normalizeLines("one\r\ntwo\rthree\n");

    assertEquals(List.of("one", "two", "three", ""), lines);
    assertEquals("one\ntwo\nthree\n", Ircv3MultilineMessagePolicy.joinLines(lines));
  }

  @Test
  void countsUtf8PayloadBytesAndNewlineSeparators() {
    assertEquals(7L, Ircv3MultilineMessagePolicy.payloadUtf8Bytes(List.of("🙂", "é")));
  }

  @Test
  void enforcesNegotiatedByteAndLineLimitsWithExistingDiagnostics() {
    IllegalArgumentException bytes =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                Ircv3MultilineMessagePolicy.requireWithinMaxBytes(
                    5L, List.of("hello", "world"), "libera"));
    assertEquals(
        "Message exceeds negotiated IRCv3 multiline max-bytes 11 > 5 for libera",
        bytes.getMessage());

    IllegalArgumentException lines =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                Ircv3MultilineMessagePolicy.requireWithinMaxLines(
                    1L, List.of("hello", "world"), "libera"));
    assertEquals(
        "Message exceeds negotiated IRCv3 multiline max-lines 2 > 1 for libera",
        lines.getMessage());
  }

  @Test
  void nonPositiveLimitsRemainUnbounded() {
    Ircv3MultilineMessagePolicy.requireWithinMaxBytes(0L, List.of("hello", "world"), "libera");
    Ircv3MultilineMessagePolicy.requireWithinMaxLines(-1L, List.of("hello", "world"), "libera");
  }
}
