package cafe.woden.ircclient.ui.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.ui.input.spi.MessageInputSpellcheckDictionaryProvider;
import cafe.woden.ircclient.ui.input.spi.MessageInputWordSuggestionProvider;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckSettings;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.swing.JTextPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageInputPluginGuideFixtureTest {

  private static final String GUIDE_DICTIONARY_PROVIDER_CLASS =
      "example.input.ExampleSpellcheckDictionaryProvider";
  private static final String GUIDE_SUGGESTION_PROVIDER_CLASS =
      "example.input.ExampleWordSuggestionProvider";

  @TempDir Path tempDir;

  @Test
  void documentedMessageInputPluginCanPackageDictionaryAndSuggestionProviders() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("message-input-guide-example.jar"),
        Map.of(
            GUIDE_DICTIONARY_PROVIDER_CLASS, guideDictionaryProviderSource(),
            GUIDE_SUGGESTION_PROVIDER_CLASS, guideSuggestionProviderSource()),
        Map.of(
            MessageInputSpellcheckDictionaryProvider.class.getName(),
            List.of(GUIDE_DICTIONARY_PROVIDER_CLASS),
            MessageInputWordSuggestionProvider.class.getName(),
            List.of(GUIDE_SUGGESTION_PROVIDER_CLASS)),
        CompiledPluginJarSupport.compatibleManifest("message-input-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    List<MessageInputSpellcheckDictionaryProvider> dictionaryProviders =
        MessageInputPluginProviders.spellcheckDictionaryProviders(installedPlugins);
    MessageInputWordSuggestionProvider suggestions =
        MessageInputPluginProviders.wordSuggestionProvider(
            new BuiltInSuggestions("builtin-help"), installedPlugins);
    MessageInputSpellcheckSupport spellcheckSupport =
        new MessageInputSpellcheckSupport(
            new JTextPane(), SpellcheckSettings.defaults(), dictionaryProviders);

    try {
      assertTrue(installedPlugins.pluginProblems().isEmpty());
      assertTrue(isCustomDictionaryWord(spellcheckSupport, "wodencafe"));
      assertTrue(isCustomDictionaryWord(spellcheckSupport, "ircafe"));
      assertFalse(isCustomDictionaryWord(spellcheckSupport, "notprovided"));
      assertEquals(
          List.of("builtin-help", "plugin-help", "plugin-history"),
          suggestions.suggestWords("hel", 4));
    } finally {
      spellcheckSupport.shutdown();
    }
  }

  private static String guideDictionaryProviderSource() {
    return """
        package example.input;

        import cafe.woden.ircclient.ui.input.spi.MessageInputSpellcheckDictionaryProvider;
        import java.util.List;

        public final class ExampleSpellcheckDictionaryProvider
            implements MessageInputSpellcheckDictionaryProvider {
          @Override
          public List<String> dictionaryWords() {
            return List.of("wodencafe", "ircafe", "");
          }
        }
        """;
  }

  private static String guideSuggestionProviderSource() {
    return """
        package example.input;

        import cafe.woden.ircclient.ui.input.spi.MessageInputWordSuggestionProvider;
        import java.util.List;

        public final class ExampleWordSuggestionProvider
            implements MessageInputWordSuggestionProvider {
          @Override
          public List<String> suggestWords(String token, int maxSuggestions) {
            return List.of("plugin-help", "plugin-history");
          }
        }
        """;
  }

  private static boolean isCustomDictionaryWord(MessageInputSpellcheckSupport support, String word)
      throws Exception {
    Method method =
        MessageInputSpellcheckSupport.class.getDeclaredMethod(
            "isCustomDictionaryWord", String.class);
    method.setAccessible(true);
    return (boolean) method.invoke(support, word);
  }

  private record BuiltInSuggestions(List<String> suggestions)
      implements MessageInputWordSuggestionProvider {
    BuiltInSuggestions(String... suggestions) {
      this(List.of(suggestions));
    }

    @Override
    public List<String> suggestWords(String token, int maxSuggestions) {
      return suggestions;
    }
  }
}
