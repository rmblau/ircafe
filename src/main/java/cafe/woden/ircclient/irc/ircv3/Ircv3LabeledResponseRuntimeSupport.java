package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignalType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Application adapter for runtime-selected labeled-response interpretation. */
@Component
@InfrastructureLayer
public final class Ircv3LabeledResponseRuntimeSupport {

  private final Ircv3InboundTagSignalRuntimeCatalog inboundTagRuntimeCatalog;

  @Autowired
  public Ircv3LabeledResponseRuntimeSupport(
      Ircv3InboundTagSignalRuntimeCatalog inboundTagRuntimeCatalog) {
    this.inboundTagRuntimeCatalog = Objects.requireNonNull(inboundTagRuntimeCatalog);
  }

  public Optional<Observation> fromTags(String command, Map<String, String> tags) {
    return observe(new Ircv3InboundTagRequest(command, "", "", List.of(), tags, ""));
  }

  public Optional<Observation> fromRawLine(String command, String rawLine) {
    return observe(new Ircv3InboundTagRequest(command, "", "", List.of(), Map.of(), rawLine));
  }

  private Optional<Observation> observe(Ircv3InboundTagRequest request) {
    for (Ircv3InboundTagSignal signal :
        inboundTagRuntimeCatalog.parse(Ircv3InboundTagOperation.LABELED_RESPONSE, request)) {
      if (signal.type() != Ircv3InboundTagSignalType.LABELED_RESPONSE
          || signal.primaryValue().isBlank()) {
        continue;
      }
      return Optional.of(
          new Observation(signal.primaryValue(), Outcome.from(signal.secondaryValue())));
    }
    return Optional.empty();
  }

  public record Observation(String label, Outcome outcome) {
    public Observation {
      label = Objects.toString(label, "").trim();
      outcome = Objects.requireNonNullElse(outcome, Outcome.SUCCESS);
    }
  }

  public enum Outcome {
    SUCCESS,
    FAILURE;

    private static Outcome from(String raw) {
      return "FAILURE".equalsIgnoreCase(Objects.toString(raw, "")) ? FAILURE : SUCCESS;
    }
  }
}
