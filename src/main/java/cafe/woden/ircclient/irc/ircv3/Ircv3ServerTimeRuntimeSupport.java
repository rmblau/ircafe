package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Installed-provider-aware adapter for IRCv3 server-time interpretation. */
@Component
@InfrastructureLayer
public final class Ircv3ServerTimeRuntimeSupport {

  private final Ircv3InboundTagSignalRuntimeCatalog runtimeCatalog;
  private final Ircv3MessageTagsRuntimeSupport messageTagsRuntimeSupport;

  @Autowired
  public Ircv3ServerTimeRuntimeSupport(
      Ircv3InboundTagSignalRuntimeCatalog runtimeCatalog,
      Ircv3MessageTagsRuntimeSupport messageTagsRuntimeSupport) {
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
    this.messageTagsRuntimeSupport =
        Objects.requireNonNull(messageTagsRuntimeSupport, "messageTagsRuntimeSupport");
  }

  public Optional<Instant> resolve(Map<String, String> tags, String rawLine) {
    for (Ircv3InboundTagSignal signal :
        runtimeCatalog.parse(
            Ircv3InboundTagOperation.SERVER_TIME, request(tags, rawLine, 0L))) {
      if (signal.type() != Ircv3InboundTagSignalType.SERVER_TIME) {
        continue;
      }
      try {
        return Optional.of(Instant.parse(signal.primaryValue()));
      } catch (RuntimeException ignored) {
        // Installed providers are treated as untrusted input at the application boundary.
      }
    }
    return Optional.empty();
  }

  public Optional<Instant> resolveEvent(Object event) {
    return resolve(messageTagsRuntimeSupport.fromEvent(event), "");
  }

  public Instant resolveOrNow(Map<String, String> tags, String rawLine) {
    return resolve(tags, rawLine).orElseGet(Instant::now);
  }

  public Instant resolveRawLineOrNow(String rawLine) {
    return resolve(Map.of(), rawLine).orElseGet(Instant::now);
  }

  public Instant resolveEventOrNow(Object event) {
    return resolveEvent(event).orElseGet(Instant::now);
  }

  public Optional<LagObservation> passiveLag(
      Map<String, String> tags, String rawLine, long observedAtEpochMilli) {
    for (Ircv3InboundTagSignal signal :
        runtimeCatalog.parse(
            Ircv3InboundTagOperation.SERVER_TIME_LAG,
            request(tags, rawLine, observedAtEpochMilli))) {
      if (signal.type() != Ircv3InboundTagSignalType.SERVER_TIME_LAG) {
        continue;
      }
      try {
        long lagMs = Long.parseLong(signal.primaryValue());
        long observedAtMs = Long.parseLong(signal.secondaryValue());
        return Optional.of(new LagObservation(lagMs, observedAtMs));
      } catch (RuntimeException ignored) {
        // Ignore malformed output from an installed provider.
      }
    }
    return Optional.empty();
  }

  private static Ircv3InboundTagRequest request(
      Map<String, String> tags, String rawLine, long observedAtEpochMilli) {
    return new Ircv3InboundTagRequest(
        "",
        "",
        "",
        List.of(),
        Objects.requireNonNullElse(tags, Map.of()),
        rawLine,
        observedAtEpochMilli);
  }

  public record LagObservation(long lagMs, long observedAtMs) {
    public LagObservation {
      if (lagMs < 0L) {
        throw new IllegalArgumentException("lagMs must not be negative");
      }
      if (observedAtMs <= 0L) {
        throw new IllegalArgumentException("observedAtMs must be positive");
      }
    }
  }
}
