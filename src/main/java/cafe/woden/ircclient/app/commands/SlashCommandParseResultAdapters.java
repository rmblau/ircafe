package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.app.commands.spi.SlashCommandParseResult;
import java.util.List;

/** Adapts plugin-facing slash command parse results into app-owned command routing values. */
final class SlashCommandParseResultAdapters {
  private static final SlashCommandParseResultMapper.CommandFactory<ParsedInput> FACTORY =
      new SlashCommandParseResultMapper.CommandFactory<>() {
        @Override
        public ParsedInput join(String channel, String key) {
          return new ParsedInput.Join(channel, key);
        }

        @Override
        public ParsedInput part(String channel, String reason) {
          return new ParsedInput.Part(channel, reason);
        }

        @Override
        public ParsedInput connect(String target) {
          return new ParsedInput.Connect(target);
        }

        @Override
        public ParsedInput disconnect(String target) {
          return new ParsedInput.Disconnect(target);
        }

        @Override
        public ParsedInput reconnect(String target) {
          return new ParsedInput.Reconnect(target);
        }

        @Override
        public ParsedInput backendNamed(String command, String args) {
          return new ParsedInput.BackendNamed(command, args);
        }

        @Override
        public ParsedInput quit(String reason) {
          return new ParsedInput.Quit(reason);
        }

        @Override
        public ParsedInput nick(String newNick) {
          return new ParsedInput.Nick(newNick);
        }

        @Override
        public ParsedInput away(String message) {
          return new ParsedInput.Away(message);
        }

        @Override
        public ParsedInput query(String nick) {
          return new ParsedInput.Query(nick);
        }

        @Override
        public ParsedInput whois(String nick) {
          return new ParsedInput.Whois(nick);
        }

        @Override
        public ParsedInput whowas(String nick, int count) {
          return new ParsedInput.Whowas(nick, count);
        }

        @Override
        public ParsedInput msg(String nick, String body) {
          return new ParsedInput.Msg(nick, body);
        }

        @Override
        public ParsedInput notice(String target, String body) {
          return new ParsedInput.Notice(target, body);
        }

        @Override
        public ParsedInput me(String action) {
          return new ParsedInput.Me(action);
        }

        @Override
        public ParsedInput topic(String first, String rest) {
          return new ParsedInput.Topic(first, rest);
        }

        @Override
        public ParsedInput kick(String channel, String nick, String reason) {
          return new ParsedInput.Kick(channel, nick, reason);
        }

        @Override
        public ParsedInput invite(String nick, String channel) {
          return new ParsedInput.Invite(nick, channel);
        }

        @Override
        public ParsedInput inviteList(String serverId) {
          return new ParsedInput.InviteList(serverId);
        }

        @Override
        public ParsedInput inviteJoin(String inviteToken) {
          return new ParsedInput.InviteJoin(inviteToken);
        }

        @Override
        public ParsedInput inviteIgnore(String inviteToken) {
          return new ParsedInput.InviteIgnore(inviteToken);
        }

        @Override
        public ParsedInput inviteWhois(String inviteToken) {
          return new ParsedInput.InviteWhois(inviteToken);
        }

        @Override
        public ParsedInput inviteBlock(String inviteToken) {
          return new ParsedInput.InviteBlock(inviteToken);
        }

        @Override
        public ParsedInput inviteAutoJoin(String mode) {
          return new ParsedInput.InviteAutoJoin(mode);
        }

        @Override
        public ParsedInput names(String channel) {
          return new ParsedInput.Names(channel);
        }

        @Override
        public ParsedInput who(String args) {
          return new ParsedInput.Who(args);
        }

        @Override
        public ParsedInput listCmd(String args) {
          return new ParsedInput.ListCmd(args);
        }

        @Override
        public ParsedInput mode(String first, String rest) {
          return new ParsedInput.Mode(first, rest);
        }

        @Override
        public ParsedInput op(String channel, List<String> nicks) {
          return new ParsedInput.Op(channel, nicks);
        }

        @Override
        public ParsedInput deop(String channel, List<String> nicks) {
          return new ParsedInput.Deop(channel, nicks);
        }

        @Override
        public ParsedInput voice(String channel, List<String> nicks) {
          return new ParsedInput.Voice(channel, nicks);
        }

        @Override
        public ParsedInput devoice(String channel, List<String> nicks) {
          return new ParsedInput.Devoice(channel, nicks);
        }

        @Override
        public ParsedInput ban(String channel, List<String> masksOrNicks) {
          return new ParsedInput.Ban(channel, masksOrNicks);
        }

        @Override
        public ParsedInput unban(String channel, List<String> masksOrNicks) {
          return new ParsedInput.Unban(channel, masksOrNicks);
        }

        @Override
        public ParsedInput ctcpVersion(String nick) {
          return new ParsedInput.CtcpVersion(nick);
        }

        @Override
        public ParsedInput ctcpPing(String nick) {
          return new ParsedInput.CtcpPing(nick);
        }

        @Override
        public ParsedInput ctcpTime(String nick) {
          return new ParsedInput.CtcpTime(nick);
        }

        @Override
        public ParsedInput ctcp(String nick, String command, String args) {
          return new ParsedInput.Ctcp(nick, command, args);
        }

        @Override
        public ParsedInput dcc(String subcommand, String nick, String argument) {
          return new ParsedInput.Dcc(subcommand, nick, argument);
        }

        @Override
        public ParsedInput ignore(String maskOrNick) {
          return new ParsedInput.Ignore(maskOrNick);
        }

        @Override
        public ParsedInput unignore(String maskOrNick) {
          return new ParsedInput.Unignore(maskOrNick);
        }

        @Override
        public ParsedInput ignoreList() {
          return new ParsedInput.IgnoreList();
        }

        @Override
        public ParsedInput softIgnore(String maskOrNick) {
          return new ParsedInput.SoftIgnore(maskOrNick);
        }

        @Override
        public ParsedInput unsoftIgnore(String maskOrNick) {
          return new ParsedInput.UnsoftIgnore(maskOrNick);
        }

        @Override
        public ParsedInput softIgnoreList() {
          return new ParsedInput.SoftIgnoreList();
        }

        @Override
        public ParsedInput chatHistoryBefore(int limit, String selector) {
          return new ParsedInput.ChatHistoryBefore(limit, selector);
        }

        @Override
        public ParsedInput chatHistoryLatest(int limit, String selector) {
          return new ParsedInput.ChatHistoryLatest(limit, selector);
        }

        @Override
        public ParsedInput chatHistoryBetween(String startSelector, String endSelector, int limit) {
          return new ParsedInput.ChatHistoryBetween(startSelector, endSelector, limit);
        }

        @Override
        public ParsedInput chatHistoryAround(String selector, int limit) {
          return new ParsedInput.ChatHistoryAround(selector, limit);
        }

        @Override
        public ParsedInput markRead() {
          return new ParsedInput.MarkRead();
        }

        @Override
        public ParsedInput monitor(String args) {
          return new ParsedInput.Monitor(args);
        }

        @Override
        public ParsedInput help(String topic) {
          return new ParsedInput.Help(topic);
        }

        @Override
        public ParsedInput upload(String provider, String path, String args) {
          return new ParsedInput.Upload(provider, path, args);
        }

        @Override
        public ParsedInput replyMessage(String msgid, String text) {
          return new ParsedInput.ReplyMessage(msgid, text);
        }

        @Override
        public ParsedInput reactMessage(String msgid, String emoji) {
          return new ParsedInput.ReactMessage(msgid, emoji);
        }

        @Override
        public ParsedInput unreactMessage(String msgid, String emoji) {
          return new ParsedInput.UnreactMessage(msgid, emoji);
        }

        @Override
        public ParsedInput editMessage(String msgid, String text) {
          return new ParsedInput.EditMessage(msgid, text);
        }

        @Override
        public ParsedInput redactMessage(String msgid, String reason) {
          return new ParsedInput.RedactMessage(msgid, reason);
        }

        @Override
        public ParsedInput quote(String rawLine) {
          return new ParsedInput.Quote(rawLine);
        }

        @Override
        public ParsedInput say(String text) {
          return new ParsedInput.Say(text);
        }

        @Override
        public ParsedInput unknown(String raw) {
          return new ParsedInput.Unknown(raw);
        }
      };

  private SlashCommandParseResultAdapters() {}

  static ParsedInput toParsedInput(SlashCommandParseResult result) {
    return SlashCommandParseResultMapper.map(result, FACTORY);
  }
}
