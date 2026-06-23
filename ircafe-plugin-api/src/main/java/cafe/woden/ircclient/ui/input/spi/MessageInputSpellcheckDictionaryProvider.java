package cafe.woden.ircclient.ui.input.spi;

import java.util.List;

/**
 * ServiceLoader-backed contribution point for message-input spellcheck dictionary words.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.input.spi.MessageInputSpellcheckDictionaryProvider}.
 */
public interface MessageInputSpellcheckDictionaryProvider {

  /** Returns additional words that should be treated as correctly spelled in message input. */
  List<String> dictionaryWords();
}
