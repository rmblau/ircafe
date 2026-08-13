package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.StsPolicyObserved;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.StsPolicyOutcome;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Built-in metadata and runtime provider for IRCv3 strict transport security. */
@AutoService({Ircv3ExtensionProvider.class, Ircv3InboundCommandSignalProvider.class})
public final class Ircv3StsExtensionProvider
    implements Ircv3ExtensionProvider, Ircv3InboundCommandSignalProvider {

  private static final String CAPABILITY = "sts";
  private final Ircv3StsPolicyLearningPlanner learningPlanner = new Ircv3StsPolicyLearningPlanner();

  @Override
  public String providerId() {
    return CAPABILITY;
  }

  @Override
  public int sortOrder() {
    return 190;
  }

  @Override
  public List<Ircv3ExtensionContribution> extensions() {
    return List.of(
        new Ircv3ExtensionContribution(
            CAPABILITY,
            Ircv3ExtensionKind.CAPABILITY,
            Ircv3SpecStatus.STABLE,
            List.of(),
            "",
            CAPABILITY,
            new Ircv3UiMetadata(
                "Strict transport security",
                Ircv3UiGroup.CORE,
                20,
                "Learns strict transport policy and upgrades future connects for this host to TLS.")));
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(Ircv3InboundCommandOperation.STS_CAPABILITY);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation != Ircv3InboundCommandOperation.STS_CAPABILITY || request == null) {
      return List.of();
    }

    List<String> values = Ircv3StsPolicyParser.findStsValues(request.rawLine());
    if (values.isEmpty()) {
      return List.of();
    }

    ArrayList<Ircv3InboundCommandSignal> signals = new ArrayList<>(values.size());
    for (String value : values) {
      Ircv3StsPolicyLearningPlanner.Decision decision =
          learningPlanner.plan(
              request.connectionHost(),
              request.secureConnection(),
              value,
              request.observedAtEpochMilli());
      signals.add(toSignal(decision));
    }
    return List.copyOf(signals);
  }

  private static StsPolicyObserved toSignal(Ircv3StsPolicyLearningPlanner.Decision decision) {
    Ircv3StsPolicy policy = decision.policy().orElse(null);
    return new StsPolicyObserved(
        StsPolicyOutcome.valueOf(decision.outcome().name()),
        decision.hostLower(),
        decision.rawValue(),
        policy == null ? 0L : policy.expiresAtEpochMs(),
        policy == null ? null : policy.port(),
        policy != null && policy.preload(),
        policy == null ? 0L : policy.durationSeconds());
  }
}
