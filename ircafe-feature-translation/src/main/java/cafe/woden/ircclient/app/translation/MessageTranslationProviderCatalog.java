package cafe.woden.ircclient.app.translation;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Merges translation provider lists after the root app resolves app and installed-plugin services.
 */
public final class MessageTranslationProviderCatalog {
  private MessageTranslationProviderCatalog() {}

  public static List<MessageTranslationBackendProvider> translationBackends(
      List<? extends MessageTranslationBackendProvider> firstProviders,
      List<? extends MessageTranslationBackendProvider> secondProviders) {
    return dedupeByProviderKey(
        List.of(copyNonNullProviders(firstProviders), copyNonNullProviders(secondProviders)),
        MessageTranslationProviderCatalog::backendProviderKey);
  }

  public static List<MessageTranslationLanguageProvider> languageProviders(
      List<? extends MessageTranslationLanguageProvider> firstProviders,
      List<? extends MessageTranslationLanguageProvider> secondProviders) {
    return dedupeByProviderClass(
        List.of(copyNonNullProviders(firstProviders), copyNonNullProviders(secondProviders)));
  }

  public static <T> List<T> copyNonNullProviders(List<? extends T> providers) {
    List<? extends T> safeProviders = providers == null ? List.of() : providers;
    if (safeProviders.isEmpty()) {
      return List.of();
    }
    ArrayList<T> nonNull = new ArrayList<>(safeProviders.size());
    for (T provider : safeProviders) {
      if (provider != null) {
        nonNull.add(provider);
      }
    }
    return List.copyOf(nonNull);
  }

  private static <T> List<T> dedupeByProviderClass(List<List<T>> providerGroups) {
    return dedupeByProviderKey(providerGroups, provider -> provider.getClass().getName());
  }

  private static <T> List<T> dedupeByProviderKey(
      List<List<T>> providerGroups, Function<? super T, String> providerKeyFunction) {
    ArrayList<T> deduped = new ArrayList<>();
    LinkedHashSet<String> providerKeys = new LinkedHashSet<>();
    for (List<T> providers : providerGroups) {
      for (T provider : providers) {
        String providerKey = Objects.toString(providerKeyFunction.apply(provider), "");
        if (!providerKeys.add(providerKey)) {
          continue;
        }
        deduped.add(provider);
      }
    }
    return List.copyOf(deduped);
  }

  private static String backendProviderKey(MessageTranslationBackendProvider provider) {
    return provider.getClass().getName()
        + '\u0000'
        + MessageTranslationBackendRegistry.normalizeBackendId(provider.backendId());
  }
}
