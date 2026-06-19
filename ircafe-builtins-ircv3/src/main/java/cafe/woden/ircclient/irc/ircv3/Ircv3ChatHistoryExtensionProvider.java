package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import com.google.auto.service.AutoService;
import java.util.List;

/** SPI provider for the IRCv3 chathistory draft extension. */
@AutoService(Ircv3ExtensionProvider.class)
public final class Ircv3ChatHistoryExtensionProvider implements Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return Ircv3CapabilityNames.CHATHISTORY;
  }

  @Override
  public int sortOrder() {
    return 240;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            Ircv3CapabilityNames.CHATHISTORY,
            Ircv3SpecStatus.DRAFT,
            Ircv3CapabilityNames.DRAFT_CHATHISTORY,
            Ircv3CapabilityNames.CHATHISTORY,
            "Chat history (draft)",
            Ircv3UiGroup.HISTORY,
            430,
            "Enables server-side history retrieval and backfill features.",
            Ircv3CapabilityNames.DRAFT_CHATHISTORY));
  }

  @Override
  public List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of(
        Ircv3ExtensionProviderSupport.feature(
            500, "History", List.of(), List.of(Ircv3CapabilityNames.CHATHISTORY, Ircv3CapabilityNames.DRAFT_CHATHISTORY, Ircv3CapabilityNames.ZNC_PLAYBACK)));
  }
}
