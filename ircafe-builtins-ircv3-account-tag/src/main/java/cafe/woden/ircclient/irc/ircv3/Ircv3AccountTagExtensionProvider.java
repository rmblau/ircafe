package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;

/** Built-in metadata and runtime provider for the IRCv3 account-tag capability. */
@AutoService({Ircv3ExtensionProvider.class, Ircv3InboundTagSignalProvider.class})
public final class Ircv3AccountTagExtensionProvider
    implements Ircv3ExtensionProvider, Ircv3InboundTagSignalProvider {

  private static final String CAPABILITY = "account-tag";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 135;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        new Ircv3ExtensionContribution(
            CAPABILITY,
            Ircv3ExtensionKind.CAPABILITY,
            Ircv3SpecStatus.STABLE,
            List.of(),
            CAPABILITY,
            CAPABILITY,
            new Ircv3UiMetadata(
                "Account tags",
                Ircv3UiGroup.CORE,
                70,
                "Attaches account metadata to messages for richer identity info.")));
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.ACCOUNT_TAG);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.ACCOUNT_TAG || request == null) {
      return List.of();
    }
    return Ircv3AccountTagSignal.fromTags(request.sourceNick(), request.tags())
        .map(
            signal ->
                List.of(
                    new Ircv3InboundTagSignal(
                        Ircv3InboundTagSignalType.ACCOUNT_TAG, signal.nick(), signal.rawAccount())))
        .orElseGet(List::of);
  }
}
