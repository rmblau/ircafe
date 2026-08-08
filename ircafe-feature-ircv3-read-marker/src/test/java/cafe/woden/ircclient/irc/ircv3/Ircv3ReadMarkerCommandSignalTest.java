package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3ReadMarkerCommandSignalTest {

  @Test
  void parsesMarkreadTargetAndMarker() {
    Ircv3ReadMarkerCommandSignal signal =
        Ircv3ReadMarkerCommandSignal.parse(
                "markread", List.of(":#ircafe", ":timestamp=2026-02-16T12:30:00.000Z"))
            .orElseThrow();

    assertEquals("#ircafe", signal.target());
    assertEquals("timestamp=2026-02-16T12:30:00.000Z", signal.marker());
  }

  @Test
  void recognizesEmptyMarkreadAndIgnoresOtherCommands() {
    assertTrue(Ircv3ReadMarkerCommandSignal.parse("MARKREAD", List.of()).isPresent());
    assertTrue(Ircv3ReadMarkerCommandSignal.parse("PRIVMSG", List.of()).isEmpty());
  }
}
