package cafe.woden.ircclient.ui;

import cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Built-in URL schemes allowed by the external browser launcher. */
@InterfaceLayer
@AutoService(ExternalBrowserSchemeProvider.class)
public final class BuiltInExternalBrowserSchemeProvider implements ExternalBrowserSchemeProvider {
  private static final Set<String> SCHEMES = Set.of("http", "https");

  static Set<String> schemes() {
    return SCHEMES;
  }

  @Override
  public Set<String> allowedSchemes() {
    return SCHEMES;
  }
}
