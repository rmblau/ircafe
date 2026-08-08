package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3SaslCapabilityOfferTest {

  @Test
  void recognizesLsContinuationMarker() {
    Ircv3SaslCapabilityOffer offer = Ircv3SaslCapabilityOffer.parse(List.of("*"));

    assertTrue(offer.continuationOnly());
    assertFalse(offer.saslOffered());
    assertEquals(Set.of(), offer.offeredMechanismsUpper());
  }

  @Test
  void extractsMechanismsFromSaslCapability() {
    Ircv3SaslCapabilityOffer offer =
        Ircv3SaslCapabilityOffer.parse(
            List.of("message-tags", "sasl=plain, scram-sha-256 ,EXTERNAL"));

    assertFalse(offer.continuationOnly());
    assertTrue(offer.saslOffered());
    assertEquals(Set.of("PLAIN", "SCRAM-SHA-256", "EXTERNAL"), offer.offeredMechanismsUpper());
  }

  @Test
  void stripsLeadingColonBeforeParsingTokens() {
    Ircv3SaslCapabilityOffer offer =
        Ircv3SaslCapabilityOffer.parse(List.of(":sasl=SCRAM-SHA-1"));

    assertTrue(offer.saslOffered());
    assertEquals(Set.of("SCRAM-SHA-1"), offer.offeredMechanismsUpper());
  }

  @Test
  void ignoresNonSaslCaps() {
    Ircv3SaslCapabilityOffer offer =
        Ircv3SaslCapabilityOffer.parse(List.of("batch", "message-tags"));

    assertFalse(offer.continuationOnly());
    assertFalse(offer.saslOffered());
    assertEquals(Set.of(), offer.offeredMechanismsUpper());
  }
}
