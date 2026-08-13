package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.config.plugins.InstalledPluginServicesTestSupport;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageTranslationLanguageProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS =
      "example.translation.ExampleTranslationLanguageProvider";

  @TempDir Path tempDir;

  @Test
  void documentedTranslationLanguageProviderContributesManualTargetChoices() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("translation-language-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        MessageTranslationLanguageProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("translation-language-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    try {
      List<MessageTranslationLanguage> commonTargets =
          MessageTranslationLanguageCatalogSupport.commonTargets(installedPlugins);

      assertTrue(installedPlugins.pluginProblems().isEmpty());
      assertTrue(commonTargets.contains(new MessageTranslationLanguage("tlh", "Klingon")));
      assertTrue(commonTargets.contains(new MessageTranslationLanguage("tok-pon", "Toki Pona")));
      assertEquals(
          1, commonTargets.stream().filter(language -> "tok-pon".equals(language.code())).count());
      assertFalse(commonTargets.stream().anyMatch(language -> language.code().isBlank()));

      List<MessageTranslationLanguage> enabledTargets =
          MessageTranslationLanguageCatalogSupport.availableTargets(
              propsWithEnabledLanguages(List.of("TOK_PON")), installedPlugins);

      assertEquals(List.of(new MessageTranslationLanguage("tok-pon", "Toki Pona")), enabledTargets);
    } finally {
      InstalledPluginServicesTestSupport.shutdown(installedPlugins);
    }
  }

  private static IrcProperties.Client.Translation propsWithEnabledLanguages(
      List<String> enabledLanguages) {
    return new IrcProperties.Client.Translation(
        false,
        IrcProperties.Client.Translation.Mode.MANUAL,
        "",
        "",
        "",
        "auto",
        "",
        true,
        false,
        enabledLanguages,
        IrcProperties.Client.Translation.DisplayMode.BELOW_ORIGINAL,
        10_000,
        4_000,
        2);
  }

  private static String guideProviderSource() {
    return """
        package example.translation;

        import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
        import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
        import java.util.Arrays;
        import java.util.List;

        public final class ExampleTranslationLanguageProvider
            implements MessageTranslationLanguageProvider {
          @Override
          public List<MessageTranslationLanguage> languages() {
            return Arrays.asList(
                new MessageTranslationLanguage(" TLH ", "Klingon"),
                new MessageTranslationLanguage("TOK_PON", "Toki Pona"),
                new MessageTranslationLanguage("tok-pon", "Later duplicate"),
                new MessageTranslationLanguage(" ", "Ignored"),
                null);
          }
        }
        """;
  }
}
