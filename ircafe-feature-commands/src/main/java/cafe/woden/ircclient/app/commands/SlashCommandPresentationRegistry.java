package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Feature-owned aggregation for slash-command presentation metadata. */
public final class SlashCommandPresentationRegistry {

  private final List<SlashCommandPresentationContributor> contributors;
  private final List<SlashCommandDescriptor> appOwnedAutocompleteCommands;
  private final List<String> appOwnedGeneralHelpLines;
  private final Map<String, Consumer<SlashCommandHelpSink>> appOwnedTopicHelpHandlers;
  private final List<SlashCommandDescriptor> backendAutocompleteCommands;
  private final List<String> backendGeneralHelpLines;
  private final Map<String, List<String>> backendTopicHelpLines;
  private final List<SlashCommandDescriptor> autocompleteCommands;

  public SlashCommandPresentationRegistry(
      List<? extends SlashCommandPresentationContributor> contributors,
      List<SlashCommandDescriptor> appOwnedAutocompleteCommands,
      List<String> appOwnedGeneralHelpLines,
      Map<String, Consumer<SlashCommandHelpSink>> appOwnedTopicHelpHandlers,
      List<SlashCommandDescriptor> backendAutocompleteCommands,
      List<String> backendGeneralHelpLines,
      Map<String, List<String>> backendTopicHelpLines) {
    this.contributors = copyNonNullContributors(contributors);
    this.appOwnedAutocompleteCommands =
        List.copyOf(Objects.requireNonNullElse(appOwnedAutocompleteCommands, List.of()));
    this.appOwnedGeneralHelpLines =
        List.copyOf(Objects.requireNonNullElse(appOwnedGeneralHelpLines, List.of()));
    this.appOwnedTopicHelpHandlers =
        Map.copyOf(Objects.requireNonNullElse(appOwnedTopicHelpHandlers, Map.of()));
    this.backendAutocompleteCommands =
        List.copyOf(Objects.requireNonNullElse(backendAutocompleteCommands, List.of()));
    this.backendGeneralHelpLines =
        List.copyOf(Objects.requireNonNullElse(backendGeneralHelpLines, List.of()));
    this.backendTopicHelpLines = copyTopicHelpLines(backendTopicHelpLines);
    this.autocompleteCommands = buildAutocompleteCommands();
  }

  public List<SlashCommandDescriptor> autocompleteCommands() {
    return autocompleteCommands;
  }

  public void appendGeneralHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    appOwnedGeneralHelpLines.forEach(line -> appendStaticHelpLine(help, line));
    for (SlashCommandPresentationContributor contributor : contributors) {
      contributor.appendGeneralHelp(help);
    }
    backendGeneralHelpLines.forEach(line -> appendStaticHelpLine(help, line));
  }

  public Map<String, Consumer<SlashCommandHelpSink>> topicHelpHandlers() {
    LinkedHashMap<String, List<Consumer<SlashCommandHelpSink>>> handlers = new LinkedHashMap<>();
    appendAppOwnedTopicHelpHandlers(handlers);
    appendPresentationTopicHelpHandlers(handlers);
    appendBackendTopicHelpHandlers(handlers);

    LinkedHashMap<String, Consumer<SlashCommandHelpSink>> composed = new LinkedHashMap<>();
    for (Map.Entry<String, List<Consumer<SlashCommandHelpSink>>> entry : handlers.entrySet()) {
      List<Consumer<SlashCommandHelpSink>> topicHandlers = List.copyOf(entry.getValue());
      composed.put(entry.getKey(), help -> topicHandlers.forEach(handler -> handler.accept(help)));
    }
    return Map.copyOf(composed);
  }

  private List<SlashCommandDescriptor> buildAutocompleteCommands() {
    LinkedHashMap<String, SlashCommandDescriptor> merged = new LinkedHashMap<>();
    appendAutocompleteCommands(merged, appOwnedAutocompleteCommands);
    for (SlashCommandPresentationContributor contributor : contributors) {
      appendAutocompleteCommands(
          merged,
          Objects.requireNonNullElse(
              contributor.autocompleteCommands(), List.<SlashCommandDescriptor>of()));
    }
    appendAutocompleteCommands(merged, backendAutocompleteCommands);
    return List.copyOf(merged.values());
  }

  private static void appendAutocompleteCommands(
      LinkedHashMap<String, SlashCommandDescriptor> merged, List<SlashCommandDescriptor> commands) {
    if (commands == null) return;
    for (SlashCommandDescriptor command : commands) {
      if (command == null) continue;
      merged.putIfAbsent(command.command().toLowerCase(Locale.ROOT), command);
    }
  }

  private void appendAppOwnedTopicHelpHandlers(
      LinkedHashMap<String, List<Consumer<SlashCommandHelpSink>>> handlers) {
    for (Map.Entry<String, Consumer<SlashCommandHelpSink>> entry :
        appOwnedTopicHelpHandlers.entrySet()) {
      String topic = normalizeHelpTopic(entry.getKey());
      Consumer<SlashCommandHelpSink> consumer = entry.getValue();
      if (!topic.isEmpty() && consumer != null) {
        addTopicHelpHandler(handlers, topic, consumer);
      }
    }
  }

  private void appendPresentationTopicHelpHandlers(
      LinkedHashMap<String, List<Consumer<SlashCommandHelpSink>>> handlers) {
    for (SlashCommandPresentationContributor contributor : contributors) {
      Map<String, Consumer<SlashCommandHelpSink>> topicHandlers =
          Objects.requireNonNullElse(
              contributor.topicHelpHandlers(), Map.<String, Consumer<SlashCommandHelpSink>>of());
      for (Map.Entry<String, Consumer<SlashCommandHelpSink>> entry : topicHandlers.entrySet()) {
        String topic = normalizeHelpTopic(entry.getKey());
        Consumer<SlashCommandHelpSink> consumer = entry.getValue();
        if (!topic.isEmpty() && consumer != null) {
          addTopicHelpHandler(handlers, topic, consumer);
        }
      }
    }
  }

  private void appendBackendTopicHelpHandlers(
      LinkedHashMap<String, List<Consumer<SlashCommandHelpSink>>> handlers) {
    for (Map.Entry<String, List<String>> entry : backendTopicHelpLines.entrySet()) {
      String topic = normalizeHelpTopic(entry.getKey());
      List<String> lines = entry.getValue();
      if (topic.isEmpty() || lines == null || lines.isEmpty()) continue;
      addTopicHelpHandler(handlers, topic, help -> lines.forEach(line -> appendStaticHelpLine(help, line)));
    }
  }

  private static void addTopicHelpHandler(
      LinkedHashMap<String, List<Consumer<SlashCommandHelpSink>>> handlers,
      String topic,
      Consumer<SlashCommandHelpSink> handler) {
    handlers.computeIfAbsent(topic, ignored -> new ArrayList<>()).add(handler);
  }

  private static List<SlashCommandPresentationContributor> copyNonNullContributors(
      List<? extends SlashCommandPresentationContributor> contributors) {
    if (contributors == null || contributors.isEmpty()) {
      return List.of();
    }
    ArrayList<SlashCommandPresentationContributor> nonNull = new ArrayList<>();
    for (SlashCommandPresentationContributor contributor : contributors) {
      if (contributor != null) {
        nonNull.add(contributor);
      }
    }
    return List.copyOf(nonNull);
  }

  private static Map<String, List<String>> copyTopicHelpLines(Map<String, List<String>> lines) {
    if (lines == null || lines.isEmpty()) {
      return Map.of();
    }
    LinkedHashMap<String, List<String>> copied = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : lines.entrySet()) {
      copied.put(entry.getKey(), List.copyOf(Objects.requireNonNullElse(entry.getValue(), List.of())));
    }
    return Map.copyOf(copied);
  }

  private static void appendStaticHelpLine(SlashCommandHelpSink help, String line) {
    if (help == null || line == null || line.isBlank()) return;
    help.appendLine(line);
  }

  private static String normalizeHelpTopic(String raw) {
    String topic = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (topic.startsWith("/")) topic = topic.substring(1).trim();
    return topic;
  }
}
