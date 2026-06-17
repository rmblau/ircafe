package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;

/**
 * Legacy translation backend service name.
 *
 * @deprecated register {@link MessageTranslationBackendProvider} implementations under {@code
 *     META-INF/services/cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider}.
 */
@Deprecated(since = "0.1", forRemoval = false)
public interface MessageTranslationBackend extends MessageTranslationBackendProvider {}
