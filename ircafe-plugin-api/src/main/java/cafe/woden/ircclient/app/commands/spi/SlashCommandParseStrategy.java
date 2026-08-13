package cafe.woden.ircclient.app.commands.spi;

/**
 * ServiceLoader-backed contribution point for parsing a subset of slash commands.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy}.
 */
public interface SlashCommandParseStrategy {

  /**
   * Attempts to parse one complete input line.
   *
   * @param line raw user input
   * @return {@code null} when this strategy does not own the command; otherwise a portable parse
   *     result. A strategy that recognizes its command but rejects the arguments should return
   *     {@link SlashCommandParseResult#unknown(String)} so later strategies do not reinterpret it.
   */
  SlashCommandParseResult tryParse(String line);
}
