package cafe.woden.ircclient.ui;

/**
 * Legacy external browser scheme provider service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface ExternalBrowserSchemeProvider
    extends cafe.woden.ircclient.ui.spi.ExternalBrowserSchemeProvider {}
