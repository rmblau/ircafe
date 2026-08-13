package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3ExtensionMetadataCatalogTest {

  @Test
  void ordersProvidersAndBuildsCanonicalLookups() {
    Ircv3ExtensionProvider later =
        provider(
            "later",
            200,
            capability("draft/example", "example", "example", List.of("example-alias")),
            new Ircv3FeatureContribution(20, "Later feature", List.of("example"), List.of()));
    Ircv3ExtensionProvider earlier =
        provider(
            "earlier",
            100,
            capability("echo-message", "echo-message", "echo-message", List.of()),
            new Ircv3FeatureContribution(
                10, "Earlier feature", List.of("echo-message"), List.of()));

    Ircv3ExtensionMetadataCatalog.Snapshot snapshot =
        Ircv3ExtensionMetadataCatalog.snapshot(List.of(later, earlier));

    assertEquals(List.of("earlier", "later"), snapshot.providerIds());
    assertEquals(List.of("echo-message", "example"), snapshot.requestableCapabilityTokens());
    assertEquals("example", snapshot.requestTokenFor("EXAMPLE-ALIAS"));
    assertEquals("example", snapshot.preferenceKeyFor("draft/example"));
    assertEquals(
        List.of("Earlier feature", "Later feature"),
        snapshot.visibleFeatures().stream().map(Ircv3FeatureContribution::label).toList());
  }

  @Test
  void evaluatesVisibleFeaturesThroughTheCatalogSnapshot() {
    Ircv3ExtensionProvider provider =
        provider(
            "history",
            10,
            capability("draft/chathistory", "draft/chathistory", "chathistory", List.of()),
            new Ircv3FeatureContribution(
                10,
                "History",
                List.of("message-tags"),
                List.of("chathistory", "draft/chathistory")));

    Ircv3FeatureAvailabilityEvaluator.Evaluation evaluation =
        Ircv3ExtensionMetadataCatalog.snapshot(List.of(provider))
            .evaluateVisibleFeatures(List.of("message-tags", "draft/chathistory"))
            .getFirst();

    assertEquals(Ircv3FeatureAvailabilityEvaluator.Readiness.READY, evaluation.readiness());
    assertEquals(List.of(), evaluation.missingRequiredAll());
    assertEquals(List.of(), evaluation.missingRequiredAny());
  }

  @Test
  void nonRequestableAndUnknownNamesKeepCompatibilitySemantics() {
    Ircv3ExtensionContribution tagFeature =
        new Ircv3ExtensionContribution(
            "typing",
            Ircv3ExtensionKind.TAG_FEATURE,
            Ircv3SpecStatus.DRAFT,
            List.of("draft/typing"),
            "",
            "typing",
            new Ircv3UiMetadata("Typing", Ircv3UiGroup.CONVERSATION, 10, "Typing status."));

    Ircv3ExtensionMetadataCatalog.Snapshot snapshot =
        Ircv3ExtensionMetadataCatalog.snapshot(List.of(provider("typing", 10, tagFeature, null)));

    assertEquals("", snapshot.normalizeRequestToken("draft/typing"));
    assertEquals("typing", snapshot.normalizePreferenceKey("draft/typing"));
    assertEquals("example/custom", snapshot.normalizeRequestToken(" Example/Custom "));
    assertEquals("example/custom", snapshot.normalizePreferenceKey(" Example/Custom "));
    assertEquals("", snapshot.normalizeRequestToken("  "));
    assertEquals(null, snapshot.normalizePreferenceKey("  "));
  }

  @Test
  void rejectsBlankAndConflictingProviderIds() {
    assertThrows(
        IllegalStateException.class,
        () -> Ircv3ExtensionMetadataCatalog.snapshot(List.of(provider(" ", 10, null, null))));

    IllegalStateException conflict =
        assertThrows(
            IllegalStateException.class,
            () ->
                Ircv3ExtensionMetadataCatalog.snapshot(
                    List.of(
                        provider("duplicate", 10, null, null),
                        new DifferentProvider("DUPLICATE"))));

    assertEquals(
        "Duplicate IRCv3 extension provider id registered: duplicate", conflict.getMessage());
  }

  @Test
  void rejectsConflictingExtensionNamesAndVisibleFeatureLabels() {
    Ircv3ExtensionProvider first =
        provider(
            "first",
            10,
            capability("first", "shared", "first", List.of()),
            new Ircv3FeatureContribution(10, "Shared", List.of("first"), List.of()));
    Ircv3ExtensionProvider second =
        provider(
            "second",
            20,
            capability("second", "shared", "second", List.of()),
            new Ircv3FeatureContribution(20, "Second", List.of("second"), List.of()));

    IllegalStateException nameConflict =
        assertThrows(
            IllegalStateException.class,
            () -> Ircv3ExtensionMetadataCatalog.snapshot(List.of(first, second)));
    assertEquals("Duplicate IRCv3 extension name registered: shared", nameConflict.getMessage());

    Ircv3ExtensionProvider uniqueSecond =
        provider(
            "second",
            20,
            capability("second", "second", "second", List.of()),
            new Ircv3FeatureContribution(20, "shared", List.of("second"), List.of()));
    IllegalStateException labelConflict =
        assertThrows(
            IllegalStateException.class,
            () -> Ircv3ExtensionMetadataCatalog.snapshot(List.of(first, uniqueSecond)));
    assertEquals(
        "Duplicate IRCv3 visible feature label registered: shared", labelConflict.getMessage());
  }

  private static Ircv3ExtensionProvider provider(
      String id,
      int sortOrder,
      Ircv3ExtensionContribution extension,
      Ircv3FeatureContribution feature) {
    return new Ircv3ExtensionProvider() {
      @Override
      public String providerId() {
        return id;
      }

      @Override
      public int sortOrder() {
        return sortOrder;
      }

      @Override
      public List<Ircv3ExtensionContribution> extensions() {
        return extension == null ? List.of() : List.of(extension);
      }

      @Override
      public List<Ircv3FeatureContribution> visibleFeatures() {
        return feature == null ? List.of() : List.of(feature);
      }
    };
  }

  private static Ircv3ExtensionContribution capability(
      String id, String requestToken, String preferenceKey, List<String> aliases) {
    return new Ircv3ExtensionContribution(
        id,
        Ircv3ExtensionKind.CAPABILITY,
        Ircv3SpecStatus.DRAFT,
        aliases,
        requestToken,
        preferenceKey,
        new Ircv3UiMetadata(id, Ircv3UiGroup.OTHER, 10, "Test capability."));
  }

  private record DifferentProvider(String providerId) implements Ircv3ExtensionProvider {
    @Override
    public int sortOrder() {
      return 20;
    }
  }
}
