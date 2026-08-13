package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Plans normalized capability observations from one IRC CAP line. */
public final class Ircv3CapabilityChangePlanner {

  public record Change(String action, String capabilityName, boolean enabled, boolean updateState) {
    public Change {
      action = Objects.toString(action, "").trim();
      capabilityName = Objects.toString(capabilityName, "").trim();
      if (capabilityName.isEmpty()) {
        throw new IllegalArgumentException("capabilityName must not be blank");
      }
    }
  }

  public record Plan(List<Change> changes, boolean refreshConnectionFeatures) {
    public Plan {
      changes = changes == null ? List.of() : List.copyOf(changes);
    }
  }

  public Plan plan(Ircv3CapabilityLine line) {
    Objects.requireNonNull(line, "line");
    if (!line.hasTokens() || !line.isAction("ACK", "DEL", "NEW", "LS", "NAK")) {
      return new Plan(List.of(), false);
    }

    boolean updateState = line.isAction("ACK", "DEL");
    boolean fromAck = line.isAction("ACK");
    ArrayList<Change> changes = new ArrayList<>(line.tokens().size());
    for (String rawToken : line.tokens()) {
      Ircv3CapabilityToken token = Ircv3CapabilityToken.parse(rawToken).orElse(null);
      if (token == null) continue;
      boolean enabled = updateState && fromAck && !token.disabled();
      changes.add(new Change(line.action(), token.name(), enabled, updateState));
    }
    return new Plan(changes, updateState && !changes.isEmpty());
  }
}
