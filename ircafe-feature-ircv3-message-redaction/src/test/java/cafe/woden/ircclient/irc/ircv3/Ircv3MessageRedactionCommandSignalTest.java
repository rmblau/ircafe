package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3MessageRedactionCommandSignalTest {

  @Test
  void parsesRedactTargetAndMessageId() {
    Ircv3MessageRedactionCommandSignal signal =
        Ircv3MessageRedactionCommandSignal.parse(
                "redact", List.of(":#ircafe", ":abc123", ":cleanup"))
            .orElseThrow();

    assertEquals("#ircafe", signal.target());
    assertEquals("abc123", signal.messageId());
  }

  @Test
  void ignoresMissingMessageIdAndOtherCommands() {
    assertTrue(Ircv3MessageRedactionCommandSignal.parse("REDACT", List.of("#ircafe")).isEmpty());
    assertTrue(Ircv3MessageRedactionCommandSignal.parse("NOTICE", List.of()).isEmpty());
  }
}
