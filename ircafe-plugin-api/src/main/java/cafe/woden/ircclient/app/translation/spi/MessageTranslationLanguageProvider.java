package cafe.woden.ircclient.app.translation.spi;

import java.util.List;

/**
 * ServiceLoader-backed contribution point for manual translation target-language metadata.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider}.
 */
public interface MessageTranslationLanguageProvider {
  List<MessageTranslationLanguage> languages();
}
