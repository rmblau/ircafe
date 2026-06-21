package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Adapts plugin-facing slash command parse results into app-owned command routing values. */
final class SlashCommandParseResultAdapters {
  private SlashCommandParseResultAdapters() {}

  static ParsedInput toParsedInput(SlashCommandParseResult result) {
    if (result == null) {
      return null;
    }
    List<String> args = List.copyOf(Objects.requireNonNullElse(result.arguments(), List.of()));
    return switch (result.kind()) {
      case "join" -> new ParsedInput.Join(arg(args, 0), arg(args, 1));
      case "part" -> new ParsedInput.Part(arg(args, 0), arg(args, 1));
      case "connect" -> new ParsedInput.Connect(arg(args, 0));
      case "disconnect" -> new ParsedInput.Disconnect(arg(args, 0));
      case "reconnect" -> new ParsedInput.Reconnect(arg(args, 0));
      case "backend-named", "backendnamed" ->
          new ParsedInput.BackendNamed(arg(args, 0), arg(args, 1));
      case "quit" -> new ParsedInput.Quit(arg(args, 0));
      case "nick" -> new ParsedInput.Nick(arg(args, 0));
      case "away" -> new ParsedInput.Away(arg(args, 0));
      case "query" -> new ParsedInput.Query(arg(args, 0));
      case "whois" -> new ParsedInput.Whois(arg(args, 0));
      case "whowas" -> new ParsedInput.Whowas(arg(args, 0), intArg(args, 1));
      case "msg" -> new ParsedInput.Msg(arg(args, 0), arg(args, 1));
      case "notice" -> new ParsedInput.Notice(arg(args, 0), arg(args, 1));
      case "me" -> new ParsedInput.Me(arg(args, 0));
      case "topic" -> new ParsedInput.Topic(arg(args, 0), arg(args, 1));
      case "kick" -> new ParsedInput.Kick(arg(args, 0), arg(args, 1), arg(args, 2));
      case "invite" -> new ParsedInput.Invite(arg(args, 0), arg(args, 1));
      case "invite-list", "invites" -> new ParsedInput.InviteList(arg(args, 0));
      case "invite-join", "invjoin" -> new ParsedInput.InviteJoin(arg(args, 0));
      case "invite-ignore", "invignore" -> new ParsedInput.InviteIgnore(arg(args, 0));
      case "invite-whois", "invwhois" -> new ParsedInput.InviteWhois(arg(args, 0));
      case "invite-block", "invblock" -> new ParsedInput.InviteBlock(arg(args, 0));
      case "invite-autojoin", "invite-auto-join" -> new ParsedInput.InviteAutoJoin(arg(args, 0));
      case "names" -> new ParsedInput.Names(arg(args, 0));
      case "who" -> new ParsedInput.Who(arg(args, 0));
      case "list", "list-cmd" -> new ParsedInput.ListCmd(arg(args, 0));
      case "mode" -> new ParsedInput.Mode(arg(args, 0), arg(args, 1));
      case "op" -> new ParsedInput.Op(arg(args, 0), tail(args, 1));
      case "deop" -> new ParsedInput.Deop(arg(args, 0), tail(args, 1));
      case "voice" -> new ParsedInput.Voice(arg(args, 0), tail(args, 1));
      case "devoice" -> new ParsedInput.Devoice(arg(args, 0), tail(args, 1));
      case "ban" -> new ParsedInput.Ban(arg(args, 0), tail(args, 1));
      case "unban" -> new ParsedInput.Unban(arg(args, 0), tail(args, 1));
      case "ctcp-version" -> new ParsedInput.CtcpVersion(arg(args, 0));
      case "ctcp-ping" -> new ParsedInput.CtcpPing(arg(args, 0));
      case "ctcp-time" -> new ParsedInput.CtcpTime(arg(args, 0));
      case "ctcp" -> new ParsedInput.Ctcp(arg(args, 0), arg(args, 1), arg(args, 2));
      case "dcc" -> new ParsedInput.Dcc(arg(args, 0), arg(args, 1), arg(args, 2));
      case "ignore" -> new ParsedInput.Ignore(arg(args, 0));
      case "unignore" -> new ParsedInput.Unignore(arg(args, 0));
      case "ignore-list", "ignorelist" -> new ParsedInput.IgnoreList();
      case "soft-ignore", "softignore" -> new ParsedInput.SoftIgnore(arg(args, 0));
      case "unsoft-ignore", "unsoftignore" -> new ParsedInput.UnsoftIgnore(arg(args, 0));
      case "soft-ignore-list", "softignorelist" -> new ParsedInput.SoftIgnoreList();
      case "chat-history-before", "chathistory-before" ->
          new ParsedInput.ChatHistoryBefore(intArg(args, 0), arg(args, 1));
      case "chat-history-latest", "chathistory-latest" ->
          new ParsedInput.ChatHistoryLatest(intArg(args, 0), defaultArg(args, 1, "*"));
      case "chat-history-between", "chathistory-between" ->
          new ParsedInput.ChatHistoryBetween(arg(args, 0), arg(args, 1), intArg(args, 2));
      case "chat-history-around", "chathistory-around" ->
          new ParsedInput.ChatHistoryAround(arg(args, 0), intArg(args, 1));
      case "mark-read", "markread" -> new ParsedInput.MarkRead();
      case "monitor" -> new ParsedInput.Monitor(arg(args, 0));
      case "help" -> new ParsedInput.Help(arg(args, 0));
      case "upload" -> new ParsedInput.Upload(arg(args, 0), arg(args, 1), arg(args, 2));
      case "reply-message", "reply" -> new ParsedInput.ReplyMessage(arg(args, 0), arg(args, 1));
      case "react-message", "react" -> new ParsedInput.ReactMessage(arg(args, 0), arg(args, 1));
      case "unreact-message", "unreact" ->
          new ParsedInput.UnreactMessage(arg(args, 0), arg(args, 1));
      case "edit-message", "edit" -> new ParsedInput.EditMessage(arg(args, 0), arg(args, 1));
      case "redact-message", "redact" -> new ParsedInput.RedactMessage(arg(args, 0), arg(args, 1));
      case "quote" -> new ParsedInput.Quote(arg(args, 0));
      case "say" -> new ParsedInput.Say(arg(args, 0));
      case "unknown" -> new ParsedInput.Unknown(arg(args, 0));
      default -> null;
    };
  }

  private static String arg(List<String> args, int index) {
    return defaultArg(args, index, "");
  }

  private static String defaultArg(List<String> args, int index, String fallback) {
    if (index < 0 || index >= args.size()) {
      return fallback;
    }
    return Objects.toString(args.get(index), fallback == null ? "" : fallback);
  }

  private static int intArg(List<String> args, int index) {
    String value = arg(args, index).trim();
    if (value.isEmpty()) {
      return 0;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private static List<String> tail(List<String> args, int firstIndex) {
    if (firstIndex < 0 || firstIndex >= args.size()) {
      return List.of();
    }
    ArrayList<String> values = new ArrayList<>();
    for (int i = firstIndex; i < args.size(); i++) {
      String value = Objects.toString(args.get(i), "").trim();
      if (!value.isEmpty()) {
        values.add(value);
      }
    }
    return List.copyOf(values);
  }
}
