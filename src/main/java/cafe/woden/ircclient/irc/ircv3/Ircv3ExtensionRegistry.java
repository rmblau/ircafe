package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import cafe.woden.ircclient.util.PluginServiceLoaderSupport;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Root compatibility facade around the feature-owned IRCv3 metadata catalog. */
public final class Ircv3ExtensionRegistry {

  public enum ExtensionKind {
    CAPABILITY,
    TAG_FEATURE,
    EXPERIMENTAL
  }

  public enum SpecStatus {
    STABLE,
    DRAFT,
    EXPERIMENTAL
  }

  public enum UiGroup {
    CORE("Core metadata and sync"),
    CONVERSATION("Conversation features"),
    HISTORY("History and playback"),
    OTHER("Other capabilities");

    private final String title;

    UiGroup(String title) {
      this.title = title;
    }

    public String title() {
      return title;
    }
  }

  public record UiMetadata(String label, UiGroup group, int sortOrder, String impactSummary) {
    public UiMetadata {
      label = normalizeLabel(label);
      group = Objects.requireNonNullElse(group, UiGroup.OTHER);
      impactSummary = Objects.toString(impactSummary, "").trim();
    }
  }

  public record ExtensionDefinition(
      String id,
      ExtensionKind kind,
      SpecStatus specStatus,
      List<String> aliases,
      String requestToken,
      String preferenceKey,
      UiMetadata uiMetadata) {
    public ExtensionDefinition {
      id = normalize(id);
      Objects.requireNonNull(kind, "kind");
      Objects.requireNonNull(specStatus, "specStatus");
      aliases = copyNormalized(aliases);
      requestToken = normalize(requestToken);
      preferenceKey = normalize(preferenceKey.isBlank() ? id : preferenceKey);
      Objects.requireNonNull(uiMetadata, "uiMetadata");
    }

    public boolean requestable() {
      return kind == ExtensionKind.CAPABILITY && !requestToken.isEmpty();
    }

    public List<String> allNames() {
      LinkedHashSet<String> names = new LinkedHashSet<>();
      if (!id.isEmpty()) {
        names.add(id);
      }
      if (!preferenceKey.isEmpty()) {
        names.add(preferenceKey);
      }
      if (!requestToken.isEmpty()) {
        names.add(requestToken);
      }
      names.addAll(aliases);
      return List.copyOf(names);
    }
  }

  public record FeatureDefinition(
      int sortOrder, String label, List<String> requiredAll, List<String> requiredAny) {
    public FeatureDefinition {
      label = normalizeLabel(label);
      requiredAll = copyNormalized(requiredAll);
      requiredAny = copyNormalized(requiredAny);
    }
  }

  private static final Snapshot DEFAULT_SNAPSHOT = new Snapshot(loadProviders());

  private Ircv3ExtensionRegistry() {}

  public static final class Snapshot {

    private final Ircv3ExtensionMetadataCatalog.Snapshot delegate;
    private final List<ExtensionDefinition> extensions;
    private final List<ExtensionDefinition> requestableCapabilities;
    private final List<FeatureDefinition> visibleFeatures;

    private Snapshot(List<? extends Ircv3ExtensionProvider> providers) {
      this.delegate = Ircv3ExtensionMetadataCatalog.snapshot(providers);
      this.extensions =
          delegate.all().stream().map(Ircv3ExtensionRegistry::toExtensionDefinition).toList();
      this.requestableCapabilities =
          delegate.requestableCapabilities().stream()
              .map(Ircv3ExtensionRegistry::toExtensionDefinition)
              .toList();
      this.visibleFeatures =
          delegate.visibleFeatures().stream()
              .map(Ircv3ExtensionRegistry::toFeatureDefinition)
              .toList();
    }

    public List<ExtensionDefinition> all() {
      return extensions;
    }

    public List<String> providerIds() {
      return delegate.providerIds();
    }

    public Optional<ExtensionDefinition> find(String name) {
      return delegate.find(name).map(Ircv3ExtensionRegistry::toExtensionDefinition);
    }

    public List<ExtensionDefinition> requestableCapabilities() {
      return requestableCapabilities;
    }

    public List<String> requestableCapabilityTokens() {
      return delegate.requestableCapabilityTokens();
    }

    public List<FeatureDefinition> visibleFeatures() {
      return visibleFeatures;
    }

    public List<Ircv3FeatureAvailabilityEvaluator.Evaluation> evaluateVisibleFeatures(
        Collection<String> enabledCapabilities) {
      return delegate.evaluateVisibleFeatures(enabledCapabilities);
    }

    private List<Ircv3ExtensionProvider> providers() {
      return delegate.providers();
    }

    public String requestTokenFor(String name) {
      return delegate.requestTokenFor(name);
    }

    public String preferenceKeyFor(String name) {
      return delegate.preferenceKeyFor(name);
    }

    public String normalizeRequestToken(String name) {
      return delegate.normalizeRequestToken(name);
    }

    public String normalizePreferenceKey(String name) {
      return delegate.normalizePreferenceKey(name);
    }
  }

  public static Snapshot snapshot() {
    return DEFAULT_SNAPSHOT;
  }

  public static List<ExtensionDefinition> all() {
    return DEFAULT_SNAPSHOT.all();
  }

  public static List<String> providerIds() {
    return DEFAULT_SNAPSHOT.providerIds();
  }

  public static Optional<ExtensionDefinition> find(String name) {
    return DEFAULT_SNAPSHOT.find(name);
  }

  public static List<ExtensionDefinition> requestableCapabilities() {
    return DEFAULT_SNAPSHOT.requestableCapabilities();
  }

  public static List<String> requestableCapabilityTokens() {
    return DEFAULT_SNAPSHOT.requestableCapabilityTokens();
  }

  public static List<FeatureDefinition> visibleFeatures() {
    return DEFAULT_SNAPSHOT.visibleFeatures();
  }

  public static List<Ircv3FeatureAvailabilityEvaluator.Evaluation> evaluateVisibleFeatures(
      Collection<String> enabledCapabilities) {
    return DEFAULT_SNAPSHOT.evaluateVisibleFeatures(enabledCapabilities);
  }

  public static String requestTokenFor(String name) {
    return DEFAULT_SNAPSHOT.requestTokenFor(name);
  }

  public static String preferenceKeyFor(String name) {
    return DEFAULT_SNAPSHOT.preferenceKeyFor(name);
  }

  public static String normalizeRequestToken(String name) {
    return DEFAULT_SNAPSHOT.normalizeRequestToken(name);
  }

  public static String normalizePreferenceKey(String name) {
    return DEFAULT_SNAPSHOT.normalizePreferenceKey(name);
  }

  static List<Ircv3ExtensionProvider> defaultProviders() {
    return DEFAULT_SNAPSHOT.providers();
  }

  static Snapshot snapshotForProviders(List<? extends Ircv3ExtensionProvider> providers) {
    return new Snapshot(providers);
  }

  private static List<Ircv3ExtensionProvider> loadProviders() {
    try {
      return PluginServiceLoaderSupport.loadApplicationServices(
          Ircv3ExtensionProvider.class, Ircv3ExtensionRegistry.class);
    } catch (RuntimeException error) {
      throw new IllegalStateException(
          "Failed to load IRCv3 extension providers for " + Ircv3ExtensionProvider.class.getName(),
          error);
    }
  }

  private static ExtensionDefinition toExtensionDefinition(
      Ircv3ExtensionContribution contribution) {
    return new ExtensionDefinition(
        contribution.id(),
        ExtensionKind.valueOf(contribution.kind().name()),
        SpecStatus.valueOf(contribution.specStatus().name()),
        contribution.aliases(),
        contribution.requestToken(),
        contribution.preferenceKey(),
        toUiMetadata(contribution.uiMetadata()));
  }

  private static UiMetadata toUiMetadata(Ircv3UiMetadata metadata) {
    return new UiMetadata(
        metadata.label(),
        UiGroup.valueOf(metadata.group().name()),
        metadata.sortOrder(),
        metadata.impactSummary());
  }

  private static FeatureDefinition toFeatureDefinition(Ircv3FeatureContribution contribution) {
    return new FeatureDefinition(
        contribution.sortOrder(),
        contribution.label(),
        contribution.requiredAll(),
        contribution.requiredAny());
  }

  private static List<String> copyNormalized(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    return values.stream()
        .map(Ircv3ExtensionRegistry::normalize)
        .filter(value -> !value.isEmpty())
        .toList();
  }

  private static String normalize(String value) {
    return Objects.toString(value, "").trim().toLowerCase(java.util.Locale.ROOT);
  }

  private static String normalizeLabel(String value) {
    return Objects.toString(value, "").trim();
  }
}
