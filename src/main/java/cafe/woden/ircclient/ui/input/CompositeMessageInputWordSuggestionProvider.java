package cafe.woden.ircclient.ui.input;

import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.ui.input.spi.MessageInputWordSuggestionProvider;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Merges built-in and plugin-provided message input word suggestion providers. */
final class CompositeMessageInputWordSuggestionProvider
    implements MessageInputWordSuggestionProvider {

  private final List<MessageInputWordSuggestionProvider> providers;

  private CompositeMessageInputWordSuggestionProvider(
      List<MessageInputWordSuggestionProvider> providers) {
    this.providers = List.copyOf(Objects.requireNonNullElse(providers, List.of()));
  }

  static MessageInputWordSuggestionProvider from(
      MessageInputWordSuggestionProvider builtInProvider, InstalledPluginsPort installedPlugins) {
    List<MessageInputWordSuggestionProvider> builtIns =
        builtInProvider == null ? List.of() : List.of(builtInProvider);
    if (installedPlugins == null) {
      return builtInProvider;
    }
    List<MessageInputWordSuggestionProvider> loaded =
        PluginServiceLoaderSupport.dedupeByProviderClass(
            installedPlugins.loadInstalledServices(
                MessageInputWordSuggestionProvider.class, builtIns));
    if (loaded.isEmpty()) {
      return builtInProvider;
    }
    if (loaded.size() == 1 && loaded.getFirst() == builtInProvider) {
      return builtInProvider;
    }
    return new CompositeMessageInputWordSuggestionProvider(loaded);
  }

  @Override
  public List<String> suggestWords(String token, int maxSuggestions) {
    int max = Math.max(0, maxSuggestions);
    if (max == 0 || providers.isEmpty()) {
      return List.of();
    }

    LinkedHashSet<String> suggestions = new LinkedHashSet<>();
    for (MessageInputWordSuggestionProvider provider : providers) {
      if (provider == null || suggestions.size() >= max) {
        continue;
      }
      List<String> providerSuggestions;
      try {
        providerSuggestions = provider.suggestWords(token, max - suggestions.size());
      } catch (RuntimeException ex) {
        continue;
      }
      addSuggestions(suggestions, providerSuggestions, max);
    }
    return List.copyOf(suggestions);
  }

  @Override
  public CompletableFuture<List<String>> suggestWordsAsync(String token, int maxSuggestions) {
    int max = Math.max(0, maxSuggestions);
    if (max == 0 || providers.isEmpty()) {
      return CompletableFuture.completedFuture(List.of());
    }

    ArrayList<CompletableFuture<List<String>>> futures = new ArrayList<>(providers.size());
    for (MessageInputWordSuggestionProvider provider : providers) {
      if (provider == null) {
        continue;
      }
      try {
        CompletableFuture<List<String>> future = provider.suggestWordsAsync(token, max);
        if (future != null) {
          futures.add(future.exceptionally(error -> List.of()));
        }
      } catch (RuntimeException ex) {
        // Ignore failing providers so one plugin cannot break the input popup.
      }
    }
    if (futures.isEmpty()) {
      return CompletableFuture.completedFuture(List.of());
    }

    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
        .thenApply(
            ignored -> {
              LinkedHashSet<String> suggestions = new LinkedHashSet<>();
              for (CompletableFuture<List<String>> future : futures) {
                if (suggestions.size() >= max) {
                  break;
                }
                addSuggestions(suggestions, future.join(), max);
              }
              return List.copyOf(suggestions);
            });
  }

  private static void addSuggestions(
      LinkedHashSet<String> out, List<String> suggestions, int maxSuggestions) {
    for (String suggestion : Objects.requireNonNullElse(suggestions, List.<String>of())) {
      if (out.size() >= maxSuggestions) {
        return;
      }
      String word = Objects.toString(suggestion, "").trim();
      if (!word.isEmpty()) {
        out.add(word);
      }
    }
  }
}
