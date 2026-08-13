package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutionContext;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandRequest;
import cafe.woden.ircclient.app.commands.spi.SlashCommandTargetView;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackendNamedCommandExecutorCatalogTest {

  private static final String SPI_PLUGIN_EXECUTOR_CLASS =
      "plugin.commands.PluginSpiBackendNamedCommandExecutor";

  private static final BackendNamedCommandExecutionContext TEST_CONTEXT =
      new BackendNamedCommandExecutionContext() {
        private final SlashCommandTargetView statusTarget =
            new SlashCommandTargetView("test", "status");

        @Override
        public SlashCommandTargetView activeTarget() {
          return null;
        }

        @Override
        public SlashCommandTargetView safeStatusTarget() {
          return statusTarget;
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

  @TempDir Path tempDir;

  @Test
  void loadsExecutionProvidersFromInstalledPluginsPort() {
    BackendNamedCommandExecutorCatalog catalog =
        new BackendNamedCommandExecutorCatalog(
            new FakeInstalledPluginsPort(List.of(new PluginProvidedBackendNamedCommandExecutor())),
            List.of());
    try {
      assertTrue(
          catalog.handle(TEST_CONTEXT, new ParsedInput.BackendNamed("backendexec", "hello")));
    } finally {
      catalog.shutdown();
    }
  }

  @Test
  void loadsExecutionProvidersFromApplicationClasspathWithoutSpringSeed() throws Exception {
    Path classpathJar = tempDir.resolve("backendexec-classpath.jar");
    CompiledPluginJarSupport.writePluginJar(
        classpathJar,
        SPI_PLUGIN_EXECUTOR_CLASS,
        pluginSpiExecutorSource(),
        cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "backend-named-executor-classpath-test", "1.0.0"));
    ClassLoader previousContextClassLoader = Thread.currentThread().getContextClassLoader();
    try (URLClassLoader classLoader =
        URLClassLoader.newInstance(
            new URL[] {classpathJar.toUri().toURL()}, previousContextClassLoader)) {
      Thread.currentThread().setContextClassLoader(classLoader);
      BackendNamedCommandExecutorCatalog catalog =
          new BackendNamedCommandExecutorCatalog(
              new FakeInstalledPluginsPort(List.of()), List.of());
      try {
        assertTrue(
            catalog.handle(TEST_CONTEXT, new ParsedInput.BackendNamed("backendexec", "hello")));
      } finally {
        catalog.shutdown();
      }
    } finally {
      Thread.currentThread().setContextClassLoader(previousContextClassLoader);
    }
  }

  @Test
  void loadsExecutionProvidersFromPluginDirectoryJar() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    writePluginJar(pluginDir.resolve("backendexec.jar"));

    BackendNamedCommandExecutorCatalog catalog =
        BackendNamedCommandExecutorCatalog.installed(
            pluginDir, BackendNamedCommandExecutorCatalogTest.class.getClassLoader());
    try {
      assertTrue(
          catalog.handle(TEST_CONTEXT, new ParsedInput.BackendNamed("backendexec", "hello")));
    } finally {
      catalog.shutdown();
    }
  }

  @Test
  void loadsExecutionProviderSpiFromPluginDirectoryJar() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("backendexec-spi.jar"),
        SPI_PLUGIN_EXECUTOR_CLASS,
        pluginSpiExecutorSource(),
        cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("backend-named-executor-spi-test", "1.0.0"));

    BackendNamedCommandExecutorCatalog catalog =
        BackendNamedCommandExecutorCatalog.installed(
            pluginDir, BackendNamedCommandExecutorCatalogTest.class.getClassLoader());
    try {
      assertTrue(
          catalog.handle(TEST_CONTEXT, new ParsedInput.BackendNamed("backendexec", "hello")));
    } finally {
      catalog.shutdown();
    }
  }

  @Test
  void loadsExecutionProvidersFromPluginsNextToRuntimeConfig() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("backendexec.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    BackendNamedCommandExecutorCatalog catalog =
        new BackendNamedCommandExecutorCatalog(runtimeConfigPathPort, List.of());
    try {
      assertTrue(
          catalog.handle(TEST_CONTEXT, new ParsedInput.BackendNamed("backendexec", "hello")));
    } finally {
      catalog.shutdown();
    }
  }

  @Test
  void duplicateExecutionCommandRegistrationsFailFast() {
    BackendNamedCommandExecutor first =
        new BackendNamedCommandExecutor() {
          @Override
          public Set<String> handledCommandNames() {
            return Set.of("backendexec");
          }

          @Override
          public boolean handle(
              BackendNamedCommandExecutionContext context, BackendNamedCommandRequest command) {
            return false;
          }
        };
    BackendNamedCommandExecutor second =
        new BackendNamedCommandExecutor() {
          @Override
          public Set<String> handledCommandNames() {
            return Set.of("backendexec");
          }

          @Override
          public boolean handle(
              BackendNamedCommandExecutionContext context, BackendNamedCommandRequest command) {
            return false;
          }
        };

    assertThrows(
        IllegalStateException.class,
        () -> BackendNamedCommandExecutorCatalog.fromExecutors(List.of(first, second)));
  }

  private static void writePluginJar(Path jarPath) throws Exception {
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        SPI_PLUGIN_EXECUTOR_CLASS,
        pluginSpiExecutorSource(),
        BackendNamedCommandExecutor.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("backend-named-executor-test", "1.0.0"));
  }

  private static String pluginSpiExecutorSource() {
    return """
        package plugin.commands;

        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutionContext;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandRequest;
        import java.util.Set;

        public final class PluginSpiBackendNamedCommandExecutor
            implements BackendNamedCommandExecutor {
          @Override
          public Set<String> handledCommandNames() {
            return Set.of("backendexec");
          }

          @Override
          public boolean handle(
              BackendNamedCommandExecutionContext context,
              BackendNamedCommandRequest command) {
            return command != null && "backendexec".equals(command.command());
          }
        }
        """;
  }

  private static final class FakeInstalledPluginsPort implements InstalledPluginsPort {
    private final List<?> pluginServices;

    private FakeInstalledPluginsPort(List<?> pluginServices) {
      this.pluginServices = List.copyOf(pluginServices);
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services =
          new ArrayList<>(java.util.Objects.requireNonNullElse(builtInServices, List.of()));
      for (Object pluginService : pluginServices) {
        if (serviceType.isInstance(pluginService)) {
          services.add(serviceType.cast(pluginService));
        }
      }
      return List.copyOf(services);
    }
  }

  public static final class PluginProvidedBackendNamedCommandExecutor
      implements BackendNamedCommandExecutor {

    @Override
    public Set<String> handledCommandNames() {
      return Set.of("backendexec");
    }

    @Override
    public boolean handle(
        BackendNamedCommandExecutionContext context, BackendNamedCommandRequest command) {
      return command != null && "backendexec".equals(command.command());
    }
  }
}
