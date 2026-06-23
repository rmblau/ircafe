package cafe.woden.ircclient.ui.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.ui.input.spi.MessageInputSpellcheckDictionaryProvider;
import cafe.woden.ircclient.ui.settings.spellcheck.SpellcheckSettings;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JTextPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageInputSpellcheckDictionaryProviderPluginTest {

  @TempDir Path tempDir;

  @Test
  void loadsSpellcheckDictionaryWordsFromPluginDirectoryJar() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    writePluginJar(pluginDir.resolve("plugin-spellcheck-dictionary.jar"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    List<MessageInputSpellcheckDictionaryProvider> providers =
        MessageInputPluginProviders.spellcheckDictionaryProviders(installedPlugins);
    MessageInputSpellcheckSupport support =
        new MessageInputSpellcheckSupport(
            new JTextPane(), SpellcheckSettings.defaults(), providers);

    try {
      assertFalse(providers.isEmpty());
      assertTrue(isCustomDictionaryWord(support, "wodencafe"));
      assertTrue(isCustomDictionaryWord(support, "plugh"));
      assertFalse(isCustomDictionaryWord(support, "notprovided"));
      assertTrue(installedPlugins.pluginProblems().isEmpty());
    } finally {
      support.shutdown();
    }
  }

  private void writePluginJar(Path jarPath) throws Exception {
    String providerClassName = "cafe.woden.ircclient.testplugins.PluginSpellcheckDictionary";
    String providerSource =
        """
        package cafe.woden.ircclient.testplugins;

        import cafe.woden.ircclient.ui.input.spi.MessageInputSpellcheckDictionaryProvider;
        import java.util.List;

        public final class PluginSpellcheckDictionary
            implements MessageInputSpellcheckDictionaryProvider {
          @Override
          public List<String> dictionaryWords() {
            return List.of("wodencafe", "!!plugh!!", "");
          }
        }
        """;
    CompiledPluginJarSupport.writePluginJar(
        jarPath,
        providerClassName,
        providerSource,
        MessageInputSpellcheckDictionaryProvider.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("plugin-spellcheck-dictionary", "1.0.0"));
  }

  private static boolean isCustomDictionaryWord(MessageInputSpellcheckSupport support, String word)
      throws Exception {
    Method method =
        MessageInputSpellcheckSupport.class.getDeclaredMethod(
            "isCustomDictionaryWord", String.class);
    method.setAccessible(true);
    return (boolean) method.invoke(support, word);
  }
}
