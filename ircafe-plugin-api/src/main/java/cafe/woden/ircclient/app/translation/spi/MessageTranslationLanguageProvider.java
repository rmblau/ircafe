package cafe.woden.ircclient.app.translation.spi;

import java.util.List;

/**
 * ServiceLoader-backed contribution point for manual translation target-language metadata.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider}.
 * Provider classes must be public, stateless, and expose public no-argument constructors.
 */
public interface MessageTranslationLanguageProvider {

  /**
   * Returns portable target-language choices.
   *
   * <p>IRCafe ignores null entries and blank codes, trims and lowercases codes, converts underscores
   * to hyphens, and uses the normalized code as the label when the supplied label is blank.
   * Providers are evaluated in order and the first language for a normalized code wins. Returning
   * {@code null} is equivalent to returning an empty list.
   */
  List<MessageTranslationLanguage> languages();
}
