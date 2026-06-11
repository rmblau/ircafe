package cafe.woden.ircclient.app.commands;

import org.jmolecules.architecture.layered.ApplicationLayer;

/** ServiceLoader-capable strategy for parsing a subset of slash commands. */
@ApplicationLayer
public interface SlashCommandParseStrategy {

  ParsedInput tryParse(String line);
}
