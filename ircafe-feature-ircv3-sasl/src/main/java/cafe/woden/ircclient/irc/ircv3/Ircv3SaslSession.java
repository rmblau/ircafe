package cafe.woden.ircclient.irc.ircv3;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Owns the transport-independent IRCv3 SASL capability and authentication lifecycle. */
public final class Ircv3SaslSession {

  private final String username;
  private final String secret;
  private final String configuredMechanism;
  private final boolean disconnectOnFailure;
  private final Ircv3SaslAuthenticateFraming authenticateFraming =
      new Ircv3SaslAuthenticateFraming();
  private final Ircv3SaslMechanismSelector mechanismSelector = new Ircv3SaslMechanismSelector();
  private final Ircv3SaslResponseFactory responseFactory = new Ircv3SaslResponseFactory();
  private final Ircv3ScramSaslConversation scramConversation;

  private Ircv3SaslCapabilityOffer capabilityOffer =
      new Ircv3SaslCapabilityOffer(false, false, Set.of());
  private boolean saslRequested;
  private boolean saslAcked;
  private State state = State.INIT;
  private String chosenMechanism;

  public Ircv3SaslSession(
      String username, String secret, String configuredMechanism, boolean disconnectOnFailure) {
    this.username = Objects.toString(username, "");
    this.secret = Objects.toString(secret, "");
    this.configuredMechanism = Objects.toString(configuredMechanism, "PLAIN").trim();
    this.disconnectOnFailure = disconnectOnFailure;
    this.scramConversation = new Ircv3ScramSaslConversation(this.username, this.secret);
  }

  public Ircv3SaslSessionUpdate onCapabilityList(Collection<String> serverCapabilities) {
    return onCapabilityList(Ircv3SaslCapabilityOffer.parse(serverCapabilities));
  }

  public Ircv3SaslSessionUpdate onCapabilityList(Ircv3SaslCapabilityOffer parsedOffer) {
    parsedOffer =
        Objects.requireNonNullElseGet(
            parsedOffer, () -> new Ircv3SaslCapabilityOffer(false, false, Set.of()));
    if (parsedOffer.continuationOnly()) {
      return Ircv3SaslSessionUpdate.active();
    }

    capabilityOffer = parsedOffer;
    if (!capabilityOffer.saslOffered()) {
      return Ircv3SaslSessionUpdate.completed();
    }
    if (!saslRequested) {
      saslRequested = true;
      state = State.REQUESTED;
      return new Ircv3SaslSessionUpdate(false, true, List.of(), null, null, null);
    }
    return Ircv3SaslSessionUpdate.active();
  }

  public Ircv3SaslSessionUpdate onCapabilityAck(Collection<String> capabilities) {
    return onCapabilityAck(Ircv3SaslCapabilityOffer.parse(capabilities));
  }

  public Ircv3SaslSessionUpdate onCapabilityAck(Ircv3SaslCapabilityOffer offer) {
    if (offer == null || !offer.saslOffered()) {
      return terminalOrActive();
    }

    saslAcked = true;
    state = State.ACKED;
    chosenMechanism =
        mechanismSelector.choose(
            configuredMechanism, username, secret, capabilityOffer.offeredMechanismsUpper());
    if (chosenMechanism == null || chosenMechanism.isBlank()) {
      return fail(
          "No usable SASL mechanism available (configured="
              + configuredMechanism
              + ", offered="
              + capabilityOffer.offeredMechanismsUpper()
              + ")");
    }

    state = State.AUTH_SENT;
    return new Ircv3SaslSessionUpdate(
        false, false, List.of("AUTHENTICATE " + chosenMechanism), chosenMechanism, null, null);
  }

  public Ircv3SaslSessionUpdate onCapabilityNak(Collection<String> capabilities) {
    return onCapabilityNak(Ircv3SaslCapabilityOffer.parse(capabilities));
  }

  public Ircv3SaslSessionUpdate onCapabilityNak(Ircv3SaslCapabilityOffer offer) {
    if (offer == null || !offer.saslOffered()) {
      return terminalOrActive();
    }
    return fail("Server NAK'd sasl capability");
  }

  public Ircv3SaslSessionUpdate onRawLine(String rawLine) throws Ircv3SaslException {
    return onParsedLine(Ircv3SaslIrcLine.parse(rawLine));
  }

  public Ircv3SaslSessionUpdate onParsedLine(Ircv3SaslIrcLine parsed) throws Ircv3SaslException {
    if (state.isTerminal()) {
      return Ircv3SaslSessionUpdate.completed();
    }
    if (!capabilityOffer.saslOffered() || !saslRequested || !saslAcked) {
      return Ircv3SaslSessionUpdate.active();
    }
    if (parsed == null) {
      return Ircv3SaslSessionUpdate.active();
    }

    if ("AUTHENTICATE".equalsIgnoreCase(parsed.command())) {
      String data = parsed.trailing() == null ? "" : parsed.trailing();
      byte[] decoded = authenticateFraming.acceptServerPayload(data).orElse(null);
      if (decoded == null) {
        return Ircv3SaslSessionUpdate.active();
      }
      return new Ircv3SaslSessionUpdate(
          state.isTerminal(), false, handleServerAuthMessage(decoded), null, null, null);
    }

    if (!parsed.isNumeric()) {
      return Ircv3SaslSessionUpdate.active();
    }

    int numeric = parsed.numeric();
    return switch (numeric) {
      case 903, 907 -> {
        state = State.DONE;
        yield new Ircv3SaslSessionUpdate(true, false, List.of(), null, numeric, null);
      }
      case 904, 905, 906 -> fail("SASL failed (" + numeric + ")");
      default -> Ircv3SaslSessionUpdate.active();
    };
  }

  private List<String> handleServerAuthMessage(byte[] decoded) throws Ircv3SaslException {
    String mechanism = Objects.toString(chosenMechanism, "").toUpperCase(Locale.ROOT);
    return switch (mechanism) {
      case "PLAIN" -> beginSingleResponse(responseFactory.createPlain(username, secret));
      case "EXTERNAL" -> beginSingleResponse(responseFactory.createExternal(username));
      case "SCRAM-SHA-1" ->
          encodeResponse(
              scramConversation.nextResponse("SHA-1", new String(decoded, StandardCharsets.UTF_8)));
      case "SCRAM-SHA-256" ->
          encodeResponse(
              scramConversation.nextResponse(
                  "SHA-256", new String(decoded, StandardCharsets.UTF_8)));
      case "ECDSA-NIST256P-CHALLENGE" ->
          beginSingleResponse(responseFactory.createEcdsa(secret, decoded));
      default ->
          throw new Ircv3SaslException(
              Ircv3SaslException.Reason.UNSUPPORTED_MECHANISM,
              "Unsupported SASL mechanism: " + chosenMechanism);
    };
  }

  private List<String> beginSingleResponse(String response) {
    if (state == State.EXCHANGING) {
      return List.of();
    }
    state = State.EXCHANGING;
    return encodeResponse(response);
  }

  private List<String> encodeResponse(String response) {
    if (response == null) {
      return List.of();
    }
    return authenticateFraming.encodeClientResponse(response).stream()
        .map(payload -> "AUTHENTICATE " + payload)
        .toList();
  }

  private Ircv3SaslSessionUpdate fail(String reason) {
    state = State.FAILED;
    List<String> rawLines = disconnectOnFailure ? List.of() : List.of("AUTHENTICATE *");
    return new Ircv3SaslSessionUpdate(
        true,
        false,
        rawLines,
        null,
        null,
        new Ircv3SaslSessionUpdate.Failure(reason, disconnectOnFailure));
  }

  private Ircv3SaslSessionUpdate terminalOrActive() {
    return state.isTerminal()
        ? Ircv3SaslSessionUpdate.completed()
        : Ircv3SaslSessionUpdate.active();
  }

  private enum State {
    INIT,
    REQUESTED,
    ACKED,
    AUTH_SENT,
    EXCHANGING,
    DONE,
    FAILED;

    boolean isTerminal() {
      return this == DONE || this == FAILED;
    }
  }
}
