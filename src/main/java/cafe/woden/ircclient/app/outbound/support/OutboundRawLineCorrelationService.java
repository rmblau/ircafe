package cafe.woden.ircclient.app.outbound.support;

import cafe.woden.ircclient.app.outbound.backend.OutboundBackendCapabilityPolicy;
import cafe.woden.ircclient.irc.ircv3.Ircv3LabeledResponseRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3OutboundCommandRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.state.api.LabeledResponseRoutingPort;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Correlates outbound raw lines with labeled-response routing metadata when available. */
@Component
@ApplicationLayer
public final class OutboundRawLineCorrelationService {

  private final OutboundBackendCapabilityPolicy backendCapabilityPolicy;
  private final LabeledResponseRoutingPort labeledResponseRoutingState;
  private final LongSupplier labelSequence;
  private final Ircv3OutboundCommandRuntimeCatalog outboundCommandRuntimeCatalog;
  private final Ircv3LabeledResponseRuntimeSupport labeledResponseRuntimeSupport;

  @Autowired
  public OutboundRawLineCorrelationService(
      OutboundBackendCapabilityPolicy backendCapabilityPolicy,
      LabeledResponseRoutingPort labeledResponseRoutingState,
      Ircv3OutboundCommandRuntimeCatalog outboundCommandRuntimeCatalog,
      Ircv3LabeledResponseRuntimeSupport labeledResponseRuntimeSupport) {
    AtomicLong sequence = new AtomicLong(System.currentTimeMillis());
    this.backendCapabilityPolicy = Objects.requireNonNull(backendCapabilityPolicy);
    this.labeledResponseRoutingState = Objects.requireNonNull(labeledResponseRoutingState);
    this.labelSequence = sequence::incrementAndGet;
    this.outboundCommandRuntimeCatalog = Objects.requireNonNull(outboundCommandRuntimeCatalog);
    this.labeledResponseRuntimeSupport = Objects.requireNonNull(labeledResponseRuntimeSupport);
  }

  public OutboundRawLineCorrelationService(
      OutboundBackendCapabilityPolicy backendCapabilityPolicy,
      LabeledResponseRoutingPort labeledResponseRoutingState,
      LongSupplier labelSequence,
      Ircv3OutboundCommandRuntimeCatalog outboundCommandRuntimeCatalog,
      Ircv3LabeledResponseRuntimeSupport labeledResponseRuntimeSupport) {
    this.backendCapabilityPolicy = Objects.requireNonNull(backendCapabilityPolicy);
    this.labeledResponseRoutingState = Objects.requireNonNull(labeledResponseRoutingState);
    this.labelSequence = Objects.requireNonNull(labelSequence);
    this.outboundCommandRuntimeCatalog = Objects.requireNonNull(outboundCommandRuntimeCatalog);
    this.labeledResponseRuntimeSupport = Objects.requireNonNull(labeledResponseRuntimeSupport);
  }

  public PreparedRawLine prepare(TargetRef origin, String rawLine) {
    String line = rawLine == null ? "" : rawLine.trim();
    if (line.isEmpty() || origin == null) return new PreparedRawLine(line, "");
    if (!supportsLabeledResponse(origin.serverId())) return new PreparedRawLine(line, "");

    String rendered =
        outboundCommandRuntimeCatalog.buildSingle(
            Ircv3OutboundCommandOperation.LABELED_RESPONSE,
            Ircv3OutboundCommandRequest.labeledResponse(
                origin.serverId(), line, labelSequence.getAsLong()));
    String sendLine = rendered.isBlank() ? line : rendered;
    String label =
        labeledResponseRuntimeSupport
            .fromRawLine("", sendLine)
            .map(Ircv3LabeledResponseRuntimeSupport.Observation::label)
            .orElse("");
    if (!label.isEmpty()) {
      labeledResponseRoutingState.remember(
          origin.serverId(), label, origin, redactIfSensitive(line), Instant.now());
    }
    return new PreparedRawLine(sendLine, label);
  }

  private boolean supportsLabeledResponse(String serverId) {
    return backendCapabilityPolicy.supportsLabeledResponse(serverId);
  }

  public static String redactIfSensitive(String raw) {
    String s = raw == null ? "" : raw.trim();
    if (s.isEmpty()) return s;

    int sp = s.indexOf(' ');
    String head = (sp < 0 ? s : s.substring(0, sp)).trim();
    String upper = head.toUpperCase(Locale.ROOT);
    if (upper.equals("PASS") || upper.equals("OPER") || upper.equals("AUTHENTICATE")) {
      return upper + (sp < 0 ? "" : " <redacted>");
    }
    return s;
  }

  public record PreparedRawLine(String line, String label) {}
}
