package cafe.woden.ircclient.irc.pircbotx.listener;

import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3SaslFailureSignal;
import cafe.woden.ircclient.irc.ircv3.Ircv3SaslRuntimeSupport;
import cafe.woden.ircclient.irc.pircbotx.state.PircbotxConnectionState;
import java.time.Instant;
import java.util.function.Consumer;
import org.pircbotx.PircBotX;

/** Applies feature-owned SASL failure signals to connection and transport state. */
final class PircbotxSaslFailureHandler {

  private final String serverId;
  private final PircbotxConnectionState conn;
  private final Consumer<ServerIrcEvent> emit;
  private final boolean disconnectOnSaslFailure;
  private final Ircv3SaslRuntimeSupport runtimeSupport;

  PircbotxSaslFailureHandler(
      String serverId,
      PircbotxConnectionState conn,
      Consumer<ServerIrcEvent> emit,
      boolean disconnectOnSaslFailure,
      Ircv3SaslRuntimeSupport runtimeSupport) {
    this.serverId = serverId;
    this.conn = conn;
    this.emit = emit;
    this.disconnectOnSaslFailure = disconnectOnSaslFailure;
    this.runtimeSupport = java.util.Objects.requireNonNull(runtimeSupport, "runtimeSupport");
  }

  boolean isFailureCode(int code) {
    return runtimeSupport.isFailureCode(code);
  }

  Integer parseFailureCode(String rawLine) {
    Ircv3SaslFailureSignal signal = runtimeSupport.failure(rawLine);
    return signal == null ? null : signal.numeric();
  }

  void handle(int code, String rawLine) {
    Ircv3SaslFailureSignal signal = runtimeSupport.failure(code, rawLine);
    if (signal == null) {
      return;
    }
    String reason = signal.disconnectReason();
    String existing = conn.disconnectReasonOverride();
    if (existing != null && !existing.isBlank()) {
      conn.suppressAutoReconnectOnce();
      return;
    }

    conn.overrideDisconnectReason(reason);
    conn.suppressAutoReconnectOnce();
    emit.accept(new ServerIrcEvent(serverId, new IrcEvent.Error(Instant.now(), reason, null)));
    if (disconnectOnSaslFailure) {
      PircBotX bot = conn.currentBot();
      if (bot != null) {
        try {
          bot.stopBotReconnect();
        } catch (Exception ignored) {
        }
        try {
          bot.sendIRC().quitServer(reason);
        } catch (Exception ignored) {
        }
      }
    }
  }
}
