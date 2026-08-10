package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InstalledPluginProblem;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Ircv3MessageMutationRuntimeCatalogTest {

  @Test
  void loadsFocusedBuiltInRuntimeProvidersFromApplicationClasspath() {
    Ircv3MessageMutationRuntimeCatalog catalog =
        Ircv3MessageMutationRuntimeCatalog.applicationClasspath();
    Ircv3MessageMutationRequest request =
        new Ircv3MessageMutationRequest("#ircafe", "abc 123;xyz\\tail", "hello");

    assertEquals(
        List.of("reply", "reactions", "message-edit", "message-redaction"), catalog.providerIds());
    assertEquals(
        "@+reply=abc\\s123\\:xyz\\\\tail PRIVMSG #ircafe :hello",
        catalog.build(Ircv3MessageMutationOperation.REPLY, request));
    assertTrue(catalog.supports(Ircv3MessageMutationOperation.REACT));
    assertTrue(catalog.supports(Ircv3MessageMutationOperation.UNREACT));
    assertTrue(catalog.supports(Ircv3MessageMutationOperation.EDIT));
    assertTrue(catalog.supports(Ircv3MessageMutationOperation.REDACT));
  }

  @Test
  void higherPriorityProviderReplacesBuiltInOperation() {
    Ircv3MessageMutationRuntimeCatalog catalog =
        Ircv3MessageMutationRuntimeCatalog.fromProviders(
            List.of(provider("built-in", 0, "built-in"), provider("plugin", 100, "plugin")));

    assertEquals(
        "plugin",
        catalog.build(
            Ircv3MessageMutationOperation.REPLY,
            new Ircv3MessageMutationRequest("#ircafe", "m-1", "hello")));
    assertEquals(List.of("plugin"), catalog.providerIds());
  }

  @Test
  void equalPriorityOperationConflictsAreRejected() {
    assertThrows(
        IllegalStateException.class,
        () ->
            Ircv3MessageMutationRuntimeCatalog.fromProviders(
                List.of(provider("one", 10, "one"), provider("two", 10, "two"))));
  }

  @Test
  void equalPriorityProvidersWithTheSameIdStillConflict() {
    assertThrows(
        IllegalStateException.class,
        () ->
            Ircv3MessageMutationRuntimeCatalog.fromProviders(
                List.of(provider("same", 10, "one"), provider("same", 10, "two"))));
  }

  @Test
  void installedProviderConflictIsReportedAndBuiltInsRemainAvailable() {
    RecordingInstalledPlugins plugins =
        new RecordingInstalledPlugins(provider("conflict", 0, "conflict"));

    Ircv3MessageMutationRuntimeCatalog catalog =
        Ircv3MessageMutationRuntimeCatalog.fromInstalledServices(plugins);

    assertTrue(catalog.supports(Ircv3MessageMutationOperation.REPLY));
    assertEquals(1, plugins.problems.size());
    assertTrue(plugins.problems.getFirst().summary().contains("message-mutation"));
  }

  private static Ircv3MessageMutationProvider provider(
      String providerId, int priority, String response) {
    return new Ircv3MessageMutationProvider() {
      @Override
      public String providerId() {
        return providerId;
      }

      @Override
      public int priority() {
        return priority;
      }

      @Override
      public Set<Ircv3MessageMutationOperation> operations() {
        return Set.of(Ircv3MessageMutationOperation.REPLY);
      }

      @Override
      public String build(
          Ircv3MessageMutationOperation operation, Ircv3MessageMutationRequest request) {
        return response;
      }
    };
  }

  private static final class RecordingInstalledPlugins implements InstalledPluginsPort {
    private final Ircv3MessageMutationProvider provider;
    private final List<InstalledPluginProblem> problems = new ArrayList<>();

    private RecordingInstalledPlugins(Ircv3MessageMutationProvider provider) {
      this.provider = provider;
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType.isInstance(provider)) {
        services.add(serviceType.cast(provider));
      }
      return List.copyOf(services);
    }

    @Override
    public void recordPluginProblem(InstalledPluginProblem problem) {
      problems.add(problem);
    }
  }
}
