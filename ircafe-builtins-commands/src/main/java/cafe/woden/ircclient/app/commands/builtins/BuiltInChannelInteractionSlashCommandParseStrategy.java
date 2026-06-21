package cafe.woden.ircclient.app.commands.builtins;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy;
import com.google.auto.service.AutoService;

/** Built-in parser for channel interaction slash commands. */
@AutoService(SlashCommandParseStrategy.class)
public final class BuiltInChannelInteractionSlashCommandParseStrategy
    implements SlashCommandParseStrategy {

  @Override
  public SlashCommandParseResult tryParse(String line) {
    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/topic")) {
      String rest = BuiltInSlashCommandParsingSupport.argAfter(line, "/topic");
      if (rest.isEmpty()) return SlashCommandParseResult.command("topic", "", "");
      int sp = rest.indexOf(' ');
      if (sp < 0) return SlashCommandParseResult.command("topic", rest.trim(), "");
      String first = rest.substring(0, sp).trim();
      String tail = rest.substring(sp + 1).trim();
      return SlashCommandParseResult.command("topic", first, tail);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/kick")) {
      BuiltInSlashCommandParsingSupport.ParsedKick parsed =
          BuiltInSlashCommandParsingSupport.parseKickArgs(
              BuiltInSlashCommandParsingSupport.argAfter(line, "/kick"));
      return SlashCommandParseResult.command(
          "kick", parsed.channel(), parsed.nick(), parsed.reason());
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invite")) {
      String rest = BuiltInSlashCommandParsingSupport.argAfter(line, "/invite");
      String r = rest == null ? "" : rest.trim();
      if (r.isEmpty()) return SlashCommandParseResult.command("invite", "", "");
      String[] toks = r.split("\\s+", 3);
      String nick = toks.length > 0 ? toks[0].trim() : "";
      String channel = toks.length > 1 ? toks[1].trim() : "";
      return SlashCommandParseResult.command("invite", nick, channel);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invites")) {
      return SlashCommandParseResult.command(
          "invite-list", BuiltInSlashCommandParsingSupport.argAfter(line, "/invites"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invjoin")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invitejoin")) {
      String token =
          BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invjoin")
              ? BuiltInSlashCommandParsingSupport.argAfter(line, "/invjoin")
              : BuiltInSlashCommandParsingSupport.argAfter(line, "/invitejoin");
      return SlashCommandParseResult.command("invite-join", token);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invignore")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/inviteignore")) {
      String token =
          BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invignore")
              ? BuiltInSlashCommandParsingSupport.argAfter(line, "/invignore")
              : BuiltInSlashCommandParsingSupport.argAfter(line, "/inviteignore");
      return SlashCommandParseResult.command("invite-ignore", token);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invwhois")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invitewhois")) {
      String token =
          BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invwhois")
              ? BuiltInSlashCommandParsingSupport.argAfter(line, "/invwhois")
              : BuiltInSlashCommandParsingSupport.argAfter(line, "/invitewhois");
      return SlashCommandParseResult.command("invite-whois", token);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invblock")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/inviteblock")) {
      String token =
          BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invblock")
              ? BuiltInSlashCommandParsingSupport.argAfter(line, "/invblock")
              : BuiltInSlashCommandParsingSupport.argAfter(line, "/inviteblock");
      return SlashCommandParseResult.command("invite-block", token);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/inviteautojoin")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invautojoin")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/ajinvite")) {
      String mode;
      if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/inviteautojoin")) {
        mode = BuiltInSlashCommandParsingSupport.argAfter(line, "/inviteautojoin");
      } else if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/invautojoin")) {
        mode = BuiltInSlashCommandParsingSupport.argAfter(line, "/invautojoin");
      } else {
        mode = BuiltInSlashCommandParsingSupport.argAfter(line, "/ajinvite");
        if (mode.isEmpty()) mode = "toggle";
      }
      return SlashCommandParseResult.command("invite-autojoin", mode);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/names")) {
      return SlashCommandParseResult.command(
          "names", BuiltInSlashCommandParsingSupport.argAfter(line, "/names"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/who")) {
      return SlashCommandParseResult.command(
          "who", BuiltInSlashCommandParsingSupport.argAfter(line, "/who"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/list")) {
      return SlashCommandParseResult.command(
          "list", BuiltInSlashCommandParsingSupport.argAfter(line, "/list"));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/monitor")
        || BuiltInSlashCommandParsingSupport.matchesCommand(line, "/mon")) {
      String args =
          BuiltInSlashCommandParsingSupport.matchesCommand(line, "/monitor")
              ? BuiltInSlashCommandParsingSupport.argAfter(line, "/monitor")
              : BuiltInSlashCommandParsingSupport.argAfter(line, "/mon");
      return SlashCommandParseResult.command("monitor", args);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/mode")) {
      String rest = BuiltInSlashCommandParsingSupport.argAfter(line, "/mode");
      if (rest.isEmpty()) return SlashCommandParseResult.command("mode", "", "");
      int sp = rest.indexOf(' ');
      if (sp < 0) return SlashCommandParseResult.command("mode", rest.trim(), "");
      String first = rest.substring(0, sp).trim();
      String tail = rest.substring(sp + 1).trim();
      return SlashCommandParseResult.command("mode", first, tail);
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/op")) {
      BuiltInSlashCommandParsingSupport.ParsedTargetList parsed =
          BuiltInSlashCommandParsingSupport.parseTargetList(
              BuiltInSlashCommandParsingSupport.argAfter(line, "/op"));
      return SlashCommandParseResult.command("op", prepend(parsed.channel(), parsed.items()));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/deop")) {
      BuiltInSlashCommandParsingSupport.ParsedTargetList parsed =
          BuiltInSlashCommandParsingSupport.parseTargetList(
              BuiltInSlashCommandParsingSupport.argAfter(line, "/deop"));
      return SlashCommandParseResult.command("deop", prepend(parsed.channel(), parsed.items()));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/voice")) {
      BuiltInSlashCommandParsingSupport.ParsedTargetList parsed =
          BuiltInSlashCommandParsingSupport.parseTargetList(
              BuiltInSlashCommandParsingSupport.argAfter(line, "/voice"));
      return SlashCommandParseResult.command("voice", prepend(parsed.channel(), parsed.items()));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/devoice")) {
      BuiltInSlashCommandParsingSupport.ParsedTargetList parsed =
          BuiltInSlashCommandParsingSupport.parseTargetList(
              BuiltInSlashCommandParsingSupport.argAfter(line, "/devoice"));
      return SlashCommandParseResult.command("devoice", prepend(parsed.channel(), parsed.items()));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/ban")) {
      BuiltInSlashCommandParsingSupport.ParsedTargetList parsed =
          BuiltInSlashCommandParsingSupport.parseTargetList(
              BuiltInSlashCommandParsingSupport.argAfter(line, "/ban"));
      return SlashCommandParseResult.command("ban", prepend(parsed.channel(), parsed.items()));
    }

    if (BuiltInSlashCommandParsingSupport.matchesCommand(line, "/unban")) {
      BuiltInSlashCommandParsingSupport.ParsedTargetList parsed =
          BuiltInSlashCommandParsingSupport.parseTargetList(
              BuiltInSlashCommandParsingSupport.argAfter(line, "/unban"));
      return SlashCommandParseResult.command("unban", prepend(parsed.channel(), parsed.items()));
    }

    return null;
  }

  private static java.util.List<String> prepend(String first, java.util.List<String> rest) {
    java.util.ArrayList<String> values = new java.util.ArrayList<>();
    values.add(first == null ? "" : first);
    values.addAll(java.util.Objects.requireNonNullElse(rest, java.util.List.of()));
    return java.util.List.copyOf(values);
  }
}
