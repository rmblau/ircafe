package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import java.util.List;

/** Shared factory helpers for built-in IRCv3 extension definition providers. */
final class Ircv3ExtensionProviderSupport {

  private Ircv3ExtensionProviderSupport() {}

  static Ircv3ExtensionContribution capability(
      String id,
      Ircv3SpecStatus specStatus,
      String label,
      Ircv3UiGroup group,
      int sortOrder,
      String impactSummary,
      String... aliases) {
    return capability(id, specStatus, id, id, label, group, sortOrder, impactSummary, aliases);
  }

  static Ircv3ExtensionContribution capability(
      String id,
      Ircv3SpecStatus specStatus,
      String requestToken,
      String preferenceKey,
      String label,
      Ircv3UiGroup group,
      int sortOrder,
      String impactSummary,
      String... aliases) {
    return new Ircv3ExtensionContribution(
        id,
        Ircv3ExtensionKind.CAPABILITY,
        specStatus,
        List.of(aliases),
        requestToken,
        preferenceKey,
        new Ircv3UiMetadata(label, group, sortOrder, impactSummary));
  }

  static Ircv3ExtensionContribution nonRequestableCapability(
      String id,
      Ircv3SpecStatus specStatus,
      String label,
      Ircv3UiGroup group,
      int sortOrder,
      String impactSummary,
      String... aliases) {
    return new Ircv3ExtensionContribution(
        id,
        Ircv3ExtensionKind.CAPABILITY,
        specStatus,
        List.of(aliases),
        "",
        id,
        new Ircv3UiMetadata(label, group, sortOrder, impactSummary));
  }

  static Ircv3ExtensionContribution tagFeature(
      String id,
      Ircv3SpecStatus specStatus,
      String label,
      Ircv3UiGroup group,
      int sortOrder,
      String impactSummary,
      String... aliases) {
    return new Ircv3ExtensionContribution(
        id,
        Ircv3ExtensionKind.TAG_FEATURE,
        specStatus,
        List.of(aliases),
        "",
        id,
        new Ircv3UiMetadata(label, group, sortOrder, impactSummary));
  }

  static Ircv3ExtensionContribution experimental(
      String id,
      String label,
      Ircv3UiGroup group,
      int sortOrder,
      String impactSummary,
      String... aliases) {
    return new Ircv3ExtensionContribution(
        id,
        Ircv3ExtensionKind.EXPERIMENTAL,
        Ircv3SpecStatus.EXPERIMENTAL,
        List.of(aliases),
        "",
        id,
        new Ircv3UiMetadata(label, group, sortOrder, impactSummary));
  }

  static Ircv3FeatureContribution feature(
      int sortOrder, String label, List<String> requiredAll, List<String> requiredAny) {
    return new Ircv3FeatureContribution(sortOrder, label, requiredAll, requiredAny);
  }
}
