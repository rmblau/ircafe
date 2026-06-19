package cafe.woden.ircclient.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PluginApiDependencyAuditTest {

  private static final Pattern IMPORT_PATTERN =
      Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.]+)\\s*;");

  private static final Set<String> APPROVED_PLUGIN_API_BLOCKERS =
      Set.of(
          "src/main/java/cafe/woden/ircclient/app/commands/spi/BackendNamedCommandExecutor.java -> cafe.woden.ircclient.app.commands.BackendNamedCommandExecutionContext",
          "src/main/java/cafe/woden/ircclient/app/commands/spi/BackendNamedCommandExecutor.java -> cafe.woden.ircclient.app.commands.ParsedInput",
          "src/main/java/cafe/woden/ircclient/app/commands/spi/BackendNamedCommandExecutor.java -> io.reactivex.rxjava3.disposables.CompositeDisposable",
          "src/main/java/cafe/woden/ircclient/app/commands/spi/BackendNamedCommandExecutor.java -> org.jmolecules.architecture.hexagonal.SecondaryPort",
          "src/main/java/cafe/woden/ircclient/app/commands/spi/BackendNamedCommandExecutor.java -> org.jmolecules.architecture.layered.ApplicationLayer",
          "src/main/java/cafe/woden/ircclient/app/commands/spi/BackendNamedCommandHandler.java -> cafe.woden.ircclient.app.commands.ParsedInput",
          "src/main/java/cafe/woden/ircclient/app/commands/spi/BackendNamedCommandHandler.java -> org.jmolecules.architecture.hexagonal.SecondaryPort",
          "src/main/java/cafe/woden/ircclient/app/commands/spi/BackendNamedCommandHandler.java -> org.jmolecules.architecture.layered.ApplicationLayer",
          "src/main/java/cafe/woden/ircclient/app/commands/spi/SlashCommandParseStrategy.java -> cafe.woden.ircclient.app.commands.ParsedInput",
          "src/main/java/cafe/woden/ircclient/app/commands/spi/SlashCommandParseStrategy.java -> org.jmolecules.architecture.layered.ApplicationLayer",
          "src/main/java/cafe/woden/ircclient/app/commands/spi/package-info.java -> org.springframework.modulith.NamedInterface",
          "src/main/java/cafe/woden/ircclient/app/outbound/backend/spi/BackendExtension.java -> cafe.woden.ircclient.app.api.BackendEditorProfileSpec",
          "src/main/java/cafe/woden/ircclient/app/outbound/backend/spi/BackendExtension.java -> cafe.woden.ircclient.app.outbound.mutation.MessageMutationOutboundCommands",
          "src/main/java/cafe/woden/ircclient/app/outbound/backend/spi/BackendExtension.java -> org.jmolecules.architecture.hexagonal.SecondaryPort",
          "src/main/java/cafe/woden/ircclient/app/outbound/backend/spi/BackendExtension.java -> org.jmolecules.architecture.layered.ApplicationLayer",
          "src/main/java/cafe/woden/ircclient/app/outbound/backend/spi/OutboundBackendFeatureAdapter.java -> cafe.woden.ircclient.irc.port.IrcNegotiatedFeaturePort",
          "src/main/java/cafe/woden/ircclient/app/outbound/backend/spi/OutboundBackendFeatureAdapter.java -> org.jmolecules.architecture.hexagonal.SecondaryPort",
          "src/main/java/cafe/woden/ircclient/app/outbound/backend/spi/OutboundBackendFeatureAdapter.java -> org.jmolecules.architecture.layered.ApplicationLayer",
          "src/main/java/cafe/woden/ircclient/app/outbound/spi/LocalFilterCommandHandler.java -> cafe.woden.ircclient.app.commands.FilterCommand",
          "src/main/java/cafe/woden/ircclient/app/outbound/spi/LocalFilterCommandHandler.java -> org.jmolecules.architecture.hexagonal.SecondaryPort",
          "src/main/java/cafe/woden/ircclient/app/outbound/spi/LocalFilterCommandHandler.java -> org.jmolecules.architecture.layered.ApplicationLayer",
          "src/main/java/cafe/woden/ircclient/app/outbound/spi/package-info.java -> org.springframework.modulith.NamedInterface",
          "src/main/java/cafe/woden/ircclient/app/outbound/upload/spi/SemanticUploadCommandHandler.java -> cafe.woden.ircclient.model.TargetRef",
          "src/main/java/cafe/woden/ircclient/app/outbound/upload/spi/SemanticUploadCommandHandler.java -> org.jmolecules.architecture.hexagonal.SecondaryPort",
          "src/main/java/cafe/woden/ircclient/app/outbound/upload/spi/SemanticUploadCommandHandler.java -> org.jmolecules.architecture.layered.ApplicationLayer",
          "src/main/java/cafe/woden/ircclient/app/translation/spi/MessageTranslationBackendProvider.java -> cafe.woden.ircclient.app.translation.MessageTranslationRequest",
          "src/main/java/cafe/woden/ircclient/app/translation/spi/MessageTranslationBackendProvider.java -> cafe.woden.ircclient.app.translation.MessageTranslationResult",
          "src/main/java/cafe/woden/ircclient/app/translation/spi/MessageTranslationBackendProvider.java -> org.jmolecules.architecture.hexagonal.SecondaryPort",
          "src/main/java/cafe/woden/ircclient/app/translation/spi/MessageTranslationBackendProvider.java -> org.jmolecules.architecture.layered.ApplicationLayer",
          "src/main/java/cafe/woden/ircclient/app/translation/spi/package-info.java -> org.springframework.modulith.NamedInterface",
          "src/main/java/cafe/woden/ircclient/bouncer/spi/package-info.java -> org.springframework.modulith.NamedInterface",
          "src/main/java/cafe/woden/ircclient/irc/backend/spi/IrcBackendClientService.java -> cafe.woden.ircclient.irc.IrcClientService",
          "src/main/java/cafe/woden/ircclient/irc/backend/spi/IrcBackendClientService.java -> cafe.woden.ircclient.irc.backend.IrcBackendAvailabilityPort",
          "src/main/java/cafe/woden/ircclient/irc/backend/spi/IrcBackendClientService.java -> cafe.woden.ircclient.irc.playback.IrcBouncerPlaybackPort",
          "src/main/java/cafe/woden/ircclient/irc/backend/spi/IrcBackendClientService.java -> cafe.woden.ircclient.irc.quassel.control.QuasselCoreControlPort",
          "src/main/java/cafe/woden/ircclient/irc/backend/spi/IrcBackendClientService.java -> org.jmolecules.architecture.hexagonal.SecondaryPort",
          "src/main/java/cafe/woden/ircclient/irc/backend/spi/IrcBackendClientService.java -> org.jmolecules.architecture.layered.ApplicationLayer",
          "src/main/java/cafe/woden/ircclient/irc/backend/spi/package-info.java -> org.springframework.modulith.NamedInterface",
          "src/main/java/cafe/woden/ircclient/irc/ircv3/spi/package-info.java -> org.springframework.modulith.NamedInterface",
          "src/main/java/cafe/woden/ircclient/notify/spi/package-info.java -> org.springframework.modulith.NamedInterface");

  @Test
  void spiPluginApiBlockersStayIntentional() throws IOException {
    Set<String> actualBlockers = scanBlockingDependencies();

    Set<String> added = new TreeSet<>(actualBlockers);
    added.removeAll(APPROVED_PLUGIN_API_BLOCKERS);

    Set<String> removed = new TreeSet<>(APPROVED_PLUGIN_API_BLOCKERS);
    removed.removeAll(actualBlockers);

    assertTrue(
        added.isEmpty() && removed.isEmpty(),
        () ->
            "SPI plugin API dependency audit changed. "
                + "Update the baseline only when the change is intentional.\n"
                + "Added blockers:\n"
                + formatLines(added)
                + "\nRemoved blockers:\n"
                + formatLines(removed));
  }

  private static Set<String> scanBlockingDependencies() throws IOException {
    Set<String> blockers = new TreeSet<>();

    for (Path sourceRoot :
        List.of(Path.of("src/main/java"), Path.of("ircafe-plugin-api/src/main/java"))) {
      if (!Files.exists(sourceRoot)) {
        continue;
      }
      try (Stream<Path> files = Files.walk(sourceRoot)) {
        for (Path file :
            files.filter(PluginApiDependencyAuditTest::isSpiJavaSource).sorted().toList()) {
          Matcher matcher = IMPORT_PATTERN.matcher(Files.readString(file));
          while (matcher.find()) {
            String dependency = matcher.group(1);
            if (!isPluginApiPortable(dependency)) {
              blockers.add(file + " -> " + dependency);
            }
          }
        }
      }
    }

    return blockers;
  }

  private static boolean isSpiJavaSource(Path path) {
    String normalized = path.toString().replace('\\', '/');
    return (normalized.startsWith("src/main/java/")
            || normalized.startsWith("ircafe-plugin-api/src/main/java/"))
        && normalized.contains("/spi/")
        && normalized.endsWith(".java");
  }

  private static boolean isPluginApiPortable(String dependency) {
    return dependency.equals("cafe.woden.ircclient.app.translation.MessageTranslationLanguage")
        || dependency.startsWith("java.")
        || dependency.startsWith("javax.annotation.")
        || (dependency.startsWith("cafe.woden.ircclient.") && dependency.contains(".spi."));
  }

  private static String formatLines(Set<String> lines) {
    return lines.isEmpty() ? "  <none>" : "  " + String.join("\n  ", lines);
  }
}
