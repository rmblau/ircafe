package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import java.util.Objects;
import java.util.Optional;
import org.jmolecules.architecture.layered.InfrastructureLayer;

/** Installed-provider-aware adapter for echo-message private-target hint interpretation. */
@InfrastructureLayer
public final class Ircv3EchoMessageRuntimeSupport {

  private final Ircv3InboundTagSignalRuntimeCatalog runtimeCatalog;

  public Ircv3EchoMessageRuntimeSupport(Ircv3InboundTagSignalRuntimeCatalog runtimeCatalog) {
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
  }

  public Optional<TargetHint> targetHint(Ircv3InboundTagRequest request) {
    if (request == null) {
      return Optional.empty();
    }
    String target = "";
    String messageId = "";
    String kind = "";
    String payload = "";
    for (Ircv3InboundTagSignal signal :
        runtimeCatalog.parse(Ircv3InboundTagOperation.ECHO_MESSAGE_TARGET_HINT, request)) {
      switch (signal.type()) {
        case ECHO_MESSAGE_TARGET_HINT -> {
          target = signal.primaryValue();
          messageId = signal.secondaryValue();
        }
        case ECHO_MESSAGE_KIND -> kind = signal.primaryValue();
        case ECHO_MESSAGE_PAYLOAD -> payload = signal.primaryValue();
        default -> {
          // Ignore unrelated or malformed output from an installed provider.
        }
      }
    }
    if (target.isBlank() || (!"PRIVMSG".equals(kind) && !"ACTION".equals(kind))) {
      return Optional.empty();
    }
    return Optional.of(new TargetHint(target, kind, payload, messageId));
  }

  public record TargetHint(String target, String kind, String payload, String messageId) {
    public TargetHint {
      target = Objects.toString(target, "").trim();
      kind = Objects.toString(kind, "").trim();
      payload = Objects.toString(payload, "");
      messageId = Objects.toString(messageId, "").trim();
      if (target.isEmpty()) {
        throw new IllegalArgumentException("target must not be blank");
      }
      if (!"PRIVMSG".equals(kind) && !"ACTION".equals(kind)) {
        throw new IllegalArgumentException("kind must be PRIVMSG or ACTION");
      }
    }
  }
}
