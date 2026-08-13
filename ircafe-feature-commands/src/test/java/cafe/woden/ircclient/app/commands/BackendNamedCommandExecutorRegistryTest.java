package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutionContext;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandRequest;
import cafe.woden.ircclient.app.commands.spi.SlashCommandTargetView;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BackendNamedCommandExecutorRegistryTest {

  private static final BackendNamedCommandExecutionContext TEST_CONTEXT =
      new BackendNamedCommandExecutionContext() {
        @Override
        public SlashCommandTargetView activeTarget() {
          return null;
        }

        @Override
        public SlashCommandTargetView safeStatusTarget() {
          return new SlashCommandTargetView("test", "status");
        }

        @Override
        public boolean isConnected(String serverId) {
          return true;
        }

        @Override
        public void appendStatus(SlashCommandTargetView target, String prefix, String message) {}

        @Override
        public void appendError(SlashCommandTargetView target, String prefix, String message) {}

        @Override
        public void ensureTargetExists(SlashCommandTargetView target) {}

        @Override
        public void selectTarget(SlashCommandTargetView target) {}

        @Override
        public void sendRaw(String serverId, String line) {}
      };

  @Test
  void dispatchesMatchingBackendNamedCommand() {
    BackendNamedCommandExecutorRegistry registry =
        new BackendNamedCommandExecutorRegistry(List.of(executor("backendexec")));

    assertTrue(registry.handle(TEST_CONTEXT, new BackendNamedCommandRequest("/BACKENDEXEC", "hi")));
  }

  @Test
  void returnsFalseForMissingContextRequestOrExecutor() {
    BackendNamedCommandExecutorRegistry registry =
        new BackendNamedCommandExecutorRegistry(List.of(executor("backendexec")));

    assertFalse(registry.handle(null, new BackendNamedCommandRequest("backendexec", "")));
    assertFalse(registry.handle(TEST_CONTEXT, null));
    assertFalse(registry.handle(TEST_CONTEXT, new BackendNamedCommandRequest("missing", "")));
  }

  @Test
  void rejectsDuplicateCommandRegistrations() {
    assertThrows(
        IllegalStateException.class,
        () ->
            new BackendNamedCommandExecutorRegistry(
                List.of(executor("backendexec"), executor("/BACKENDEXEC"))));
  }

  @Test
  void rejectsReservedCommandRegistrations() {
    assertThrows(
        IllegalStateException.class,
        () -> new BackendNamedCommandExecutorRegistry(List.of(executor("/join"))));
  }

  private static BackendNamedCommandExecutor executor(String commandName) {
    return new BackendNamedCommandExecutor() {
      @Override
      public Set<String> handledCommandNames() {
        return Set.of(commandName);
      }

      @Override
      public boolean handle(
          BackendNamedCommandExecutionContext context, BackendNamedCommandRequest command) {
        return command != null && "backendexec".equals(command.command());
      }
    };
  }
}
