package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class FilterLifecycleCommandParserTest {

  private final FilterLifecycleCommandParser parser = new FilterLifecycleCommandParser();

  @Test
  void parsesRenameAndRecreateAliases() {
    assertEquals(
        new FilterLifecycleCommandSpec.Rename("old", "new"),
        parser.parse("ren", List.of("/filter", "ren", "old", "new")));
    assertEquals(
        new FilterLifecycleCommandSpec.Recreate("named"),
        parser.parse("rec", List.of("/filter", "rec", "named")));
  }

  @Test
  void preservesCanonicalRenameAndRecreateUsageMessages() {
    assertEquals(
        "Usage: /filter rename <old> <new>",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("rename", List.of("/filter", "rename", "old")))
            .getMessage());
    assertEquals(
        "Usage: /filter recreate <name>",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("recreate", List.of("/filter", "recreate")))
            .getMessage());
  }

  @Test
  void parsesDeleteAliasesAndPreservesMaskOrder() {
    FilterLifecycleCommandSpec.Targets parsed =
        (FilterLifecycleCommandSpec.Targets)
            parser.parse("remove", List.of("/filter", "remove", "named", "irc-*", "re:/ops.*/"));

    assertEquals(FilterTargetActionSpec.DELETE, parsed.action());
    assertEquals(List.of("named", "irc-*", "re:/ops.*/"), parsed.namesOrMasks());
  }

  @Test
  void deleteRequiresAtLeastOneNameOrMask() {
    assertEquals(
        "Usage: /filter del <name-or-mask> [more...] (use '*' and '?' for masks, or re:/.../)",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("del", List.of("/filter", "del")))
            .getMessage());
  }

  @Test
  void parsesGlobalAndTargetedEnableDisableToggleCommands() {
    assertEquals(
        new FilterLifecycleCommandSpec.Targets(FilterTargetActionSpec.ENABLE, List.of()),
        parser.parse("enable", List.of("/filter", "enable")));
    assertEquals(
        new FilterLifecycleCommandSpec.Targets(
            FilterTargetActionSpec.DISABLE, List.of("@", "named")),
        parser.parse("disable", List.of("/filter", "disable", "@", "named")));
    assertEquals(
        new FilterLifecycleCommandSpec.Targets(FilterTargetActionSpec.TOGGLE, List.of("irc-*")),
        parser.parse("toggle", List.of("/filter", "toggle", "irc-*")));
  }

  @Test
  void rejectsUnsupportedLifecycleCommands() {
    assertEquals(
        "Unsupported /filter lifecycle command: 'list'",
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("list", List.of("/filter", "list")))
            .getMessage());
  }
}
