package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;

/** Built-in metadata and runtime provider for the IRCv3 multiline draft capability. */
@AutoService({
  Ircv3ExtensionProvider.class,
  Ircv3OutboundCommandProvider.class,
  Ircv3InboundCommandSignalProvider.class
})
public final class Ircv3MultilineExtensionProvider
    implements Ircv3ExtensionProvider,
        Ircv3OutboundCommandProvider,
        Ircv3InboundCommandSignalProvider {

  private static final String CAPABILITY = "multiline";
  private static final String DRAFT_CAPABILITY = "draft/multiline";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 210;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        new Ircv3ExtensionContribution(
            CAPABILITY,
            Ircv3ExtensionKind.CAPABILITY,
            Ircv3SpecStatus.DRAFT,
            List.of(DRAFT_CAPABILITY),
            DRAFT_CAPABILITY,
            CAPABILITY,
            new Ircv3UiMetadata(
                "Multiline messages (draft)",
                Ircv3UiGroup.CONVERSATION,
                220,
                "Allows sending and receiving multiline messages as a single logical message.")));
  }

  @Override
  public Set<Ircv3OutboundCommandOperation> operations() {
    return Set.of(Ircv3OutboundCommandOperation.MULTILINE);
  }

  @Override
  public List<String> build(
      Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
    if (operation != Ircv3OutboundCommandOperation.MULTILINE || request == null) {
      return List.of();
    }
    return Ircv3MultilineCommandPlanner.plan(
            request.primaryValue(),
            request.target(),
            request.payload(),
            new Ircv3MultilineCommandPlanner.NegotiatedState(
                request.finalCapability(),
                request.draftCapability(),
                request.maxBytes(),
                request.maxLines()),
            request.batchId(),
            request.serverId())
        .rawLines();
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(Ircv3InboundCommandOperation.MULTILINE_CAPABILITY_STATE);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation != Ircv3InboundCommandOperation.MULTILINE_CAPABILITY_STATE
        || request == null
        || !"CAP".equalsIgnoreCase(request.command())
        || request.parameters().size() < 3) {
      return List.of();
    }

    Ircv3CapabilityLine line =
        Ircv3CapabilityLine.parse(request.parameters().get(1), request.parameters().get(2));
    if (!line.hasTokens() || !line.isAction("LS", "NEW", "ACK", "DEL")) {
      return List.of();
    }

    Ircv3InboundCommandRequest.MultilineState current = request.multilineState();
    Ircv3MultilineCapabilityStatePlanner.State prior =
        new Ircv3MultilineCapabilityStatePlanner.State(
            limits(
                current.finalOfferedMaxBytes(),
                current.finalOfferedMaxLines(),
                current.finalNegotiatedMaxBytes(),
                current.finalNegotiatedMaxLines()),
            limits(
                current.draftOfferedMaxBytes(),
                current.draftOfferedMaxLines(),
                current.draftNegotiatedMaxBytes(),
                current.draftNegotiatedMaxLines()));
    Ircv3MultilineCapabilityStatePlanner.State next =
        new Ircv3MultilineCapabilityStatePlanner().apply(line, prior);
    if (next.equals(prior)) {
      return List.of();
    }

    return List.of(toSignal(false, next.multiline()), toSignal(true, next.draftMultiline()));
  }

  private static Ircv3MultilineCapabilityStatePlanner.Limits limits(
      long offeredBytes, long offeredLines, long negotiatedBytes, long negotiatedLines) {
    return new Ircv3MultilineCapabilityStatePlanner.Limits(
        offeredBytes, offeredLines, negotiatedBytes, negotiatedLines);
  }

  private static Ircv3InboundCommandSignal.MultilineLimitsObserved toSignal(
      boolean draft, Ircv3MultilineCapabilityStatePlanner.Limits limits) {
    return new Ircv3InboundCommandSignal.MultilineLimitsObserved(
        draft,
        limits.offeredMaxBytes(),
        limits.offeredMaxLines(),
        limits.negotiatedMaxBytes(),
        limits.negotiatedMaxLines());
  }
}
