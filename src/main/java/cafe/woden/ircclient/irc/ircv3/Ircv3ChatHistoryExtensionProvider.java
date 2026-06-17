package cafe.woden.ircclient.irc.ircv3;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.CHATHISTORY;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_CHATHISTORY;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.ZNC_PLAYBACK;

import java.util.List;

/** SPI provider for the IRCv3 chathistory draft extension. */
public final class Ircv3ChatHistoryExtensionProvider
    implements cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return CHATHISTORY;
  }

  @Override
  public int sortOrder() {
    return 240;
  }

  @Override
  public List<Ircv3ExtensionRegistry.ExtensionDefinition> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            CHATHISTORY,
            Ircv3ExtensionRegistry.SpecStatus.DRAFT,
            DRAFT_CHATHISTORY,
            CHATHISTORY,
            "Chat history (draft)",
            Ircv3ExtensionRegistry.UiGroup.HISTORY,
            430,
            "Enables server-side history retrieval and backfill features.",
            DRAFT_CHATHISTORY));
  }

  @Override
  public List<Ircv3ExtensionRegistry.FeatureDefinition> visibleFeatures() {
    return List.of(
        Ircv3ExtensionProviderSupport.feature(
            500, "History", List.of(), List.of(CHATHISTORY, DRAFT_CHATHISTORY, ZNC_PLAYBACK)));
  }
}
