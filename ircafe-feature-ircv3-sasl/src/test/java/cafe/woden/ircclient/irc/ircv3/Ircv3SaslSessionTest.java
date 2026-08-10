package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3SaslSessionTest {

  @Test
  void waitsForFinalCapabilityListAndRequestsSaslOnce() {
    Ircv3SaslSession session = new Ircv3SaslSession("user", "secret", "PLAIN", false);

    Ircv3SaslSessionUpdate continuation = session.onCapabilityList(List.of("*"));
    Ircv3SaslSessionUpdate offered = session.onCapabilityList(List.of("sasl=PLAIN"));
    Ircv3SaslSessionUpdate repeated = session.onCapabilityList(List.of("sasl=PLAIN"));

    assertFalse(continuation.complete());
    assertFalse(continuation.requestCapability());
    assertTrue(offered.requestCapability());
    assertFalse(repeated.requestCapability());
  }

  @Test
  void selectsMechanismAndFramesPlainResponse() throws Exception {
    Ircv3SaslSession session = new Ircv3SaslSession("user", "secret", "AUTO", false);
    session.onCapabilityList(List.of("sasl=PLAIN,EXTERNAL"));

    Ircv3SaslSessionUpdate ack = session.onCapabilityAck(List.of("sasl"));
    Ircv3SaslSessionUpdate response = session.onRawLine("AUTHENTICATE +");

    assertEquals("PLAIN", ack.startedMechanism());
    assertEquals(List.of("AUTHENTICATE PLAIN"), ack.rawLines());
    assertEquals(1, response.rawLines().size());
    String payload = response.rawLines().getFirst().substring("AUTHENTICATE ".length());
    assertEquals(
        "\0user\0secret", new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8));
  }

  @Test
  void nonFatalFailureAbortsAndCompletesRegistration() {
    Ircv3SaslSession session = new Ircv3SaslSession("", "", "AUTO", false);
    session.onCapabilityList(List.of("sasl=PLAIN"));

    Ircv3SaslSessionUpdate failure = session.onCapabilityAck(List.of("sasl"));

    assertTrue(failure.complete());
    assertEquals(List.of("AUTHENTICATE *"), failure.rawLines());
    assertFalse(failure.failure().disconnect());
  }

  @Test
  void fatalFailureDoesNotPlanAbortLine() {
    Ircv3SaslSession session = new Ircv3SaslSession("", "", "AUTO", true);
    session.onCapabilityList(List.of("sasl=PLAIN"));

    Ircv3SaslSessionUpdate failure = session.onCapabilityAck(List.of("sasl"));

    assertTrue(failure.complete());
    assertTrue(failure.rawLines().isEmpty());
    assertTrue(failure.failure().disconnect());
  }

  @Test
  void acceptsRuntimeParsedCapabilityAndServerObservations() throws Exception {
    Ircv3SaslSession session = new Ircv3SaslSession("user", "secret", "PLAIN", false);

    Ircv3SaslSessionUpdate offered =
        session.onCapabilityList(
            new Ircv3SaslCapabilityOffer(false, true, java.util.Set.of("PLAIN")));
    Ircv3SaslSessionUpdate ack =
        session.onCapabilityAck(new Ircv3SaslCapabilityOffer(false, true, java.util.Set.of()));
    Ircv3SaslSessionUpdate response =
        session.onParsedLine(new Ircv3SaslIrcLine("AUTHENTICATE", "+"));

    assertTrue(offered.requestCapability());
    assertEquals("PLAIN", ack.startedMechanism());
    assertEquals(1, response.rawLines().size());
  }

  @Test
  void successNumericCompletesSession() throws Exception {
    Ircv3SaslSession session = new Ircv3SaslSession("user", "secret", "PLAIN", false);
    session.onCapabilityList(List.of("sasl=PLAIN"));
    session.onCapabilityAck(List.of("sasl"));

    Ircv3SaslSessionUpdate success = session.onRawLine(":server 903 me :SASL success");

    assertTrue(success.complete());
    assertEquals(903, success.successNumeric());
    assertNull(success.failure());
  }
}
