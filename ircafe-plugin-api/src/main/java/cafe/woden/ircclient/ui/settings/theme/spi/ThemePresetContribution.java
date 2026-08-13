package cafe.woden.ircclient.ui.settings.theme.spi;

import java.util.Map;
import java.util.Objects;

/**
 * Plugin-contributed FlatLaf preset defaults keyed by the matching theme option id.
 *
 * @param id matching theme option id; trimmed on construction and matched case-insensitively
 * @param dark whether IRCafe should use the dark FlatLaf base rather than the light base
 * @param extraDefaults immutable FlatLaf UI defaults; a {@code null} map becomes empty
 */
public record ThemePresetContribution(String id, boolean dark, Map<String, String> extraDefaults) {
  public ThemePresetContribution {
    id = Objects.toString(id, "").trim();
    extraDefaults = Map.copyOf(Objects.requireNonNullElse(extraDefaults, Map.of()));
  }
}
