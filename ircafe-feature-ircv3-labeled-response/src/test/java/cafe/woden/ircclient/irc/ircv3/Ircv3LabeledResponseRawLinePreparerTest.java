package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class Ircv3LabeledResponseRawLinePreparerTest {

  @Test
  void injectsGeneratedLabelWhenMissing() {
    var prepared = Ircv3LabeledResponseRawLinePreparer.prepare("WHO #ircafe", () -> "req 42;next");

    assertTrue(prepared.injected());
    assertEquals("req 42;next", prepared.label());
    assertEquals("@label=req\\s42\\:next WHO #ircafe", prepared.line());
  }

  @Test
  void preservesAndDecodesExistingLabelWithoutGeneratingAnother() {
    AtomicInteger calls = new AtomicInteger();
    var prepared =
        Ircv3LabeledResponseRawLinePreparer.prepare(
            "@+label=req\\:42;time=2026-07-12T00:00:00Z WHO #ircafe",
            () -> {
              calls.incrementAndGet();
              return "unused";
            });

    assertFalse(prepared.injected());
    assertEquals("req;42", prepared.label());
    assertEquals(0, calls.get());
  }

  @Test
  void appendsLabelToExistingTagSection() {
    var prepared =
        Ircv3LabeledResponseRawLinePreparer.prepare(
            "@time=2026-07-12T00:00:00Z WHO #ircafe", () -> "req-1");

    assertTrue(prepared.injected());
    assertEquals("@time=2026-07-12T00:00:00Z;label=req-1 WHO #ircafe", prepared.line());
  }
}
