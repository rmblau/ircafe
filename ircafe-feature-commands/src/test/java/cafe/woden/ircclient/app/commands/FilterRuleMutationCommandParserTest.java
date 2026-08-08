package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class FilterRuleMutationCommandParserTest {

  private final FilterRuleMutationCommandParser parser =
      new FilterRuleMutationCommandParser();

  @Test
  void parsesKeyValueAddEnvelopeAndPatch() {
    FilterRuleMutationCommandSpec.Add parsed =
        assertInstanceOf(
            FilterRuleMutationCommandSpec.Add.class,
            parser.parse(
                "add",
                List.of(
                    "/filter",
                    "add",
                    "named",
                    "scope=LIBERA/#JAVA",
                    "enabled=on",
                    "text=hello")));

    assertEquals("named", parsed.name());
    assertEquals("LIBERA/#java", parsed.patch().scope());
    assertTrue(parsed.patch().scopeSpecified());
    assertEquals(Boolean.TRUE, parsed.patch().enabled());
    assertTrue(parsed.patch().enabledSpecified());
    assertEquals("hello", parsed.patch().textRegex().pattern());
  }

  @Test
  void parsesWeeChatPositionalAddWhenRegexContainsEquals() {
    FilterRuleMutationCommandSpec.Add parsed =
        assertInstanceOf(
            FilterRuleMutationCommandSpec.Add.class,
            parser.parse(
                "add",
                List.of(
                    "/filter",
                    "add",
                    "eqrule",
                    "irc.libera.#chan",
                    "irc_privmsg",
                    "foo=bar")));

    assertEquals("eqrule", parsed.name());
    assertEquals("libera/#chan", parsed.patch().scope());
    assertEquals("foo=bar", parsed.patch().textRegex().pattern());
  }

  @Test
  void parsesEveryAddReplaceAlias() {
    for (String alias : List.of("addreplace", "add-replace", "addr")) {
      FilterRuleMutationCommandSpec.AddReplace parsed =
          assertInstanceOf(
              FilterRuleMutationCommandSpec.AddReplace.class,
              parser.parse(
                  alias,
                  List.of("/filter", alias, "named", "scope=*/#ops", "action=dim")));

      assertEquals("named", parsed.name());
      assertEquals("*/#ops", parsed.patch().scope());
      assertEquals(FilterRulePatchSpec.Action.DIM, parsed.patch().action());
    }
  }

  @Test
  void parsesSetAndPreservesAnEmptyPatch() {
    FilterRuleMutationCommandSpec.Set empty =
        assertInstanceOf(
            FilterRuleMutationCommandSpec.Set.class,
            parser.parse("set", List.of("/filter", "set", "named")));
    FilterRuleMutationCommandSpec.Set populated =
        assertInstanceOf(
            FilterRuleMutationCommandSpec.Set.class,
            parser.parse(
                "set",
                List.of("/filter", "set", "named", "enabled=off", "from=alice,bob")));

    assertEquals("named", empty.name());
    assertFalse(empty.patch().scopeSpecified());
    assertFalse(empty.patch().enabledSpecified());
    assertEquals(Boolean.FALSE, populated.patch().enabled());
    assertEquals(List.of("alice", "bob"), populated.patch().from());
  }

  @Test
  void preservesCanonicalUsageMessages() {
    assertEquals(
        "Usage: /filter add <name> key=value ... (or: /filter add <name> <buffer> <tags> <regex>)",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("add", List.of("/filter", "add")))
            .getMessage());
    assertEquals(
        "Usage: /filter addreplace <name> key=value ... (or: /filter addreplace <name> <buffer> <tags> <regex>)",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("addr", List.of("/filter", "addr")))
            .getMessage());
    assertEquals(
        "Usage: /filter set <name> key=value ...",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("set", List.of("/filter", "set")))
            .getMessage());
  }

  @Test
  void rejectsUnsupportedRuleMutationCommands() {
    assertEquals(
        "Unsupported /filter rule mutation command: 'rename'",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("rename", List.of("/filter", "rename")))
            .getMessage());
  }
}
