package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Maps plugin-facing slash-command parse results through a root-supplied command factory. */
public final class SlashCommandParseResultMapper {
  private SlashCommandParseResultMapper() {}

  public static <T> T map(SlashCommandParseResult result, CommandFactory<T> factory) {
    if (result == null) {
      return null;
    }
    Objects.requireNonNull(factory, "factory");
    List<String> args = List.copyOf(Objects.requireNonNullElse(result.arguments(), List.of()));
    return switch (result.kind()) {
      case "join" -> factory.join(arg(args, 0), arg(args, 1));
      case "part" -> factory.part(arg(args, 0), arg(args, 1));
      case "connect" -> factory.connect(arg(args, 0));
      case "disconnect" -> factory.disconnect(arg(args, 0));
      case "reconnect" -> factory.reconnect(arg(args, 0));
      case "backend-named", "backendnamed" -> factory.backendNamed(arg(args, 0), arg(args, 1));
      case "quit" -> factory.quit(arg(args, 0));
      case "nick" -> factory.nick(arg(args, 0));
      case "away" -> factory.away(arg(args, 0));
      case "query" -> factory.query(arg(args, 0));
      case "whois" -> factory.whois(arg(args, 0));
      case "whowas" -> factory.whowas(arg(args, 0), intArg(args, 1));
      case "msg" -> factory.msg(arg(args, 0), arg(args, 1));
      case "notice" -> factory.notice(arg(args, 0), arg(args, 1));
      case "me" -> factory.me(arg(args, 0));
      case "topic" -> factory.topic(arg(args, 0), arg(args, 1));
      case "kick" -> factory.kick(arg(args, 0), arg(args, 1), arg(args, 2));
      case "invite" -> factory.invite(arg(args, 0), arg(args, 1));
      case "invite-list", "invites" -> factory.inviteList(arg(args, 0));
      case "invite-join", "invjoin" -> factory.inviteJoin(arg(args, 0));
      case "invite-ignore", "invignore" -> factory.inviteIgnore(arg(args, 0));
      case "invite-whois", "invwhois" -> factory.inviteWhois(arg(args, 0));
      case "invite-block", "invblock" -> factory.inviteBlock(arg(args, 0));
      case "invite-autojoin", "invite-auto-join" -> factory.inviteAutoJoin(arg(args, 0));
      case "names" -> factory.names(arg(args, 0));
      case "who" -> factory.who(arg(args, 0));
      case "list", "list-cmd" -> factory.listCmd(arg(args, 0));
      case "mode" -> factory.mode(arg(args, 0), arg(args, 1));
      case "op" -> factory.op(arg(args, 0), tail(args, 1));
      case "deop" -> factory.deop(arg(args, 0), tail(args, 1));
      case "voice" -> factory.voice(arg(args, 0), tail(args, 1));
      case "devoice" -> factory.devoice(arg(args, 0), tail(args, 1));
      case "ban" -> factory.ban(arg(args, 0), tail(args, 1));
      case "unban" -> factory.unban(arg(args, 0), tail(args, 1));
      case "ctcp-version" -> factory.ctcpVersion(arg(args, 0));
      case "ctcp-ping" -> factory.ctcpPing(arg(args, 0));
      case "ctcp-time" -> factory.ctcpTime(arg(args, 0));
      case "ctcp" -> factory.ctcp(arg(args, 0), arg(args, 1), arg(args, 2));
      case "dcc" -> factory.dcc(arg(args, 0), arg(args, 1), arg(args, 2));
      case "ignore" -> factory.ignore(arg(args, 0));
      case "unignore" -> factory.unignore(arg(args, 0));
      case "ignore-list", "ignorelist" -> factory.ignoreList();
      case "soft-ignore", "softignore" -> factory.softIgnore(arg(args, 0));
      case "unsoft-ignore", "unsoftignore" -> factory.unsoftIgnore(arg(args, 0));
      case "soft-ignore-list", "softignorelist" -> factory.softIgnoreList();
      case "chat-history-before", "chathistory-before" ->
          factory.chatHistoryBefore(intArg(args, 0), arg(args, 1));
      case "chat-history-latest", "chathistory-latest" ->
          factory.chatHistoryLatest(intArg(args, 0), defaultArg(args, 1, "*"));
      case "chat-history-between", "chathistory-between" ->
          factory.chatHistoryBetween(arg(args, 0), arg(args, 1), intArg(args, 2));
      case "chat-history-around", "chathistory-around" ->
          factory.chatHistoryAround(arg(args, 0), intArg(args, 1));
      case "mark-read", "markread" -> factory.markRead();
      case "monitor" -> factory.monitor(arg(args, 0));
      case "help" -> factory.help(arg(args, 0));
      case "upload" -> factory.upload(arg(args, 0), arg(args, 1), arg(args, 2));
      case "reply-message", "reply" -> factory.replyMessage(arg(args, 0), arg(args, 1));
      case "react-message", "react" -> factory.reactMessage(arg(args, 0), arg(args, 1));
      case "unreact-message", "unreact" -> factory.unreactMessage(arg(args, 0), arg(args, 1));
      case "edit-message", "edit" -> factory.editMessage(arg(args, 0), arg(args, 1));
      case "redact-message", "redact" -> factory.redactMessage(arg(args, 0), arg(args, 1));
      case "quote" -> factory.quote(arg(args, 0));
      case "say" -> factory.say(arg(args, 0));
      case "unknown" -> factory.unknown(arg(args, 0));
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

  public interface CommandFactory<T> {
    T join(String channel, String key);

    T part(String channel, String reason);

    T connect(String target);

    T disconnect(String target);

    T reconnect(String target);

    T backendNamed(String command, String args);

    T quit(String reason);

    T nick(String newNick);

    T away(String message);

    T query(String nick);

    T whois(String nick);

    T whowas(String nick, int count);

    T msg(String nick, String body);

    T notice(String target, String body);

    T me(String action);

    T topic(String first, String rest);

    T kick(String channel, String nick, String reason);

    T invite(String nick, String channel);

    T inviteList(String serverId);

    T inviteJoin(String inviteToken);

    T inviteIgnore(String inviteToken);

    T inviteWhois(String inviteToken);

    T inviteBlock(String inviteToken);

    T inviteAutoJoin(String mode);

    T names(String channel);

    T who(String args);

    T listCmd(String args);

    T mode(String first, String rest);

    T op(String channel, List<String> nicks);

    T deop(String channel, List<String> nicks);

    T voice(String channel, List<String> nicks);

    T devoice(String channel, List<String> nicks);

    T ban(String channel, List<String> masksOrNicks);

    T unban(String channel, List<String> masksOrNicks);

    T ctcpVersion(String nick);

    T ctcpPing(String nick);

    T ctcpTime(String nick);

    T ctcp(String nick, String command, String args);

    T dcc(String subcommand, String nick, String argument);

    T ignore(String maskOrNick);

    T unignore(String maskOrNick);

    T ignoreList();

    T softIgnore(String maskOrNick);

    T unsoftIgnore(String maskOrNick);

    T softIgnoreList();

    T chatHistoryBefore(int limit, String selector);

    T chatHistoryLatest(int limit, String selector);

    T chatHistoryBetween(String startSelector, String endSelector, int limit);

    T chatHistoryAround(String selector, int limit);

    T markRead();

    T monitor(String args);

    T help(String topic);

    T upload(String provider, String path, String args);

    T replyMessage(String msgid, String text);

    T reactMessage(String msgid, String emoji);

    T unreactMessage(String msgid, String emoji);

    T editMessage(String msgid, String text);

    T redactMessage(String msgid, String reason);

    T quote(String rawLine);

    T say(String text);

    T unknown(String raw);
  }
}
