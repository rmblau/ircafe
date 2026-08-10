package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class FilterRuleMutationCommandAdapterTest {

  private final FilterRuleMutationCommandParser parser = new FilterRuleMutationCommandParser();
  private final FilterRuleMutationCommandAdapter adapter = new FilterRuleMutationCommandAdapter();

  @Test
  void adaptsAddAndAddReplaceWithoutChangingNamesOrPatches() {
    FilterCommand.Add add =
        assertInstanceOf(
            FilterCommand.Add.class,
            adapter.toRoot(
                parser.parse(
                    "add",
                    List.of("/filter", "add", "named rule", "scope=LIBERA/#JAVA", "text=hello"))));
    FilterCommand.AddReplace addReplace =
        assertInstanceOf(
            FilterCommand.AddReplace.class,
            adapter.toRoot(
                parser.parse(
                    "addr",
                    List.of("/filter", "addr", "replacement", "action=highlight", "tags=notice"))));

    assertEquals("named rule", add.name());
    assertEquals("LIBERA/#java", add.patch().scope());
    assertEquals("hello", add.patch().textRegex().pattern());
    assertEquals("replacement", addReplace.name());
    assertTrue(addReplace.patch().actionSpecified());
    assertEquals("notice", addReplace.patch().tagsExpr());
  }

  @Test
  void adaptsSetIncludingTheExistingEmptyPatchForm() {
    FilterCommand.Set empty =
        assertInstanceOf(
            FilterCommand.Set.class,
            adapter.toRoot(parser.parse("set", List.of("/filter", "set", "named"))));
    FilterCommand.Set populated =
        assertInstanceOf(
            FilterCommand.Set.class,
            adapter.toRoot(
                parser.parse(
                    "set", List.of("/filter", "set", "named", "enabled=off", "from=alice,bob"))));

    assertEquals("named", empty.name());
    assertEquals(FilterCommand.FilterRulePatch.empty(), empty.patch());
    assertEquals(Boolean.FALSE, populated.patch().enabled());
    assertEquals(List.of("alice", "bob"), populated.patch().from());
  }
}
