package cafe.woden.ircclient.ui.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class PathChooserControlsSupportTest {

  @Test
  void refreshGatesPathControlsWithAvailability() {
    AtomicBoolean available = new AtomicBoolean(false);
    PathChooserControlsSupport.Controls controls = controls(" /tmp/script.sh ", available, true);

    assertFalse(controls.path().isEnabled());
    assertFalse(controls.path().isEditable());
    assertFalse(controls.browseButton().isEnabled());
    assertFalse(controls.clearButton().isEnabled());

    available.set(true);
    controls.refresh();

    assertTrue(controls.path().isEnabled());
    assertTrue(controls.path().isEditable());
    assertTrue(controls.browseButton().isEnabled());
    assertTrue(controls.clearButton().isEnabled());
    assertEquals("/tmp/script.sh", controls.pathValue());
  }

  @Test
  void clearButtonClearsPathAndRefreshesState() {
    PathChooserControlsSupport.Controls controls =
        controls("/tmp/working-directory", new AtomicBoolean(true), true);

    assertTrue(controls.clearButton().isEnabled());

    controls.clearButton().doClick();

    assertEquals("", controls.path().getText());
    assertFalse(controls.clearButton().isEnabled());
  }

  @Test
  void canKeepAvailablePathReadOnly() {
    PathChooserControlsSupport.Controls controls =
        controls("/tmp/generated", new AtomicBoolean(true), false);

    assertTrue(controls.path().isEnabled());
    assertFalse(controls.path().isEditable());
    assertTrue(controls.browseButton().isEnabled());
  }

  private static PathChooserControlsSupport.Controls controls(
      String initialPath, AtomicBoolean available, boolean editableWhenAvailable) {
    return PathChooserControlsSupport.buildControls(
        PathChooserControlsSupport.Request.builder()
            .initialPath(initialPath)
            .browseButtonText("Browse")
            .clearButtonText("Clear")
            .browseTooltip("Browse for path")
            .clearTooltip("Clear path")
            .chooserDialogTitle("Select path")
            .selectionMode(PathChooserControlsSupport.SelectionMode.FILES)
            .availableSupplier(available::get)
            .editableWhenAvailable(editableWhenAvailable)
            .build());
  }
}
