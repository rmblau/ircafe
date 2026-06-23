package cafe.woden.ircclient.app.commands.builtins;

import cafe.woden.ircclient.app.commands.spi.SlashCommandDescriptor;
import cafe.woden.ircclient.app.commands.spi.SlashCommandHelpSink;
import cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Shared presentation metadata for built-in slash commands handled by the typed parser path. */
@AutoService(SlashCommandPresentationContributor.class)
public final class BuiltInSlashCommandPresentationContributor
    implements SlashCommandPresentationContributor {

  private static final List<SlashCommandDescriptor> COMMANDS =
      List.of(
          new SlashCommandDescriptor("/join", "Join channel"),
          new SlashCommandDescriptor("/j", "Alias: /join"),
          new SlashCommandDescriptor("/part", "Leave channel"),
          new SlashCommandDescriptor("/leave", "Alias: /part"),
          new SlashCommandDescriptor("/connect", "Connect server/all"),
          new SlashCommandDescriptor("/disconnect", "Disconnect server/all"),
          new SlashCommandDescriptor("/reconnect", "Reconnect server/all"),
          new SlashCommandDescriptor("/quit", "Disconnect all and quit"),
          new SlashCommandDescriptor("/nick", "Change nickname"),
          new SlashCommandDescriptor("/away", "Set/remove away status"),
          new SlashCommandDescriptor("/query", "Open private message"),
          new SlashCommandDescriptor("/whois", "WHOIS lookup"),
          new SlashCommandDescriptor("/wi", "Alias: /whois"),
          new SlashCommandDescriptor("/whowas", "WHOWAS lookup"),
          new SlashCommandDescriptor("/msg", "Send private message"),
          new SlashCommandDescriptor("/notice", "Send notice"),
          new SlashCommandDescriptor("/me", "Send action"),
          new SlashCommandDescriptor("/topic", "View/change topic"),
          new SlashCommandDescriptor("/kick", "Kick user"),
          new SlashCommandDescriptor("/invite", "Invite user"),
          new SlashCommandDescriptor("/invites", "List pending invites"),
          new SlashCommandDescriptor("/invjoin", "Join pending invite"),
          new SlashCommandDescriptor("/invitejoin", "Alias: /invjoin"),
          new SlashCommandDescriptor("/invignore", "Ignore pending invite"),
          new SlashCommandDescriptor("/inviteignore", "Alias: /invignore"),
          new SlashCommandDescriptor("/invwhois", "WHOIS inviter from invite"),
          new SlashCommandDescriptor("/invitewhois", "Alias: /invwhois"),
          new SlashCommandDescriptor("/invblock", "Block inviter nick"),
          new SlashCommandDescriptor("/inviteblock", "Alias: /invblock"),
          new SlashCommandDescriptor("/inviteautojoin", "Toggle invite auto-join"),
          new SlashCommandDescriptor("/invautojoin", "Alias: /inviteautojoin"),
          new SlashCommandDescriptor("/ajinvite", "Alias: /inviteautojoin (toggle)"),
          new SlashCommandDescriptor("/names", "Request NAMES"),
          new SlashCommandDescriptor("/who", "Request WHO"),
          new SlashCommandDescriptor("/list", "Request LIST"),
          new SlashCommandDescriptor("/monitor", "Manage watched nicks"),
          new SlashCommandDescriptor("/mon", "Alias: /monitor"),
          new SlashCommandDescriptor("/mode", "Set/query mode"),
          new SlashCommandDescriptor("/op", "Grant op"),
          new SlashCommandDescriptor("/deop", "Remove op"),
          new SlashCommandDescriptor("/voice", "Grant voice"),
          new SlashCommandDescriptor("/devoice", "Remove voice"),
          new SlashCommandDescriptor("/ban", "Set ban"),
          new SlashCommandDescriptor("/unban", "Remove ban"),
          new SlashCommandDescriptor("/ignore", "Add hard ignore"),
          new SlashCommandDescriptor("/unignore", "Remove hard ignore"),
          new SlashCommandDescriptor("/ignorelist", "Show hard ignores"),
          new SlashCommandDescriptor("/ignores", "Alias: /ignorelist"),
          new SlashCommandDescriptor("/softignore", "Add soft ignore"),
          new SlashCommandDescriptor("/unsoftignore", "Remove soft ignore"),
          new SlashCommandDescriptor("/softignorelist", "Show soft ignores"),
          new SlashCommandDescriptor("/softignores", "Alias: /softignorelist"),
          new SlashCommandDescriptor("/version", "CTCP VERSION"),
          new SlashCommandDescriptor("/ping", "CTCP PING"),
          new SlashCommandDescriptor("/time", "CTCP TIME"),
          new SlashCommandDescriptor("/ctcp", "Send CTCP"),
          new SlashCommandDescriptor("/dcc", "DCC command"),
          new SlashCommandDescriptor("/dccmsg", "DCC message"),
          new SlashCommandDescriptor("/chathistory", "IRCv3 CHATHISTORY"),
          new SlashCommandDescriptor("/history", "Alias: /chathistory"),
          new SlashCommandDescriptor("/markread", "Set read marker for current target"),
          new SlashCommandDescriptor("/help", "Show command help"),
          new SlashCommandDescriptor("/commands", "Alias: /help"),
          new SlashCommandDescriptor("/upload", "Upload or send media"),
          new SlashCommandDescriptor("/reply", "Reply to message-id"),
          new SlashCommandDescriptor("/react", "React to message-id"),
          new SlashCommandDescriptor("/unreact", "Remove reaction from message-id"),
          new SlashCommandDescriptor("/edit", "Edit message-id (experimental draft)"),
          new SlashCommandDescriptor("/redact", "Redact message-id"),
          new SlashCommandDescriptor("/delete", "Alias: /redact"),
          new SlashCommandDescriptor("/quote", "Send raw IRC line"),
          new SlashCommandDescriptor("/raw", "Alias: /quote"));

  @Override
  public List<SlashCommandDescriptor> autocompleteCommands() {
    return COMMANDS;
  }

  @Override
  public void appendGeneralHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine(
        "Common: /join /part /msg /notice /me /query /whois /names /list /topic /monitor /chathistory /quote /dcc");
    help.appendLine(
        "Invites: /invites /invjoin (/join -i) /invignore /invwhois /invblock /inviteautojoin (/ajinvite)");
    help.appendLine("Tip: /help dcc for direct-chat/file-transfer commands.");
    help.appendLine("/reply <msgid> <message> (requires message-tags)");
    help.appendLine("/react <msgid> <reaction-token> (requires message-tags)");
    help.appendLine("/unreact <msgid> <reaction-token> (requires message-tags)");
    help.appendLine(
        "Tip: /help edit, /help redact, /help markread, or /help upload for focused details.");
  }

  @Override
  public Map<String, Consumer<SlashCommandHelpSink>> topicHelpHandlers() {
    return Map.ofEntries(
        Map.entry("nick", BuiltInSlashCommandPresentationContributor::appendNickHelp),
        Map.entry("away", BuiltInSlashCommandPresentationContributor::appendAwayHelp),
        Map.entry("query", BuiltInSlashCommandPresentationContributor::appendQueryHelp),
        Map.entry("msg", BuiltInSlashCommandPresentationContributor::appendMsgHelp),
        Map.entry("notice", BuiltInSlashCommandPresentationContributor::appendNoticeHelp),
        Map.entry("me", BuiltInSlashCommandPresentationContributor::appendMeHelp),
        Map.entry("whois", BuiltInSlashCommandPresentationContributor::appendWhoisHelp),
        Map.entry("wi", BuiltInSlashCommandPresentationContributor::appendWhoisHelp),
        Map.entry("whowas", BuiltInSlashCommandPresentationContributor::appendWhowasHelp),
        Map.entry("ctcp", BuiltInSlashCommandPresentationContributor::appendCtcpHelp),
        Map.entry("version", BuiltInSlashCommandPresentationContributor::appendCtcpShortcutHelp),
        Map.entry("ping", BuiltInSlashCommandPresentationContributor::appendCtcpShortcutHelp),
        Map.entry("time", BuiltInSlashCommandPresentationContributor::appendCtcpShortcutHelp),
        Map.entry("dcc", BuiltInSlashCommandPresentationContributor::appendDccHelp),
        Map.entry("monitor", BuiltInSlashCommandPresentationContributor::appendMonitorHelp),
        Map.entry("mon", BuiltInSlashCommandPresentationContributor::appendMonitorHelp),
        Map.entry(
            "chathistory",
            BuiltInSlashCommandPresentationContributor::appendChatHistoryHelpDetails),
        Map.entry(
            "history", BuiltInSlashCommandPresentationContributor::appendChatHistoryHelpDetails),
        Map.entry("quote", BuiltInSlashCommandPresentationContributor::appendRawQuoteHelp),
        Map.entry("raw", BuiltInSlashCommandPresentationContributor::appendRawQuoteHelp));
  }

  private static void appendNickHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /nick <newNick>");
    help.appendLine("Changes your nickname on the active server.");
  }

  private static void appendAwayHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /away [message]");
    help.appendLine("Sets your away message, or clears away status when no message is provided.");
  }

  private static void appendQueryHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /query <nick>");
    help.appendLine("Opens or focuses a private message tab for the nick.");
  }

  private static void appendMsgHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /msg <nick> <message>");
    help.appendLine("Sends a private message without changing the active target.");
  }

  private static void appendNoticeHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /notice <target> <message>");
    help.appendLine("Sends an IRC NOTICE to a nick or channel.");
  }

  private static void appendMeHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /me <action>");
    help.appendLine("Sends a CTCP ACTION to the active channel or private message.");
  }

  private static void appendWhoisHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /whois <nick>");
    help.appendLine("Alias: /wi <nick>");
  }

  private static void appendWhowasHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /whowas <nick> [count]");
    help.appendLine("Requests WHOWAS information for a nick that is no longer online.");
  }

  private static void appendCtcpHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /ctcp <nick> <command> [args...]");
    help.appendLine("Shortcuts: /version <nick>, /ping <nick>, /time <nick>.");
  }

  private static void appendCtcpShortcutHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /version <nick>  |  /ping <nick>  |  /time <nick>");
    help.appendLine("Sends common CTCP requests without typing /ctcp explicitly.");
  }

  private static void appendDccHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("/dcc chat <nick>");
    help.appendLine("/dcc send <nick> <file-path>");
    help.appendLine("/dcc accept <nick>");
    help.appendLine("/dcc get <nick> [save-path]");
    help.appendLine("/dcc msg <nick> <text>  (alias: /dccmsg <nick> <text>)");
    help.appendLine("/dcc close <nick>  /dcc list  /dcc panel");
    help.appendLine("UI: right-click a nick and use the DCC submenu.");
  }

  private static void appendMonitorHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /monitor <+|-|list|status|clear> [nicks]");
    help.appendLine("Aliases: /mon, /monitor +nick1 nick2, /monitor -nick1,nick2");
    help.appendLine("Examples: /monitor +alice,bob  |  /monitor list  |  /monitor clear");
  }

  private static void appendChatHistoryHelpDetails(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("/chathistory before <msgid=...|timestamp=...> [limit]");
    help.appendLine("/chathistory latest [*|msgid=...|timestamp=...] [limit]");
    help.appendLine("/chathistory around <msgid=...|timestamp=...> [limit]");
    help.appendLine("/chathistory between <start> <end> [limit]");
  }

  private static void appendRawQuoteHelp(SlashCommandHelpSink help) {
    if (help == null) {
      return;
    }
    help.appendLine("Usage: /quote <RAW IRC LINE>");
    help.appendLine("Alias: /raw <RAW IRC LINE>");
    help.appendLine("Sends one raw IRC protocol line to the active server.");
  }
}
