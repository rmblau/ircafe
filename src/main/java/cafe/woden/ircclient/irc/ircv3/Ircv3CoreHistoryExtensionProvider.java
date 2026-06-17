package cafe.woden.ircclient.irc.ircv3;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.BATCH;

import java.util.List;

/** Built-in provider for core history-related IRCv3 transport capabilities. */
public final class Ircv3CoreHistoryExtensionProvider
    implements cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return "core-history";
  }

  @Override
  public int sortOrder() {
    return 230;
  }

  @Override
  public List<Ircv3ExtensionRegistry.ExtensionDefinition> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            BATCH,
            Ircv3ExtensionRegistry.SpecStatus.STABLE,
            BATCH,
            Ircv3ExtensionRegistry.UiGroup.HISTORY,
            410,
            "Groups related events into coherent batches (useful for playback/history)."));
  }
}
