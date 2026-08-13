package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.translation.builtins.CommonMessageTranslationLanguageProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageTranslationLanguageCatalogTest {

  private static final String SPI_PROVIDER_CLASS = "plugin.translation.PluginLanguageProvider";

  @TempDir Path tempDir;

  @Test
  void includesLanguagesLoadedThroughInstalledPluginPort() {
    MessageTranslationLanguage pluginLanguage = new MessageTranslationLanguage(" TLH ", "Klingon");

    List<MessageTranslationLanguage> languages =
        MessageTranslationLanguageCatalogSupport.commonTargets(
            new RecordingInstalledPluginsPort(List.of(pluginLanguage)));

    assertTrue(
        languages.stream()
            .anyMatch(
                language -> language.code().equals("tlh") && language.label().equals("Klingon")));
  }

  @Test
  void filtersPluginLanguagesWithDetectionLanguageSettings() {
    MessageTranslationLanguage pluginLanguage = new MessageTranslationLanguage("tlh", "Klingon");
    IrcProperties.Client.Translation translation =
        new IrcProperties.Client.Translation(
            false,
            IrcProperties.Client.Translation.Mode.MANUAL,
            "",
            "",
            "",
            "auto",
            "",
            true,
            false,
            List.of("TLH"),
            IrcProperties.Client.Translation.DisplayMode.BELOW_ORIGINAL,
            10_000,
            4_000,
            2);

    List<MessageTranslationLanguage> languages =
        MessageTranslationLanguageCatalogSupport.availableTargets(
            translation, new RecordingInstalledPluginsPort(List.of(pluginLanguage)));

    assertEquals(List.of(new MessageTranslationLanguage("tlh", "Klingon")), languages);
  }

  @Test
  void keepsBuiltInLanguageWhenPluginDuplicatesLanguageCode() {
    MessageTranslationLanguage pluginEnglish =
        new MessageTranslationLanguage("en", "Plugin English");

    List<MessageTranslationLanguage> languages =
        MessageTranslationLanguageCatalogSupport.commonTargets(
            new RecordingInstalledPluginsPort(List.of(pluginEnglish)));

    MessageTranslationLanguage english =
        languages.stream()
            .filter(language -> language.code().equals("en"))
            .findFirst()
            .orElseThrow();

    assertEquals("English", english.label());
  }

  @Test
  void loadsServiceLoaderTranslationLanguageProvidersFromInstalledPlugins() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("example-translation-language-provider.jar"),
        SPI_PROVIDER_CLASS,
        pluginLanguageProviderSource(),
        cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest(
            "example-translation-language-provider", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    List<MessageTranslationLanguage> languages =
        MessageTranslationLanguageCatalogSupport.commonTargets(installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(
        languages.stream()
            .anyMatch(
                language -> language.code().equals("tlh") && language.label().equals("Klingon")));
  }

  @Test
  void loadsCommonLanguageProviderThroughClasspathServiceLoader() {
    RuntimeConfigPathPort runtimeConfigPathPort = () -> tempDir.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);

    List<MessageTranslationLanguageProvider> providers =
        MessageTranslationPluginProviders.languageProviders(List.of(), installedPlugins);

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(
        providers.stream()
            .anyMatch(provider -> provider instanceof CommonMessageTranslationLanguageProvider));
  }

  @Test
  void dedupesLanguageProviderClassesLoadedFromPlugins() {
    RecordingLanguageProvider builtInProvider =
        new RecordingLanguageProvider(List.of(new MessageTranslationLanguage("tlh", "Klingon")));
    RecordingLanguageProvider duplicatePluginProvider =
        new RecordingLanguageProvider(
            List.of(new MessageTranslationLanguage("tlh", "Plugin Klingon")));

    List<MessageTranslationLanguageProvider> providers =
        MessageTranslationPluginProviders.languageProviders(
            List.of(builtInProvider),
            new RecordingLanguageProviderInstalledPluginsPort(List.of(duplicatePluginProvider)));

    assertEquals(List.of(builtInProvider), providers);
  }

  private static String pluginLanguageProviderSource() {
    return """
        package plugin.translation;

        import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
        import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
        import java.util.List;

        public final class PluginLanguageProvider implements MessageTranslationLanguageProvider {
          @Override
          public List<MessageTranslationLanguage> languages() {
            return List.of(new MessageTranslationLanguage("tlh", "Klingon"));
          }
        }
        """;
  }

  private record RecordingLanguageProvider(List<MessageTranslationLanguage> languages)
      implements MessageTranslationLanguageProvider {}

  private record RecordingLanguageProviderInstalledPluginsPort(
      List<MessageTranslationLanguageProvider> pluginProviders) implements InstalledPluginsPort {
    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType == MessageTranslationLanguageProvider.class) {
        for (MessageTranslationLanguageProvider provider : pluginProviders) {
          services.add(serviceType.cast(provider));
        }
      }
      return List.copyOf(services);
    }
  }

  private record RecordingInstalledPluginsPort(List<MessageTranslationLanguage> pluginLanguages)
      implements InstalledPluginsPort {
    @Override
    public <T> List<T> loadInstalledServices(Class<T> serviceType, List<T> builtInServices) {
      ArrayList<T> services = new ArrayList<>(builtInServices);
      if (serviceType == MessageTranslationLanguageProvider.class) {
        services.add(serviceType.cast((MessageTranslationLanguageProvider) () -> pluginLanguages));
      }
      return List.copyOf(services);
    }
  }
}
