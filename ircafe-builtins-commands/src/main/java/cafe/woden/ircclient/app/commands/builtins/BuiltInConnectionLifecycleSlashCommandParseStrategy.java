package cafe.woden.ircclient.app.commands.builtins;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import com.google.auto.service.AutoService;

/** Built-in parser for connection lifecycle slash commands. */
@AutoService(SlashCommandParseStrategy.class)
public final class BuiltInConnectionLifecycleSlashCommandParseStrategy
    implements SlashCommandParseStrategy {

  @Override
  public SlashCommandParseResult tryParse(String line) {
    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/join")) {
      return BuiltInSlashCommandParsingSupport.parseJoinInput(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/join"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/j")) {
      return BuiltInSlashCommandParsingSupport.parseJoinInput(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/j"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/part")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/leave")) {
      String rest =
          BuiltInSlashCommandParsingSupport.matchesCommand(line, "/part")
              ? BuiltInSlashCommandParsingSupport.argAfter(line, "/part")
              : BuiltInSlashCommandParsingSupport.argAfter(line, "/leave");
      String r = rest == null ? "" : rest.trim();
      if (r.isEmpty()) return SlashCommandParseResult.part("", "");
      String first;
      String tail;
      int sp = r.indexOf(' ');
      if (sp < 0) {
        first = r;
        tail = "";
      } else {
        first = r.substring(0, sp).trim();
        tail = r.substring(sp + 1).trim();
      }
      if (BuiltInSlashCommandParsingSupport.looksLikePartTarget(first)) {
        return SlashCommandParseResult.part(first, tail);
      }
      return SlashCommandParseResult.part("", r);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/connect")) {
      return SlashCommandParseResult.command(
          "connect", BuiltInSlashCommandParsingSupport.argAfter(line, "/connect"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/disconnect")) {
      return SlashCommandParseResult.command(
          "disconnect", BuiltInSlashCommandParsingSupport.argAfter(line, "/disconnect"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/reconnect")) {
      return SlashCommandParseResult.command(
          "reconnect", BuiltInSlashCommandParsingSupport.argAfter(line, "/reconnect"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/quit")) {
      return SlashCommandParseResult.command(
          "quit", BuiltInSlashCommandParsingSupport.argAfter(line, "/quit"));
    }

    return null;
  }
}
