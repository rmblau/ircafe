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

/** Built-in metadata and runtime provider for the IRCv3 echo-message capability. */
@AutoService({Ircv3ExtensionProvider.class, Ircv3InboundTagSignalProvider.class})
public final class Ircv3EchoMessageExtensionProvider
    implements Ircv3ExtensionProvider, Ircv3InboundTagSignalProvider {

  private static final String CAPABILITY = "echo-message";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 111;
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
                "Echo own messages",
                Ircv3UiGroup.CORE,
                40,
                "Server echoes your outbound messages, improving multi-client/bouncer consistency.")));
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.ECHO_MESSAGE_TARGET_HINT);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.ECHO_MESSAGE_TARGET_HINT || request == null) {
      return List.of();
    }

    return Ircv3EchoMessageTargetHintPlanner.plan(
            request.sourceNick(),
            request.rawTarget(),
            request.command(),
            request.rawLine(),
            request.parameters(),
            request.tags(),
            request.selfNickAliases())
        .map(
            hint ->
                List.of(
                    new Ircv3InboundTagSignal(
                        Ircv3InboundTagSignalType.ECHO_MESSAGE_TARGET_HINT,
                        hint.target(),
                        hint.messageId()),
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.ECHO_MESSAGE_KIND, hint.kind()),
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.ECHO_MESSAGE_PAYLOAD, hint.payload())))
        .orElseGet(List::of);
  }
}
