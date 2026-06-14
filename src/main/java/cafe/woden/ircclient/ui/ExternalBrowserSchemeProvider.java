package cafe.woden.ircclient.ui;

import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * ServiceLoader-backed contribution point for URL schemes the external browser launcher may open.
 */
@InterfaceLayer
public interface ExternalBrowserSchemeProvider {

  /**
   * Returns additional lowercase URL schemes that may be opened by the external browser launcher.
   */
  Set<String> allowedSchemes();
}
