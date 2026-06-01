package cafe.woden.ircclient.ui.input;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Supplies word suggestions for the message input completion popup. */
interface MessageInputWordSuggestionProvider {

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
