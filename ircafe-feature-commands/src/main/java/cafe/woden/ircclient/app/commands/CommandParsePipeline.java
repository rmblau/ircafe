package cafe.woden.ircclient.app.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Root-independent command parse pipeline.
 *
 * <p>Slash parsers return {@code null} when they do not handle a line.
 */
public final class CommandParsePipeline<T> {
  private final Function<String, T> sayFactory;
  private final Function<String, T> unknownFactory;
  private final List<Function<String, T>> slashParsers;

  public CommandParsePipeline(
      Function<String, T> sayFactory,
      Function<String, T> unknownFactory,
      List<? extends Function<String, T>> slashParsers) {
    this.sayFactory = Objects.requireNonNull(sayFactory, "sayFactory");
    this.unknownFactory = Objects.requireNonNull(unknownFactory, "unknownFactory");
    this.slashParsers = copyNonNullParsers(slashParsers);
  }

  public T parse(String raw) {
    String line = raw == null ? "" : raw.trim();
    if (line.isEmpty()) return sayFactory.apply("");

    if (!line.startsWith("/")) {
      return sayFactory.apply(line);
    }

    if (line.startsWith("//")) {
      return sayFactory.apply(line.substring(1));
    }

    for (Function<String, T> parser : slashParsers) {
      T parsed = parser.apply(line);
      if (parsed != null) return parsed;
    }

    return unknownFactory.apply(line);
  }

  private static <T> List<Function<String, T>> copyNonNullParsers(
      List<? extends Function<String, T>> parsers) {
    if (parsers == null) {
      return List.of();
    }
    ArrayList<Function<String, T>> nonNull = new ArrayList<>();
    for (Function<String, T> parser : parsers) {
      if (parser != null) {
        nonNull.add(parser);
      }
    }
    return List.copyOf(nonNull);
  }
}
