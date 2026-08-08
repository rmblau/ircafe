package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;

/** Built-in metadata and runtime provider for IRCv3 labeled-response. */
@AutoService({
  Ircv3ExtensionProvider.class,
  Ircv3OutboundCommandProvider.class,
  Ircv3InboundTagSignalProvider.class
})
public final class Ircv3LabeledResponseExtensionProvider
    implements Ircv3ExtensionProvider,
        Ircv3OutboundCommandProvider,
        Ircv3InboundTagSignalProvider {

  private static final String CAPABILITY = "labeled-response";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 115;
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
                "Labeled responses",
                Ircv3UiGroup.CORE,
                50,
                "Correlates command responses with requests more reliably.")));
  }

  @Override
  public Set<Ircv3OutboundCommandOperation> operations() {
    return Set.of(Ircv3OutboundCommandOperation.LABELED_RESPONSE);
  }

  @Override
  public List<String> build(
      Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
    if (operation != Ircv3OutboundCommandOperation.LABELED_RESPONSE || request == null) {
      return List.of();
    }
    Ircv3LabeledResponseRawLinePreparer.PreparedRawLine prepared =
        Ircv3LabeledResponseRawLinePreparer.prepare(
            request.payload(),
            () ->
                Ircv3LabeledResponseValues.generateClientLabel(
                    request.serverId(), request.sequence()));
    return prepared.line().isBlank() ? List.of() : List.of(prepared.line());
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.LABELED_RESPONSE);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.LABELED_RESPONSE || request == null) {
      return List.of();
    }
    return Ircv3LabeledResponseTagSignal.fromTags(request.tags())
        .or(() -> Ircv3LabeledResponseTagSignal.fromRawLine(request.rawLine()))
        .map(
            label ->
                List.of(
                    new Ircv3InboundTagSignal(
                        Ircv3InboundTagSignalType.LABELED_RESPONSE,
                        label,
                        Ircv3LabeledResponseTagSignal.outcomeForStandardReply(request.command())
                            .name())))
        .orElseGet(List::of);
  }
}
