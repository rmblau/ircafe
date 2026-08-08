package cafe.woden.ircclient.bouncer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ZncBouncerListNetworksParserTest {

  private final ZncBouncerListNetworksParser parser = new ZncBouncerListNetworksParser();

  @Test
  void parsesPipeDelimitedRowsAndConnectionStates() {
    assertEquals(
        new ZncBouncerListNetworksParser.ParsedRow("Libera Chat", true),
        parser.parseRow("| Libera Chat | yes |"));
    assertEquals(
        new ZncBouncerListNetworksParser.ParsedRow("OFTC", false),
        parser.parseRow("| OFTC | disconnected |"));
    assertEquals(
        new ZncBouncerListNetworksParser.ParsedRow("Unknown", null),
        parser.parseRow("| Unknown | maybe |"));
  }

  @Test
  void parsesFallbackTokenRows() {
    assertEquals(
        new ZncBouncerListNetworksParser.ParsedRow("libera", true),
        parser.parseRow("libera connected"));
    assertEquals(
        new ZncBouncerListNetworksParser.ParsedRow("oftc", null), parser.parseRow("oftc"));
  }

  @Test
  void ignoresHeadersBordersAndUnrelatedRows() {
    assertNull(parser.parseRow(null));
    assertNull(parser.parseRow(""));
    assertNull(parser.parseRow("+---------+--------+"));
    assertNull(parser.parseRow("| Network | On IRC |"));
    assertNull(parser.parseRow("ListNetworks complete"));
    assertNull(parser.parseRow("[module] status"));
  }

  @Test
  void detectsTolerantCompletionLines() {
    assertTrue(parser.isDoneLine("End of network list"));
    assertTrue(parser.isDoneLine("ListNetworks complete"));
    assertTrue(parser.isDoneLine("Use /znc ListNetworks again"));
    assertFalse(parser.isDoneLine("Libera connected"));
    assertFalse(parser.isDoneLine(null));
  }
}
