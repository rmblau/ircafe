package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BackendNamedCommandHandlerRegistryTest {

  @Test
  void parsesMatchingBackendNamedCommand() {
    BackendNamedCommandHandlerRegistry registry =
        new BackendNamedCommandHandlerRegistry(List.of(handler("backendping")));

    BackendNamedCommandParseResult parsed = registry.parse("/BackendPing hello");

    assertEquals("backendping", parsed.command());
    assertEquals("hello", parsed.args());
  }

  @Test
  void returnsNullForUnknownOrNonCommandInput() {
    BackendNamedCommandHandlerRegistry registry =
        new BackendNamedCommandHandlerRegistry(List.of(handler("backendping")));

    assertNull(registry.parse("plain message"));
    assertNull(registry.parse("/unknown hello"));
  }

  @Test
  void aggregatesPresentationMetadata() {
    BackendNamedCommandHandlerRegistry registry =
        new BackendNamedCommandHandlerRegistry(
            List.of(
                handler(
                    "backendping",
                    List.of(
                        new SlashCommandDescriptor("/backendping", "Backend ping"),
                        new SlashCommandDescriptor("/backendping", "Duplicate ignored")),
                    List.of("  Backend commands are available.  ", ""),
                    Map.of("/backendping", List.of("Usage: /backendping [args]", "")))));

    assertEquals(
        List.of(new SlashCommandDescriptor("/backendping", "Backend ping")),
        registry.autocompleteCommands());
    assertEquals(List.of("Backend commands are available."), registry.generalHelpLines());
    assertEquals(
        List.of("Usage: /backendping [args]"), registry.topicHelpLines().get("backendping"));
  }

  @Test
  void rejectsDuplicateCommandRegistrations() {
    assertThrows(
        IllegalStateException.class,
        () ->
            new BackendNamedCommandHandlerRegistry(
                List.of(handler("backendping"), handler("/BACKENDPING"))));
  }

  @Test
  void rejectsReservedCommandRegistrations() {
    assertThrows(
        IllegalStateException.class,
        () -> new BackendNamedCommandHandlerRegistry(List.of(handler("/join"))));
  }

  private static BackendNamedCommandHandler handler(String commandName) {
    return handler(commandName, List.of(), List.of(), Map.of());
  }

  private static BackendNamedCommandHandler handler(
      String commandName,
      List<SlashCommandDescriptor> autocompleteCommands,
      List<String> generalHelpLines,
      Map<String, List<String>> topicHelpLines) {
    return new BackendNamedCommandHandler() {
      @Override
      public Set<String> supportedCommandNames() {
        return Set.of(commandName);
      }

      @Override
      public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
        String commandToken = "/" + matchedCommandName;
        String args =
            line.length() > commandToken.length()
                ? line.substring(commandToken.length()).trim()
                : "";
        return new BackendNamedCommandParseResult(matchedCommandName, args);
      }

      @Override
      public List<SlashCommandDescriptor> autocompleteCommands() {
        return autocompleteCommands;
      }

      @Override
      public List<String> generalHelpLines() {
        return generalHelpLines;
      }

      @Override
      public Map<String, List<String>> topicHelpLines() {
        return topicHelpLines;
      }
    };
  }
}
