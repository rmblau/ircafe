package cafe.woden.ircclient.ui;

/**
 * Legacy external browser command provider service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.ui.spi.ExternalBrowserCommandProvider}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.ui.spi.ExternalBrowserCommandProvider}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface ExternalBrowserCommandProvider
    extends cafe.woden.ircclient.ui.spi.ExternalBrowserCommandProvider {}
