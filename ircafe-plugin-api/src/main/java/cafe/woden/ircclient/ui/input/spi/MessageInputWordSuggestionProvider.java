package cafe.woden.ircclient.ui.input.spi;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ServiceLoader-backed contribution point for message input completion popup suggestions.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.input.spi.MessageInputWordSuggestionProvider}.
 */
public interface MessageInputWordSuggestionProvider {

  /**
   * Suggest replacement/completion candidates for the provided token.
   *
   * @param token current token under the caret
   * @param maxSuggestions maximum number of suggestions to return
   */
  List<String> suggestWords(String token, int maxSuggestions);

  default CompletableFuture<List<String>> suggestWordsAsync(String token, int maxSuggestions) {
    return CompletableFuture.completedFuture(suggestWords(token, maxSuggestions));
  }
}
