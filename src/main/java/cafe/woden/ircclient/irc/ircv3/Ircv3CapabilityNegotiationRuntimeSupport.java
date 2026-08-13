package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Validates portable CAP negotiation signals before application-owned state or transport changes.
 */
public final class Ircv3CapabilityNegotiationRuntimeSupport {

  private static final Set<String> ACTIONS = Set.of("ACK", "DEL", "NEW", "LS", "NAK");
  private static final Set<String> HISTORY_CAPABILITIES =
      Set.of("", "chathistory", "draft/chathistory");

  public record CapabilityChange(
      String action, String capabilityName, boolean enabled, boolean updateState) {
    public CapabilityChange {
      action = Objects.toString(action, "").trim().toUpperCase(Locale.ROOT);
      capabilityName = Objects.toString(capabilityName, "").trim().toLowerCase(Locale.ROOT);
    }
  }

  public record Plan(
      List<CapabilityChange> changes,
      boolean requestMessageTags,
      boolean requestBatch,
      String historyCapability) {
    public Plan {
      changes = List.copyOf(Objects.requireNonNullElse(changes, List.of()));
      historyCapability = Objects.toString(historyCapability, "").trim().toLowerCase(Locale.ROOT);
    }

    public boolean requestHistory() {
      return !historyCapability.isEmpty();
    }

    public boolean refreshConnectionFeatures() {
      return changes.stream().anyMatch(CapabilityChange::updateState);
    }
  }

  private final Ircv3InboundCommandSignalRuntimeCatalog catalog;

  public Ircv3CapabilityNegotiationRuntimeSupport(Ircv3InboundCommandSignalRuntimeCatalog catalog) {
    this.catalog = Objects.requireNonNull(catalog, "catalog");
  }

  public Plan plan(Ircv3InboundCommandRequest request) {
    String observedAction = requestAction(request);
    ArrayList<CapabilityChange> changes = new ArrayList<>();
    boolean requestMessageTags = false;
    boolean requestBatch = false;
    String historyCapability = "";

    for (Ircv3InboundCommandSignal signal :
        catalog.parse(Ircv3InboundCommandOperation.CAP_NEGOTIATION, request)) {
      if (signal instanceof Ircv3InboundCommandSignal.CapabilityChangeObserved change) {
        CapabilityChange validated = validateChange(change, observedAction);
        if (validated != null) {
          changes.add(validated);
        }
      } else if (signal instanceof Ircv3InboundCommandSignal.CapabilityFallbackPlanned fallback) {
        if ("LS".equals(observedAction) || "NEW".equals(observedAction)) {
          requestMessageTags |= fallback.requestMessageTags();
          requestBatch |= fallback.requestBatch();
          String candidate = normalizeHistoryCapability(fallback.historyCapability());
          if (!candidate.isEmpty()) {
            historyCapability = candidate;
          }
        }
      }
    }
    return new Plan(changes, requestMessageTags, requestBatch, historyCapability);
  }

  private static CapabilityChange validateChange(
      Ircv3InboundCommandSignal.CapabilityChangeObserved signal, String observedAction) {
    String action = Objects.toString(signal.action(), "").trim().toUpperCase(Locale.ROOT);
    String capabilityName =
        Objects.toString(signal.capabilityName(), "").trim().toLowerCase(Locale.ROOT);
    if (!ACTIONS.contains(action)
        || !action.equals(observedAction)
        || !isCapabilityName(capabilityName)) {
      return null;
    }
    boolean updateState = signal.updateState() && ("ACK".equals(action) || "DEL".equals(action));
    boolean enabled = updateState && "ACK".equals(action) && signal.enabled();
    return new CapabilityChange(action, capabilityName, enabled, updateState);
  }

  private static String requestAction(Ircv3InboundCommandRequest request) {
    for (String parameter : request.parameters()) {
      String candidate = Objects.toString(parameter, "").trim().toUpperCase(Locale.ROOT);
      if (ACTIONS.contains(candidate)) {
        return candidate;
      }
    }
    for (String token : Objects.toString(request.rawLine(), "").trim().split("\\s+")) {
      String candidate = Objects.toString(token, "").trim().toUpperCase(Locale.ROOT);
      if (ACTIONS.contains(candidate)) {
        return candidate;
      }
    }
    return "";
  }

  private static String normalizeHistoryCapability(String raw) {
    String normalized = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    return HISTORY_CAPABILITIES.contains(normalized) ? normalized : "";
  }

  private static boolean isCapabilityName(String value) {
    if (value.isEmpty() || value.length() > 128) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (Character.isWhitespace(ch) || ch == ':' || ch == '=') {
        return false;
      }
    }
    return true;
  }
}
