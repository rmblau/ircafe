package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Feature-owned registry for typed slash-command parse strategies. */
public final class SlashCommandParseStrategyRegistry {

  private final List<SlashCommandParseStrategy> strategies;

  public SlashCommandParseStrategyRegistry(List<? extends SlashCommandParseStrategy> strategies) {
    this.strategies = copyNonNullStrategies(strategies);
  }

  public SlashCommandParseResult tryParse(String line) {
    return tryParse(line, Function.identity());
  }

  public <T> T tryParse(String line, Function<SlashCommandParseResult, T> resultMapper) {
    Objects.requireNonNull(resultMapper, "resultMapper");
    for (SlashCommandParseStrategy strategy : strategies) {
      SlashCommandParseResult result = strategy.tryParse(line);
      if (result != null) {
        T mapped = resultMapper.apply(result);
        if (mapped != null) {
          return mapped;
        }
      }
    }
    return null;
  }

  private static List<SlashCommandParseStrategy> copyNonNullStrategies(
      List<? extends SlashCommandParseStrategy> strategies) {
    if (strategies == null || strategies.isEmpty()) {
      return List.of();
    }
    ArrayList<SlashCommandParseStrategy> nonNull = new ArrayList<>();
    for (SlashCommandParseStrategy strategy : strategies) {
      if (strategy != null) {
        nonNull.add(strategy);
      }
    }
    return List.copyOf(nonNull);
  }
}
