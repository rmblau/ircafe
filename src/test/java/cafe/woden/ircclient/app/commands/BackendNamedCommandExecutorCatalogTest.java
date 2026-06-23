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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
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

  private static void writePluginJar(Path jarPath) throws IOException {
    Manifest manifest = new Manifest();
    Attributes attributes = manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    for (var entry :
        CompiledPluginJarSupport.compatibleManifest("backend-named-executor-test", "1.0.0")
            .entrySet()) {
      attributes.putValue(entry.getKey(), entry.getValue());
    }
    try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
      out.putNextEntry(
          new JarEntry("META-INF/services/" + BackendNamedCommandExecutor.class.getName()));
      out.write(
          (PluginProvidedBackendNamedCommandExecutor.class.getName() + System.lineSeparator())
              .getBytes(StandardCharsets.UTF_8));
      out.closeEntry();
    }
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
