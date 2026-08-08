package cafe.woden.ircclient.app.outbound.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.BackendDescriptorCatalog;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationProvider;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackendExtensionCatalogTest {
  private static final BackendDescriptorCatalog BACKEND_DESCRIPTORS =
      BackendDescriptorCatalog.builtIns();
  private static final Ircv3MessageMutationRuntimeCatalog TEST_MUTATION_CATALOG =
      Ircv3MessageMutationRuntimeCatalog.applicationClasspath();
  private static final String PLUGIN_BACKEND_EXTENSION_CLASS =
      "plugin.backend.PluginBackendExtension";
  private static final String PLUGIN_MESSAGE_MUTATION_PROVIDER_CLASS =
      "plugin.ircv3.PluginReplyRuntimeProvider";

  @TempDir Path tempDir;

  @Test
  void factoriesRequireExplicitMutationRuntimeCatalogs() throws Exception {
    assertThrows(
        NoSuchMethodException.class,
        () -> BackendExtensionCatalog.class.getDeclaredMethod("fromApplicationClasspath"));
    assertThrows(
        NoSuchMethodException.class,
        () ->
            BackendExtensionCatalog.class.getDeclaredMethod(
                "fromExtensions", java.util.List.class));

    assertPublicStaticFactory(
        BackendExtensionCatalog.class.getDeclaredMethod(
            "fromApplicationClasspath", Ircv3MessageMutationRuntimeCatalog.class));
    assertPublicStaticFactory(
        BackendExtensionCatalog.class.getDeclaredMethod(
            "fromExtensions", java.util.List.class, Ircv3MessageMutationRuntimeCatalog.class));
  }

  @Test
  void resolvesBuiltInBackendStrategiesFromExtensions() {
    BackendExtensionCatalog catalog =
        BackendExtensionCatalog.fromExtensions(
            java.util.List.of(
                new IrcBackendExtension(),
                new MatrixBackendExtension(),
                new QuasselBackendExtension()),
            TEST_MUTATION_CATALOG);

    assertInstanceOf(
        Ircv3MessageMutationOutboundCommands.class, catalog.messageMutationCommandsFor("irc"));
    assertInstanceOf(
        MatrixUploadCommandTranslationHandler.class, catalog.uploadTranslationHandlerFor("matrix"));
    assertInstanceOf(
        Ircv3MessageMutationOutboundCommands.class,
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
                java.util.List.of(new IrcBackendExtension(), new DuplicateIrcBackendExtension()),
                TEST_MUTATION_CATALOG));
  }

  @Test
  void rejectsContributionBackendMismatch() {
    assertThrows(
        IllegalStateException.class,
        () ->
            BackendExtensionCatalog.fromExtensions(
                java.util.List.of(new MismatchedBackendExtension()), TEST_MUTATION_CATALOG));
  }

  @Test
  void resolvesCustomBackendIdExtensions() {
    BackendExtensionCatalog catalog =
        BackendExtensionCatalog.fromExtensions(
            java.util.List.of(new PluginBackendExtension()), TEST_MUTATION_CATALOG);

    assertTrue(catalog.featureAdapterFor("plugin-backend").supportsSemanticUpload());
    assertTrue(catalog.availableBackendIds().contains("plugin-backend"));
    assertTrue(
        catalog.availableBackendEditorProfiles().stream()
            .anyMatch(profile -> "Plugin Backend".equals(profile.displayName())));
  }

  @Test
  void loadsBuiltInBackendExtensionsFromApplicationClasspath() {
    BackendExtensionCatalog catalog =
        BackendExtensionCatalog.fromApplicationClasspath(TEST_MUTATION_CATALOG);

    assertInstanceOf(
        Ircv3MessageMutationOutboundCommands.class, catalog.messageMutationCommandsFor("irc"));
    assertInstanceOf(
        MatrixUploadCommandTranslationHandler.class, catalog.uploadTranslationHandlerFor("matrix"));
    assertInstanceOf(
        Ircv3MessageMutationOutboundCommands.class,
        catalog.messageMutationCommandsFor("quassel-core"));
    assertTrue(catalog.featureAdapterFor("matrix").supportsSemanticUpload());
    assertTrue(catalog.featureAdapterFor("quassel-core").supportsQuasselCoreCommands());
  }

  @Test
  void loadsBackendExtensionsFromInstalledPluginsPortWithoutReloadingRuntimeCatalogs() {
    FakeInstalledPluginsPort installedPlugins =
        new FakeInstalledPluginsPort(java.util.List.of(new PluginBackendExtension()));
    BackendExtensionCatalog catalog =
        new BackendExtensionCatalog(
            BackendExtensionCatalogState.fromInstalledServices(
                installedPlugins, TEST_MUTATION_CATALOG));

    assertEquals(1, installedPlugins.loadCount(BackendExtension.class));
    assertEquals(0, installedPlugins.loadCount(Ircv3MessageMutationProvider.class));
    assertInstanceOf(
        Ircv3MessageMutationOutboundCommands.class, catalog.messageMutationCommandsFor("irc"));
    assertTrue(catalog.featureAdapterFor("plugin-backend").supportsSemanticUpload());
    assertTrue(catalog.availableBackendIds().contains("plugin-backend"));
    assertTrue("Plugin Backend".equals(catalog.backendDisplayName("plugin-backend")));
  }

  @Test
  void loadsBackendExtensionsFromPluginDirectoryJar() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-backend.jar"));
    writeMutationPluginJar(pluginDir.resolve("plugin-reply-runtime.jar"));

    BackendExtensionCatalog catalog =
        BackendExtensionCatalog.installed(
            pluginDir, BackendExtensionCatalogTest.class.getClassLoader());
    try {
      assertInstanceOf(
          Ircv3MessageMutationOutboundCommands.class, catalog.messageMutationCommandsFor("irc"));
      assertInstanceOf(
          MatrixUploadCommandTranslationHandler.class,
          catalog.uploadTranslationHandlerFor("matrix"));
      assertInstanceOf(
          Ircv3MessageMutationOutboundCommands.class,
          catalog.messageMutationCommandsFor("quassel-core"));
      assertTrue(catalog.featureAdapterFor("plugin-backend").supportsSemanticUpload());
      assertTrue(catalog.availableBackendIds().contains("plugin-backend"));
      assertTrue("Plugin Backend".equals(catalog.backendDisplayName("plugin-backend")));
      assertEquals(
          "@draft/reply=msg-1 privmsg #ircafe :hello",
          catalog
              .messageMutationCommandsFor("irc")
              .buildReplyRawLine(
                  new MessageMutationTargetView("server", "#ircafe"), "msg-1", "hello"));
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
          Ircv3MessageMutationOutboundCommands.class, catalog.messageMutationCommandsFor("irc"));
      assertInstanceOf(
          MatrixUploadCommandTranslationHandler.class,
          catalog.uploadTranslationHandlerFor("matrix"));
      assertInstanceOf(
          Ircv3MessageMutationOutboundCommands.class,
          catalog.messageMutationCommandsFor("quassel-core"));
      assertTrue(catalog.featureAdapterFor("plugin-backend").supportsSemanticUpload());
      assertTrue(catalog.availableBackendIds().contains("plugin-backend"));
      assertTrue("Plugin Backend".equals(catalog.backendDisplayName("plugin-backend")));
    } finally {
      catalog.shutdown();
    }
  }

  private static void assertPublicStaticFactory(Method method) {
    assertTrue(Modifier.isPublic(method.getModifiers()), method.getName());
    assertTrue(Modifier.isStatic(method.getModifiers()), method.getName());
  }

  private static final class FakeInstalledPluginsPort implements InstalledPluginsPort {
    private final java.util.List<?> pluginServices;
    private final java.util.Map<Class<?>, Integer> loadCounts = new java.util.HashMap<>();

    private FakeInstalledPluginsPort(java.util.List<?> pluginServices) {
      this.pluginServices = java.util.List.copyOf(pluginServices);
    }

    @Override
    public <T> java.util.List<T> loadInstalledServices(
        Class<T> serviceType, java.util.List<T> builtInServices) {
      loadCounts.merge(serviceType, 1, Integer::sum);
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

    private int loadCount(Class<?> serviceType) {
      return loadCounts.getOrDefault(serviceType, 0);
    }
  }

  private static void writePluginJar(Path jarPath) throws Exception {
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        PLUGIN_BACKEND_EXTENSION_CLASS,
        pluginBackendExtensionSource(),
        BackendExtension.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-backend", "1.0.0"));
  }

  private static void writeMutationPluginJar(Path jarPath) throws Exception {
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        PLUGIN_MESSAGE_MUTATION_PROVIDER_CLASS,
        pluginMessageMutationProviderSource(),
        Ircv3MessageMutationProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-reply-runtime", "1.0.0"));
  }

  private static String pluginMessageMutationProviderSource() {
    return """
        package plugin.ircv3;

        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationOperation;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationProvider;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3MessageMutationRequest;
        import java.util.Set;

        public final class PluginReplyRuntimeProvider
            implements Ircv3MessageMutationProvider {

          @Override
          public String providerId() {
            return "plugin-reply-runtime";
          }

          @Override
          public int priority() {
            return 100;
          }

          @Override
          public Set<Ircv3MessageMutationOperation> operations() {
            return Set.of(Ircv3MessageMutationOperation.REPLY);
          }

          @Override
          public String build(
              Ircv3MessageMutationOperation operation,
              Ircv3MessageMutationRequest request) {
            if (operation != Ircv3MessageMutationOperation.REPLY || request == null) {
              return "";
            }
            return "@draft/reply="
                + request.messageId()
                + " privmsg "
                + request.target()
                + " :"
                + request.payload();
          }
        }
        """;
  }

  private static String pluginBackendExtensionSource() {
    return """
        package plugin.backend;

        import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
        import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
        import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;

        public final class PluginBackendExtension implements BackendExtension {
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
          public BackendEditorProfile editorProfile() {
            return new BackendEditorProfile(
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
        """;
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
    public BackendEditorProfile editorProfile() {
      return new BackendEditorProfile(
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
