package cafe.woden.ircclient.irc.ircv3;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_READ_MARKER;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.READ_MARKER;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import com.google.auto.service.AutoService;
import java.util.List;

/** SPI provider for the IRCv3 read-marker draft extension. */
@AutoService(Ircv3ExtensionProvider.class)
public final class Ircv3ReadMarkerExtensionProvider implements Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return READ_MARKER;
  }

  @Override
  public int sortOrder() {
    return 200;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            READ_MARKER,
            Ircv3SpecStatus.DRAFT,
            DRAFT_READ_MARKER,
            READ_MARKER,
            "Read markers (draft)",
            Ircv3UiGroup.CONVERSATION,
            240,
            "Enables read-position markers on servers that support them.",
            DRAFT_READ_MARKER));
  }

  @Override
  public List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of(
        Ircv3ExtensionProviderSupport.feature(
            700, "Read markers", List.of(), List.of(READ_MARKER, DRAFT_READ_MARKER)));
  }
}
