package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Pure IRCv3 extension metadata catalog over already-resolved plugin providers. */
public final class Ircv3ExtensionMetadataCatalog {

  private Ircv3ExtensionMetadataCatalog() {}

  public static Snapshot snapshot(List<? extends Ircv3ExtensionProvider> providers) {
    return new Snapshot(providers);
  }

  public static final class Snapshot {

    private final List<Ircv3ExtensionProvider> providers;
    private final List<String> providerIds;
    private final List<Ircv3ExtensionContribution> extensions;
    private final List<Ircv3ExtensionContribution> requestableCapabilities;
    private final List<String> requestableCapabilityTokens;
    private final List<Ircv3FeatureContribution> visibleFeatures;
    private final Map<String, Ircv3ExtensionContribution> byName;

    private Snapshot(List<? extends Ircv3ExtensionProvider> providers) {
      this.providers = normalizeProviders(providers);
      this.providerIds = this.providers.stream().map(Ircv3ExtensionProvider::providerId).toList();
      this.extensions = collectExtensions(this.providers);
      this.requestableCapabilities =
          this.extensions.stream().filter(Ircv3ExtensionContribution::requestable).toList();
      this.requestableCapabilityTokens =
          this.requestableCapabilities.stream()
              .map(Ircv3ExtensionContribution::requestToken)
              .toList();
      this.visibleFeatures = collectVisibleFeatures(this.providers);
      this.byName = indexExtensions(this.extensions);
    }

    public List<Ircv3ExtensionProvider> providers() {
      return providers;
    }

    public List<String> providerIds() {
      return providerIds;
    }

    public List<Ircv3ExtensionContribution> all() {
      return extensions;
    }

    public Optional<Ircv3ExtensionContribution> find(String name) {
      return Optional.ofNullable(byName.get(normalize(name)));
    }

    public List<Ircv3ExtensionContribution> requestableCapabilities() {
      return requestableCapabilities;
    }

    public List<String> requestableCapabilityTokens() {
      return requestableCapabilityTokens;
    }

    public List<Ircv3FeatureContribution> visibleFeatures() {
      return visibleFeatures;
    }

    public List<Ircv3FeatureAvailabilityEvaluator.Evaluation> evaluateVisibleFeatures(
        Collection<String> enabledCapabilities) {
      return Ircv3FeatureAvailabilityEvaluator.evaluate(visibleFeatures, enabledCapabilities);
    }

    public String requestTokenFor(String name) {
      return find(name)
          .filter(Ircv3ExtensionContribution::requestable)
          .map(Ircv3ExtensionContribution::requestToken)
          .orElse("");
    }

    public String preferenceKeyFor(String name) {
      return find(name)
          .map(Ircv3ExtensionContribution::preferenceKey)
          .orElse(normalize(name));
    }

    public String normalizeRequestToken(String name) {
      String normalized = normalize(name);
      if (normalized.isEmpty()) {
        return "";
      }
      return find(normalized)
          .map(contribution -> contribution.requestable() ? contribution.requestToken() : "")
          .orElse(normalized);
    }

    public String normalizePreferenceKey(String name) {
      String normalized = normalize(name);
      if (normalized.isEmpty()) {
        return null;
      }
      return find(normalized)
          .map(Ircv3ExtensionContribution::preferenceKey)
          .orElse(normalized);
    }
  }

  private static List<Ircv3ExtensionProvider> normalizeProviders(
      List<? extends Ircv3ExtensionProvider> providers) {
    ArrayList<Ircv3ExtensionProvider> sorted =
        new ArrayList<>(Objects.requireNonNullElse(providers, List.of()));
    sorted.sort(
        Comparator.<Ircv3ExtensionProvider>comparingInt(
                provider -> provider == null ? Integer.MAX_VALUE : provider.sortOrder())
            .thenComparing(provider -> normalize(provider == null ? "" : provider.providerId())));

    LinkedHashMap<String, Ircv3ExtensionProvider> byId = new LinkedHashMap<>();
    for (Ircv3ExtensionProvider provider : sorted) {
      String providerId = normalize(provider == null ? "" : provider.providerId());
      if (provider == null || providerId.isEmpty()) {
        throw new IllegalStateException("IRCv3 extension provider must declare a non-blank id");
      }
      Ircv3ExtensionProvider previous = byId.putIfAbsent(providerId, provider);
      if (previous != null && previous.getClass() != provider.getClass()) {
        throw new IllegalStateException(
            "Duplicate IRCv3 extension provider id registered: " + providerId);
      }
    }
    return List.copyOf(byId.values());
  }

  private static List<Ircv3ExtensionContribution> collectExtensions(
      List<Ircv3ExtensionProvider> providers) {
    ArrayList<Ircv3ExtensionContribution> extensions = new ArrayList<>();
    for (Ircv3ExtensionProvider provider : providers) {
      List<Ircv3ExtensionContribution> contributions = provider.extensions();
      if (contributions == null) {
        continue;
      }
      for (Ircv3ExtensionContribution contribution : contributions) {
        if (contribution != null) {
          extensions.add(contribution);
        }
      }
    }
    return List.copyOf(extensions);
  }

  private static List<Ircv3FeatureContribution> collectVisibleFeatures(
      List<Ircv3ExtensionProvider> providers) {
    ArrayList<Ircv3FeatureContribution> features = new ArrayList<>();
    for (Ircv3ExtensionProvider provider : providers) {
      List<Ircv3FeatureContribution> contributions = provider.visibleFeatures();
      if (contributions == null) {
        continue;
      }
      for (Ircv3FeatureContribution contribution : contributions) {
        if (contribution != null) {
          features.add(contribution);
        }
      }
    }
    features.sort(
        Comparator.comparingInt(Ircv3FeatureContribution::sortOrder)
            .thenComparing(Ircv3FeatureContribution::label, String.CASE_INSENSITIVE_ORDER));

    LinkedHashMap<String, Ircv3FeatureContribution> byLabel = new LinkedHashMap<>();
    for (Ircv3FeatureContribution feature : features) {
      String label = normalizeLabel(feature.label());
      if (label.isEmpty()) {
        continue;
      }
      Ircv3FeatureContribution previous =
          byLabel.putIfAbsent(label.toLowerCase(Locale.ROOT), feature);
      if (previous != null && !previous.equals(feature)) {
        throw new IllegalStateException(
            "Duplicate IRCv3 visible feature label registered: " + label);
      }
    }
    return List.copyOf(features);
  }

  private static Map<String, Ircv3ExtensionContribution> indexExtensions(
      List<Ircv3ExtensionContribution> extensions) {
    LinkedHashMap<String, Ircv3ExtensionContribution> byName = new LinkedHashMap<>();
    for (Ircv3ExtensionContribution extension : extensions) {
      for (String name : extension.allNames()) {
        if (name.isEmpty()) {
          continue;
        }
        Ircv3ExtensionContribution previous = byName.putIfAbsent(name, extension);
        if (previous != null && !previous.equals(extension)) {
          throw new IllegalStateException("Duplicate IRCv3 extension name registered: " + name);
        }
      }
    }
    return Map.copyOf(byName);
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
  }

  private static String normalizeLabel(String value) {
    return Objects.toString(value, "").trim();
  }
}
