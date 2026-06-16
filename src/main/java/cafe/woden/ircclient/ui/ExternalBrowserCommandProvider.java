package cafe.woden.ircclient.ui;

import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * ServiceLoader-backed contribution point for custom external-browser launch commands.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.ExternalBrowserCommandProvider}.
 */
@InterfaceLayer
public interface ExternalBrowserCommandProvider {

  /**
   * Returns process command candidates for the normalized URL and lowercase OS name.
   *
   * <p>Each inner list is passed to {@link ProcessBuilder} as one command line. Providers should
   * return an empty list when they do not support the current platform.
   */
  List<List<String>> browserCommands(String normalizedUrl, String osName);
}
