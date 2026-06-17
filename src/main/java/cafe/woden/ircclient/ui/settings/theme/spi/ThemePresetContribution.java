package cafe.woden.ircclient.ui.settings.theme.spi;

import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Plugin-contributed FlatLaf preset defaults keyed by the matching theme option id. */
@InterfaceLayer
public record ThemePresetContribution(String id, boolean dark, Map<String, String> extraDefaults) {
  public ThemePresetContribution {
    id = Objects.toString(id, "").trim();
    extraDefaults = Map.copyOf(Objects.requireNonNullElse(extraDefaults, Map.of()));
  }
}
