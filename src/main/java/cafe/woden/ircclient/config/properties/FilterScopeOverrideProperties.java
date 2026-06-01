package cafe.woden.ircclient.config.properties;

import java.util.Objects;
import org.jmolecules.architecture.layered.InfrastructureLayer;

/**
 * Config-backed per-scope filter behavior overrides.
 *
 * <p>YAML binding location: {@code ircafe.ui.filters.overrides}.
 */
@InfrastructureLayer
public record FilterScopeOverrideProperties(
    String scope,
    Boolean filtersEnabled,
    Boolean placeholdersEnabled,
    Boolean placeholdersCollapsed) {

  public FilterScopeOverrideProperties {
    scope = Objects.toString(scope, "*").trim();
    if (scope.isBlank()) scope = "*";
  }
}
