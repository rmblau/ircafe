package cafe.woden.ircclient.ui.settings.theme;

import java.util.Map;
import java.util.Objects;

/** Plugin-contributed FlatLaf preset defaults keyed by the matching theme option id. */
public record ThemePresetContribution(String id, boolean dark, Map<String, String> extraDefaults) {
  public ThemePresetContribution {
    id = Objects.toString(id, "").trim();
    extraDefaults = Map.copyOf(Objects.requireNonNullElse(extraDefaults, Map.of()));
  }
}
