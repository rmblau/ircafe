package cafe.woden.ircclient.irc.ircv3;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.BATCH;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import com.google.auto.service.AutoService;
import java.util.List;

/** Built-in provider for core history-related IRCv3 transport capabilities. */
@AutoService(Ircv3ExtensionProvider.class)
public final class Ircv3CoreHistoryExtensionProvider implements Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return "core-history";
  }

  @Override
  public int sortOrder() {
    return 230;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            BATCH,
            Ircv3SpecStatus.STABLE,
            BATCH,
            Ircv3UiGroup.HISTORY,
            410,
            "Groups related events into coherent batches (useful for playback/history)."));
  }
}
