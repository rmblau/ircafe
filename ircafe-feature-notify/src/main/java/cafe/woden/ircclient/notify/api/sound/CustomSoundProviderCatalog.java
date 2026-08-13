package cafe.woden.ircclient.notify.api.sound;

import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Feature-owned provider-list and file-extension rules for custom sound plugins. */
public final class CustomSoundProviderCatalog {
  private CustomSoundProviderCatalog() {}

  public static List<CustomSoundFileExtensionProvider> extensionProviders(
      List<? extends CustomSoundFileExtensionProvider> builtInProviders,
      List<? extends CustomSoundFileExtensionProvider> installedProviders) {
    return dedupeByProviderClass(builtInProviders, installedProviders);
  }

  public static List<CustomSoundPlaybackProvider> playbackProviders(
      List<? extends CustomSoundPlaybackProvider> installedProviders) {
    return dedupeByProviderClass(installedProviders);
  }

  public static String extensionFor(
      String fileName,
      List<? extends CustomSoundFileExtensionProvider> builtInProviders,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    String lower = Objects.toString(fileName, "").trim().toLowerCase(Locale.ROOT);
    if (lower.isEmpty()) return null;
    for (String extension : supportedExtensions(builtInProviders, extensionProviders)) {
      if (lower.endsWith("." + extension)) return extension;
    }
    return null;
  }

  public static Set<String> supportedExtensions(
      List<? extends CustomSoundFileExtensionProvider> builtInProviders,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    LinkedHashSet<String> out = new LinkedHashSet<>();
    for (CustomSoundFileExtensionProvider provider :
        extensionProviders(builtInProviders, extensionProviders)) {
      for (String extension :
          Objects.requireNonNullElse(provider.soundFileExtensions(), List.<String>of())) {
        String normalized = normalizeExtension(extension);
        if (normalized != null) out.add(normalized);
      }
    }
    return Set.copyOf(out);
  }

  public static String supportedExtensionSentence(
      List<? extends CustomSoundFileExtensionProvider> builtInProviders,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return joinHuman(
        sortedExtensions(builtInProviders, extensionProviders).stream()
            .map(extension -> "." + extension)
            .toList(),
        "and");
  }

  public static String supportedExtensionTitleList(
      List<? extends CustomSoundFileExtensionProvider> builtInProviders,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return joinHuman(
        sortedExtensions(builtInProviders, extensionProviders).stream()
            .map(extension -> extension.toUpperCase(Locale.ROOT))
            .toList(),
        "or");
  }

  public static String supportedExtensionFilterPattern(
      List<? extends CustomSoundFileExtensionProvider> builtInProviders,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return sortedExtensions(builtInProviders, extensionProviders).stream()
        .map(extension -> "*." + extension)
        .reduce((left, right) -> left + ", " + right)
        .orElse("*");
  }

  public static boolean hasOnlyBuiltInExtensions(
      List<? extends CustomSoundFileExtensionProvider> builtInProviders,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return supportedExtensions(builtInProviders, extensionProviders)
        .equals(supportedExtensions(builtInProviders, List.of()));
  }

  @SafeVarargs
  private static <T> List<T> dedupeByProviderClass(List<? extends T>... providerGroups) {
    LinkedHashSet<String> providerClassNames = new LinkedHashSet<>();
    ArrayList<T> deduped = new ArrayList<>();
    for (List<? extends T> providerGroup : providerGroups) {
      if (providerGroup == null) continue;
      for (T provider : providerGroup) {
        if (provider == null) continue;
        if (!providerClassNames.add(provider.getClass().getName())) continue;
        deduped.add(provider);
      }
    }
    return List.copyOf(deduped);
  }

  private static List<String> sortedExtensions(
      List<? extends CustomSoundFileExtensionProvider> builtInProviders,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    return supportedExtensions(builtInProviders, extensionProviders).stream().sorted().toList();
  }

  private static String joinHuman(List<String> values, String conjunction) {
    if (values == null || values.isEmpty()) return "";
    if (values.size() == 1) return values.getFirst();
    if (values.size() == 2) return values.getFirst() + " " + conjunction + " " + values.get(1);
    return String.join(", ", values.subList(0, values.size() - 1))
        + ", "
        + conjunction
        + " "
        + values.getLast();
  }

  private static String normalizeExtension(String extension) {
    String normalized = Objects.toString(extension, "").trim().toLowerCase(Locale.ROOT);
    while (normalized.startsWith(".")) {
      normalized = normalized.substring(1).trim();
    }
    if (normalized.isEmpty()) return null;
    for (int i = 0; i < normalized.length(); i++) {
      char c = normalized.charAt(i);
      boolean allowed = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
      if (!allowed) return null;
    }
    return normalized;
  }
}
