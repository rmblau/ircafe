package cafe.woden.ircclient.app.commands.spi;

/**
 * ServiceLoader-backed contribution point for parsing a subset of slash commands.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy}.
 */
public interface SlashCommandParseStrategy {

  SlashCommandParseResult tryParse(String line);
}
