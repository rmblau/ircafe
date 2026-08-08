package cafe.woden.ircclient.plugin.spi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PluginAuthoringReadmeTest {

  @Test
  void documentsPublishedPluginAuthoringContract() throws IOException {
    String readme = Files.readString(Path.of("ircafe-plugin-api/README.md"));

    assertContains(readme, IrcafePluginManifest.PLUGIN_ID_ATTRIBUTE);
    assertContains(readme, IrcafePluginManifest.PLUGIN_VERSION_ATTRIBUTE);
    assertContains(readme, IrcafePluginManifest.FALLBACK_PLUGIN_VERSION_ATTRIBUTE);
    assertContains(readme, IrcafePluginManifest.PLUGIN_API_VERSION_ATTRIBUTE);
    assertContains(readme, Integer.toString(IrcafePluginManifest.SUPPORTED_PLUGIN_API_VERSION));
    assertContains(readme, Integer.toString(IrcafePluginManifest.REQUIRED_JAVA_RELEASE));
    assertContains(readme, IrcafePluginManifest.DEFAULT_PLUGIN_DIRECTORY_NAME);
    assertContains(readme, IrcafePluginServiceDescriptors.SERVICE_DESCRIPTOR_DIRECTORY);
    assertContains(readme, "MessageTranslationBackendContext");
    assertContains(readme, "LinkPreviewHttp");
    assertContains(readme, "BouncerNetworkMappingContext");
  }

  @Test
  void documentsPhaseFourGuideFixtureFamilies() throws IOException {
    String readme = Files.readString(Path.of("ircafe-plugin-api/README.md"));

    assertContains(readme, "MessageTranslationBackendProvider");
    assertContains(readme, "MessageTranslationLanguageProvider");
    assertContains(readme, "Ircv3ExtensionProvider");
    assertContains(readme, "ThemeContributionProvider");
    assertContains(readme, "BouncerNetworkMappingStrategy");
    assertContains(readme, "BackendExtension");
    assertContains(readme, "SlashCommandParseStrategy");
    assertContains(readme, "SlashCommandPresentationContributor");
    assertContains(readme, "BackendNamedCommandHandler");
    assertContains(readme, "BackendNamedCommandExecutor");
    assertContains(readme, "OutboundHelpContributor");
  }

  @Test
  void documentsTranslationPluginOwnershipAndNormalizationContracts() throws IOException {
    String readme = Files.readString(Path.of("ircafe-plugin-api/README.md"));

    assertContains(readme, "Translation Plugin Walkthrough");
    assertContains(readme, "compile against `:ircafe-plugin-api` only");
    assertContains(readme, "`:ircafe-feature-translation`");
    assertContains(readme, "MessageTranslationBackendProvider");
    assertContains(readme, "MessageTranslationLanguageProvider");
    assertContains(readme, "MessageTranslationBackendContext");
    assertContains(readme, "case-insensitively");
    assertContains(readme, "converts underscores to hyphens");
    assertContains(readme, "Return a non-null `CompletionStage`");
    assertContains(readme, "public no-argument constructors");
  }

  @Test
  void documentsCommandPluginOwnershipAndFallthroughContracts() throws IOException {
    String readme = Files.readString(Path.of("ircafe-plugin-api/README.md"));

    assertContains(readme, "Command Plugin Walkthrough");
    assertContains(readme, "compile against `:ircafe-plugin-api` only");
    assertContains(readme, "Do not depend on");
    assertContains(readme, "`:ircafe-feature-commands`");
    assertContains(readme, "return `null`");
    assertContains(readme, "SlashCommandParseResult.unknown(line)");
    assertContains(readme, "BackendNamedCommandExecutionContext");
    assertContains(readme, "public no-argument constructors");
  }

  @Test
  void documentsBouncerPluginOwnershipAndMatchingContracts() throws IOException {
    String readme = Files.readString(Path.of("ircafe-plugin-api/README.md"));

    assertContains(readme, "Bouncer Plugin Walkthrough");
    assertContains(readme, "compile against `:ircafe-plugin-api` only");
    assertContains(readme, "`:ircafe-feature-bouncer`");
    assertContains(readme, "BouncerNetworkMappingStrategy");
    assertContains(readme, "BouncerBackendDiscoveryHandler");
    assertContains(readme, "case-insensitively");
    assertContains(readme, "BouncerNetworkMappingContext");
    assertContains(readme, "ResolvedBouncerNetwork");
    assertContains(readme, "onOriginDisconnected");
    assertContains(readme, "public no-argument constructors");
  }

  @Test
  void documentsNotificationSoundPluginOwnershipAndFallthroughContracts() throws IOException {
    String readme = Files.readString(Path.of("ircafe-plugin-api/README.md"));

    assertContains(readme, "Notification Sound Plugin Walkthrough");
    assertContains(readme, "compile against `:ircafe-plugin-api` only");
    assertContains(readme, "`:ircafe-feature-notify`");
    assertContains(readme, "CustomSoundFileExtensionProvider");
    assertContains(readme, "CustomSoundPlaybackProvider");
    assertContains(readme, "case-insensitively");
    assertContains(readme, "Return `false`");
    assertContains(readme, "Java Sound fallback");
    assertContains(readme, "must not move, delete, or retain ownership");
    assertContains(readme, "public no-argument constructors");
  }

  @Test
  void documentsThemePluginOwnershipAndMergeContracts() throws IOException {
    String readme = Files.readString(Path.of("ircafe-plugin-api/README.md"));

    assertContains(readme, "Theme Plugin Walkthrough");
    assertContains(readme, "compile against `:ircafe-plugin-api` only");
    assertContains(readme, "ThemeContributionProvider");
    assertContains(readme, "ThemeOption");
    assertContains(readme, "ThemePresetContribution");
    assertContains(readme, "ThemePack.PLUGIN");
    assertContains(readme, "case-insensitively");
    assertContains(readme, "built-in or earlier contributions win");
    assertContains(readme, "Look & Feel installation");
    assertContains(readme, "public no-argument constructors");
  }

  @Test
  void documentsEmbedPluginOwnershipAndFallthroughContracts() throws IOException {
    String readme = Files.readString(Path.of("ircafe-plugin-api/README.md"));

    assertContains(readme, "Embed and Link Preview Plugin Walkthrough");
    assertContains(readme, "compile against `:ircafe-plugin-api` only");
    assertContains(readme, "`:ircafe-feature-embed`");
    assertContains(readme, "LinkPreviewResolver");
    assertContains(readme, "OEmbedLinkPreviewProvider");
    assertContains(readme, "ImageUrlExtensionProvider");
    assertContains(readme, "EmbedHttpHeaderProvider");
    assertContains(readme, "NewsPublisherProfileProvider");
    assertContains(readme, "Return `null`");
    assertContains(readme, "later resolvers continue");
    assertContains(readme, "ascending `sortOrder()`");
    assertContains(readme, "public no-argument constructors");
  }

  @Test
  void documentsReleaseSupportNotes() throws IOException {
    String readme = Files.readString(Path.of("ircafe-plugin-api/README.md"));

    assertContains(readme, "Release and Support Notes");
    assertContains(readme, "verifyPluginReleaseGraph");
    assertContains(readme, "verifyPluginApiJarPolicy");
    assertContains(readme, "verifyPluginApiV1Baseline");
    assertContains(readme, "generatePluginApiV1Baseline");
    assertContains(readme, "api-baseline/v1.txt");
    assertContains(readme, "new plugin API version");
    assertContains(readme, "verifyBuiltInProviderPackaging");
    assertContains(readme, "verifyBootJarPluginPackaging");
    assertContains(readme, "externalPluginSmokeTest");
    assertContains(readme, "bootJar");
    assertContains(readme, "ircafe-builtins-*");
    assertContains(readme, "ircafe-feature-*");
    assertContains(readme, "Support artifacts");
    assertContains(readme, "Plugins diagnostics");
  }

  private static void assertContains(String text, String expected) {
    assertTrue(text.contains(expected), () -> "README should contain: " + expected);
  }
}
