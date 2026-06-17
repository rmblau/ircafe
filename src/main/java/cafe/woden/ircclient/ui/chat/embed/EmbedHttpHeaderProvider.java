package cafe.woden.ircclient.ui.chat.embed;

/**
 * Legacy shared embed HTTP-header provider service name.
 *
 * @deprecated register {@link cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider}
 *     implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface EmbedHttpHeaderProvider
    extends cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider {}
