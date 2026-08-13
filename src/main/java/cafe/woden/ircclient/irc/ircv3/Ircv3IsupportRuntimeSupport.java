package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Application-side adapter for runtime-provider RPL_ISUPPORT interpretation. */
public final class Ircv3IsupportRuntimeSupport {

  private final Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog;

  public Ircv3IsupportRuntimeSupport(Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog) {
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
  }

  public List<TokenUpdate> tokenUpdates(String rawLine) {
    ArrayList<TokenUpdate> updates = new ArrayList<>();
    for (Ircv3InboundCommandSignal signal :
        parse(Ircv3InboundCommandOperation.ISUPPORT_TOKENS, rawLine)) {
      if (signal instanceof Ircv3InboundCommandSignal.IsupportTokenObserved token) {
        String key = Objects.toString(token.key(), "").trim();
        if (!key.isEmpty()) {
          updates.add(new TokenUpdate(key, token.value(), token.removed()));
        }
      }
    }
    return List.copyOf(updates);
  }

  public Optional<Boolean> whoxSupport(String rawLine) {
    for (Ircv3InboundCommandSignal signal :
        parse(Ircv3InboundCommandOperation.ISUPPORT_WHOX, rawLine)) {
      if (signal instanceof Ircv3InboundCommandSignal.WhoxSupportObserved whox) {
        return Optional.of(whox.supported());
      }
    }
    return Optional.empty();
  }

  public Optional<MonitorSupport> monitorSupport(String rawLine) {
    for (Ircv3InboundCommandSignal signal :
        parse(Ircv3InboundCommandOperation.ISUPPORT_MONITOR, rawLine)) {
      if (signal instanceof Ircv3InboundCommandSignal.MonitorSupportObserved monitor) {
        return Optional.of(new MonitorSupport(monitor.supported(), monitor.limit()));
      }
    }
    return Optional.empty();
  }

  private List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, String rawLine) {
    return runtimeCatalog.parse(
        operation,
        new Ircv3InboundCommandRequest(
            "", "005", Objects.toString(rawLine, ""), List.of(), Map.of()));
  }

  public record TokenUpdate(String key, String value, boolean removed) {
    public TokenUpdate {
      key = Objects.toString(key, "").trim();
      value = Objects.toString(value, "").trim();
    }
  }

  public record MonitorSupport(boolean supported, int limit) {
    public MonitorSupport {
      if (limit < 0) limit = 0;
      if (!supported) limit = 0;
    }
  }
}
