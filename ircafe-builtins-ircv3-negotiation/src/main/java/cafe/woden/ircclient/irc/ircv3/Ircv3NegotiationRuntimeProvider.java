package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Built-in runtime provider for capability and ISUPPORT negotiation signals. */
@AutoService(Ircv3InboundCommandSignalProvider.class)
public final class Ircv3NegotiationRuntimeProvider implements Ircv3InboundCommandSignalProvider {

  private final Ircv3CapabilityChangePlanner changePlanner = new Ircv3CapabilityChangePlanner();
  private final Ircv3CapabilityFallbackPlanner fallbackPlanner =
      new Ircv3CapabilityFallbackPlanner();

  @Override
  public String providerId() {
    return "negotiation";
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(
        Ircv3InboundCommandOperation.CAP_NEGOTIATION, Ircv3InboundCommandOperation.ISUPPORT_TOKENS);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (request == null) {
      return List.of();
    }
    if (operation == Ircv3InboundCommandOperation.CAP_NEGOTIATION) {
      return parseCapabilityNegotiation(request);
    }
    if (operation == Ircv3InboundCommandOperation.ISUPPORT_TOKENS) {
      Ircv3IsupportLine line = Ircv3IsupportLine.parse(request.rawLine()).orElse(null);
      if (line == null || line.tokens().isEmpty()) {
        return List.of();
      }
      ArrayList<Ircv3InboundCommandSignal> signals = new ArrayList<>(line.tokens().size());
      for (Ircv3IsupportLine.Token token : line.tokens()) {
        signals.add(
            new Ircv3InboundCommandSignal.IsupportTokenObserved(
                token.key(), token.value(), token.removed()));
      }
      return List.copyOf(signals);
    }
    return List.of();
  }

  private List<Ircv3InboundCommandSignal> parseCapabilityNegotiation(
      Ircv3InboundCommandRequest request) {
    Ircv3CapabilityLine line = capabilityLine(request);
    if (!line.hasTokens()) {
      return List.of();
    }

    Ircv3CapabilityChangePlanner.Plan changes = changePlanner.plan(line);
    Ircv3CapabilityFallbackPlanner.Plan fallback =
        fallbackPlanner.plan(
            line,
            new Ircv3CapabilityFallbackPlanner.State(
                request.messageTagsEnabled(),
                request.batchEnabled(),
                request.chatHistoryEnabled(),
                request.pendingCapabilities()));

    ArrayList<Ircv3InboundCommandSignal> signals = new ArrayList<>(changes.changes().size() + 1);
    for (Ircv3CapabilityChangePlanner.Change change : changes.changes()) {
      signals.add(
          new Ircv3InboundCommandSignal.CapabilityChangeObserved(
              change.action(), change.capabilityName(), change.enabled(), change.updateState()));
    }
    if (fallback.requestMessageTags() || fallback.requestBatch() || fallback.requestHistory()) {
      signals.add(
          new Ircv3InboundCommandSignal.CapabilityFallbackPlanned(
              fallback.requestMessageTags(),
              fallback.requestBatch(),
              fallback.historyCapability()));
    }
    return List.copyOf(signals);
  }

  private static Ircv3CapabilityLine capabilityLine(Ircv3InboundCommandRequest request) {
    Ircv3CapabilityLine fromParameters = capabilityLine(request.parameters());
    if (fromParameters.hasTokens()) {
      return fromParameters;
    }

    String rawLine = Objects.toString(request.rawLine(), "").trim();
    int capOffset = rawLine.toUpperCase(Locale.ROOT).indexOf(" CAP ");
    if (capOffset < 0) {
      return Ircv3CapabilityLine.parse("", "");
    }
    return capabilityLine(List.of(rawLine.substring(capOffset + 5).trim().split("\\s+")));
  }

  private static Ircv3CapabilityLine capabilityLine(List<String> parameters) {
    for (int i = 0; i < parameters.size(); i++) {
      String action = Objects.toString(parameters.get(i), "").trim().toUpperCase(Locale.ROOT);
      if (!isCapabilityAction(action)) {
        continue;
      }
      int start = i + 1;
      if (start < parameters.size() && "*".equals(parameters.get(start))) {
        start++;
      }
      StringBuilder capabilities = new StringBuilder();
      for (int j = start; j < parameters.size(); j++) {
        String token = Objects.toString(parameters.get(j), "").trim();
        if (token.isEmpty()) {
          continue;
        }
        if (capabilities.length() > 0) {
          capabilities.append(' ');
        }
        capabilities.append(token);
      }
      return Ircv3CapabilityLine.parse(action, capabilities.toString());
    }
    return Ircv3CapabilityLine.parse("", "");
  }

  private static boolean isCapabilityAction(String action) {
    return "ACK".equals(action)
        || "DEL".equals(action)
        || "NEW".equals(action)
        || "LS".equals(action)
        || "NAK".equals(action);
  }
}
