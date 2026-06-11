package cafe.woden.ircclient.app.translation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageTranslationLanguageCatalogTest {

  @Test
  void includesLanguagesLoadedThroughInstalledPluginPort() {
    MessageTranslationLanguage pluginLanguage = new MessageTranslationLanguage(" TLH ", "Klingon");

    List<MessageTranslationLanguage> languages =
        MessageTranslationLanguageCatalog.commonTargets(
            new RecordingInstalledPluginsPort(List.of(pluginLanguage)));

    assertTrue(
        languages.stream()
            .anyMatch(
                language ->
                    language.code().equals("tlh") && language.label().equals("Klingon")));
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
        MessageTranslationLanguageCatalog.availableTargets(
            translation, new RecordingInstalledPluginsPort(List.of(pluginLanguage)));

    assertEquals(List.of(new MessageTranslationLanguage("tlh", "Klingon")), languages);
  }

  @Test
  void keepsBuiltInLanguageWhenPluginDuplicatesLanguageCode() {
    MessageTranslationLanguage pluginEnglish =
        new MessageTranslationLanguage("en", "Plugin English");

    List<MessageTranslationLanguage> languages =
        MessageTranslationLanguageCatalog.commonTargets(
            new RecordingInstalledPluginsPort(List.of(pluginEnglish)));

    MessageTranslationLanguage english =
        languages.stream()
            .filter(language -> language.code().equals("en"))
            .findFirst()
            .orElseThrow();

    assertEquals("English", english.label());
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
