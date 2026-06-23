package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.builtins.BuiltInQuasselBackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.BuiltInBackendNamedCommandNames;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BuiltInQuasselBackendNamedCommandHandlerTest {

  private final BuiltInQuasselBackendNamedCommandHandler handler =
      new BuiltInQuasselBackendNamedCommandHandler();

  @Test
  void exposesSupportedCommandNames() {
    Set<String> commandNames = handler.supportedCommandNames();
    assertTrue(commandNames.contains("quasselsetup"));
    assertTrue(commandNames.contains("qsetup"));
    assertTrue(commandNames.contains("quasselnet"));
    assertTrue(commandNames.contains("qnet"));
  }

  @Test
  void parsesQuasselSetupAliasToCanonicalCommand() {
    BackendNamedCommandParseResult parsed = handler.parse("/qsetup core", "qsetup");

    assertEquals(BuiltInBackendNamedCommandNames.QUASSEL_SETUP, parsed.command());
    assertEquals("core", parsed.args());
  }

  @Test
  void parsesQuasselNetworkAliasToCanonicalCommand() {
    BackendNamedCommandParseResult parsed = handler.parse("/qnet list", "qnet");

    assertEquals(BuiltInBackendNamedCommandNames.QUASSEL_NETWORK, parsed.command());
    assertEquals("list", parsed.args());
  }

  @Test
  void exposesTopicHelpLines() {
    Map<String, List<String>> topics = handler.topicHelpLines();

    assertTrue(topics.get("qsetup").contains("Usage: /quasselsetup [serverId]"));
    assertTrue(
        topics
            .get("quasselnet")
            .contains("Usage: /quasselnet [serverId] list|connect|disconnect|remove|add|edit ..."));
  }
}
