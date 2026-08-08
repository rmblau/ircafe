package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class Ircv3StsPolicyParserTest {

  @Test
  void findsStsValuesAcrossCapTokensAndRemovalTokens() {
    assertEquals(
        List.of("duration=3600,port=6697", "duration=0"),
        Ircv3StsPolicyParser.findStsValues(
            ":message-tags sts=duration=3600,port=6697 -sts=duration=0 batch"));
  }

  @Test
  void ignoresUnrelatedCapabilities() {
    assertTrue(Ircv3StsPolicyParser.findStsValues(":batch message-tags sasl=PLAIN").isEmpty());
  }

  @Test
  void parsesDurationPortAndPreload() {
    Ircv3StsPolicyDirective directive =
        Ircv3StsPolicyParser.parse("duration=86400,port=6697,preload").orElseThrow();

    assertEquals(86400L, directive.durationSeconds());
    assertEquals(Integer.valueOf(6697), directive.port());
    assertTrue(directive.preload());
    assertEquals("duration=86400,port=6697,preload", directive.rawValue());
  }

  @Test
  void acceptsDurationZeroForPolicyRemoval() {
    assertEquals(
        0L, Ircv3StsPolicyParser.parse("duration=0").orElseThrow().durationSeconds());
  }

  @Test
  void rejectsMissingNegativeOrMalformedDuration() {
    assertTrue(Ircv3StsPolicyParser.parse("port=6697").isEmpty());
    assertTrue(Ircv3StsPolicyParser.parse("duration=-1").isEmpty());
    assertTrue(Ircv3StsPolicyParser.parse("duration=tomorrow").isEmpty());
  }

  @Test
  void rejectsInvalidPorts() {
    assertTrue(Ircv3StsPolicyParser.parse("duration=60,port=0").isEmpty());
    assertTrue(Ircv3StsPolicyParser.parse("duration=60,port=65536").isEmpty());
    assertTrue(Ircv3StsPolicyParser.parse("duration=60,port=tls").isEmpty());
  }
}
