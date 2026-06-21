package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.builtins.BuiltInQuasselBackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
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

    assertEquals(BackendNamedCommandNames.QUASSEL_SETUP, parsed.command());
    assertEquals("core", parsed.args());
  }

  @Test
  void parsesQuasselNetworkAliasToCanonicalCommand() {
    BackendNamedCommandParseResult parsed = handler.parse("/qnet list", "qnet");

    assertEquals(BackendNamedCommandNames.QUASSEL_NETWORK, parsed.command());
    assertEquals("list", parsed.args());
  }
}
