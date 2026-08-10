package cafe.woden.ircclient.config.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
import cafe.woden.ircclient.app.translation.MessageTranslationBackendRegistry;
import cafe.woden.ircclient.app.translation.MessageTranslationLanguageCatalog;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
import cafe.woden.ircclient.app.translation.spi.MessageTranslationTargetView;
import cafe.woden.ircclient.bouncer.BouncerBackendCatalog;
import cafe.woden.ircclient.bouncer.BouncerBackendDescriptor;
import cafe.woden.ircclient.bouncer.BouncerDiscoveryEventRouter;
import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
import cafe.woden.ircclient.bouncer.spi.BouncerEphemeralServerSpec;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingContext;
import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.notify.api.sound.CustomSoundPlaybackProviderChain;
import cafe.woden.ircclient.notify.api.sound.CustomSoundPlaybackProviderResult;
import cafe.woden.ircclient.notify.api.sound.CustomSoundProviderCatalog;
import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
import cafe.woden.ircclient.plugin.spi.InstalledPluginDescriptor;
import cafe.woden.ircclient.plugin.spi.IrcafePluginManifest;
import cafe.woden.ircclient.ui.chat.embed.LinkPreviewHttpHeaderCatalog;
import cafe.woden.ircclient.ui.chat.embed.LinkPreviewProviderCatalog;
import cafe.woden.ircclient.ui.chat.embed.spi.BuiltInLinkPreviewResolverOrders;
import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedResponseFields;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeOption;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemePack;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemePresetContribution;
import cafe.woden.ircclient.ui.settings.theme.spi.ThemeTone;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExternalPluginReleaseSmokeTest {

  private static final String PLUGIN_ID = "external-release-smoke";
  private static final String PLUGIN_VERSION = "1.0.0";
  private static final String HANDLER_CLASS = "plugin.release.ReleaseSmokeBackendCommand";
  private static final String EXECUTOR_CLASS = "plugin.release.ReleaseSmokeBackendExecutor";
  private static final String PARSER_CLASS = "plugin.release.ReleaseSmokeSlashParser";
  private static final String PRESENTATION_CLASS = "plugin.release.ReleaseSmokePresentation";
  private static final String BOUNCER_MAPPING_CLASS =
      "plugin.release.ReleaseSmokeBouncerMappingStrategy";
  private static final String BOUNCER_HANDLER_CLASS =
      "plugin.release.ReleaseSmokeBouncerDiscoveryHandler";
  private static final String SOUND_EXTENSION_CLASS =
      "plugin.release.ReleaseSmokeCustomSoundExtensionProvider";
  private static final String SOUND_PLAYBACK_CLASS =
      "plugin.release.ReleaseSmokeCustomSoundPlaybackProvider";
  private static final String THEME_PROVIDER_CLASS =
      "plugin.release.ReleaseSmokeThemeContributionProvider";
  private static final String LINK_PREVIEW_RESOLVER_CLASS =
      "plugin.release.ReleaseSmokeLinkPreviewResolver";
  private static final String OEMBED_PROVIDER_CLASS = "plugin.release.ReleaseSmokeOEmbedProvider";
  private static final String EMBED_HEADER_PROVIDER_CLASS =
      "plugin.release.ReleaseSmokeEmbedHttpHeaderProvider";
  private static final String IMAGE_EXTENSION_PROVIDER_CLASS =
      "plugin.release.ReleaseSmokeImageUrlExtensionProvider";
  private static final String NEWS_PROFILE_PROVIDER_CLASS =
      "plugin.release.ReleaseSmokeNewsPublisherProfileProvider";
  private static final String TRANSLATION_BACKEND_CLASS =
      "plugin.release.ReleaseSmokeTranslationBackendProvider";
  private static final String TRANSLATION_LANGUAGE_CLASS =
      "plugin.release.ReleaseSmokeTranslationLanguageProvider";

  @TempDir Path tempDir;

  @Test
  void
      loadsCompleteExternalCommandBouncerSoundThemeEmbedAndTranslationPluginFromConfiguredPluginDirectory()
          throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDirectory =
        Files.createDirectories(
            runtimeConfigDirectory.resolve(IrcafePluginManifest.DEFAULT_PLUGIN_DIRECTORY_NAME));
    Path pluginJar =
        CompiledPluginJarSupport.writePluginJar(
            pluginDirectory.resolve("external-release-smoke.jar"),
            Map.ofEntries(
                Map.entry(HANDLER_CLASS, handlerSource()),
                Map.entry(EXECUTOR_CLASS, executorSource()),
                Map.entry(PARSER_CLASS, parserSource()),
                Map.entry(PRESENTATION_CLASS, presentationSource()),
                Map.entry(BOUNCER_MAPPING_CLASS, bouncerMappingSource()),
                Map.entry(BOUNCER_HANDLER_CLASS, bouncerHandlerSource()),
                Map.entry(SOUND_EXTENSION_CLASS, soundExtensionSource()),
                Map.entry(SOUND_PLAYBACK_CLASS, soundPlaybackSource()),
                Map.entry(THEME_PROVIDER_CLASS, themeProviderSource()),
                Map.entry(LINK_PREVIEW_RESOLVER_CLASS, linkPreviewResolverSource()),
                Map.entry(OEMBED_PROVIDER_CLASS, oEmbedProviderSource()),
                Map.entry(EMBED_HEADER_PROVIDER_CLASS, embedHttpHeaderProviderSource()),
                Map.entry(IMAGE_EXTENSION_PROVIDER_CLASS, imageUrlExtensionProviderSource()),
                Map.entry(NEWS_PROFILE_PROVIDER_CLASS, newsPublisherProfileProviderSource()),
                Map.entry(TRANSLATION_BACKEND_CLASS, translationBackendSource()),
                Map.entry(TRANSLATION_LANGUAGE_CLASS, translationLanguageSource())),
            Map.ofEntries(
                Map.entry(BackendNamedCommandHandler.class.getName(), List.of(HANDLER_CLASS)),
                Map.entry(BackendNamedCommandExecutor.class.getName(), List.of(EXECUTOR_CLASS)),
                Map.entry(SlashCommandParseStrategy.class.getName(), List.of(PARSER_CLASS)),
                Map.entry(
                    SlashCommandPresentationContributor.class.getName(),
                    List.of(PRESENTATION_CLASS)),
                Map.entry(
                    BouncerNetworkMappingStrategy.class.getName(), List.of(BOUNCER_MAPPING_CLASS)),
                Map.entry(
                    BouncerBackendDiscoveryHandler.class.getName(), List.of(BOUNCER_HANDLER_CLASS)),
                Map.entry(
                    CustomSoundFileExtensionProvider.class.getName(),
                    List.of(SOUND_EXTENSION_CLASS)),
                Map.entry(
                    CustomSoundPlaybackProvider.class.getName(), List.of(SOUND_PLAYBACK_CLASS)),
                Map.entry(ThemeContributionProvider.class.getName(), List.of(THEME_PROVIDER_CLASS)),
                Map.entry(
                    LinkPreviewResolver.class.getName(), List.of(LINK_PREVIEW_RESOLVER_CLASS)),
                Map.entry(
                    OEmbedLinkPreviewProvider.class.getName(), List.of(OEMBED_PROVIDER_CLASS)),
                Map.entry(
                    EmbedHttpHeaderProvider.class.getName(), List.of(EMBED_HEADER_PROVIDER_CLASS)),
                Map.entry(
                    ImageUrlExtensionProvider.class.getName(),
                    List.of(IMAGE_EXTENSION_PROVIDER_CLASS)),
                Map.entry(
                    NewsPublisherProfileProvider.class.getName(),
                    List.of(NEWS_PROFILE_PROVIDER_CLASS)),
                Map.entry(
                    MessageTranslationBackendProvider.class.getName(),
                    List.of(TRANSLATION_BACKEND_CLASS)),
                Map.entry(
                    MessageTranslationLanguageProvider.class.getName(),
                    List.of(TRANSLATION_LANGUAGE_CLASS))),
            CompiledPluginJarSupport.compatibleManifest(PLUGIN_ID, PLUGIN_VERSION));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");
    Path discoveredMarker = tempDir.resolve("release-bouncer-discovered.txt");
    Path disconnectedMarker = tempDir.resolve("release-bouncer-disconnected.txt");
    Path soundMarker = tempDir.resolve("release-custom-sound.txt");

    System.setProperty("ircafe.test.releaseBouncerDiscovered", discoveredMarker.toString());
    System.setProperty("ircafe.test.releaseBouncerDisconnected", disconnectedMarker.toString());
    System.setProperty("ircafe.test.releaseCustomSound", soundMarker.toString());
    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    try {
      List<BackendNamedCommandHandler> handlers =
          installedPlugins.loadInstalledServices(BackendNamedCommandHandler.class, List.of());
      List<BackendNamedCommandExecutor> executors =
          installedPlugins.loadInstalledServices(BackendNamedCommandExecutor.class, List.of());
      List<SlashCommandParseStrategy> parsers =
          installedPlugins.loadInstalledServices(SlashCommandParseStrategy.class, List.of());
      List<SlashCommandPresentationContributor> presentations =
          installedPlugins.loadInstalledServices(
              SlashCommandPresentationContributor.class, List.of());
      List<BouncerNetworkMappingStrategy> mappingStrategies =
          installedPlugins.loadInstalledServices(BouncerNetworkMappingStrategy.class, List.of());
      List<BouncerBackendDiscoveryHandler> discoveryHandlers =
          installedPlugins.loadInstalledServices(BouncerBackendDiscoveryHandler.class, List.of());
      List<CustomSoundFileExtensionProvider> soundExtensionProviders =
          installedPlugins.loadInstalledServices(CustomSoundFileExtensionProvider.class, List.of());
      List<CustomSoundPlaybackProvider> soundPlaybackProviders =
          installedPlugins.loadInstalledServices(CustomSoundPlaybackProvider.class, List.of());
      List<ThemeContributionProvider> themeProviders =
          installedPlugins.loadInstalledServices(ThemeContributionProvider.class, List.of());
      List<LinkPreviewResolver> linkPreviewResolvers =
          installedPlugins.loadInstalledServices(LinkPreviewResolver.class, List.of());
      List<OEmbedLinkPreviewProvider> oEmbedProviders =
          installedPlugins.loadInstalledServices(OEmbedLinkPreviewProvider.class, List.of());
      List<EmbedHttpHeaderProvider> embedHeaderProviders =
          installedPlugins.loadInstalledServices(EmbedHttpHeaderProvider.class, List.of());
      List<ImageUrlExtensionProvider> imageExtensionProviders =
          installedPlugins.loadInstalledServices(ImageUrlExtensionProvider.class, List.of());
      List<NewsPublisherProfileProvider> newsProfileProviders =
          installedPlugins.loadInstalledServices(NewsPublisherProfileProvider.class, List.of());
      List<MessageTranslationBackendProvider> translationBackends =
          installedPlugins.loadInstalledServices(
              MessageTranslationBackendProvider.class, List.of());
      List<MessageTranslationLanguageProvider> translationLanguageProviders =
          installedPlugins.loadInstalledServices(
              MessageTranslationLanguageProvider.class, List.of());

      assertEquals(pluginDirectory, installedPlugins.pluginDirectory());
      assertTrue(installedPlugins.pluginProblems().isEmpty());
      assertEquals(1, installedPlugins.installedPlugins().size());
      InstalledPluginDescriptor descriptor = installedPlugins.installedPlugins().getFirst();
      assertEquals(PLUGIN_ID, descriptor.pluginId());
      assertEquals(PLUGIN_VERSION, descriptor.pluginVersion());
      assertEquals(pluginJar, descriptor.sourceJar());

      BackendNamedCommandHandler handler = onlyProvider(handlers, HANDLER_CLASS);
      assertTrue(handler.supportedCommandNames().contains("releaseping"));

      BackendNamedCommandExecutor executor = onlyProvider(executors, EXECUTOR_CLASS);
      assertTrue(executor.handledCommandNames().contains("releaseping"));

      SlashCommandParseStrategy parser = onlyProvider(parsers, PARSER_CLASS);
      SlashCommandParseResult parsed = parser.tryParse("/releasequote smoke ready");
      assertEquals(SlashCommandParseResult.quote("NOTICE * :smoke ready"), parsed);
      assertNull(parser.tryParse("/join #ircafe"));

      SlashCommandPresentationContributor presentation =
          onlyProvider(presentations, PRESENTATION_CLASS);
      assertTrue(
          presentation.autocompleteCommands().stream()
              .map(SlashCommandDescriptor::command)
              .anyMatch("/releasequote"::equals));

      BouncerNetworkMappingStrategy loadedMapping =
          onlyProvider(mappingStrategies, BOUNCER_MAPPING_CLASS);
      BouncerBackendDiscoveryHandler loadedDiscoveryHandler =
          onlyProvider(discoveryHandlers, BOUNCER_HANDLER_CLASS);
      assertEquals(" Release-Bouncer ", loadedMapping.backendId());
      assertEquals(" RELEASE-BOUNCER ", loadedDiscoveryHandler.backendId());

      BouncerBackendCatalog bouncerCatalog =
          BouncerBackendCatalog.fromStrategies(mappingStrategies);
      BouncerBackendDescriptor bouncerDescriptor =
          bouncerCatalog.find(" release-bouncer ").orElseThrow();
      assertEquals("release:", bouncerDescriptor.ephemeralIdPrefix());
      assertEquals("Release Bouncer Networks", bouncerDescriptor.networksGroupLabel());
      assertEquals(Set.of("release.example/networks"), bouncerDescriptor.capabilityHints());

      BouncerNetworkMappingStrategy mapping =
          bouncerCatalog.mappingStrategy("RELEASE-BOUNCER").orElseThrow();
      BouncerServerProfile profile =
          new BouncerServerProfile("origin-1", "login-user", "sasl-user");
      BouncerDiscoveredNetwork network =
          new BouncerDiscoveredNetwork(
              "release-bouncer",
              "origin-1",
              "libera",
              "Libera Chat",
              "Libera Auto",
              "hint-user",
              Set.of("NETWORKS"),
              Map.of("source", "release-smoke"));
      ResolvedBouncerNetwork resolved =
          mapping.resolveNetwork(
              profile, network, new BouncerNetworkMappingContext("{base}/{network}", true));
      BouncerEphemeralServerSpec serverSpec =
          mapping.buildEphemeralServer(profile, resolved, List.of("#ircafe", " ", "#plugins"));
      assertEquals("release:origin-1:libera", resolved.serverId());
      assertEquals("hint-user/libera", resolved.loginUser());
      assertEquals(List.of("#ircafe", "#plugins"), serverSpec.autoJoinChannels());

      BouncerDiscoveryEventRouter eventRouter =
          BouncerDiscoveryEventRouter.fromHandlers(discoveryHandlers);
      eventRouter.routeNetworkDiscovered(network);
      eventRouter.routeOriginDisconnected(" RELEASE-BOUNCER ", "origin-1");
      assertEquals(
          "origin-1|libera|Libera Chat|hint-user|true|release-smoke",
          Files.readString(discoveredMarker));
      assertEquals("origin-1", Files.readString(disconnectedMarker));

      CustomSoundFileExtensionProvider soundExtension =
          onlyProvider(soundExtensionProviders, SOUND_EXTENSION_CLASS);
      CustomSoundPlaybackProvider soundPlayback =
          onlyProvider(soundPlaybackProviders, SOUND_PLAYBACK_CLASS);
      assertEquals(
          Set.of("opus"),
          CustomSoundProviderCatalog.supportedExtensions(List.of(), List.of(soundExtension)));
      Path soundPath = Files.writeString(tempDir.resolve("release.OPUS"), "release audio");
      CustomSoundPlaybackProviderResult soundResult =
          CustomSoundPlaybackProviderChain.play(soundPath, List.of(soundPlayback), () -> false);
      assertTrue(soundResult.handled());
      assertTrue(soundResult.handledWhileFresh());
      assertEquals(soundPath.toString(), Files.readString(soundMarker));

      ThemeContributionProvider themeProvider = onlyProvider(themeProviders, THEME_PROVIDER_CLASS);
      ThemeOption themeOption = themeProvider.themeOptions().getFirst();
      ThemePresetContribution themePreset = themeProvider.themePresets().getFirst();
      assertEquals("release-nebula", themeOption.id());
      assertEquals("Release Nebula", themeOption.label());
      assertEquals(ThemeTone.DARK, themeOption.tone());
      assertEquals(ThemePack.PLUGIN, themeOption.pack());
      assertTrue(themeOption.featured());
      assertEquals("release-nebula", themePreset.id());
      assertTrue(themePreset.dark());
      assertEquals("#6C63FF", themePreset.extraDefaults().get("@accentColor"));

      LinkPreviewResolver linkPreviewResolver =
          onlyProvider(linkPreviewResolvers, LINK_PREVIEW_RESOLVER_CLASS);
      assertEquals(
          BuiltInLinkPreviewResolverOrders.PLUGIN_DEFAULT + 25, linkPreviewResolver.sortOrder());
      String previewUrl = "https://release-preview.example/items/42";
      LinkPreview linkPreview =
          linkPreviewResolver.tryResolve(URI.create(previewUrl), previewUrl, null);
      assertEquals("Release preview", linkPreview.title());
      assertEquals("External Embed Smoke", linkPreview.siteName());
      assertEquals(previewUrl, linkPreview.url());
      assertNull(
          linkPreviewResolver.tryResolve(
              URI.create("https://unrelated.example/items/42"),
              "https://unrelated.example/items/42",
              null));

      OEmbedLinkPreviewProvider oEmbedProvider =
          onlyProvider(oEmbedProviders, OEMBED_PROVIDER_CLASS);
      URI oEmbedTarget = URI.create("https://release-oembed.example/posts/42");
      assertTrue(oEmbedProvider.matches(oEmbedTarget));
      assertEquals("release-oembed", oEmbedProvider.id());
      assertEquals(
          URI.create(
              "https://api.release-oembed.example/oembed?url="
                  + "https%3A%2F%2Frelease-oembed.example%2Fposts%2F42"),
          oEmbedProvider.endpointFor(oEmbedTarget, oEmbedTarget.toString()));
      assertEquals("Release oEmbed", oEmbedProvider.defaultSiteName());
      assertEquals(
          "Release oEmbed preview",
          oEmbedProvider.titleFallback(new OEmbedResponseFields(null, null, null, null, null)));

      EmbedHttpHeaderProvider embedHeaderProvider =
          onlyProvider(embedHeaderProviders, EMBED_HEADER_PROVIDER_CLASS);
      var headerResult =
          new LinkPreviewHttpHeaderCatalog()
              .applyProviderHeaders(
                  Map.of("Accept", "text/html", "X-Release-Embed", "base"),
                  URI.create("https://cdn.release-embed.example/artwork.avif"),
                  List.of(embedHeaderProvider));
      assertTrue(headerResult.failures().isEmpty());
      assertEquals("plugin", headerResult.headers().get("X-Release-Embed"));
      assertEquals("https://cdn.release-embed.example/", headerResult.headers().get("Referer"));
      assertTrue(!headerResult.headers().containsKey("Ignored-Blank"));

      ImageUrlExtensionProvider imageExtensionProvider =
          onlyProvider(imageExtensionProviders, IMAGE_EXTENSION_PROVIDER_CLASS);
      assertEquals(
          Set.of(".avif"),
          new LinkPreviewProviderCatalog().imageExtensions(List.of(imageExtensionProvider)));

      NewsPublisherProfileProvider newsProfileProvider =
          onlyProvider(newsProfileProviders, NEWS_PROFILE_PROVIDER_CLASS);
      List<NewsPublisherProfile> releaseProfiles =
          new LinkPreviewProviderCatalog().newsPublisherProfiles(List.of(newsProfileProvider));
      assertEquals(1, releaseProfiles.size());
      NewsPublisherProfile releaseProfile = releaseProfiles.getFirst();
      assertEquals("release-daily", releaseProfile.key());
      assertEquals("Release Daily", releaseProfile.displayName());
      assertEquals(List.of("release-news.example"), List.of(releaseProfile.hostSuffixes()));
      assertEquals(
          List.of("article[data-release-story] p"), List.of(releaseProfile.paragraphSelectors()));

      MessageTranslationBackendProvider translationBackend =
          onlyProvider(translationBackends, TRANSLATION_BACKEND_CLASS);
      MessageTranslationBackendRegistry translationRegistry =
          new MessageTranslationBackendRegistry(List.of(translationBackend));
      MessageTranslationRequest translationRequest =
          new MessageTranslationRequest(
              new MessageTranslationTargetView(" libera ", " #ircafe "),
              Instant.parse("2026-07-11T12:00:00Z"),
              "woden",
              "release-message-1",
              "hello translation plugin",
              "EN",
              "ES");
      MessageTranslationResult translationResult =
          translationRegistry
              .find(" RELEASE-TRANSLATE ")
              .orElseThrow()
              .translate(
                  translationRequest,
                  new MessageTranslationBackendContext(
                      " https://translate.release.example/v1 ", " release-token ", 999_999L))
              .toCompletableFuture()
              .get(1, TimeUnit.SECONDS);
      assertEquals(
          "[libera/#ircafe] en->es: hello translation plugin", translationResult.translatedText());
      assertEquals("en", translationResult.sourceLanguage());
      assertEquals("es", translationResult.targetLanguage());
      assertEquals(
          "https://translate.release.example/v1|release-token|120000",
          translationResult.provider());

      MessageTranslationLanguageProvider translationLanguageProvider =
          onlyProvider(translationLanguageProviders, TRANSLATION_LANGUAGE_CLASS);
      assertEquals(
          List.of(
              new MessageTranslationLanguage("tlh", "Klingon"),
              new MessageTranslationLanguage("tok-pon", "Toki Pona")),
          MessageTranslationLanguageCatalog.commonTargets(List.of(translationLanguageProvider)));
    } finally {
      installedPlugins.shutdown();
      System.clearProperty("ircafe.test.releaseBouncerDiscovered");
      System.clearProperty("ircafe.test.releaseBouncerDisconnected");
      System.clearProperty("ircafe.test.releaseCustomSound");
    }
  }

  private static <T> T onlyProvider(List<T> services, String providerClassName) {
    List<T> matches =
        services.stream()
            .filter(service -> providerClassName.equals(service.getClass().getName()))
            .toList();
    assertEquals(1, matches.size());
    return matches.getFirst();
  }

  private static String handlerSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
        import java.util.Set;

        public final class ReleaseSmokeBackendCommand implements BackendNamedCommandHandler {
          @Override
          public Set<String> supportedCommandNames() {
            return Set.of("releaseping");
          }

          @Override
          public BackendNamedCommandParseResult parse(String line, String matchedCommandName) {
            return new BackendNamedCommandParseResult(matchedCommandName, "external-smoke");
          }
        }
        """;
  }

  private static String executorSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutionContext;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor;
        import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandRequest;
        import java.util.Set;

        public final class ReleaseSmokeBackendExecutor implements BackendNamedCommandExecutor {
          @Override
          public Set<String> handledCommandNames() {
            return Set.of("releaseping");
          }

          @Override
          public boolean handle(
              BackendNamedCommandExecutionContext context,
              BackendNamedCommandRequest command) {
            return command != null && "releaseping".equals(command.command());
          }
        }
        """;
  }

  private static String parserSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;

        public final class ReleaseSmokeSlashParser implements SlashCommandParseStrategy {
          private static final String COMMAND = "/releasequote";

          @Override
          public SlashCommandParseResult tryParse(String line) {
            if (line == null) {
              return null;
            }
            String trimmed = line.trim();
            if (!trimmed.equals(COMMAND) && !trimmed.startsWith(COMMAND + " ")) {
              return null;
            }
            String args = trimmed.substring(COMMAND.length()).trim();
            if (args.isEmpty()) {
              return SlashCommandParseResult.unknown(line);
            }
            return SlashCommandParseResult.quote("NOTICE * :" + args);
          }
        }
        """;
  }

  private static String presentationSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
        import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
        import java.util.List;

        public final class ReleaseSmokePresentation
            implements SlashCommandPresentationContributor {
          @Override
          public List<SlashCommandDescriptor> autocompleteCommands() {
            return List.of(new SlashCommandDescriptor("releasequote", "Release smoke command"));
          }
        }
        """;
  }

  private static String bouncerMappingSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
        import cafe.woden.ircclient.bouncer.spi.BouncerEphemeralServerSpec;
        import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingContext;
        import cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy;
        import cafe.woden.ircclient.bouncer.spi.BouncerServerProfile;
        import cafe.woden.ircclient.bouncer.spi.ResolvedBouncerNetwork;
        import java.util.List;
        import java.util.Set;

        public final class ReleaseSmokeBouncerMappingStrategy
            implements BouncerNetworkMappingStrategy {
          @Override
          public String backendId() {
            return " Release-Bouncer ";
          }

          @Override
          public String ephemeralIdPrefix() {
            return "release:";
          }

          @Override
          public String networksGroupLabel() {
            return "Release Bouncer Networks";
          }

          @Override
          public Set<String> capabilityHints() {
            return Set.of("release.example/networks");
          }

          @Override
          public ResolvedBouncerNetwork resolveNetwork(
              BouncerServerProfile bouncer,
              BouncerDiscoveredNetwork network,
              BouncerNetworkMappingContext context) {
            String base =
                context.preferLoginHint() ? network.loginUserHint() : bouncer.preferredLoginUser();
            if (base == null || base.isBlank()) {
              base = "guest";
            }
            String login =
                context
                    .genericLoginTemplate()
                    .replace("{base}", base)
                    .replace("{network}", network.networkId());
            return new ResolvedBouncerNetwork(
                "release:" + network.originServerId() + ":" + network.networkId(),
                login,
                network.displayName(),
                network.autoConnectName());
          }

          @Override
          public BouncerEphemeralServerSpec buildEphemeralServer(
              BouncerServerProfile bouncer,
              ResolvedBouncerNetwork resolved,
              List<String> autoJoinChannels) {
            List<String> channels =
                autoJoinChannels.stream()
                    .filter(channel -> channel != null && !channel.isBlank())
                    .toList();
            return new BouncerEphemeralServerSpec(
                resolved.serverId(), resolved.loginUser(), channels);
          }
        }
        """;
  }

  private static String bouncerHandlerSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler;
        import cafe.woden.ircclient.bouncer.spi.BouncerDiscoveredNetwork;
        import java.nio.file.Files;
        import java.nio.file.Path;

        public final class ReleaseSmokeBouncerDiscoveryHandler
            implements BouncerBackendDiscoveryHandler {
          @Override
          public String backendId() {
            return " RELEASE-BOUNCER ";
          }

          @Override
          public void onNetworkDiscovered(BouncerDiscoveredNetwork network) {
            String marker = System.getProperty("ircafe.test.releaseBouncerDiscovered");
            if (marker == null || marker.isBlank()) {
              return;
            }
            try {
              Files.writeString(
                  Path.of(marker),
                  network.originServerId()
                      + "|"
                      + network.networkId()
                      + "|"
                      + network.displayName()
                      + "|"
                      + network.loginUserHint()
                      + "|"
                      + network.hasCapability("networks")
                      + "|"
                      + network.attributes().get("source"));
            } catch (Exception ignored) {
            }
          }

          @Override
          public void onOriginDisconnected(String originServerId) {
            String marker = System.getProperty("ircafe.test.releaseBouncerDisconnected");
            if (marker == null || marker.isBlank()) {
              return;
            }
            try {
              Files.writeString(Path.of(marker), originServerId);
            } catch (Exception ignored) {
            }
          }
        }
        """;
  }

  private static String soundExtensionSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
        import java.util.List;

        public final class ReleaseSmokeCustomSoundExtensionProvider
            implements CustomSoundFileExtensionProvider {
          @Override
          public List<String> soundFileExtensions() {
            return List.of(".OPUS", " opus ", "bad extension");
          }
        }
        """;
  }

  private static String themeProviderSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemeOption;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemePack;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemePresetContribution;
        import cafe.woden.ircclient.ui.settings.theme.spi.ThemeTone;
        import java.util.List;
        import java.util.Map;

        public final class ReleaseSmokeThemeContributionProvider
            implements ThemeContributionProvider {
          @Override
          public List<ThemeOption> themeOptions() {
            return List.of(
                new ThemeOption(
                    "release-nebula",
                    "Release Nebula",
                    ThemeTone.DARK,
                    ThemePack.PLUGIN,
                    true));
          }

          @Override
          public List<ThemePresetContribution> themePresets() {
            return List.of(
                new ThemePresetContribution(
                    "release-nebula",
                    true,
                    Map.of("@accentColor", "#6C63FF", "@background", "#171529")));
          }
        }
        """;
  }

  private static String linkPreviewResolverSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.ui.chat.embed.spi.BuiltInLinkPreviewResolverOrders;
        import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreview;
        import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewHttp;
        import cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver;
        import java.net.URI;

        public final class ReleaseSmokeLinkPreviewResolver implements LinkPreviewResolver {
          @Override
          public int sortOrder() {
            return BuiltInLinkPreviewResolverOrders.PLUGIN_DEFAULT + 25;
          }

          @Override
          public LinkPreview tryResolve(URI uri, String originalUrl, LinkPreviewHttp http) {
            if (uri == null || !"release-preview.example".equals(uri.getHost())) {
              return null;
            }
            return new LinkPreview(
                originalUrl,
                "Release preview",
                "Resolved by the external release-smoke embed provider.",
                "External Embed Smoke",
                null,
                0);
          }
        }
        """;
  }

  private static String oEmbedProviderSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider;
        import cafe.woden.ircclient.ui.chat.embed.spi.OEmbedResponseFields;
        import java.net.URI;
        import java.net.URLEncoder;
        import java.nio.charset.StandardCharsets;

        public final class ReleaseSmokeOEmbedProvider implements OEmbedLinkPreviewProvider {
          @Override
          public String id() {
            return "release-oembed";
          }

          @Override
          public boolean matches(URI uri) {
            return uri != null && "release-oembed.example".equals(uri.getHost());
          }

          @Override
          public URI endpointFor(URI uri, String originalUrl) {
            return URI.create(
                "https://api.release-oembed.example/oembed?url="
                    + URLEncoder.encode(originalUrl, StandardCharsets.UTF_8));
          }

          @Override
          public String defaultSiteName() {
            return "Release oEmbed";
          }

          @Override
          public String titleFallback(OEmbedResponseFields fields) {
            return "Release oEmbed preview";
          }
        }
        """;
  }

  private static String embedHttpHeaderProviderSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider;
        import java.net.URI;
        import java.util.Map;

        public final class ReleaseSmokeEmbedHttpHeaderProvider
            implements EmbedHttpHeaderProvider {
          @Override
          public Map<String, String> embedHttpHeaders(URI uri) {
            if (uri == null || !"cdn.release-embed.example".equals(uri.getHost())) {
              return Map.of();
            }
            return Map.of(
                "Referer", "https://cdn.release-embed.example/",
                "X-Release-Embed", "plugin",
                "Ignored-Blank", " ");
          }
        }
        """;
  }

  private static String imageUrlExtensionProviderSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider;
        import java.util.List;

        public final class ReleaseSmokeImageUrlExtensionProvider
            implements ImageUrlExtensionProvider {
          @Override
          public List<String> imageFileExtensions() {
            return List.of(" AVIF ", ".avif", "bad/path");
          }
        }
        """;
  }

  private static String newsPublisherProfileProviderSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfile;
        import cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider;
        import java.util.List;

        public final class ReleaseSmokeNewsPublisherProfileProvider
            implements NewsPublisherProfileProvider {
          @Override
          public List<NewsPublisherProfile> publisherProfiles() {
            return List.of(
                new NewsPublisherProfile(
                    " Release-Daily ",
                    "Release Daily",
                    new String[] {"www.release-news.example", " release-news.example "},
                    new String[] {" article[data-release-story] p ",
                        "article[data-release-story] p"},
                    new String[] {"meta[name='release-byline']"},
                    new String[] {"meta[property='og:image']"},
                    new String[] {"release-author"},
                    new String[] {"release-date"}));
          }
        }
        """;
  }

  private static String translationBackendSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendContext;
        import cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider;
        import cafe.woden.ircclient.app.translation.spi.MessageTranslationRequest;
        import cafe.woden.ircclient.app.translation.spi.MessageTranslationResult;
        import java.util.concurrent.CompletableFuture;
        import java.util.concurrent.CompletionStage;

        public final class ReleaseSmokeTranslationBackendProvider
            implements MessageTranslationBackendProvider {
          @Override
          public String backendId() {
            return " Release-Translate ";
          }

          @Override
          public CompletionStage<MessageTranslationResult> translate(
              MessageTranslationRequest request, MessageTranslationBackendContext context) {
            String translated =
                "["
                    + request.target().serverId()
                    + "/"
                    + request.target().target()
                    + "] "
                    + request.sourceLanguage()
                    + "->"
                    + request.targetLanguage()
                    + ": "
                    + request.text();
            String provider =
                context.endpoint()
                    + "|"
                    + context.apiKey()
                    + "|"
                    + context.requestTimeoutMs();
            return CompletableFuture.completedFuture(
                new MessageTranslationResult(
                    translated,
                    request.sourceLanguage(),
                    request.targetLanguage(),
                    provider));
          }
        }
        """;
  }

  private static String translationLanguageSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguage;
        import cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider;
        import java.util.Arrays;
        import java.util.List;

        public final class ReleaseSmokeTranslationLanguageProvider
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

  private static String soundPlaybackSource() {
    return """
        package plugin.release;

        import cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider;
        import java.nio.file.Files;
        import java.nio.file.Path;

        public final class ReleaseSmokeCustomSoundPlaybackProvider
            implements CustomSoundPlaybackProvider {
          @Override
          public boolean playCustomSound(Path path) throws Exception {
            if (path == null || !path.toString().toLowerCase().endsWith(".opus")) {
              return false;
            }
            String marker = System.getProperty("ircafe.test.releaseCustomSound");
            if (marker == null || marker.isBlank()) {
              return false;
            }
            Files.writeString(Path.of(marker), path.toString());
            return true;
          }
        }
        """;
  }
}
