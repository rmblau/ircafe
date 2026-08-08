package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class FilterCommandSpecParserTest {

  private final FilterCommandSpecParser parser = new FilterCommandSpecParser();

  @Test
  void parsesBlankAndExplicitHelpAsHelp() {
    assertInstanceOf(FilterCommandSpec.Help.class, parser.parse(null));
    assertInstanceOf(FilterCommandSpec.Help.class, parser.parse("   "));
    assertInstanceOf(FilterCommandSpec.Help.class, parser.parse("/filter"));
    assertInstanceOf(FilterCommandSpec.Help.class, parser.parse("/FILTER HELP"));
  }

  @Test
  void rejectsNonFilterInputAndUnknownSubcommands() {
    FilterCommandSpec.Error plain =
        assertInstanceOf(FilterCommandSpec.Error.class, parser.parse("hello"));
    assertEquals("Not a /filter command.", plain.message());

    FilterCommandSpec.Error otherSlash =
        assertInstanceOf(FilterCommandSpec.Error.class, parser.parse("/join #java"));
    assertEquals("Not a /filter command.", otherSlash.message());

    FilterCommandSpec.Error unknown =
        assertInstanceOf(FilterCommandSpec.Error.class, parser.parse("/filter mystery"));
    assertEquals(
        "Unknown /filter subcommand: 'mystery'. Try: /filter help", unknown.message());
  }

  @Test
  void reportsTokenizerFailuresWithoutThrowing() {
    FilterCommandSpec.Error quote =
        assertInstanceOf(FilterCommandSpec.Error.class, parser.parse("/filter add 'open"));
    assertEquals("Unterminated quoted string.", quote.message());

    FilterCommandSpec.Error escape =
        assertInstanceOf(FilterCommandSpec.Error.class, parser.parse("/filter add name \\"));
    assertEquals("Dangling escape at end of line.", escape.message());
  }

  @Test
  void dispatchesRuleMutationCommands() {
    FilterCommandSpec.RuleMutation mutation =
        assertInstanceOf(
            FilterCommandSpec.RuleMutation.class,
            parser.parse("/filter add named scope=libera/#java text=ping"));
    FilterRuleMutationCommandSpec.Add add =
        assertInstanceOf(FilterRuleMutationCommandSpec.Add.class, mutation.command());
    assertEquals("named", add.name());
    assertEquals("libera/#java", add.patch().scope());
  }

  @Test
  void dispatchesDisplayAndManagementCommands() {
    FilterCommandSpec.Display display =
        assertInstanceOf(
            FilterCommandSpec.Display.class,
            parser.parse("/filter show on scope=libera/#JAVA"));
    FilterDisplayCommandSpec.Show show =
        assertInstanceOf(FilterDisplayCommandSpec.Show.class, display.command());
    assertEquals("libera/#java", show.scopePattern());

    FilterCommandSpec.Management management =
        assertInstanceOf(
            FilterCommandSpec.Management.class, parser.parse("/filter move alpha top"));
    assertInstanceOf(FilterManagementCommandSpec.Move.class, management.command());
  }

  @Test
  void dispatchesLifecycleCommandsAndMapsChildErrors() {
    FilterCommandSpec.Lifecycle lifecycle =
        assertInstanceOf(
            FilterCommandSpec.Lifecycle.class,
            parser.parse("/filter toggle alpha beta*"));
    FilterLifecycleCommandSpec.Targets targets =
        assertInstanceOf(FilterLifecycleCommandSpec.Targets.class, lifecycle.command());
    assertEquals(FilterTargetActionSpec.TOGGLE, targets.action());
    assertEquals(2, targets.namesOrMasks().size());

    FilterCommandSpec.Error invalid =
        assertInstanceOf(FilterCommandSpec.Error.class, parser.parse("/filter move"));
    assertEquals(
        "Usage: /filter move <name> <pos|top|bottom|up [n]|down [n]|before <other>|after <other>>",
        invalid.message());
  }
}
