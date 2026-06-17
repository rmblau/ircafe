package cafe.woden.ircclient.ui.spi;

import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * ServiceLoader-backed contribution point for URL schemes the external browser launcher may open.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider}.
 */
@InterfaceLayer
public interface ExternalBrowserSchemeProvider {

  /**
   * Returns additional lowercase URL schemes that may be opened by the external browser launcher.
   */
  Set<String> allowedSchemes();
}
