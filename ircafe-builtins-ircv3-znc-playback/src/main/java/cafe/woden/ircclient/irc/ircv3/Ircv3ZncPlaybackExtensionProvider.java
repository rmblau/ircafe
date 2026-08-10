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
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;

/** Built-in metadata and runtime provider for ZNC playback integration. */
@AutoService({
  Ircv3ExtensionProvider.class,
  Ircv3InboundCommandSignalProvider.class,
  Ircv3InboundTagSignalProvider.class,
  Ircv3OutboundCommandProvider.class
})
public final class Ircv3ZncPlaybackExtensionProvider
    implements Ircv3ExtensionProvider,
        Ircv3InboundCommandSignalProvider,
        Ircv3InboundTagSignalProvider,
        Ircv3OutboundCommandProvider {

  private static final String CAPABILITY = "znc.in/playback";

  @Override
  public String providerId() {
    return "znc-playback";
  }

  @Override
  public int sortOrder() {
    return 231;
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
                "ZNC playback",
                Ircv3UiGroup.HISTORY,
                440,
                "Requests playback support from ZNC bouncers when available.")));
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(
        Ircv3InboundCommandOperation.HISTORY_ZNC_CAPABILITY,
        Ircv3InboundCommandOperation.HISTORY_ZNC_RPL004);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation == null || request == null) {
      return List.of();
    }
    return switch (operation) {
      case HISTORY_ZNC_CAPABILITY -> parseCapability(request);
      case HISTORY_ZNC_RPL004 -> parseRpl004(request);
      default -> List.of();
    };
  }

  private static List<Ircv3InboundCommandSignal> parseCapability(
      Ircv3InboundCommandRequest request) {
    for (String parameter : request.parameters()) {
      String capability = parameter == null ? "" : parameter.trim();
      if (Ircv3ZncDetector.seemsZncCapability(capability)) {
        return List.of(new Ircv3InboundCommandSignal.ZncDetectedObserved("CAP", capability));
      }
    }
    return List.of();
  }

  private static List<Ircv3InboundCommandSignal> parseRpl004(Ircv3InboundCommandRequest request) {
    if (!Ircv3ZncDetector.seemsRpl004Znc(request.rawLine())) {
      return List.of();
    }
    return List.of(
        new Ircv3InboundCommandSignal.ZncDetectedObserved("RPL_MYINFO/004", request.rawLine()));
  }

  @Override
  public Set<Ircv3InboundTagOperation> inboundTagOperations() {
    return Set.of(Ircv3InboundTagOperation.HISTORY_BOOTSTRAP_SUPPRESSION);
  }

  @Override
  public List<Ircv3InboundTagSignal> parse(
      Ircv3InboundTagOperation operation, Ircv3InboundTagRequest request) {
    if (operation != Ircv3InboundTagOperation.HISTORY_BOOTSTRAP_SUPPRESSION || request == null) {
      return List.of();
    }
    String message = request.parameters().isEmpty() ? "" : request.parameters().getFirst();
    if (!Ircv3HistoryBootstrapSuppressionPolicy.shouldSuppress(
        request.selfAuthored(), request.rawTarget(), message)) {
      return List.of();
    }
    return List.of(
        Ircv3InboundTagSignal.of(Ircv3InboundTagSignalType.HISTORY_BOOTSTRAP_SUPPRESSED, "true"));
  }

  @Override
  public Set<Ircv3OutboundCommandOperation> operations() {
    return Set.of(Ircv3OutboundCommandOperation.ZNC_PLAYBACK);
  }

  @Override
  public List<String> build(
      Ircv3OutboundCommandOperation operation, Ircv3OutboundCommandRequest request) {
    if (operation != Ircv3OutboundCommandOperation.ZNC_PLAYBACK || request == null) {
      return List.of();
    }
    Ircv3ZncPlaybackRequestPlanner.Plan plan =
        new Ircv3ZncPlaybackRequestPlanner()
            .plan(request.target(), request.timestamp(), request.secondaryTimestamp());
    return List.of(plan.renderCommand(plan.target()));
  }
}
