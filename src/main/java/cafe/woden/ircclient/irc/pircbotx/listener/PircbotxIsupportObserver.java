package cafe.woden.ircclient.irc.pircbotx.listener;

import cafe.woden.ircclient.bouncer.SojuBouncerProtocolParser;
import cafe.woden.ircclient.irc.*;
import cafe.woden.ircclient.irc.backend.*;
import cafe.woden.ircclient.irc.ircv3.*;
import cafe.woden.ircclient.irc.pircbotx.parse.*;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import cafe.woden.ircclient.irc.playback.*;
import cafe.woden.ircclient.state.api.ServerIsupportStatePort;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Applies RPL_ISUPPORT tokens and emits derived connection-feature observations. */
final class PircbotxIsupportObserver {
  private static final Logger log = LoggerFactory.getLogger(PircbotxIsupportObserver.class);

  @NonNull private final String serverId;
  @NonNull private final PircbotxConnectionState conn;
  @NonNull private final ServerIsupportStatePort serverIsupportState;
  @NonNull private final Consumer<ServerIrcEvent> emit;
  @NonNull private final Consumer<String> sojuNetIdObserver;
  @NonNull private final Ircv3IsupportRuntimeSupport isupportRuntimeSupport;
  @NonNull private final Ircv3TypingRuntimeSupport typingRuntimeSupport;

  private final SojuBouncerProtocolParser sojuProtocolParser = new SojuBouncerProtocolParser();

  PircbotxIsupportObserver(
      String serverId,
      PircbotxConnectionState conn,
      ServerIsupportStatePort serverIsupportState,
      Consumer<ServerIrcEvent> emit,
      Consumer<String> sojuNetIdObserver,
      Ircv3IsupportRuntimeSupport isupportRuntimeSupport,
      Ircv3TypingRuntimeSupport typingRuntimeSupport) {
    this.serverId = Objects.requireNonNull(serverId, "serverId");
    this.conn = Objects.requireNonNull(conn, "conn");
    this.serverIsupportState = Objects.requireNonNull(serverIsupportState, "serverIsupportState");
    this.emit = Objects.requireNonNull(emit, "emit");
    this.sojuNetIdObserver = Objects.requireNonNull(sojuNetIdObserver, "sojuNetIdObserver");
    this.isupportRuntimeSupport =
        Objects.requireNonNull(isupportRuntimeSupport, "isupportRuntimeSupport");
    this.typingRuntimeSupport =
        Objects.requireNonNull(typingRuntimeSupport, "typingRuntimeSupport");
  }

  void observe(String rawLine) {
    if (rawLine == null || rawLine.isBlank()) return;

    for (Ircv3IsupportRuntimeSupport.TokenUpdate token :
        isupportRuntimeSupport.tokenUpdates(rawLine)) {
      serverIsupportState.applyIsupportToken(
          serverId, token.key(), token.removed() ? null : token.value());
    }
    sojuNetIdObserver.accept(sojuProtocolParser.parseBouncerNetId(rawLine));

    isupportRuntimeSupport
        .whoxSupport(rawLine)
        .ifPresent(
            supported ->
                emit.accept(
                    new ServerIrcEvent(
                        serverId, new IrcEvent.WhoxSupportObserved(Instant.now(), supported))));

    isupportRuntimeSupport.monitorSupport(rawLine).ifPresent(this::applyMonitorSupport);
    typingRuntimeSupport.clientTagPolicy(rawLine).ifPresent(this::applyTypingClientTagPolicy);
  }

  private void applyMonitorSupport(Ircv3IsupportRuntimeSupport.MonitorSupport monitor) {
    if (conn.updateMonitorSupport(monitor.supported(), monitor.limit())) {
      log.debug(
          "[{}] monitor support changed: supported={} max-targets={}",
          serverId,
          monitor.supported(),
          Math.max(0, monitor.limit()));
    }
  }

  private void applyTypingClientTagPolicy(Ircv3TypingRuntimeSupport.ClientTagPolicy policy) {
    if (conn.updateTypingClientTagPolicy(policy.allowed())) {
      log.debug(
          "[{}] CLIENTTAGDENY -> typing allowed={} (raw={})",
          serverId,
          policy.allowed(),
          policy.rawDenyValue());
    }
  }
}
