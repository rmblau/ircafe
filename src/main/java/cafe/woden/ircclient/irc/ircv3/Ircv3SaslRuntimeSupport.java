package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.SaslCapabilityObserved;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.SaslCapabilityPhase;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.SaslFailureObserved;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal.SaslServerMessageObserved;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Validates plugin-provided SASL server observations without exposing client credentials. */
public final class Ircv3SaslRuntimeSupport {

  private final Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog;

  public Ircv3SaslRuntimeSupport(Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog) {
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
  }

  public Ircv3SaslCapabilityOffer capabilityList(Collection<String> capabilities) {
    return capability(
        Ircv3InboundCommandOperation.SASL_CAPABILITY_LIST,
        SaslCapabilityPhase.LIST,
        capabilities);
  }

  public Ircv3SaslCapabilityOffer capabilityAck(Collection<String> capabilities) {
    return capability(
        Ircv3InboundCommandOperation.SASL_CAPABILITY_ACK,
        SaslCapabilityPhase.ACK,
        capabilities);
  }

  public Ircv3SaslCapabilityOffer capabilityNak(Collection<String> capabilities) {
    return capability(
        Ircv3InboundCommandOperation.SASL_CAPABILITY_NAK,
        SaslCapabilityPhase.NAK,
        capabilities);
  }

  public Ircv3SaslIrcLine serverMessage(String rawLine) {
    Ircv3InboundCommandRequest request =
        new Ircv3InboundCommandRequest("", "", rawLine, List.of(), Map.of());
    for (Ircv3InboundCommandSignal signal :
        runtimeCatalog.parse(Ircv3InboundCommandOperation.SASL_SERVER_MESSAGE, request)) {
      if (signal instanceof SaslServerMessageObserved observed) {
        Ircv3SaslIrcLine line = validatedServerMessage(observed);
        if (line != null) {
          return line;
        }
      }
    }
    return null;
  }

  public boolean isFailureCode(int code) {
    return failure(code, "") != null;
  }

  public Ircv3SaslFailureSignal failure(String rawLine) {
    return failure(null, rawLine);
  }

  public Ircv3SaslFailureSignal failure(int code, String rawLine) {
    return failure(Integer.valueOf(code), rawLine);
  }

  private Ircv3SaslCapabilityOffer capability(
      Ircv3InboundCommandOperation operation,
      SaslCapabilityPhase expectedPhase,
      Collection<String> capabilities) {
    List<String> parameters =
        capabilities == null
            ? List.of()
            : capabilities.stream().map(value -> Objects.toString(value, "")).toList();
    Ircv3InboundCommandRequest request =
        new Ircv3InboundCommandRequest("", "CAP", "", parameters, Map.of());
    for (Ircv3InboundCommandSignal signal : runtimeCatalog.parse(operation, request)) {
      if (!(signal instanceof SaslCapabilityObserved observed)
          || observed.phase() != expectedPhase) {
        continue;
      }
      if (observed.continuationOnly()) {
        return new Ircv3SaslCapabilityOffer(true, false, Set.of());
      }
      Set<String> mechanisms = Set.copyOf(observed.mechanismsUpper());
      if (!observed.saslOffered()) {
        mechanisms = Set.of();
      }
      return new Ircv3SaslCapabilityOffer(false, observed.saslOffered(), mechanisms);
    }
    return new Ircv3SaslCapabilityOffer(false, false, Set.of());
  }

  private Ircv3SaslFailureSignal failure(Integer expectedCode, String rawLine) {
    String command = expectedCode == null ? "" : Integer.toString(expectedCode);
    Ircv3InboundCommandRequest request =
        new Ircv3InboundCommandRequest("", command, rawLine, List.of(), Map.of());
    for (Ircv3InboundCommandSignal signal :
        runtimeCatalog.parse(Ircv3InboundCommandOperation.SASL_FAILURE, request)) {
      if (!(signal instanceof SaslFailureObserved observed)) {
        continue;
      }
      if (expectedCode != null && observed.numeric() != expectedCode.intValue()) {
        continue;
      }
      if (!Ircv3SaslFailureSignal.isFailureNumeric(observed.numeric())
          || observed.detail().isBlank()
          || observed.disconnectReason().isBlank()) {
        continue;
      }
      return new Ircv3SaslFailureSignal(
          observed.numeric(),
          observed.trailingMessage(),
          observed.detail(),
          observed.disconnectReason());
    }
    return null;
  }

  private static Ircv3SaslIrcLine validatedServerMessage(SaslServerMessageObserved observed) {
    String command = Objects.toString(observed.command(), "").trim();
    if (command.equalsIgnoreCase("AUTHENTICATE")) {
      if (observed.numeric() != null) {
        return null;
      }
      return new Ircv3SaslIrcLine("AUTHENTICATE", observed.trailing());
    }
    if (command.length() != 3
        || !command.chars().allMatch(Character::isDigit)
        || observed.numeric() == null
        || Integer.parseInt(command) != observed.numeric()) {
      return null;
    }
    return new Ircv3SaslIrcLine(command, observed.trailing());
  }
}
