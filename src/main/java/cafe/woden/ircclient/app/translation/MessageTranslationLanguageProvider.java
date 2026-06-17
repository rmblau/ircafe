package cafe.woden.ircclient.app.translation;

/**
 * Legacy translation language service name.
 *
 * @deprecated register {@link
 *     cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider} implementations
 *     under {@code
 *     META-INF/services/cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface MessageTranslationLanguageProvider
    extends cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider {}
