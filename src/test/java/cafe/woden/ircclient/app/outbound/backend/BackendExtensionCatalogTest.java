package cafe.woden.ircclient.app.outbound.backend;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.api.BackendEditorProfileSpec;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.BackendDescriptorCatalog;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackendExtensionCatalogTest {
  private static final BackendDescriptorCatalog BACKEND_DESCRIPTORS =
      BackendDescriptorCatalog.builtIns();

  @TempDir Path tempDir;

  @Test
  void resolvesBuiltInBackendStrategiesFromExtensions() {
    BackendExtensionCatalog catalog =
        BackendExtensionCatalog.fromExtensions(
            java.util.List.of(
                new IrcBackendExtension(),
                new MatrixBackendExtension(),
                new QuasselBackendExtension()));

    assertInstanceOf(
        IrcMessageMutationOutboundCommands.class, catalog.messageMutationCommandsFor("irc"));
    assertInstanceOf(
        MatrixUploadCommandTranslationHandler.class, catalog.uploadTranslationHandlerFor("matrix"));
    assertInstanceOf(
        QuasselMessageMutationOutboundCommands.class,
        catalog.messageMutationCommandsFor("quassel-core"));
    assertTrue(catalog.featureAdapterFor("matrix").supportsSemanticUpload());
    assertTrue(catalog.featureAdapterFor("quassel-core").supportsQuasselCoreCommands());
    assertNull(catalog.uploadTranslationHandlerFor("quassel-core"));
  }

  @Test
  void rejectsDuplicateBackendExtensions() {
    assertThrows(
        IllegalStateException.class,
        () ->
            BackendExtensionCatalog.fromExtensions(
                java.util.List.of(new IrcBackendExtension(), new DuplicateIrcBackendExtension())));
  }

  @Test
  void rejectsContributionBackendMismatch() {
    assertThrows(
        IllegalStateException.class,
        () ->
            BackendExtensionCatalog.fromExtensions(
                java.util.List.of(new MismatchedBackendExtension())));
  }

  @Test
  void resolvesCustomBackendIdExtensions() {
    BackendExtensionCatalog catalog =
        BackendExtensionCatalog.fromExtensions(java.util.List.of(new PluginBackendExtension()));

    assertTrue(catalog.featureAdapterFor("plugin-backend").supportsSemanticUpload());
    assertTrue(catalog.availableBackendIds().contains("plugin-backend"));
    assertTrue(
        catalog.availableBackendEditorProfiles().stream()
            .anyMatch(profile -> "Plugin Backend".equals(profile.displayName())));
  }

  @Test
  void loadsBackendExtensionsFromInstalledPluginsPort() {
    BackendExtensionCatalog catalog =
        new BackendExtensionCatalog(
            BackendExtensionCatalogState.fromInstalledServices(
                java.util.List.of(new IrcBackendExtension()),
                new FakeInstalledPluginsPort(java.util.List.of(new PluginBackendExtension()))));

    assertInstanceOf(
        IrcMessageMutationOutboundCommands.class, catalog.messageMutationCommandsFor("irc"));
    assertTrue(catalog.featureAdapterFor("plugin-backend").supportsSemanticUpload());
    assertTrue(catalog.availableBackendIds().contains("plugin-backend"));
    assertTrue("Plugin Backend".equals(catalog.backendDisplayName("plugin-backend")));
  }

  @Test
  void loadsBackendExtensionsFromPluginDirectoryJar() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-backend.jar"));

    BackendExtensionCatalog catalog =
        BackendExtensionCatalog.installed(
            pluginDir, BackendExtensionCatalogTest.class.getClassLoader());
    try {
      assertInstanceOf(
          IrcMessageMutationOutboundCommands.class, catalog.messageMutationCommandsFor("irc"));
      assertInstanceOf(
          MatrixUploadCommandTranslationHandler.class,
          catalog.uploadTranslationHandlerFor("matrix"));
      assertInstanceOf(
          QuasselMessageMutationOutboundCommands.class,
          catalog.messageMutationCommandsFor("quassel-core"));
      assertTrue(catalog.featureAdapterFor("plugin-backend").supportsSemanticUpload());
      assertTrue(catalog.availableBackendIds().contains("plugin-backend"));
      assertTrue("Plugin Backend".equals(catalog.backendDisplayName("plugin-backend")));
    } finally {
      catalog.shutdown();
    }
  }

  @Test
  void loadsBackendExtensionsFromPluginsNextToRuntimeConfig() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-backend.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    BackendExtensionCatalog catalog =
        BackendExtensionCatalog.installed(
            runtimeConfigPathPort, BackendExtensionCatalogTest.class.getClassLoader());
    try {
      assertInstanceOf(
          IrcMessageMutationOutboundCommands.class, catalog.messageMutationCommandsFor("irc"));
      assertInstanceOf(
          MatrixUploadCommandTranslationHandler.class,
          catalog.uploadTranslationHandlerFor("matrix"));
      assertInstanceOf(
          QuasselMessageMutationOutboundCommands.class,
          catalog.messageMutationCommandsFor("quassel-core"));
      assertTrue(catalog.featureAdapterFor("plugin-backend").supportsSemanticUpload());
      assertTrue(catalog.availableBackendIds().contains("plugin-backend"));
      assertTrue("Plugin Backend".equals(catalog.backendDisplayName("plugin-backend")));
    } finally {
      catalog.shutdown();
    }
  }

  private static final class FakeInstalledPluginsPort implements InstalledPluginsPort {
    private final java.util.List<?> pluginServices;

    private FakeInstalledPluginsPort(java.util.List<?> pluginServices) {
      this.pluginServices = java.util.List.copyOf(pluginServices);
    }

    @Override
    public <T> java.util.List<T> loadInstalledServices(
        Class<T> serviceType, java.util.List<T> builtInServices) {
      java.util.ArrayList<T> services =
          new java.util.ArrayList<>(
              java.util.Objects.requireNonNullElse(builtInServices, java.util.List.of()));
      for (Object pluginService : pluginServices) {
        if (serviceType.isInstance(pluginService)) {
          services.add(serviceType.cast(pluginService));
        }
      }
      return java.util.List.copyOf(services);
    }
  }

  private static void writePluginJar(Path jarPath) throws IOException {
    Manifest manifest = new Manifest();
    Attributes attributes = manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    for (var entry :
        CompiledPluginJarSupport.compatibleManifest("plugin-backend", "1.0.0").entrySet()) {
      attributes.putValue(entry.getKey(), entry.getValue());
    }
    try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
      out.putNextEntry(new JarEntry("META-INF/services/" + BackendExtension.class.getName()));
      out.write(
          (PluginBackendExtension.class.getName() + System.lineSeparator())
              .getBytes(StandardCharsets.UTF_8));
      out.closeEntry();
    }
  }

  private static final class DuplicateIrcBackendExtension implements BackendExtension {
    @Override
    public String backendId() {
      return BACKEND_DESCRIPTORS.idFor(IrcProperties.Server.Backend.IRC);
    }
  }

  private static final class MismatchedBackendExtension implements BackendExtension {
    @Override
    public String backendId() {
      return BACKEND_DESCRIPTORS.idFor(IrcProperties.Server.Backend.MATRIX);
    }

    @Override
    public OutboundBackendFeatureAdapter featureAdapter() {
      return new QuasselOutboundBackendFeatureAdapter();
    }
  }

  public static final class PluginBackendExtension implements BackendExtension {
    @Override
    public String backendId() {
      return "plugin-backend";
    }

    @Override
    public OutboundBackendFeatureAdapter featureAdapter() {
      return new OutboundBackendFeatureAdapter() {
        @Override
        public String backendId() {
          return "plugin-backend";
        }

        @Override
        public boolean supportsSemanticUpload() {
          return true;
        }
      };
    }

    @Override
    public BackendEditorProfileSpec editorProfile() {
      return new BackendEditorProfileSpec(
          "plugin-backend",
          "Plugin Backend",
          7000,
          7443,
          true,
          false,
          true,
          true,
          false,
          "plugin-user",
          "Host",
          "Server password",
          "Nick",
          "Login",
          "Real name",
          "Use TLS",
          "Plugin backend connection.",
          "Plugin backend auth is configured directly.",
          "(optional)",
          "plugin.example.net",
          "plugin-user",
          "PluginUser",
          "Plugin User");
    }
  }
}
