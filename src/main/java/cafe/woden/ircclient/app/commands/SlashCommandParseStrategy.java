package cafe.woden.ircclient.app.commands;

import org.jmolecules.architecture.layered.ApplicationLayer;

/**
 * ServiceLoader-backed strategy for parsing a subset of slash commands.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.app.commands.SlashCommandParseStrategy}.
 */
@ApplicationLayer
public interface SlashCommandParseStrategy {

  ParsedInput tryParse(String line);
}
