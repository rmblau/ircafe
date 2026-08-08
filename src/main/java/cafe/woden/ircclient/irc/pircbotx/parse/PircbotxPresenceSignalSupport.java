package cafe.woden.ircclient.irc.pircbotx.parse;

import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3InboundCommandSignalRuntimeCatalog;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundCommandSignal;
import cafe.woden.ircclient.irc.pircbotx.support.PircbotxUtil;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adapts runtime SPI-owned IRCv3 presence and identity observations to root {@link IrcEvent}s. */
public final class PircbotxPresenceSignalSupport {

  private static final Logger log = LoggerFactory.getLogger(PircbotxPresenceSignalSupport.class);

  private final String serverId;
  private final Consumer<ServerIrcEvent> sink;
  private final Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog;

  public PircbotxPresenceSignalSupport(
      String serverId,
      Consumer<ServerIrcEvent> sink,
      Ircv3InboundCommandSignalRuntimeCatalog runtimeCatalog) {
    this.serverId = Objects.requireNonNull(serverId, "serverId");
    this.sink = Objects.requireNonNull(sink, "sink");
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog");
  }

  public void observe(
      Instant at, String nick, String command, String rawLine, List<String> parsedLine) {
    Ircv3InboundCommandOperation operation = presenceOperation(command);
    if (operation == null) return;
    emit(
        at,
        command,
        parsedLine,
        rawLine,
        parseFocusedOrLegacy(
            operation,
            Ircv3InboundCommandOperation.PRESENCE,
            request(nick, command, rawLine, parsedLine)));
  }

  public boolean observeIdentityChange(
      Instant at, String nick, String command, String rawLine, List<String> parsedLine) {
    Ircv3InboundCommandOperation operation = identityOperation(command);
    if (operation == null) return false;
    List<Ircv3InboundCommandSignal> signals =
        parseFocusedOrLegacy(
            operation,
            Ircv3InboundCommandOperation.IDENTITY_CHANGE,
            request(nick, command, rawLine, parsedLine));
    emit(at, command, parsedLine, rawLine, signals);
    return !signals.isEmpty();
  }

  public boolean observeAwayNotifyRawLine(Instant at, String rawLine) {
    List<Ircv3InboundCommandSignal> signals =
        parseFocusedOrLegacy(
            Ircv3InboundCommandOperation.AWAY_NOTIFY,
            Ircv3InboundCommandOperation.PRESENCE,
            request("", "AWAY", rawLine, List.of()));
    List<Ircv3InboundCommandSignal> awaySignals =
        signals.stream()
            .filter(Ircv3InboundCommandSignal.UserAwayObserved.class::isInstance)
            .toList();
    emit(at, "AWAY", List.of(), rawLine, awaySignals);
    return !awaySignals.isEmpty();
  }

  public boolean observeSelfAwayConfirmation(Instant at, int numeric, String rawLine) {
    if (numeric != 305 && numeric != 306) return false;
    List<Ircv3InboundCommandSignal> signals =
        parseFocusedOrLegacy(
            Ircv3InboundCommandOperation.AWAY_NOTIFY,
            Ircv3InboundCommandOperation.PRESENCE,
            request("", Integer.toString(numeric), rawLine, List.of()));
    List<Ircv3InboundCommandSignal> selfAwaySignals =
        signals.stream()
            .filter(Ircv3InboundCommandSignal.SelfAwayObserved.class::isInstance)
            .toList();
    emit(at, Integer.toString(numeric), List.of(), rawLine, selfAwaySignals);
    return !selfAwaySignals.isEmpty();
  }

  public boolean observeSelfAwayConfirmationRawLine(Instant at, String rawLine) {
    List<Ircv3InboundCommandSignal> signals =
        parseFocusedOrLegacy(
            Ircv3InboundCommandOperation.AWAY_NOTIFY,
            Ircv3InboundCommandOperation.PRESENCE,
            request("", "", rawLine, List.of()));
    List<Ircv3InboundCommandSignal> selfAwaySignals =
        signals.stream()
            .filter(Ircv3InboundCommandSignal.SelfAwayObserved.class::isInstance)
            .toList();
    emit(at, "", List.of(), rawLine, selfAwaySignals);
    return !selfAwaySignals.isEmpty();
  }

  private List<Ircv3InboundCommandSignal> parseFocusedOrLegacy(
      Ircv3InboundCommandOperation focused,
      Ircv3InboundCommandOperation legacy,
      Ircv3InboundCommandRequest request) {
    List<Ircv3InboundCommandSignal> focusedSignals = runtimeCatalog.parse(focused, request);
    if (!focusedSignals.isEmpty() || !runtimeCatalog.supports(legacy)) {
      return focusedSignals;
    }
    return runtimeCatalog.parse(legacy, request);
  }

  private static Ircv3InboundCommandOperation presenceOperation(String command) {
    String normalized = Objects.toString(command, "").trim().toUpperCase(java.util.Locale.ROOT);
    return switch (normalized) {
      case "AWAY" -> Ircv3InboundCommandOperation.AWAY_NOTIFY;
      case "ACCOUNT" -> Ircv3InboundCommandOperation.ACCOUNT_NOTIFY;
      case "JOIN" -> Ircv3InboundCommandOperation.EXTENDED_JOIN;
      default -> null;
    };
  }

  private static Ircv3InboundCommandOperation identityOperation(String command) {
    String normalized = Objects.toString(command, "").trim().toUpperCase(java.util.Locale.ROOT);
    return switch (normalized) {
      case "CHGHOST" -> Ircv3InboundCommandOperation.CHGHOST;
      case "SETNAME" -> Ircv3InboundCommandOperation.SETNAME;
      default -> null;
    };
  }

  private void emit(
      Instant at,
      String command,
      List<String> parsedLine,
      String rawLine,
      List<Ircv3InboundCommandSignal> signals) {
    for (Ircv3InboundCommandSignal signal : signals) {
      if (signal instanceof Ircv3InboundCommandSignal.HostmaskObserved hostmask) {
        if (PircbotxUtil.isUsefulHostmask(hostmask.hostmask())) {
          sink.accept(
              new ServerIrcEvent(
                  serverId,
                  new IrcEvent.UserHostmaskObserved(
                      at, "", hostmask.nick(), hostmask.hostmask())));
        }
        continue;
      }

      if (signal instanceof Ircv3InboundCommandSignal.UserAwayObserved away) {
        IrcEvent.AwayState state = away.away() ? IrcEvent.AwayState.AWAY : IrcEvent.AwayState.HERE;
        log.debug(
            "[{}] away-notify observed via InputParser: nick={} state={} msg={} params={} raw={}",
            serverId,
            away.nick(),
            state,
            away.message(),
            parsedLine,
            rawLine);
        sink.accept(
            new ServerIrcEvent(
                serverId,
                new IrcEvent.UserAwayStateObserved(at, away.nick(), state, away.message())));
        continue;
      }

      if (signal instanceof Ircv3InboundCommandSignal.SelfAwayObserved away) {
        String message = away.message();
        if (message == null || message.isBlank()) {
          message =
              away.away()
                  ? "You have been marked as being away"
                  : "You are no longer marked as being away";
        }
        sink.accept(
            new ServerIrcEvent(
                serverId, new IrcEvent.AwayStatusChanged(at, away.away(), message)));
        continue;
      }

      if (signal instanceof Ircv3InboundCommandSignal.AccountObserved account) {
        IrcEvent.AccountState state =
            account.state() == Ircv3InboundCommandSignal.AccountState.LOGGED_IN
                ? IrcEvent.AccountState.LOGGED_IN
                : IrcEvent.AccountState.LOGGED_OUT;
        log.debug(
            "[{}] {} observed via InputParser: nick={} state={} account={} params={} raw={}",
            serverId,
            "JOIN".equalsIgnoreCase(command) ? "extended-join" : "account-notify",
            account.nick(),
            state,
            account.accountName(),
            parsedLine,
            rawLine);
        sink.accept(
            new ServerIrcEvent(
                serverId,
                new IrcEvent.UserAccountStateObserved(
                    at, account.nick(), state, account.accountName())));
        continue;
      }

      if (signal instanceof Ircv3InboundCommandSignal.SetNameObserved setName) {
        IrcEvent.UserSetNameObserved.Source source =
            setName.source() == Ircv3InboundCommandSignal.SetNameSource.SETNAME
                ? IrcEvent.UserSetNameObserved.Source.SETNAME
                : IrcEvent.UserSetNameObserved.Source.EXTENDED_JOIN;
        sink.accept(
            new ServerIrcEvent(
                serverId,
                new IrcEvent.UserSetNameObserved(at, setName.nick(), setName.realName(), source)));
        continue;
      }

      if (signal instanceof Ircv3InboundCommandSignal.HostChangedObserved hostChanged) {
        sink.accept(
            new ServerIrcEvent(
                serverId,
                new IrcEvent.UserHostChanged(
                    at, hostChanged.nick(), hostChanged.user(), hostChanged.host())));
        if (PircbotxUtil.isUsefulHostmask(hostChanged.hostmask())) {
          sink.accept(
              new ServerIrcEvent(
                  serverId,
                  new IrcEvent.UserHostmaskObserved(
                      at, "", hostChanged.nick(), hostChanged.hostmask())));
        }
      }
    }
  }

  private static Ircv3InboundCommandRequest request(
      String nick, String command, String rawLine, List<String> parsedLine) {
    return new Ircv3InboundCommandRequest(nick, command, rawLine, parsedLine, Map.of());
  }
}
