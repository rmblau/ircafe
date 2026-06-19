package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import com.google.auto.service.AutoService;
import java.util.List;

/** SPI provider for the IRCv3 multiline draft extension. */
@AutoService(Ircv3ExtensionProvider.class)
public final class Ircv3MultilineExtensionProvider implements Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return Ircv3CapabilityNames.MULTILINE;
  }

  @Override
  public int sortOrder() {
    return 210;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            Ircv3CapabilityNames.MULTILINE,
            Ircv3SpecStatus.DRAFT,
            Ircv3CapabilityNames.DRAFT_MULTILINE,
            Ircv3CapabilityNames.MULTILINE,
            "Multiline messages (draft)",
            Ircv3UiGroup.CONVERSATION,
            220,
            "Allows sending and receiving multiline messages as a single logical message.",
            Ircv3CapabilityNames.DRAFT_MULTILINE));
  }
}
