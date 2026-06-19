package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import com.google.auto.service.AutoService;
import java.util.List;

/** SPI provider for the IRCv3 message-redaction draft extension. */
@AutoService(Ircv3ExtensionProvider.class)
public final class Ircv3MessageRedactionExtensionProvider implements Ircv3ExtensionProvider {

  @Override
  public String providerId() {
    return Ircv3CapabilityNames.MESSAGE_REDACTION;
  }

  @Override
  public int sortOrder() {
    return 220;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        Ircv3ExtensionProviderSupport.capability(
            Ircv3CapabilityNames.MESSAGE_REDACTION,
            Ircv3SpecStatus.DRAFT,
            Ircv3CapabilityNames.DRAFT_MESSAGE_REDACTION,
            Ircv3CapabilityNames.MESSAGE_REDACTION,
            "Message redaction (draft)",
            Ircv3UiGroup.CONVERSATION,
            300,
            "Allows delete/redaction updates for messages.",
            Ircv3CapabilityNames.DRAFT_MESSAGE_REDACTION));
  }

  @Override
  public List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of(
        Ircv3ExtensionProviderSupport.feature(
            400,
            "Message redaction",
            List.of(),
            List.of(Ircv3CapabilityNames.MESSAGE_REDACTION, Ircv3CapabilityNames.DRAFT_MESSAGE_REDACTION)));
  }
}
