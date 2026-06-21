package cafe.woden.ircclient.app.commands;

/** App-owned parse strategy for built-in slash commands that still return app command models. */
interface BuiltInSlashCommandParseStrategy {

  ParsedInput tryParse(String line);
}
