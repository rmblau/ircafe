package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class OptionalHexPreviewStateTest {

  @Test
  void blankHexClearsLastValidPreviewValue() {
    OptionalHexPreviewState state = new OptionalHexPreviewState("#AABBCC");

    assertNull(state.resolve(new JTextField("  ")));
    assertNull(state.resolve(new JTextField("not-a-color")));
  }

  @Test
  void validHexUpdatesLastValidPreviewValue() {
    OptionalHexPreviewState state = new OptionalHexPreviewState("#AABBCC");

    assertEquals("#336699", state.resolve(new JTextField(" 336699 ")));
  }

  @Test
  void invalidHexKeepsLastValidPreviewValue() {
    OptionalHexPreviewState state = new OptionalHexPreviewState("#AABBCC");

    assertEquals("#AABBCC", state.resolve(new JTextField("not-a-color")));
  }

  @Test
  void documentChangesRememberValueAndSchedulePreview() {
    OptionalHexPreviewState state = new OptionalHexPreviewState(null);
    JTextField field = new JTextField();
    AtomicInteger previewCount = new AtomicInteger();
    state.attachTo(field, previewCount::incrementAndGet);

    field.setText("abc");

    assertEquals("#AABBCC", state.resolve(new JTextField("invalid")));
    assertEquals(1, previewCount.get());
  }
}
