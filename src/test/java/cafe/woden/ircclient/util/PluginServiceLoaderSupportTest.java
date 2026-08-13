package cafe.woden.ircclient.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
import cafe.woden.ircclient.plugin.spi.InstalledPluginDescriptor;
import cafe.woden.ircclient.plugin.spi.IrcafePluginManifest;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PluginServiceLoaderSupportTest {

  private static final String REAL_PLUGIN_PROVIDER_CLASS =
      "plugin.real.ManifestBackendNamedCommandHandler";
  private static final String SECOND_PLUGIN_PROVIDER_CLASS =
      "plugin.real.SecondManifestBackendNamedCommandHandler";
  private static final String HELPER_BACKED_PLUGIN_PROVIDER_CLASS =
      "plugin.helperbacked.HelperBackedBackendNamedCommandHandler";

  @TempDir Path tempDir;

  @Test
  void copyNonNullServicesKeepsDuplicatesAndFiltersNulls() {
    TestProvider first = new RecordingTestProvider("first");
    TestProvider second = new RecordingTestProvider("second");
    ArrayList<TestProvider> providers = new ArrayList<>();
    providers.add(first);
    providers.add(null);
    providers.add(second);

    List<TestProvider> copied = PluginServiceLoaderSupport.copyNonNullServices(providers);

    assertEquals(2, copied.size());
    assertSame(first, copied.get(0));
    assertSame(second, copied.get(1));
    assertTrue(PluginServiceLoaderSupport.copyNonNullServices(null).isEmpty());
    assertThrows(
        UnsupportedOperationException.class, () -> copied.add(new RecordingTestProvider("late")));
  }

  @Test
  void dedupeByProviderClassKeepsFirstNonNullInstancePerClass() {
    TestProvider first = new RecordingTestProvider("first");
    TestProvider duplicate = new RecordingTestProvider("duplicate");
    TestProvider other = new OtherRecordingTestProvider("other");
    ArrayList<TestProvider> providers = new ArrayList<>(List.of(first, duplicate, other));
    providers.add(null);

    List<TestProvider> deduped = PluginServiceLoaderSupport.dedupeByProviderClass(providers);

    assertEquals(2, deduped.size());
    assertSame(first, deduped.get(0));
    assertSame(other, deduped.get(1));
  }

  @Test
  void dedupeByProviderClassMergesProviderGroupsInOrder() {
    TestProvider first = new RecordingTestProvider("first");
    TestProvider duplicate = new RecordingTestProvider("duplicate");
    TestProvider other = new OtherRecordingTestProvider("other");

    List<TestProvider> deduped =
        PluginServiceLoaderSupport.dedupeByProviderClass(List.of(first), List.of(duplicate, other));

    assertEquals(2, deduped.size());
    assertSame(first, deduped.get(0));
    assertSame(other, deduped.get(1));
  }

  @Test
  void dedupeByProviderKeyKeepsFirstMatchingKeyAndAllowsSameClassDifferentKeys() {
    TestProvider first = new RecordingTestProvider("first");
    TestProvider duplicateKey = new RecordingTestProvider("first");
    TestProvider sameClassDifferentKey = new RecordingTestProvider("second");
    TestProvider otherClassSameValue = new OtherRecordingTestProvider("first");
    ArrayList<TestProvider> providers =
        new ArrayList<>(List.of(first, duplicateKey, sameClassDifferentKey, otherClassSameValue));
    providers.add(null);

    List<TestProvider> deduped =
        PluginServiceLoaderSupport.dedupeByProviderKey(
            providers, provider -> provider.getClass().getName() + '\u0000' + provider.value());

    assertEquals(3, deduped.size());
    assertSame(first, deduped.get(0));
    assertSame(sameClassDifferentKey, deduped.get(1));
    assertSame(otherClassSameValue, deduped.get(2));
  }

  @Test
  void pluginManifestExposesCompatibilityBaselineForAuthors() {
    assertEquals(1, IrcafePluginManifest.SUPPORTED_PLUGIN_API_VERSION);
    assertEquals(25, IrcafePluginManifest.REQUIRED_JAVA_RELEASE);
    assertEquals("plugins", IrcafePluginManifest.DEFAULT_PLUGIN_DIRECTORY_NAME);
    assertEquals(
        List.of(
            IrcafePluginManifest.PLUGIN_ID_ATTRIBUTE,
            IrcafePluginManifest.PLUGIN_VERSION_ATTRIBUTE,
            IrcafePluginManifest.PLUGIN_API_VERSION_ATTRIBUTE),
        IrcafePluginManifest.REQUIRED_PLUGIN_ATTRIBUTE_NAMES);
    assertEquals(
        List.of(
            IrcafePluginManifest.PLUGIN_VERSION_ATTRIBUTE,
            IrcafePluginManifest.FALLBACK_PLUGIN_VERSION_ATTRIBUTE),
        IrcafePluginManifest.SUPPORTED_PLUGIN_VERSION_ATTRIBUTE_NAMES);
  }

  @Test
  void pluginManifestBuildsCompatibleManifestAttributesForAuthorTools() {
    Map<String, String> attributes =
        IrcafePluginManifest.compatibleManifestAttributes(" example-plugin ", " 1.2.3 ");

    assertEquals("example-plugin", attributes.get(IrcafePluginManifest.PLUGIN_ID_ATTRIBUTE));
    assertEquals("1.2.3", attributes.get(IrcafePluginManifest.PLUGIN_VERSION_ATTRIBUTE));
    assertEquals(
        Integer.toString(IrcafePluginManifest.SUPPORTED_PLUGIN_API_VERSION),
        attributes.get(IrcafePluginManifest.PLUGIN_API_VERSION_ATTRIBUTE));
    assertEquals(
        IrcafePluginManifest.REQUIRED_PLUGIN_ATTRIBUTE_NAMES, List.copyOf(attributes.keySet()));
  }

  @Test
  void loadApplicationServicesKeepsSeededProvidersBeforeClasspathProviders() {
    PluginServiceLoaderSupportTestProvider first = new RecordingTestProvider("first");
    PluginServiceLoaderSupportTestProvider second = new OtherRecordingTestProvider("second");

    List<PluginServiceLoaderSupportTestProvider> providers =
        PluginServiceLoaderSupport.loadApplicationServices(
            PluginServiceLoaderSupportTestProvider.class,
            List.of(first, second),
            PluginServiceLoaderSupportTest.class);

    assertEquals(2, providers.size());
    assertSame(first, providers.get(0));
    assertSame(second, providers.get(1));
  }

  @Test
  void wrapsInvalidProviderConfigurationWithHelpfulMessage() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    writePluginJar(pluginDir.resolve("broken-provider.jar"));

    IllegalStateException error = loadFailureFromSharedPluginClassLoader(pluginDir);

    assertTrue(error.getMessage().contains(BackendNamedCommandHandler.class.getName()));
    assertTrue(error.getMessage().contains(PrivateBackendNamedCommandHandler.class.getName()));
    assertTrue(error.getMessage().contains("public no-arg constructor"));
  }

  @Test
  void loadsPluginProviderFromJarWithCompatibleManifest() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("compatible-provider.jar"),
        REAL_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(REAL_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("example-plugin", "1.2.3"));

    List<BackendNamedCommandHandler> services =
        PluginServiceLoaderSupport.loadInstalledServices(
                BackendNamedCommandHandler.class,
                List.of(),
                pluginDir,
                PluginServiceLoaderSupportTest.class.getClassLoader(),
                null)
            .services();

    assertTrue(
        services.stream()
            .anyMatch(service -> REAL_PLUGIN_PROVIDER_CLASS.equals(service.getClass().getName())));
  }

  @Test
  void rejectsPluginProviderWithMissingManifestMetadata() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("missing-manifest.jar"),
        REAL_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(REAL_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        Map.of());

    IllegalStateException error = loadFailureFromSharedPluginClassLoader(pluginDir);

    assertTrue(error.getMessage().contains(REAL_PLUGIN_PROVIDER_CLASS));
    assertTrue(error.getMessage().contains(IrcafePluginManifest.PLUGIN_ID_ATTRIBUTE));
  }

  @Test
  void rejectsPluginProviderWithUnsupportedApiVersion() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("unsupported-api.jar"),
        REAL_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(REAL_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        Map.of(
            IrcafePluginManifest.PLUGIN_ID_ATTRIBUTE,
            "example-plugin",
            IrcafePluginManifest.PLUGIN_VERSION_ATTRIBUTE,
            "1.2.3",
            IrcafePluginManifest.PLUGIN_API_VERSION_ATTRIBUTE,
            "99"));

    IllegalStateException error = loadFailureFromSharedPluginClassLoader(pluginDir);

    assertTrue(error.getMessage().contains("unsupported plugin API version 99"));
    assertTrue(
        error
            .getMessage()
            .contains(Integer.toString(IrcafePluginManifest.SUPPORTED_PLUGIN_API_VERSION)));
  }

  @Test
  void rejectsDuplicatePluginIdsAcrossDifferentPluginJars() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    Map<String, String> duplicateManifest =
        CompiledPluginJarSupport.compatibleManifest("duplicate-plugin", "1.0.0");
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("duplicate-one.jar"),
        REAL_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(REAL_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        duplicateManifest);
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("duplicate-two.jar"),
        SECOND_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(SECOND_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        duplicateManifest);

    IllegalStateException error = loadFailureFromSharedPluginClassLoader(pluginDir);

    assertTrue(error.getMessage().contains("duplicate plugin id 'duplicate-plugin'"));
  }

  @Test
  void discoversFirstPluginAndReportsDuplicatePluginIdProblem() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("duplicate-discovery/plugins"));
    Map<String, String> duplicateManifest =
        CompiledPluginJarSupport.compatibleManifest("duplicate-plugin", "1.0.0");
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("duplicate-one.jar"),
        REAL_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(REAL_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        duplicateManifest);
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("duplicate-two.jar"),
        SECOND_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(SECOND_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        duplicateManifest);

    PluginServiceLoaderSupport.PluginDiscovery discovery =
        PluginServiceLoaderSupport.discoverInstalledPluginDescriptors(pluginDir, null);

    assertEquals(1, discovery.installedPlugins().size());
    assertEquals("duplicate-plugin", discovery.installedPlugins().getFirst().pluginId());
    assertEquals(1, discovery.problems().size());
    assertTrue(discovery.problems().getFirst().summary().contains("duplicate-plugin"));
    assertTrue(discovery.problems().getFirst().details().contains("duplicate-one.jar"));
    assertTrue(discovery.problems().getFirst().details().contains("duplicate-two.jar"));
    assertTrue(discovery.problems().getFirst().details().contains("Error type:"));
  }

  @Test
  void discoversDeclaredPluginDescriptorsAndSkipsDependencyJars() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("discoverable-plugin.jar"),
        REAL_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(REAL_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("discoverable-plugin", "2.0.0"));
    writeDependencyJar(pluginDir.resolve("helper-library.jar"));

    List<InstalledPluginDescriptor> plugins =
        PluginServiceLoaderSupport.discoverInstalledPlugins(pluginDir, null);

    assertTrue(plugins.size() == 1);
    assertTrue("discoverable-plugin".equals(plugins.getFirst().pluginId()));
    assertTrue("2.0.0".equals(plugins.getFirst().pluginVersion()));
  }

  @Test
  void discoversDeclaredPluginVersionFromImplementationVersionFallback() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("implementation-version/plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("implementation-version-plugin.jar"),
        REAL_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(REAL_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        CompiledPluginJarSupport.compatibleManifestUsingImplementationVersion(
            "fallback-version-plugin", "4.5.6"));

    List<InstalledPluginDescriptor> plugins =
        PluginServiceLoaderSupport.discoverInstalledPlugins(pluginDir, null);

    assertEquals(1, plugins.size());
    assertEquals("fallback-version-plugin", plugins.getFirst().pluginId());
    assertEquals("4.5.6", plugins.getFirst().pluginVersion());
    assertEquals(
        IrcafePluginManifest.SUPPORTED_PLUGIN_API_VERSION, plugins.getFirst().pluginApiVersion());
  }

  @Test
  void discoversHealthyPluginsWhenAnotherPluginManifestIsInvalid() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("invalid-discovery-manifest/plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("healthy-provider.jar"),
        REAL_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(REAL_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("healthy-plugin", "1.2.3"));
    writeInvalidDeclaredPluginJar(pluginDir.resolve("invalid-plugin.jar"), "invalid-plugin");
    writeDependencyJar(pluginDir.resolve("helper-library.jar"));

    PluginServiceLoaderSupport.PluginDiscovery discovery =
        PluginServiceLoaderSupport.discoverInstalledPluginDescriptors(pluginDir, null);

    assertEquals(1, discovery.installedPlugins().size());
    assertEquals("healthy-plugin", discovery.installedPlugins().getFirst().pluginId());
    assertEquals(1, discovery.problems().size());
    assertTrue(discovery.problems().getFirst().details().contains("invalid-plugin.jar"));
    assertTrue(
        discovery
            .problems()
            .getFirst()
            .details()
            .contains(IrcafePluginManifest.PLUGIN_API_VERSION_ATTRIBUTE));
  }

  @Test
  void pathBasedLoadingKeepsHealthyPluginProvidersWhenAnotherPluginIsInvalid() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("healthy-provider.jar"),
        REAL_PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(REAL_PLUGIN_PROVIDER_CLASS),
        BackendNamedCommandHandler.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("healthy-plugin", "1.2.3"));
    writeBrokenManifestProviderJar(pluginDir.resolve("broken-provider.jar"), "broken-plugin");

    PluginServiceLoaderSupport.LoadedServices<BackendNamedCommandHandler> loadedServices =
        PluginServiceLoaderSupport.loadInstalledServices(
            BackendNamedCommandHandler.class,
            List.of(),
            pluginDir,
            PluginServiceLoaderSupportTest.class.getClassLoader(),
            null);
    try {
      assertEquals(2, loadedServices.pluginClassLoaders().size());
      assertTrue(
          loadedServices.services().stream()
              .anyMatch(
                  service -> REAL_PLUGIN_PROVIDER_CLASS.equals(service.getClass().getName())));
      assertTrue(
          loadedServices.services().stream()
              .noneMatch(
                  service ->
                      "plugin.installed.MissingBackendNamedCommandHandler"
                          .equals(service.getClass().getName())));
    } finally {
      PluginServiceLoaderSupport.closePluginClassLoaders(
          loadedServices.pluginClassLoaders(), null, "ignored");
    }
  }

  @Test
  void pathBasedLoadingResolvesHelperJarsNextToInstalledPlugin() throws Exception {
    Path pluginDir = Files.createDirectories(tempDir.resolve("helper-jar-placement/plugins"));
    Path helperJar =
        CompiledPluginJarSupport.writeLibraryJar(
            pluginDir.resolve("helper-library.jar"),
            Map.of("plugin.helper.HelperMessage", helperMessageSource()));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("helper-backed-provider.jar"),
        Map.of(
            HELPER_BACKED_PLUGIN_PROVIDER_CLASS,
            helperBackedPluginProviderSource(HELPER_BACKED_PLUGIN_PROVIDER_CLASS)),
        Map.of(
            BackendNamedCommandHandler.class.getName(),
            List.of(HELPER_BACKED_PLUGIN_PROVIDER_CLASS)),
        CompiledPluginJarSupport.compatibleManifest("helper-backed-plugin", "1.0.0"),
        List.of(helperJar));

    PluginServiceLoaderSupport.LoadedServices<BackendNamedCommandHandler> loadedServices =
        PluginServiceLoaderSupport.loadInstalledServices(
            BackendNamedCommandHandler.class,
            List.of(),
            pluginDir,
            PluginServiceLoaderSupportTest.class.getClassLoader(),
            null);
    try {
      assertEquals(1, loadedServices.pluginClassLoaders().size());
      BackendNamedCommandHandler helperBackedProvider =
          loadedServices.services().stream()
              .filter(
                  service ->
                      HELPER_BACKED_PLUGIN_PROVIDER_CLASS.equals(service.getClass().getName()))
              .findFirst()
              .orElseThrow();

      assertTrue(helperBackedProvider.supportedCommandNames().contains("helper-backed"));
      assertEquals(
          new BackendNamedCommandParseResult("helper-backed", "helper-jar-ok"),
          helperBackedProvider.parse("/helper-backed", "helper-backed"));
    } finally {
      PluginServiceLoaderSupport.closePluginClassLoaders(
          loadedServices.pluginClassLoaders(), null, "ignored");
    }
  }

  private static void writePluginJar(Path jarPath) throws IOException {
    try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarPath))) {
      out.putNextEntry(
          new JarEntry("META-INF/services/" + BackendNamedCommandHandler.class.getName()));
      out.write(
          (PrivateBackendNamedCommandHandler.class.getName() + System.lineSeparator())
              .getBytes(StandardCharsets.UTF_8));
      out.closeEntry();
    }
  }

  private static void writeBrokenManifestProviderJar(Path jarPath, String pluginId)
      throws IOException {
    var manifest = new java.util.jar.Manifest();
    var attributes = manifest.getMainAttributes();
    attributes.put(java.util.jar.Attributes.Name.MANIFEST_VERSION, "1.0");
    for (var entry : CompiledPluginJarSupport.compatibleManifest(pluginId, "1.0.0").entrySet()) {
      attributes.putValue(entry.getKey(), entry.getValue());
    }
    try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
      out.putNextEntry(
          new JarEntry("META-INF/services/" + BackendNamedCommandHandler.class.getName()));
      out.write(
          "plugin.installed.MissingBackendNamedCommandHandler\n".getBytes(StandardCharsets.UTF_8));
      out.closeEntry();
    }
  }

  private static void writeInvalidDeclaredPluginJar(Path jarPath, String pluginId)
      throws IOException {
    var manifest = new java.util.jar.Manifest();
    var attributes = manifest.getMainAttributes();
    attributes.put(java.util.jar.Attributes.Name.MANIFEST_VERSION, "1.0");
    attributes.putValue(IrcafePluginManifest.PLUGIN_ID_ATTRIBUTE, pluginId);
    attributes.putValue(IrcafePluginManifest.PLUGIN_VERSION_ATTRIBUTE, "1.0.0");
    try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
      out.putNextEntry(new JarEntry("plugin/metadata.txt"));
      out.write("invalid".getBytes(StandardCharsets.UTF_8));
      out.closeEntry();
    }
  }

  private static void writeDependencyJar(Path jarPath) throws IOException {
    try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarPath))) {
      out.putNextEntry(new JarEntry("plugin/helper.txt"));
      out.write("helper".getBytes(StandardCharsets.UTF_8));
      out.closeEntry();
    }
  }

  private static String pluginProviderSource(String providerClassName) {
    int lastDot = providerClassName.lastIndexOf('.');
    String packageName = providerClassName.substring(0, lastDot);
    String simpleName = providerClassName.substring(lastDot + 1);
    return """
        package %s;

        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
        import java.util.Set;

        public final class %s implements BackendNamedCommandHandler {
          @Override
          public Set<String> supportedCommandNames() {
            return Set.of("manifestping");
          }

          @Override
          public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
            return new BackendNamedCommandParseResult(matchedCommandName, "ok");
          }
        }
        """
        .formatted(packageName, simpleName);
  }

  private static String helperMessageSource() {
    return """
        package plugin.helper;

        public final class HelperMessage {
          private HelperMessage() {}

          public static String text() {
            return "helper-jar-ok";
          }
        }
        """;
  }

  private static String helperBackedPluginProviderSource(String providerClassName) {
    int lastDot = providerClassName.lastIndexOf('.');
    String packageName = providerClassName.substring(0, lastDot);
    String simpleName = providerClassName.substring(lastDot + 1);
    return """
        package %s;

        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
        import java.util.Set;
        import plugin.helper.HelperMessage;

        public final class %s implements BackendNamedCommandHandler {
          @Override
          public Set<String> supportedCommandNames() {
            return Set.of("helper-backed");
          }

          @Override
          public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
            return new BackendNamedCommandParseResult(matchedCommandName, HelperMessage.text());
          }
        }
        """
        .formatted(packageName, simpleName);
  }

  private static IllegalStateException loadFailureFromSharedPluginClassLoader(Path pluginDir) {
    URLClassLoader pluginClassLoader =
        PluginServiceLoaderSupport.openPluginClassLoader(
            pluginDir, PluginServiceLoaderSupportTest.class.getClassLoader(), null);
    assertNotNull(pluginClassLoader);
    try {
      return assertThrows(
          IllegalStateException.class,
          () ->
              PluginServiceLoaderSupport.loadInstalledServices(
                  BackendNamedCommandHandler.class, List.of(), null, pluginClassLoader));
    } finally {
      PluginServiceLoaderSupport.closePluginClassLoader(pluginClassLoader, null, "ignored");
    }
  }

  private interface TestProvider {
    String value();
  }

  private record RecordingTestProvider(String value)
      implements TestProvider, PluginServiceLoaderSupportTestProvider {}

  private record OtherRecordingTestProvider(String value)
      implements TestProvider, PluginServiceLoaderSupportTestProvider {}

  private static final class PrivateBackendNamedCommandHandler
      implements BackendNamedCommandHandler {

    @Override
    public Set<String> supportedCommandNames() {
      return Set.of("broken");
    }

    @Override
    public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
      return null;
    }
  }
}
