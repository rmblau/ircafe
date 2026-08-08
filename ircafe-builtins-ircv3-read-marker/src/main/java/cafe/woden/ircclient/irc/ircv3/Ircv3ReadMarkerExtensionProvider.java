package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
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

/** Built-in metadata and runtime provider for the IRCv3 read-marker draft capability. */
@AutoService({
  Ircv3ExtensionProvider.class,
  Ircv3OutboundCommandProvider.class,
  Ircv3InboundTagSignalProvider.class,
  Ircv3InboundCommandSignalProvider.class
})
public final class Ircv3ReadMarkerExtensionProvider
    implements Ircv3ExtensionProvider,
        Ircv3OutboundCommandProvider,
        Ircv3InboundTagSignalProvider,
        Ircv3InboundCommandSignalProvider {

  private static final String CAPABILITY = "read-marker";
  private static final String DRAFT_CAPABILITY = "draft/read-marker";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 200;
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
                "Read markers (draft)",
                Ircv3UiGroup.CONVERSATION,
                240,
                "Enables read-position markers on servers that support them.")));
  }

  @Override
  public Set<Ircv3OutboundCommandOperation> operations() {
    return Set.of(Ircv3OutboundCommandOperation.READ_MARKER);
  }

  @Override
  public List<String> build(
      Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
    if (operation != Ircv3OutboundCommandOperation.READ_MARKER || request == null) {
      return List.of();
    }
    return List.of(
        Ircv3ReadMarkerCommandBuilder.buildTimestampRawLine(
            request.target(), request.timestamp()));
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.READ_MARKER);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.READ_MARKER || request == null) {
      return List.of();
    }
    return Ircv3ReadMarkerTagSignal.fromTags(request.tags())
        .map(
            signal ->
                List.of(
                    Ircv3InboundTagSignal.of(
                        Ircv3InboundTagSignalType.READ_MARKER, signal.marker())))
        .orElseGet(List::of);
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(Ircv3InboundCommandOperation.READ_MARKER);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation != Ircv3InboundCommandOperation.READ_MARKER || request == null) {
      return List.of();
    }
    return Ircv3ReadMarkerCommandSignal.parse(request.command(), request.parameters())
        .map(
            signal ->
                List.<Ircv3InboundCommandSignal>of(
                    new Ircv3InboundCommandSignal.ReadMarkerObserved(
                        signal.target(), signal.marker())))
        .orElseGet(List::of);
  }

  @Override
  public List<Ircv3FeatureContribution> visibleFeatures() {
    return List.of(
        new Ircv3FeatureContribution(
            700, "Read markers", List.of(), List.of(CAPABILITY, DRAFT_CAPABILITY)));
  }
}
