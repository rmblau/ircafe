package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
import cafe.woden.ircclient.app.commands.spi.SlashCommandTargetView;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.model.TargetRef;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Shared autocomplete/help catalog for app-owned, built-in, and backend-named slash commands. */
@Component
@ApplicationLayer
public class SlashCommandPresentationCatalog {

  private final SlashCommandPresentationRegistry registry;

  @Autowired
  public SlashCommandPresentationCatalog(
      BackendNamedCommandCatalog backendNamedCommandCatalog,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(
        CommandPluginProviders.slashCommandPresentationContributors(
            List.of(), CommandPluginProviders.resolveInstalledPlugins(installedPluginsProvider)),
        backendNamedCommandCatalog);
  }

  public SlashCommandPresentationCatalog(
      List<? extends cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
          contributors,
      BackendNamedCommandCatalog backendNamedCommandCatalog) {
    BackendNamedCommandCatalog backend =
        Objects.requireNonNull(backendNamedCommandCatalog, "backendNamedCommandCatalog");
    this.registry =
        new SlashCommandPresentationRegistry(
            contributors,
            AppOwnedSlashCommandPresentation.autocompleteCommands(),
            AppOwnedSlashCommandPresentation.generalHelpLines(),
            AppOwnedSlashCommandPresentation.topicHelpHandlers(),
            backend.autocompleteCommands(),
            backend.generalHelpLines(),
            backend.topicHelpLines());
  }

  SlashCommandPresentationCatalog(
      List<? extends cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
          contributors,
      BackendNamedCommandCatalog backendNamedCommandCatalog,
      InstalledPluginsPort installedPlugins) {
    this(
        CommandPluginProviders.slashCommandPresentationContributors(contributors, installedPlugins),
        backendNamedCommandCatalog);
  }

  public List<SlashCommandDescriptor> autocompleteCommands() {
    return registry.autocompleteCommands();
  }

  public void appendGeneralHelp(TargetRef out, BiConsumer<TargetRef, String> lineAppender) {
    Objects.requireNonNull(lineAppender, "lineAppender");
    registry.appendGeneralHelp(helpSink(out, lineAppender));
  }

  public Map<String, Consumer<TargetRef>> topicHelpHandlers(
      BiConsumer<TargetRef, String> lineAppender) {
    Objects.requireNonNull(lineAppender, "lineAppender");
    Map<String, Consumer<SlashCommandHelpSink>> featureHandlers = registry.topicHelpHandlers();
    return featureHandlers.entrySet().stream()
        .collect(
            java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> out -> entry.getValue().accept(helpSink(out, lineAppender))));
  }

  private static void appendStaticHelpLine(
      TargetRef out, String line, BiConsumer<TargetRef, String> lineAppender) {
    if (out == null || line == null || line.isBlank() || lineAppender == null) return;
    lineAppender.accept(out, line);
  }

  private static SlashCommandHelpSink helpSink(
      TargetRef out, BiConsumer<TargetRef, String> lineAppender) {
    return new CatalogSlashCommandHelpSink(out, lineAppender);
  }

  private record CatalogSlashCommandHelpSink(
      TargetRef out, BiConsumer<TargetRef, String> lineAppender) implements SlashCommandHelpSink {

    @Override
    public SlashCommandTargetView target() {
      if (out == null) {
        return new SlashCommandTargetView("", "");
      }
      return new SlashCommandTargetView(out.serverId(), out.target());
    }

    @Override
    public void appendLine(String line) {
      appendStaticHelpLine(out, line, lineAppender);
    }
  }
}
