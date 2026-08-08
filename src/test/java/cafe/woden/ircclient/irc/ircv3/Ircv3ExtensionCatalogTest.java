package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.InstalledPluginProblem;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class Ircv3ExtensionCatalogTest {

  private static final String PLUGIN_PROVIDER_CLASS = "plugin.ircv3.RuntimeIrcv3ExtensionProvider";

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withUserConfiguration(Ircv3ExtensionCatalog.class)
          .withBean(InstalledPluginsPort.class, RecordingInstalledPluginsPort::new);

  @TempDir Path tempDir;

  @Test
  void createsCatalogBeanThroughAutowiredInstalledPluginsConstructor() {
    runner.run(
        ctx -> {
          Ircv3ExtensionCatalog catalog = ctx.getBean(Ircv3ExtensionCatalog.class);

          assertNotNull(catalog);
          assertFalse(catalog.providerIds().isEmpty());
        });
  }

  @Test
  void runtimeCatalogPreservesClasspathSpiExtensionProviders() {
    Ircv3ExtensionCatalog catalog = new Ircv3ExtensionCatalog(new RecordingInstalledPluginsPort());

    assertTrue(catalog.providerIds().contains("message-tags"));
    assertTrue(catalog.providerIds().contains("server-time"));
    assertTrue(catalog.providerIds().contains("echo-message"));
    assertTrue(catalog.providerIds().contains("standard-replies"));
    assertTrue(catalog.providerIds().contains("labeled-response"));
    assertTrue(catalog.providerIds().contains("away-notify"));
    assertTrue(catalog.providerIds().contains("account-notify"));
    assertTrue(catalog.providerIds().contains("extended-join"));
    assertTrue(catalog.providerIds().contains("chghost"));
    assertTrue(catalog.providerIds().contains("setname"));
    assertTrue(catalog.providerIds().contains("invite-notify"));
    assertTrue(catalog.providerIds().contains("monitor"));
    assertTrue(catalog.providerIds().contains("extended-monitor"));
    assertTrue(catalog.providerIds().contains("account-tag"));
    assertTrue(catalog.providerIds().contains("multi-prefix"));
    assertTrue(catalog.providerIds().contains("userhost-in-names"));
    assertFalse(catalog.providerIds().contains("names"));
    assertTrue(catalog.providerIds().contains("cap-notify"));
    assertFalse(catalog.providerIds().contains("negotiation"));
    assertTrue(catalog.providerIds().contains("batch"));
    assertTrue(catalog.providerIds().contains("znc-playback"));
    assertFalse(catalog.providerIds().contains("history-transport"));
    assertTrue(catalog.providerIds().contains("read-marker"));
    assertTrue(catalog.providerIds().contains("multiline"));
    assertTrue(catalog.providerIds().contains("message-redaction"));
    assertTrue(catalog.providerIds().contains("chathistory"));
    assertTrue(catalog.providerIds().contains("reply"));
    assertTrue(catalog.providerIds().contains("reactions"));
    assertTrue(catalog.providerIds().contains("typing"));
    assertTrue(catalog.providerIds().contains("channel-context"));
    assertTrue(catalog.providerIds().contains("message-edit"));
    assertEquals("draft/read-marker", catalog.requestTokenFor("read-marker"));
    assertEquals("draft/multiline", catalog.requestTokenFor("multiline"));
    assertEquals("draft/message-redaction", catalog.requestTokenFor("message-redaction"));
    assertEquals("draft/chathistory", catalog.requestTokenFor("chathistory"));
    assertEquals("reply", catalog.preferenceKeyFor("draft/reply"));
    assertEquals("react", catalog.preferenceKeyFor("draft/react"));
    assertEquals("typing", catalog.preferenceKeyFor("draft/typing"));
    assertEquals("channel-context", catalog.preferenceKeyFor("draft/channel-context"));
    assertEquals("message-edit", catalog.preferenceKeyFor("draft/message-edit"));
  }

  @Test
  void runtimeCatalogLoadsInstalledIrcv3ExtensionProviders() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("example-ircv3-provider.jar"),
        PLUGIN_PROVIDER_CLASS,
        pluginProviderSource(),
        Ircv3ExtensionProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("example-ircv3-provider", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    Ircv3ExtensionCatalog catalog = new Ircv3ExtensionCatalog(installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(catalog.providerIds().contains("plugin-example"));
    assertTrue(catalog.requestableCapabilityTokens().contains("draft/example-cap"));
    assertEquals("draft/example-cap", catalog.requestTokenFor("example-cap"));
    assertEquals("example-cap", catalog.preferenceKeyFor("draft/example-cap"));
    assertEquals("draft/example-cap", catalog.normalizeRequestToken("example-cap"));
    assertEquals("example-cap", catalog.normalizePreferenceKey("draft/example-cap"));
    assertTrue(
        catalog.visibleFeatures().stream()
            .anyMatch(feature -> "Example feature".equals(feature.label())));
  }

  @Test
  void conflictingPluginMetadataFallsBackToBuiltInsAndRecordsProblem() {
    RecordingInstalledPluginsPort installedPlugins =
        new RecordingInstalledPluginsPort(
            List.of(
                new Ircv3ExtensionProvider() {
                  @Override
                  public String providerId() {
                    return "plugin-conflict";
                  }

                  @Override
                  public int sortOrder() {
                    return 950;
                  }

                  @Override
                  public List<Ircv3ExtensionContribution> extensions() {
                    return List.of(
                        Ircv3TestExtensionContributions.capability(
                            "plugin-conflict-cap",
                            Ircv3SpecStatus.DRAFT,
                            "echo-message",
                            "plugin-conflict-cap",
                            "Conflicting capability",
                            Ircv3UiGroup.OTHER,
                            950,
                            "Conflicting test-only capability."));
                  }
                }));

    Ircv3ExtensionCatalog catalog = new Ircv3ExtensionCatalog(installedPlugins);

    assertFalse(catalog.providerIds().contains("plugin-conflict"));
    assertEquals(
        Ircv3ExtensionRegistry.providerIds(),
        catalog.providerIds(),
        "conflicting plugin metadata should fall back to built-ins");
    assertEquals(1, installedPlugins.pluginProblems().size());
    assertTrue(
        installedPlugins
            .pluginProblems()
            .getFirst()
            .summary()
            .contains("IRCv3 extension metadata"));
    assertTrue(installedPlugins.pluginProblems().getFirst().details().contains("plugin-conflict"));
  }

  private static String pluginProviderSource() {
    return """
        package plugin.ircv3;

        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionContribution;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionKind;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3ExtensionProvider;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3FeatureContribution;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3SpecStatus;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiGroup;
        import cafe.woden.ircclient.irc.ircv3.spi.Ircv3UiMetadata;
        import java.util.List;

        public final class RuntimeIrcv3ExtensionProvider
            implements Ircv3ExtensionProvider {
          @Override
          public String providerId() {
            return "plugin-example";
          }

          @Override
          public int sortOrder() {
            return 950;
          }

          @Override
          public List<Ircv3ExtensionContribution> extensions() {
            return List.of(
                new Ircv3ExtensionContribution(
                    "example-cap",
                    Ircv3ExtensionKind.CAPABILITY,
                    Ircv3SpecStatus.DRAFT,
                    List.of("draft/example-cap"),
                    "draft/example-cap",
                    "example-cap",
                    new Ircv3UiMetadata(
                        "Example capability (draft)",
                        Ircv3UiGroup.OTHER,
                        910,
                        "Adds an example plugin-provided capability.")));
          }

          @Override
          public List<Ircv3FeatureContribution> visibleFeatures() {
            return List.of(
                new Ircv3FeatureContribution(
                    910,
                    "Example feature",
                    List.of("message-tags"),
                    List.of("example-cap", "draft/example-cap")));
          }
        }
        """;
  }

  private static final class RecordingInstalledPluginsPort implements InstalledPluginsPort {
    private final List<Ircv3ExtensionProvider> providers;
    private final List<InstalledPluginProblem> problems = new ArrayList<>();

    private RecordingInstalledPluginsPort() {
      this(List.of());
    }

    private RecordingInstalledPluginsPort(List<Ircv3ExtensionProvider> providers) {
      this.providers = List.copyOf(providers);
    }

    @Override
    public List<InstalledPluginProblem> pluginProblems() {
      return List.copyOf(problems);
    }

    @Override
    public void recordPluginProblem(InstalledPluginProblem problem) {
      if (problem != null) {
        problems.add(problem);
      }
    }

    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType == Ircv3ExtensionProvider.class) {
        for (Ircv3ExtensionProvider provider : providers) {
          services.add(serviceType.cast(provider));
        }
      }
      return List.copyOf(services);
    }
  }
}
