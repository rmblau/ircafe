package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignalProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3SaslRuntimeSupportTest {

  @Test
  void adaptsBuiltInCapabilityServerMessageAndFailureSignals() {
    Ircv3SaslRuntimeSupport support = Ircv3RuntimeTestFixtures.sasl();

    Ircv3SaslCapabilityOffer offer =
        support.capabilityList(List.of("message-tags", "sasl=plain,SCRAM-SHA-256"));
    assertTrue(offer.saslOffered());
    assertEquals(Set.of("PLAIN", "SCRAM-SHA-256"), offer.offeredMechanismsUpper());

    Ircv3SaslIrcLine message = support.serverMessage("AUTHENTICATE +");
    assertNotNull(message);
    assertEquals("AUTHENTICATE", message.command());
    assertEquals("+", message.trailing());

    Ircv3SaslFailureSignal failure = support.failure(905, ":server 905 me :SASL message too long");
    assertNotNull(failure);
    assertEquals(905, failure.numeric());
    assertEquals(
        "Login failed — SASL authentication failed (payload too long): SASL message too long",
        failure.disconnectReason());
  }

  @Test
  void keepsCredentialsOutOfPortableProviderRequests() {
    List<Ircv3InboundCommandRequest> requests = new ArrayList<>();
    Ircv3InboundCommandSignalProvider provider =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "capture";
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(
                Ircv3InboundCommandOperation.SASL_CAPABILITY_LIST,
                Ircv3InboundCommandOperation.SASL_CAPABILITY_ACK);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            requests.add(request);
            Ircv3InboundCommandSignal.SaslCapabilityPhase phase =
                operation == Ircv3InboundCommandOperation.SASL_CAPABILITY_LIST
                    ? Ircv3InboundCommandSignal.SaslCapabilityPhase.LIST
                    : Ircv3InboundCommandSignal.SaslCapabilityPhase.ACK;
            return List.of(
                new Ircv3InboundCommandSignal.SaslCapabilityObserved(
                    phase, false, true, List.of("PLAIN")));
          }
        };
    Ircv3SaslRuntimeSupport support =
        new Ircv3SaslRuntimeSupport(
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider)));

    assertTrue(support.capabilityList(List.of("sasl=PLAIN")).saslOffered());
    assertTrue(support.capabilityAck(List.of("sasl")).saslOffered());
    assertEquals(2, requests.size());
    for (Ircv3InboundCommandRequest request : requests) {
      String portable =
          request.sourceNick()
              + request.command()
              + request.rawLine()
              + request.parameters()
              + request.tags()
              + request.connectionHost();
      assertFalse(portable.contains("username-secret"));
      assertFalse(portable.contains("password-secret"));
    }
  }

  @Test
  void rejectsMalformedPluginServerAndFailureSignals() {
    Ircv3InboundCommandSignalProvider provider =
        new Ircv3InboundCommandSignalProvider() {
          @Override
          public String providerId() {
            return "invalid";
          }

          @Override
          public Set<Ircv3InboundCommandOperation> inboundCommandOperations() {
            return Set.of(
                Ircv3InboundCommandOperation.SASL_SERVER_MESSAGE,
                Ircv3InboundCommandOperation.SASL_FAILURE);
          }

          @Override
          public List<Ircv3InboundCommandSignal> parse(
              Ircv3InboundCommandOperation operation, Ircv3InboundCommandRequest request) {
            if (operation == Ircv3InboundCommandOperation.SASL_SERVER_MESSAGE) {
              return List.of(
                  new Ircv3InboundCommandSignal.SaslServerMessageObserved("903", "ok", 904));
            }
            return List.of(
                new Ircv3InboundCommandSignal.SaslFailureObserved(
                    999, null, "not sasl", "not sasl"));
          }
        };
    Ircv3SaslRuntimeSupport support =
        new Ircv3SaslRuntimeSupport(
            Ircv3InboundCommandSignalRuntimeCatalog.fromProviders(List.of(provider)));

    assertNull(support.serverMessage(":server 903 me :ok"));
    assertNull(support.failure(999, ":server 999 me :not sasl"));
  }
}
