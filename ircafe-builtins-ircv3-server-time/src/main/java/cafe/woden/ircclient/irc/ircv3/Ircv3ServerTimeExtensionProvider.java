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

/** Built-in metadata and runtime provider for the IRCv3 server-time capability. */
@AutoService({Ircv3ExtensionProvider.class, Ircv3InboundTagSignalProvider.class})
public final class Ircv3ServerTimeExtensionProvider
    implements Ircv3ExtensionProvider, Ircv3InboundTagSignalProvider {

  private static final String CAPABILITY = "server-time";

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 110;
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
                "Server timestamps",
                Ircv3UiGroup.CORE,
                30,
                "Uses server-provided timestamps to improve ordering and replay accuracy.")));
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.SERVER_TIME, Ircv3InboundTagOperation.SERVER_TIME_LAG);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation == null || request == null) {
      return List.of();
    }
    return switch (operation) {
      case SERVER_TIME ->
          Ircv3ServerTime.fromTagsOrRawLine(request.tags(), request.rawLine())
              .map(
                  instant ->
                      List.of(
                          Ircv3InboundTagSignal.of(
                              Ircv3InboundTagSignalType.SERVER_TIME, instant.toString())))
              .orElseGet(List::of);
      case SERVER_TIME_LAG ->
          Ircv3ServerTime.fromTagsOrRawLine(request.tags(), request.rawLine())
              .flatMap(
                  instant -> Ircv3ServerTimeLagSample.from(instant, request.observedAtEpochMilli()))
              .map(
                  sample ->
                      List.of(
                          new Ircv3InboundTagSignal(
                              Ircv3InboundTagSignalType.SERVER_TIME_LAG,
                              Long.toString(sample.lagMs()),
                              Long.toString(sample.observedAtMs()))))
              .orElseGet(List::of);
      default -> List.of();
    };
  }
}
