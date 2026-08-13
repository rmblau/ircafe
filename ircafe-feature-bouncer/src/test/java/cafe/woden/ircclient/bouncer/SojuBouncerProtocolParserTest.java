package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SojuBouncerProtocolParserTest {

  private final SojuBouncerProtocolParser parser = new SojuBouncerProtocolParser();

  @Test
  void parsesBouncerNetIdFromIsupportLines() {
    assertEquals(
        "123",
        parser.parseBouncerNetId(
            ":server 005 me MONITOR=250 BOUNCER_NETID=123 CLIENTTAGDENY=* :supported"));
    assertEquals("abc", parser.parseBouncerNetId("BOUNCER_NETID=:abc"));
    assertNull(parser.parseBouncerNetId("MONITOR=250"));
    assertNull(parser.parseBouncerNetId("BOUNCER_NETID="));
    assertNull(parser.parseBouncerNetId(null));
  }

  @Test
  void parsesNetworkRowsAndSanitizesDisplayNames() {
    SojuBouncerProtocolParser.ParsedNetwork parsed =
        parser.parseNetworkLine(
            ":server BOUNCER NETWORK 123 name=Libera Chat;loginUser=alice/lib;caps=message-tags,draft/react");

    assertEquals("123", parsed.networkId());
    assertEquals("Libera_Chat", parsed.name());
    assertEquals("alice/lib", parsed.attributes().get("loginUser"));
    assertEquals("message-tags,draft/react", parsed.attributes().get("caps"));
  }

  @Test
  void parsesTrailingAttributesAndAppliesFallbackName() {
    SojuBouncerProtocolParser.ParsedNetwork trailing =
        parser.parseNetworkLine(":server BOUNCER NETWORK 7 :name=OFTC;flag");
    SojuBouncerProtocolParser.ParsedNetwork fallback =
        parser.parseNetworkLine("BOUNCER NETWORK 8 flag");

    assertEquals("OFTC", trailing.name());
    assertEquals(Map.of("name", "OFTC", "flag", ""), trailing.attributes());
    assertEquals("net-8", fallback.name());
    assertEquals(Map.of("flag", ""), fallback.attributes());
  }

  @Test
  void rejectsUnrelatedOrIncompleteRows() {
    assertNull(parser.parseNetworkLine(null));
    assertNull(parser.parseNetworkLine(""));
    assertNull(parser.parseNetworkLine("BOUNCER"));
    assertNull(parser.parseNetworkLine("BOUNCER OTHER 123 name=x"));
    assertNull(parser.parseNetworkLine("NOTICE NETWORK 123 name=x"));
  }

  @Test
  void sanitizesNetworkNamesWithoutChangingSafeCharacters() {
    assertEquals("Libera.Chat-test_1", parser.sanitizeNetworkName("Libera.Chat-test_1"));
    assertEquals("Libera_Chat__", parser.sanitizeNetworkName("Libera Chat!!"));
    assertEquals("", parser.sanitizeNetworkName(null));
  }
}
