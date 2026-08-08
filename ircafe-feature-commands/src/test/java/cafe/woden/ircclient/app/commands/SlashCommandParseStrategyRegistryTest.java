package cafe.woden.ircclient.app.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class SlashCommandParseStrategyRegistryTest {

  @Test
  void returnsFirstNonNullParseResult() {
    SlashCommandParseStrategyRegistry registry =
        new SlashCommandParseStrategyRegistry(
            List.of(
                line -> null,
                line -> SlashCommandParseResult.quote("RAW first"),
                line -> SlashCommandParseResult.quote("RAW second")));

    SlashCommandParseResult result = registry.tryParse("/plugin RAW");

    assertEquals("quote", result.kind());
    assertEquals(List.of("RAW first"), result.arguments());
  }

  @Test
  void returnsNullWhenNoStrategyParsesLine() {
    SlashCommandParseStrategyRegistry registry =
        new SlashCommandParseStrategyRegistry(List.of(line -> null));

    assertNull(registry.tryParse("/unknown"));
  }

  @Test
  void continuesWhenMappedResultIsRejected() {
    SlashCommandParseStrategyRegistry registry =
        new SlashCommandParseStrategyRegistry(
            List.of(
                line -> new SlashCommandParseResult("unsupported", List.of()),
                line -> SlashCommandParseResult.quote("RAW accepted")));

    SlashCommandParseResult result =
        registry.tryParse(
            "/plugin RAW", parsed -> "quote".equals(parsed.kind()) ? parsed : null);

    assertEquals("quote", result.kind());
    assertEquals(List.of("RAW accepted"), result.arguments());
  }

  @Test
  void acceptsNullStrategyLists() {
    SlashCommandParseStrategyRegistry registry = new SlashCommandParseStrategyRegistry(null);

    assertNull(registry.tryParse("/unknown"));
  }
}
