package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutionContext;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandRequest;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CommandProviderCatalogTest {

  @Test
  void normalizesParseStrategiesByProviderClass() {
    SlashCommandParseStrategy first = new TestParseStrategy("first");
    SlashCommandParseStrategy duplicate = new TestParseStrategy("duplicate");
    SlashCommandParseStrategy other = new OtherParseStrategy();

    assertEquals(
        List.of(first, other),
        CommandProviderCatalog.slashCommandParseStrategies(List.of(first, duplicate, other)));
    assertTrue(CommandProviderCatalog.slashCommandParseStrategies(null).isEmpty());
  }

  @Test
  void normalizesPresentationContributorsByProviderClass() {
    SlashCommandPresentationContributor first = new TestPresentationContributor("first");
    SlashCommandPresentationContributor duplicate = new TestPresentationContributor("duplicate");
    SlashCommandPresentationContributor other = new OtherPresentationContributor();

    assertEquals(
        List.of(first, other),
        CommandProviderCatalog.slashCommandPresentationContributors(
            Arrays.asList(first, duplicate, null, other)));
  }

  @Test
  void normalizesBackendNamedHandlersByProviderClass() {
    BackendNamedCommandHandler first = new TestBackendNamedHandler("first");
    BackendNamedCommandHandler duplicate = new TestBackendNamedHandler("duplicate");
    BackendNamedCommandHandler other = new OtherBackendNamedHandler();

    assertEquals(
        List.of(first, other),
        CommandProviderCatalog.backendNamedCommandHandlers(List.of(first, duplicate, other)));
  }

  @Test
  void normalizesBackendNamedExecutorsByProviderClass() {
    BackendNamedCommandExecutor first = new TestBackendNamedExecutor("first");
    BackendNamedCommandExecutor duplicate = new TestBackendNamedExecutor("duplicate");
    BackendNamedCommandExecutor other = new OtherBackendNamedExecutor();

    assertEquals(
        List.of(first, other),
        CommandProviderCatalog.backendNamedCommandExecutors(List.of(first, duplicate, other)));
  }

  private record TestParseStrategy(String name) implements SlashCommandParseStrategy {
    @Override
    public SlashCommandParseResult tryParse(String line) {
      return null;
    }
  }

  private static final class OtherParseStrategy implements SlashCommandParseStrategy {
    @Override
    public SlashCommandParseResult tryParse(String line) {
      return null;
    }
  }

  private record TestPresentationContributor(String name)
      implements SlashCommandPresentationContributor {}

  private static final class OtherPresentationContributor
      implements SlashCommandPresentationContributor {}

  private record TestBackendNamedHandler(String name) implements BackendNamedCommandHandler {
    @Override
    public Set<String> supportedCommandNames() {
      return Set.of(name);
    }

    @Override
    public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
      return null;
    }
  }

  private static final class OtherBackendNamedHandler implements BackendNamedCommandHandler {
    @Override
    public Set<String> supportedCommandNames() {
      return Set.of("other");
    }

    @Override
    public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
      return null;
    }
  }

  private record TestBackendNamedExecutor(String name) implements BackendNamedCommandExecutor {
    @Override
    public Set<String> handledCommandNames() {
      return Set.of(name);
    }

    @Override
    public boolean handle(
        BackendNamedCommandExecutionContext context, BackendNamedCommandRequest command) {
      return false;
    }
  }

  private static final class OtherBackendNamedExecutor implements BackendNamedCommandExecutor {
    @Override
    public Set<String> handledCommandNames() {
      return Set.of("other");
    }

    @Override
    public boolean handle(
        BackendNamedCommandExecutionContext context, BackendNamedCommandRequest command) {
      return false;
    }
  }
}
