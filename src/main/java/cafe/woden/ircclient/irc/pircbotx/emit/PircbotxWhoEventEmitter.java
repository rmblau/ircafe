package cafe.woden.ircclient.irc.pircbotx.emit;

import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3InboundCommandSignalRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3WhoisProbeTracker;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.pircbotx.parse.ParsedIrcLine;
import cafe.woden.ircclient.irc.pircbotx.parse.PircbotxInboundLineParsers;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import cafe.woden.ircclient.irc.pircbotx.support.PircbotxUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Emits structured WHO/WHOX/WHOIS events from runtime SPI-owned numeric observations. */
public final class PircbotxWhoEventEmitter {
  private static final Logger log = LoggerFactory.getLogger(PircbotxWhoEventEmitter.class);

  private final String serverId;
  private final PircbotxConnectionState conn;
  private final Consumer<ServerIrcEvent> emit;
  private final Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog;

  public PircbotxWhoEventEmitter(
      String serverId,
      PircbotxConnectionState conn,
      Consumer<ServerIrcEvent> emit,
      Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog) {
    this.serverId = Objects.requireNonNull(serverId, "serverId");
    this.conn = Objects.requireNonNull(conn, "conn");
    this.emit = Objects.requireNonNull(emit, "emit");
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
  }

  public void maybeEmitLine(String rawLine) {
    if (rawLine == null || rawLine.isBlank()) return;
    emit(Ircv3InboundCommandOperation.USERHOST, rawLine);
    emit(Ircv3InboundCommandOperation.WHOIS_AWAY, rawLine);
    emit(Ircv3InboundCommandOperation.WHOIS_ACCOUNT, rawLine);
    emit(Ircv3InboundCommandOperation.WHOIS_END, rawLine);
    emit(Ircv3InboundCommandOperation.WHOIS_USER, rawLine);
    emit(Ircv3InboundCommandOperation.WHO, rawLine);
    emit(Ircv3InboundCommandOperation.WHOX, rawLine);
  }

  public boolean maybeEmitNumeric(int code, String line) {
    Ircv3InboundCommandOperation operation =
        switch (code) {
          case 302 -> Ircv3InboundCommandOperation.USERHOST;
          case 352 -> Ircv3InboundCommandOperation.WHO;
          case 354 -> Ircv3InboundCommandOperation.WHOX;
          case 330 -> Ircv3InboundCommandOperation.WHOIS_ACCOUNT;
          case 301 -> Ircv3InboundCommandOperation.WHOIS_AWAY;
          case 318 -> Ircv3InboundCommandOperation.WHOIS_END;
          default -> null;
        };
    if (operation == null) return false;
    emit(operation, line);
    return true;
  }

  private void emit(Ircv3InboundCommandOperation operation, String line) {
    List<Ircv3InboundCommandSignal> signals = runtimeCatalog.parse(operation, request(line));
    if (signals.isEmpty()) return;
    Instant at = Instant.now();
    for (Ircv3InboundCommandSignal signal : signals) {
      adapt(operation, at, line, signal);
    }
  }

  private void adapt(
      Ircv3InboundCommandOperation operation,
      Instant at,
      String rawLine,
      Ircv3InboundCommandSignal signal) {
    if (signal instanceof Ircv3InboundCommandSignal.HostmaskObserved hostmask) {
      emitHostmask(at, "", hostmask.nick(), hostmask.hostmask());
      return;
    }
    if (signal instanceof Ircv3InboundCommandSignal.ChannelHostmaskObserved hostmask) {
      emitHostmask(at, hostmask.channel(), hostmask.nick(), hostmask.hostmask());
      return;
    }
    if (signal instanceof Ircv3InboundCommandSignal.UserAwayObserved away) {
      if (operation == Ircv3InboundCommandOperation.WHOIS_AWAY) {
        conn.markWhoisAwayObserved(away.nick());
      }
      emit.accept(
          new ServerIrcEvent(
              serverId,
              new IrcEvent.UserAwayStateObserved(
                  at,
                  away.nick(),
                  away.away() ? IrcEvent.AwayState.AWAY : IrcEvent.AwayState.HERE,
                  away.message())));
      return;
    }
    if (signal instanceof Ircv3InboundCommandSignal.AccountObserved account) {
      if (operation == Ircv3InboundCommandOperation.WHOIS_ACCOUNT) {
        conn.markWhoisAccountObserved(account.nick());
      }
      emit.accept(
          new ServerIrcEvent(
              serverId,
              new IrcEvent.UserAccountStateObserved(
                  at,
                  account.nick(),
                  account.state() == Ircv3InboundCommandSignal.AccountState.LOGGED_IN
                      ? IrcEvent.AccountState.LOGGED_IN
                      : IrcEvent.AccountState.LOGGED_OUT,
                  account.accountName())));
      return;
    }
    if (signal instanceof Ircv3InboundCommandSignal.WhoisEndedObserved ended) {
      emitWhoisCompletion(at, ended.nick());
      return;
    }
    if (signal instanceof Ircv3InboundCommandSignal.WhoxSchemaObserved schema) {
      emitWhoxSchemaObservation(at, rawLine, schema);
    }
  }

  private void emitHostmask(Instant at, String channel, String nick, String hostmask) {
    if (!PircbotxUtil.isUsefulHostmask(hostmask)) return;
    emit.accept(
        new ServerIrcEvent(
            serverId,
            new IrcEvent.UserHostmaskObserved(
                at,
                Objects.toString(channel, "").trim(),
                Objects.toString(nick, "").trim(),
                hostmask)));
  }

  private void emitWhoisCompletion(Instant at, String nick) {
    String normalizedNick = Objects.toString(nick, "").trim();
    if (normalizedNick.isEmpty()) return;
    Ircv3WhoisProbeTracker.Completion completion = conn.completeWhoisProbe(normalizedNick);
    if (completion == null) return;

    if (!completion.sawAway()) {
      emit.accept(
          new ServerIrcEvent(
              serverId,
              new IrcEvent.UserAwayStateObserved(
                  at, normalizedNick, IrcEvent.AwayState.HERE)));
    }
    if (!completion.sawAccount() && completion.accountNumericSupported()) {
      emit.accept(
          new ServerIrcEvent(
              serverId,
              new IrcEvent.UserAccountStateObserved(
                  at, normalizedNick, IrcEvent.AccountState.LOGGED_OUT)));
    }
    emit.accept(
        new ServerIrcEvent(
            serverId,
            new IrcEvent.WhoisProbeCompleted(
                at,
                normalizedNick,
                completion.sawAway(),
                completion.sawAccount(),
                completion.accountNumericSupported())));
  }

  private void emitWhoxSchemaObservation(
      Instant at, String rawLine, Ircv3InboundCommandSignal.WhoxSchemaObserved schema) {
    boolean shouldEmit =
        schema.compatible()
            ? conn.markWhoxSchemaCompatibleObserved()
            : conn.markWhoxSchemaIncompatibleObserved();
    if (!shouldEmit) return;
    if (!schema.compatible()) {
      log.debug("[{}] WHOX schema mismatch: {}: {}", serverId, schema.reason(), rawLine);
    }
    emit.accept(
        new ServerIrcEvent(
            serverId,
            new IrcEvent.WhoxSchemaCompatibleObserved(
                at, schema.compatible(), schema.reason())));
  }

  private static Ircv3InboundCommandRequest request(String rawLine) {
    String normalized = Objects.toString(rawLine, "").trim();
    ParsedIrcLine parsed = PircbotxInboundLineParsers.parseIrcLine(normalized);
    if (parsed == null) {
      return new Ircv3InboundCommandRequest("", "", normalized, List.of(), Map.of());
    }
    ArrayList<String> parameters = new ArrayList<>(parsed.params());
    String trailing = Objects.toString(parsed.trailing(), "").trim();
    if (!trailing.isEmpty()) {
      parameters.add(":" + trailing);
    }
    return new Ircv3InboundCommandRequest(
        PircbotxInboundLineParsers.nickFromPrefix(parsed.prefix()),
        parsed.command(),
        normalized,
        parameters,
        Map.of());
  }
}
