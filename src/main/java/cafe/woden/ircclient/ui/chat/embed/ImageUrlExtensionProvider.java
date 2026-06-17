package cafe.woden.ircclient.ui.chat.embed;

/**
 * Legacy direct image URL extension provider service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface ImageUrlExtensionProvider
    extends cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider {}
