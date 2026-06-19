package cafe.woden.ircclient.app.commands.spi;

import cafe.woden.ircclient.app.commands.ParsedInput;

/**
 * ServiceLoader-backed contribution point for parsing a subset of slash commands.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy}.
 */
public interface SlashCommandParseStrategy {

  ParsedInput tryParse(String line);
}
