package cafe.woden.ircclient.app.outbound.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.app.api.BackendEditorProfileSpec;
import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;
import cafe.woden.ircclient.app.outbound.upload.spi.UploadCommandTranslationHandler;
import cafe.woden.ircclient.config.api.RuntimeConfigPathPort;
import cafe.woden.ircclient.config.plugins.InstalledPluginServices;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeCatalog;
import cafe.woden.ircclient.util.CompiledPluginJarSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackendExtensionProviderGuideFixtureTest {

  private static final String GUIDE_PROVIDER_CLASS = "example.backend.ExampleBackendExtension";

  @TempDir Path tempDir;

  @Test
  void documentedBackendExtensionContributesMetadataFeaturesAndPortableHandlers() throws Exception {
    Path runtimeConfigDirectory = Files.createDirectories(tempDir.resolve("config-home/ircafe"));
    Path pluginDir = Files.createDirectories(runtimeConfigDirectory.resolve("plugins"));
    CompiledPluginJarSupport.writePluginJar(
        pluginDir.resolve("backend-extension-guide-example.jar"),
        GUIDE_PROVIDER_CLASS,
        guideProviderSource(),
        BackendExtension.class.getName(),
        CompiledPluginJarSupport.compatibleManifest("backend-extension-guide-example", "1.0.0"));
    RuntimeConfigPathPort runtimeConfigPathPort =
        () -> runtimeConfigDirectory.resolve("ircafe.yml");

    InstalledPluginServices installedPlugins = new InstalledPluginServices(runtimeConfigPathPort);
    BackendExtensionCatalog catalog =
        new BackendExtensionCatalog(
            BackendExtensionCatalogState.fromInstalledServices(
                installedPlugins, Ircv3MessageMutationRuntimeCatalog.applicationClasspath()));

    assertTrue(installedPlugins.pluginProblems().isEmpty());
    assertTrue(catalog.availableBackendIds().contains("guide-backend"));
    assertEquals("Guide Backend", catalog.backendDisplayName("GUIDE-BACKEND"));

    OutboundBackendFeatureAdapter adapter = catalog.featureAdapterFor("guide-backend");
    assertEquals("guide-backend", adapter.backendId());
    assertTrue(adapter.supportsSemanticUpload());
    assertFalse(adapter.persistsJoinedChannelsLocally());
    assertFalse(adapter.supportsQuasselCoreCommands());

    MessageMutationOutboundCommands mutationCommands =
        new MessageMutationOutboundCommandsRouter(catalog).commandsFor("guide-backend");
    MessageMutationTargetView target = new MessageMutationTargetView("libera", "#guide");

    assertEquals("guide-backend", mutationCommands.backendId());
    assertEquals(
        "GUIDE REPLY libera #guide msg-123 hello",
        mutationCommands.buildReplyRawLine(target, "msg-123", "hello"));
    assertEquals(
        "GUIDE REACT libera #guide msg-123 +1",
        mutationCommands.buildReactRawLine(target, "msg-123", "+1"));
    assertEquals(
        "GUIDE UNREACT libera #guide msg-123 +1",
        mutationCommands.buildUnreactRawLine(target, "msg-123", "+1"));
    assertEquals(
        "GUIDE EDIT libera #guide msg-456 edited text",
        mutationCommands.buildEditRawLine(target, "msg-456", "edited text"));
    assertEquals(
        "GUIDE REDACT libera #guide msg-456 reason",
        mutationCommands.buildRedactRawLine(target, "msg-456", "reason"));

    UploadCommandTranslationHandler uploadHandler =
        new BackendUploadCommandRegistry(catalog).find("guide-backend");

    assertEquals("guide-backend", uploadHandler.backendId());
    assertEquals(
        "GUIDE UPLOAD #guide m.image /tmp/example.png Guide image",
        uploadHandler.translateUpload("#guide", "m.image", "/tmp/example.png", "Guide image"));

    BackendEditorProfileSpec profile =
        catalog.availableBackendEditorProfiles().stream()
            .filter(candidate -> "guide-backend".equals(candidate.backendId()))
            .findFirst()
            .orElseThrow();

    assertEquals("Guide Backend", profile.displayName());
    assertEquals(1667, profile.defaultPort(false));
    assertEquals(6697, profile.defaultPort(true));
    assertTrue(profile.directAuthEnabled());
    assertTrue(profile.requiresNick());
    assertEquals("guide.example.net", profile.hostPlaceholder());
    assertEquals("guide-user", profile.loginPlaceholder());
  }

  private static String guideProviderSource() {
    return """
        package example.backend;

        import cafe.woden.ircclient.app.outbound.backend.spi.BackendEditorProfile;
        import cafe.woden.ircclient.app.outbound.backend.spi.BackendExtension;
        import cafe.woden.ircclient.app.outbound.backend.spi.OutboundBackendFeatureAdapter;
        import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationOutboundCommands;
        import cafe.woden.ircclient.app.outbound.mutation.spi.MessageMutationTargetView;
        import cafe.woden.ircclient.app.outbound.upload.spi.UploadCommandTranslationHandler;

        public final class ExampleBackendExtension implements BackendExtension {
          private static final String BACKEND_ID = "guide-backend";

          @Override
          public String backendId() {
            return BACKEND_ID;
          }

          @Override
          public OutboundBackendFeatureAdapter featureAdapter() {
            return new OutboundBackendFeatureAdapter() {
              @Override
              public String backendId() {
                return BACKEND_ID;
              }

              @Override
              public boolean supportsSemanticUpload() {
                return true;
              }

              @Override
              public boolean persistsJoinedChannelsLocally() {
                return false;
              }
            };
          }

          @Override
          public MessageMutationOutboundCommands messageMutationOutboundCommands() {
            return new MessageMutationOutboundCommands() {
              @Override
              public String backendId() {
                return BACKEND_ID;
              }

              @Override
              public String buildReplyRawLine(
                  MessageMutationTargetView target, String replyToMessageId, String message) {
                return command("REPLY", target, replyToMessageId, message);
              }

              @Override
              public String buildReactRawLine(
                  MessageMutationTargetView target, String replyToMessageId, String reaction) {
                return command("REACT", target, replyToMessageId, reaction);
              }

              @Override
              public String buildUnreactRawLine(
                  MessageMutationTargetView target, String replyToMessageId, String reaction) {
                return command("UNREACT", target, replyToMessageId, reaction);
              }

              @Override
              public String buildEditRawLine(
                  MessageMutationTargetView target, String targetMessageId, String editedText) {
                return command("EDIT", target, targetMessageId, editedText);
              }

              @Override
              public String buildRedactRawLine(
                  MessageMutationTargetView target, String targetMessageId, String reason) {
                return command("REDACT", target, targetMessageId, reason);
              }

              private String command(
                  String command,
                  MessageMutationTargetView target,
                  String messageId,
                  String payload) {
                return "GUIDE "
                    + command
                    + " "
                    + target.serverId()
                    + " "
                    + target.target()
                    + " "
                    + messageId
                    + " "
                    + payload;
              }
            };
          }

          @Override
          public UploadCommandTranslationHandler uploadCommandTranslationHandler() {
            return new UploadCommandTranslationHandler() {
              @Override
              public String backendId() {
                return BACKEND_ID;
              }

              @Override
              public String translateUpload(
                  String target, String msgType, String sourcePath, String displayBody) {
                return "GUIDE UPLOAD "
                    + target
                    + " "
                    + msgType
                    + " "
                    + sourcePath
                    + " "
                    + displayBody;
              }
            };
          }

          @Override
          public BackendEditorProfile editorProfile() {
            return new BackendEditorProfile(
                BACKEND_ID,
                "Guide Backend",
                1667,
                6697,
                true,
                false,
                true,
                false,
                false,
                "guide-user",
                "Guide host",
                "Guide password",
                "Guide nick",
                "Guide login",
                "Guide real name",
                "Use Guide TLS",
                "Connects to a guide backend endpoint.",
                "Guide backend auth is configured directly.",
                "(optional)",
                "guide.example.net",
                "guide-user",
                "GuideNick",
                "Guide User");
          }
        }
        """;
  }
}
