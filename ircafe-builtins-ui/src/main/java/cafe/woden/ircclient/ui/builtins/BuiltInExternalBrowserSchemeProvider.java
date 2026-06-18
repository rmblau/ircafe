package cafe.woden.ircclient.ui.builtins;

import cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Built-in URL schemes allowed by the external browser launcher. */
@AutoService(ExternalBrowserSchemeProvider.class)
public final class BuiltInExternalBrowserSchemeProvider implements ExternalBrowserSchemeProvider {
  private static final Set<String> SCHEMES = Set.of("http", "https");

  @Override
  public Set<String> allowedSchemes() {
    return SCHEMES;
  }
}
