package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler;
import cafe.woden.ircclient.app.commands.spi.BackendNamedCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Feature-owned registry for backend-scoped named command parser/presentation providers. */
public final class BackendNamedCommandHandlerRegistry {

  private final Map<String, BackendNamedCommandHandler> handlersByCommandName;
  private final List<SlashCommandDescriptor> autocompleteCommands;
  private final List<String> generalHelpLines;
  private final Map<String, List<String>> topicHelpLines;

  public BackendNamedCommandHandlerRegistry(List<? extends BackendNamedCommandHandler> handlers) {
    List<BackendNamedCommandHandler> safeHandlers = copyNonNullHandlers(handlers);
    this.handlersByCommandName = indexHandlersByCommandName(safeHandlers);
    this.autocompleteCommands = buildAutocompleteCommands(safeHandlers);
    this.generalHelpLines = buildGeneralHelpLines(safeHandlers);
    this.topicHelpLines = buildTopicHelpLines(safeHandlers);
  }

  public BackendNamedCommandParseResult parse(String line) {
    String raw = Objects.toString(line, "").trim();
    if (raw.isEmpty() || !raw.startsWith("/")) return null;

    String commandName = extractCommandName(raw);
    if (commandName.isEmpty()) return null;
    BackendNamedCommandHandler handler = handlersByCommandName.get(commandName);
    if (handler == null) return null;
    return handler.parse(raw, commandName);
  }

  public List<SlashCommandDescriptor> autocompleteCommands() {
    return autocompleteCommands;
  }

  public List<String> generalHelpLines() {
    return generalHelpLines;
  }

  public Map<String, List<String>> topicHelpLines() {
    return topicHelpLines;
  }

  private static List<BackendNamedCommandHandler> copyNonNullHandlers(
      List<? extends BackendNamedCommandHandler> handlers) {
    if (handlers == null || handlers.isEmpty()) {
      return List.of();
    }
    ArrayList<BackendNamedCommandHandler> nonNull = new ArrayList<>();
    for (BackendNamedCommandHandler handler : handlers) {
      if (handler != null) {
        nonNull.add(handler);
      }
    }
    return List.copyOf(nonNull);
  }

  private static Map<String, BackendNamedCommandHandler> indexHandlersByCommandName(
      List<BackendNamedCommandHandler> handlers) {
    LinkedHashMap<String, BackendNamedCommandHandler> index = new LinkedHashMap<>();
    for (BackendNamedCommandHandler handler : handlers) {
      Set<String> commandNames =
          Objects.requireNonNullElse(handler.supportedCommandNames(), Set.<String>of());
      for (String commandName : commandNames) {
        String normalized =
            BackendNamedCommandRegistrationSupport.normalizeCommandName(commandName);
        if (normalized.isEmpty()) continue;
        if (BackendNamedCommandRegistrationSupport.isReservedCommandName(normalized)) {
          throw new IllegalStateException(
              "Backend named command '"
                  + normalized
                  + "' collides with a reserved built-in command");
        }
        BackendNamedCommandHandler previous = index.putIfAbsent(normalized, handler);
        if (previous != null && previous != handler) {
          throw new IllegalStateException(
              "Duplicate backend named parser handler registered for command '" + normalized + "'");
        }
      }
    }
    return Map.copyOf(index);
  }

  private static List<SlashCommandDescriptor> buildAutocompleteCommands(
      List<BackendNamedCommandHandler> handlers) {
    LinkedHashMap<String, SlashCommandDescriptor> byCommand = new LinkedHashMap<>();
    for (BackendNamedCommandHandler handler : handlers) {
      List<SlashCommandDescriptor> commands =
          Objects.requireNonNullElse(handler.autocompleteCommands(), List.of());
      for (SlashCommandDescriptor command : commands) {
        if (command == null) continue;
        byCommand.putIfAbsent(command.command().toLowerCase(Locale.ROOT), command);
      }
    }
    return List.copyOf(byCommand.values());
  }

  private static List<String> buildGeneralHelpLines(List<BackendNamedCommandHandler> handlers) {
    ArrayList<String> lines = new ArrayList<>();
    for (BackendNamedCommandHandler handler : handlers) {
      List<String> handlerLines = Objects.requireNonNullElse(handler.generalHelpLines(), List.of());
      appendLines(lines, handlerLines);
    }
    return List.copyOf(lines);
  }

  private static Map<String, List<String>> buildTopicHelpLines(
      List<BackendNamedCommandHandler> handlers) {
    LinkedHashMap<String, ArrayList<String>> linesByTopic = new LinkedHashMap<>();
    for (BackendNamedCommandHandler handler : handlers) {
      Map<String, List<String>> handlerLines =
          Objects.requireNonNullElse(handler.topicHelpLines(), Map.of());
      for (Map.Entry<String, List<String>> entry : handlerLines.entrySet()) {
        String topic = normalizeHelpTopic(entry.getKey());
        if (topic.isEmpty()) continue;
        ArrayList<String> lines = linesByTopic.computeIfAbsent(topic, __ -> new ArrayList<>());
        appendLines(lines, entry.getValue());
      }
    }
    LinkedHashMap<String, List<String>> immutable = new LinkedHashMap<>();
    for (Map.Entry<String, ArrayList<String>> entry : linesByTopic.entrySet()) {
      immutable.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return Map.copyOf(immutable);
  }

  private static void appendLines(List<String> out, List<String> lines) {
    if (out == null || lines == null) return;
    for (String line : lines) {
      String normalized = Objects.toString(line, "").trim();
      if (!normalized.isEmpty()) {
        out.add(normalized);
      }
    }
  }

  private static String extractCommandName(String line) {
    int end = line.indexOf(' ');
    String token = end < 0 ? line : line.substring(0, end);
    return BackendNamedCommandRegistrationSupport.normalizeCommandName(token);
  }

  private static String normalizeHelpTopic(String raw) {
    String topic = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (topic.startsWith("/")) topic = topic.substring(1).trim();
    return topic;
  }
}
