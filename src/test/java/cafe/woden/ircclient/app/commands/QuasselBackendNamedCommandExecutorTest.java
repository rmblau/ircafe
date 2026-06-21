package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandRequest;
import cafe.woden.ircclient.app.outbound.backend.QuasselOutboundCommandService;
import java.util.Set;
import org.junit.jupiter.api.Test;

class QuasselBackendNamedCommandExecutorTest {

  private final QuasselOutboundCommandService quasselOutboundCommandService =
      mock(QuasselOutboundCommandService.class);
  private final QuasselBackendNamedCommandExecutor executor =
      new QuasselBackendNamedCommandExecutor(quasselOutboundCommandService);

  @Test
  void exposesHandledCommandNames() {
    Set<String> commandNames = executor.handledCommandNames();
    assertTrue(commandNames.contains(BackendNamedCommandNames.QUASSEL_SETUP));
    assertTrue(commandNames.contains(BackendNamedCommandNames.QUASSEL_NETWORK));
    assertTrue(commandNames.contains(BackendNamedCommandNames.QUASSEL_NETWORK_MANAGER));
    assertFalse(commandNames.contains("join"));
  }

  @Test
  void handleQuasselSetupDelegatesToService() {
    boolean handled =
        executor.handle(
            null, new BackendNamedCommandRequest(BackendNamedCommandNames.QUASSEL_SETUP, "core"));

    assertTrue(handled);
    verify(quasselOutboundCommandService).handleQuasselSetup("core");
  }

  @Test
  void handleQuasselNetworkDelegatesToService() {
    boolean handled =
        executor.handle(
            null, new BackendNamedCommandRequest(BackendNamedCommandNames.QUASSEL_NETWORK, "list"));

    assertTrue(handled);
    verify(quasselOutboundCommandService).handleQuasselNetwork("list");
  }

  @Test
  void handleQuasselNetworkManagerDelegatesToService() {
    boolean handled =
        executor.handle(
            null,
            new BackendNamedCommandRequest(
                BackendNamedCommandNames.QUASSEL_NETWORK_MANAGER, "core"));

    assertTrue(handled);
    verify(quasselOutboundCommandService).handleQuasselNetworkManager("core");
  }

  @Test
  void handleIgnoresUnknownCommands() {
    boolean handled = executor.handle(null, new BackendNamedCommandRequest("unknown", "x"));

    assertFalse(handled);
    verifyNoInteractions(quasselOutboundCommandService);
  }
}
