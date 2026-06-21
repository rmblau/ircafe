package cafe.woden.ircclient.app.commands.builtins;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import com.google.auto.service.AutoService;

/** Built-in parser for advanced pure slash commands that do not need app runtime collaborators. */
@AutoService(SlashCommandParseStrategy.class)
public final class BuiltInAdvancedFeatureSlashCommandParseStrategy
    implements SlashCommandParseStrategy {

  @Override
  public SlashCommandParseResult tryParse(String line) {
    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/ignore")) {
      return SlashCommandParseResult.command(
          "ignore", BuiltInSlashCommandParsingSupport.argAfter(line, "/ignore"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/unignore")) {
      return SlashCommandParseResult.command(
          "unignore", BuiltInSlashCommandParsingSupport.argAfter(line, "/unignore"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/ignorelist")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/ignores")) {
      return SlashCommandParseResult.command("ignore-list");
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/softignore")) {
      return SlashCommandParseResult.command(
          "soft-ignore", BuiltInSlashCommandParsingSupport.argAfter(line, "/softignore"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/unsoftignore")) {
      return SlashCommandParseResult.command(
          "unsoft-ignore", BuiltInSlashCommandParsingSupport.argAfter(line, "/unsoftignore"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/softignorelist")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/softignores")) {
      return SlashCommandParseResult.command("soft-ignore-list");
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/version")) {
      return SlashCommandParseResult.command(
          "ctcp-version", BuiltInSlashCommandParsingSupport.argAfter(line, "/version"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/ping")) {
      return SlashCommandParseResult.command(
          "ctcp-ping", BuiltInSlashCommandParsingSupport.argAfter(line, "/ping"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/time")) {
      return SlashCommandParseResult.command(
          "ctcp-time", BuiltInSlashCommandParsingSupport.argAfter(line, "/time"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/ctcp")) {
      String rest = BuiltInSlashCommandParsingSupport.argAfter(line, "/ctcp");
      String nick;
      String cmd = "";
      String args = "";

      int sp1 = rest.indexOf(' ');
      if (sp1 < 0) {
        nick = rest.trim();
      } else {
        nick = rest.substring(0, sp1).trim();
        String rest2 = rest.substring(sp1 + 1).trim();
        int sp2 = rest2.indexOf(' ');
        if (sp2 < 0) {
          cmd = rest2.trim();
        } else {
          cmd = rest2.substring(0, sp2).trim();
          args = rest2.substring(sp2 + 1).trim();
        }
      }

      return SlashCommandParseResult.command("ctcp", nick, cmd, args);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/dcc")) {
      return BuiltInSlashCommandParsingSupport.parseDccInput(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/dcc"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/dccmsg")) {
      String rest = BuiltInSlashCommandParsingSupport.argAfter(line, "/dccmsg");
      int sp = rest.indexOf(' ');
      if (sp <= 0) return SlashCommandParseResult.command("dcc", "msg", rest.trim(), "");
      String nick = rest.substring(0, sp).trim();
      String text = rest.substring(sp + 1).trim();
      return SlashCommandParseResult.command("dcc", "msg", nick, text);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/chathistory")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/history")) {
      String rest =
          BuiltInSlashCommandParsingSupport.matchesCommand(line, "/chathistory")
              ? BuiltInSlashCommandParsingSupport.argAfter(line, "/chathistory")
              : BuiltInSlashCommandParsingSupport.argAfter(line, "/history");
      return BuiltInSlashCommandParsingSupport.parseChatHistoryInput(rest);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/markread")) {
      return SlashCommandParseResult.command("mark-read");
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/help")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/commands")) {
      String topic =
          BuiltInSlashCommandParsingSupport.matchesCommand(line, "/help")
              ? BuiltInSlashCommandParsingSupport.argAfter(line, "/help")
              : BuiltInSlashCommandParsingSupport.argAfter(line, "/commands");
      return SlashCommandParseResult.command("help", topic);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/upload")) {
      return BuiltInSlashCommandParsingSupport.parseUploadInput(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/upload"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/reply")) {
      return BuiltInSlashCommandParsingSupport.parseReplyInput(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/reply"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/react")) {
      return BuiltInSlashCommandParsingSupport.parseReactInput(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/react"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/unreact")) {
      return BuiltInSlashCommandParsingSupport.parseUnreactInput(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/unreact"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/edit")) {
      return BuiltInSlashCommandParsingSupport.parseEditInput(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/edit"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/redact")) {
      return BuiltInSlashCommandParsingSupport.parseRedactInput(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/redact"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/delete")) {
      return BuiltInSlashCommandParsingSupport.parseRedactInput(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/delete"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/quote")) {
      return SlashCommandParseResult.quote(
          BuiltInSlashCommandParsingSupport.argAfter(line, "/quote"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/raw")) {
      return SlashCommandParseResult.quote(BuiltInSlashCommandParsingSupport.argAfter(line, "/raw"));
    }

    return null;
  }
}
