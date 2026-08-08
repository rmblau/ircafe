package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.StsPolicyObserved;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Validates plugin-provided STS policy observations at the application boundary. */
final class Ircv3StsRuntimeSupport {

  private final Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog;

  Ircv3StsRuntimeSupport(Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog) {
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
  }

  List<Ircv3StsPolicyLearningPlanner.Decision> observe(
      String host, boolean secureConnection, String capListRaw, long observedAtEpochMilli) {
    String expectedHost = Ircv3StsPolicy.normalizeHost(host);
    Ircv3InboundCommandRequest request =
        new Ircv3InboundCommandRequest(
            "",
            "CAP",
            capListRaw,
            List.of(),
            Map.of(),
            expectedHost,
            secureConnection,
            observedAtEpochMilli);

    List<Ircv3InboundCommandSignal> signals =
        runtimeCatalog.parse(Ircv3InboundCommandOperation.STS_CAPABILITY, request);
    if (signals.isEmpty()) {
      return List.of();
    }

    ArrayList<Ircv3StsPolicyLearningPlanner.Decision> decisions =
        new ArrayList<>(signals.size());
    for (Ircv3InboundCommandSignal signal : signals) {
      if (signal instanceof StsPolicyObserved observed) {
        toDecision(expectedHost, observedAtEpochMilli, observed).ifPresent(decisions::add);
      }
    }
    return List.copyOf(decisions);
  }

  private static Optional<Ircv3StsPolicyLearningPlanner.Decision> toDecision(
      String expectedHost, long observedAtEpochMilli, StsPolicyObserved observed) {
    Ircv3StsPolicyLearningPlanner.Outcome outcome;
    try {
      outcome = Ircv3StsPolicyLearningPlanner.Outcome.valueOf(observed.outcome().name());
    } catch (RuntimeException ignored) {
      return Optional.empty();
    }

    String hostLower = Ircv3StsPolicy.normalizeHost(observed.host());
    if (outcome != Ircv3StsPolicyLearningPlanner.Outcome.IGNORE_MISSING_HOST) {
      if (hostLower.isEmpty() || !hostLower.equals(expectedHost)) {
        return Optional.empty();
      }
    }

    String rawValue = Objects.toString(observed.rawValue(), "").trim();
    if (outcome == Ircv3StsPolicyLearningPlanner.Outcome.LEARN) {
      Integer port = observed.port();
      if (observed.durationSeconds() <= 0L
          || observed.expiresAtEpochMilli() <= observedAtEpochMilli
          || (port != null && (port <= 0 || port > 65_535))
          || rawValue.isEmpty()) {
        return Optional.empty();
      }
      try {
        Ircv3StsPolicy policy =
            new Ircv3StsPolicy(
                hostLower,
                observed.expiresAtEpochMilli(),
                port,
                observed.preload(),
                observed.durationSeconds(),
                rawValue);
        return Optional.of(
            new Ircv3StsPolicyLearningPlanner.Decision(
                outcome, hostLower, rawValue, Optional.of(policy)));
      } catch (RuntimeException ignored) {
        return Optional.empty();
      }
    }

    return Optional.of(
        new Ircv3StsPolicyLearningPlanner.Decision(
            outcome, hostLower, rawValue, Optional.empty()));
  }
}
