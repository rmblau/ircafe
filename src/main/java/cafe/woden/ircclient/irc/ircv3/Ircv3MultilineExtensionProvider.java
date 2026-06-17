package cafe.woden.ircclient.irc.ircv3;

import static cafe.woden.ircclient.util.Ircv3CapabilityNames.DRAFT_MULTILINE;
import static cafe.woden.ircclient.util.Ircv3CapabilityNames.MULTILINE;

import java.util.List;

/** SPI provider for the IRCv3 multiline draft extension. */
public final class Ircv3MultilineExtensionProvider
    implements cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return MULTILINE;
  }

  @Override
  public int sortOrder() {
    return 210;
  }

  @Override
  public List<Ircv3ExtensionRegistry.ExtensionDefinition> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            MULTILINE,
            Ircv3ExtensionRegistry.SpecStatus.DRAFT,
            DRAFT_MULTILINE,
            MULTILINE,
            "Multiline messages (draft)",
            Ircv3ExtensionRegistry.UiGroup.CONVERSATION,
            220,
            "Allows sending and receiving multiline messages as a single logical message.",
            DRAFT_MULTILINE));
  }
}
