package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.SaslCapabilityObserved;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.SaslCapabilityPhase;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.SaslFailureObserved;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.SaslServerMessageObserved;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;

/** Built-in runtime provider for transport-neutral IRCv3 SASL server interpretation. */
@AutoService(Ircv3InboundCommandSignalProvider.class)
public final class Ircv3SaslRuntimeProvider implements Ircv3InboundCommandSignalProvider {

  @Override
  public String providerId() {
    return "sasl";
  }

  @Override
  public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
    return Set.of(
        Ircv3InboundCommandOperation.SASL_CAPABILITY_LIST,
        Ircv3InboundCommandOperation.SASL_CAPABILITY_ACK,
        Ircv3InboundCommandOperation.SASL_CAPABILITY_NAK,
        Ircv3InboundCommandOperation.SASL_SERVER_MESSAGE,
        Ircv3InboundCommandOperation.SASL_FAILURE);
  }

  @Override
  public List<Ircv3InboundCommandSignal> parse(
      Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
    if (operation == null || request == null) {
      return List.of();
    }
    return switch (operation) {
      case SASL_CAPABILITY_LIST -> capability(SaslCapabilityPhase.LIST, request);
      case SASL_CAPABILITY_ACK -> capability(SaslCapabilityPhase.ACK, request);
      case SASL_CAPABILITY_NAK -> capability(SaslCapabilityPhase.NAK, request);
      case SASL_SERVER_MESSAGE -> serverMessage(request);
      case SASL_FAILURE -> failure(request);
      default -> List.of();
    };
  }

  private static List<Ircv3InboundCommandSignal> capability(
      SaslCapabilityPhase phase, Ircv3InboundCommandRequest request) {
    Ircv3SaslCapabilityOffer offer = Ircv3SaslCapabilityOffer.parse(request.parameters());
    return List.of(
        new SaslCapabilityObserved(
            phase,
            offer.continuationOnly(),
            offer.saslOffered(),
            offer.offeredMechanismsUpper().stream().sorted().toList()));
  }

  private static List<Ircv3InboundCommandSignal> serverMessage(
      Ircv3InboundCommandRequest request) {
    Ircv3SaslIrcLine line = Ircv3SaslIrcLine.parse(request.rawLine());
    if (line == null) {
      return List.of();
    }
    return List.of(
        new SaslServerMessageObserved(
            line.command(), line.trailing(), line.isNumeric() ? line.numeric() : null));
  }

  private static List<Ircv3InboundCommandSignal> failure(Ircv3InboundCommandRequest request) {
    Ircv3SaslFailureSignal failure = Ircv3SaslFailureSignal.parse(request.rawLine());
    if (failure == null) {
      Integer numeric = parseNumeric(request.command());
      if (numeric == null || !Ircv3SaslFailureSignal.isFailureNumeric(numeric)) {
        return List.of();
      }
      failure = Ircv3SaslFailureSignal.from(numeric, request.rawLine());
    }
    return List.of(
        new SaslFailureObserved(
            failure.numeric(),
            failure.trailingMessage(),
            failure.detail(),
            failure.disconnectReason()));
  }

  private static Integer parseNumeric(String command) {
    try {
      return Integer.valueOf(command);
    } catch (RuntimeException ignored) {
      return null;
    }
  }
}
