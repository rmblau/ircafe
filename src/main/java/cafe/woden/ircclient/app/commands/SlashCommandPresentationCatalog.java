package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
import cafe.woden.ircclient.app.commands.spi.SlashCommandTargetView;
import cafe.woden.ircclient.config.api.InstalledPluginsPort;
import cafe.woden.ircclient.model.TargetRef;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

  private static final List<SlashCommandDescriptor> APP_OWNED_AUTOCOMPLETE_COMMANDS =
      List.of(new SlashCommandDescriptor("/filter", "Local filtering controls"));

  private static final List<String> APP_OWNED_GENERAL_HELP_LINES =
      List.of("Local: /filter help for local filtering controls.");

  private static final Map<String, Consumer<SlashCommandHelpSink>> APP_OWNED_TOPIC_HELP_HANDLERS =
      Map.of("filter", SlashCommandPresentationCatalog::appendFilterHelp);

  private final List<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
      contributors;
  private final BackendNamedCommandCatalog backendNamedCommandCatalog;
  private final List<SlashCommandDescriptor> autocompleteCommands;

  @Autowired
  public SlashCommandPresentationCatalog(
      List<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor> contributors,
      BackendNamedCommandCatalog backendNamedCommandCatalog,
      ObjectProvider<InstalledPluginsPort> installedPluginsProvider) {
    this(
        CommandPluginProviders.slashCommandPresentationContributors(
            contributors, CommandPluginProviders.resolveInstalledPlugins(installedPluginsProvider)),
        backendNamedCommandCatalog);
  }

  public SlashCommandPresentationCatalog(
      List<? extends cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
          contributors,
      BackendNamedCommandCatalog backendNamedCommandCatalog) {
    this.contributors = nonNullContributors(contributors);
    this.backendNamedCommandCatalog =
        Objects.requireNonNull(backendNamedCommandCatalog, "backendNamedCommandCatalog");
    this.autocompleteCommands = buildAutocompleteCommands();
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
    return autocompleteCommands;
  }

  public void appendGeneralHelp(TargetRef out, BiConsumer<TargetRef, String> lineAppender) {
    Objects.requireNonNull(lineAppender, "lineAppender");
    APP_OWNED_GENERAL_HELP_LINES.forEach(line -> appendStaticHelpLine(out, line, lineAppender));
    SlashCommandHelpSink help = helpSink(out, lineAppender);
    for (cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor contributor :
        contributors) {
      contributor.appendGeneralHelp(help);
    }
    backendNamedCommandCatalog
        .generalHelpLines()
        .forEach(line -> appendStaticHelpLine(out, line, lineAppender));
  }

  public Map<String, Consumer<TargetRef>> topicHelpHandlers(
      BiConsumer<TargetRef, String> lineAppender) {
    Objects.requireNonNull(lineAppender, "lineAppender");
    LinkedHashMap<String, List<Consumer<TargetRef>>> handlers = new LinkedHashMap<>();
    appendAppOwnedTopicHelpHandlers(handlers, lineAppender);
    appendPresentationTopicHelpHandlers(handlers, lineAppender);
    appendBackendTopicHelpHandlers(handlers, lineAppender);

    LinkedHashMap<String, Consumer<TargetRef>> composed = new LinkedHashMap<>();
    for (Map.Entry<String, List<Consumer<TargetRef>>> entry : handlers.entrySet()) {
      List<Consumer<TargetRef>> topicHandlers = List.copyOf(entry.getValue());
      composed.put(entry.getKey(), out -> topicHandlers.forEach(handler -> handler.accept(out)));
    }
    return Map.copyOf(composed);
  }

  private List<SlashCommandDescriptor> buildAutocompleteCommands() {
    LinkedHashMap<String, SlashCommandDescriptor> merged = new LinkedHashMap<>();
    APP_OWNED_AUTOCOMPLETE_COMMANDS.forEach(
        command -> merged.putIfAbsent(command.command().toLowerCase(Locale.ROOT), command));
    for (cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor contributor :
        contributors) {
      for (SlashCommandDescriptor command :
          Objects.requireNonNullElse(
              contributor.autocompleteCommands(), List.<SlashCommandDescriptor>of())) {
        if (command == null) continue;
        merged.putIfAbsent(command.command().toLowerCase(Locale.ROOT), command);
      }
    }
    for (SlashCommandDescriptor command : backendNamedCommandCatalog.autocompleteCommands()) {
      if (command == null) continue;
      merged.putIfAbsent(command.command().toLowerCase(Locale.ROOT), command);
    }
    return List.copyOf(merged.values());
  }

  private void appendAppOwnedTopicHelpHandlers(
      LinkedHashMap<String, List<Consumer<TargetRef>>> handlers,
      BiConsumer<TargetRef, String> lineAppender) {
    for (Map.Entry<String, Consumer<SlashCommandHelpSink>> entry :
        APP_OWNED_TOPIC_HELP_HANDLERS.entrySet()) {
      String topic = normalizeHelpTopic(entry.getKey());
      Consumer<SlashCommandHelpSink> consumer = entry.getValue();
      if (!topic.isEmpty() && consumer != null) {
        addTopicHelpHandler(handlers, topic, out -> consumer.accept(helpSink(out, lineAppender)));
      }
    }
  }

  private void appendPresentationTopicHelpHandlers(
      LinkedHashMap<String, List<Consumer<TargetRef>>> handlers,
      BiConsumer<TargetRef, String> lineAppender) {
    for (cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor contributor :
        contributors) {
      Map<String, Consumer<SlashCommandHelpSink>> topicHandlers =
          Objects.requireNonNullElse(
              contributor.topicHelpHandlers(), Map.<String, Consumer<SlashCommandHelpSink>>of());
      for (Map.Entry<String, Consumer<SlashCommandHelpSink>> entry : topicHandlers.entrySet()) {
        String topic = normalizeHelpTopic(entry.getKey());
        Consumer<SlashCommandHelpSink> consumer = entry.getValue();
        if (!topic.isEmpty() && consumer != null) {
          addTopicHelpHandler(handlers, topic, out -> consumer.accept(helpSink(out, lineAppender)));
        }
      }
    }
  }

  private void appendBackendTopicHelpHandlers(
      LinkedHashMap<String, List<Consumer<TargetRef>>> handlers,
      BiConsumer<TargetRef, String> lineAppender) {
    for (Map.Entry<String, List<String>> entry :
        backendNamedCommandCatalog.topicHelpLines().entrySet()) {
      String topic = normalizeHelpTopic(entry.getKey());
      List<String> lines = entry.getValue();
      if (topic.isEmpty() || lines == null || lines.isEmpty()) continue;
      addTopicHelpHandler(
          handlers,
          topic,
          out -> lines.forEach(line -> appendStaticHelpLine(out, line, lineAppender)));
    }
  }

  private static void addTopicHelpHandler(
      LinkedHashMap<String, List<Consumer<TargetRef>>> handlers,
      String topic,
      Consumer<TargetRef> handler) {
    handlers.computeIfAbsent(topic, ignored -> new ArrayList<>()).add(handler);
  }

  private static List<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
      nonNullContributors(
          List<? extends cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
              contributors) {
    if (contributors == null || contributors.isEmpty()) {
      return List.of();
    }
    java.util.ArrayList<cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor>
        nonNull = new java.util.ArrayList<>();
    for (cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor contributor :
        contributors) {
      if (contributor != null) {
        nonNull.add(contributor);
      }
    }
    return List.copyOf(nonNull);
  }

  private static void appendFilterHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /filter help");
    help.appendLine(
        "Examples: /filter list, /filter add <name> key=value ..., /filter defaults ...");
    help.appendLine(
        "Local filtering remains app-owned because it depends on filter state and UI rendering.");
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

  private static String normalizeHelpTopic(String raw) {
    String topic = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (topic.startsWith("/")) topic = topic.substring(1).trim();
    return topic;
  }
}
