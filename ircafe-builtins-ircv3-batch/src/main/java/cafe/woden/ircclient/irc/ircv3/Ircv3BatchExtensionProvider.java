package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
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

/** Built-in metadata and runtime provider for the IRCv3 BATCH capability. */
@AutoService({
  Ircv3ExtensionProvider.class,
  Ircv3InboundCommandSignalProvider.class,
  Ircv3InboundTagSignalProvider.class
})
public final class Ircv3BatchExtensionProvider
    implements Ircv3ExtensionProvider,
        Ircv3InboundCommandSignalProvider,
        Ircv3InboundTagSignalProvider {

  private static final String CAPABILITY = "batch";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 230;
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
                "batch",
                Ircv3UiGroup.HISTORY,
                410,
                "Groups related events into coherent batches (useful for playback/history).")));
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(Ircv3InboundCommandOperation.HISTORY_BATCH_CONTROL);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation != Ircv3InboundCommandOperation.HISTORY_BATCH_CONTROL || request == null) {
      return List.of();
    }
    Ircv3HistoryBatchControlParser.Result parsed =
        Ircv3HistoryBatchControlParser.parse(request.rawLine());
    if (!parsed.batchCommand()) {
      return List.of();
    }
    if (parsed.control() instanceof Ircv3HistoryBatchControlParser.Start start) {
      return List.of(
          new Ircv3InboundCommandSignal.HistoryBatchStarted(
              start.batchId(), start.type(), start.target()));
    }
    if (parsed.control() instanceof Ircv3HistoryBatchControlParser.End end) {
      return List.of(new Ircv3InboundCommandSignal.HistoryBatchEnded(end.batchId()));
    }
    return List.of(new Ircv3InboundCommandSignal.HistoryBatchIgnored());
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.HISTORY_BATCH_REFERENCE);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.HISTORY_BATCH_REFERENCE || request == null) {
      return List.of();
    }
    return Ircv3BatchTag.fromTags(request.tags())
        .map(
            batchId ->
                List.of(
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.HISTORY_BATCH_REFERENCE, batchId)))
        .orElseGet(List::of);
  }
}
