package cafe.woden.ircclient.irc.pircbotx.parse;

import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3AccountTagRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3AccountTagTracker;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adapts runtime SPI-owned account-tag signals to root {@link IrcEvent}s. */
public final class PircbotxAccountTagSupport {

  private static final Logger log = LoggerFactory.getLogger(PircbotxAccountTagSupport.class);

  private final String serverId;
  private final Consumer<ServerIrcEvent> sink;
  private final Ircv3AccountTagRuntimeSupport runtimeSupport;
  private final Ircv3AccountTagTracker tracker = new Ircv3AccountTagTracker();

  public PircbotxAccountTagSupport(
      String serverId,
      Consumer<ServerIrcEvent> sink,
      Ircv3AccountTagRuntimeSupport runtimeSupport) {
    this.serverId = Objects.requireNonNull(serverId, "serverId");
    this.sink = Objects.requireNonNull(sink, "sink");
    this.runtimeSupport = Objects.requireNonNull(runtimeSupport, "runtimeSupport");
  }

  public void observe(
      Instant at, String nick, String command, String target, ImmutableMap<String, String> tags) {
    Ircv3InboundTagRequest request =
        new Ircv3InboundTagRequest(command, nick, target, List.of(), tags);
    runtimeSupport
        .observe(request)
        .flatMap(observed -> tracker.observe(observed.nick(), observed.rawAccount()))
        .ifPresent(change -> emit(at, command, target, change));
  }

  private void emit(
      Instant at,
      String command,
      String target,
      Ircv3AccountTagTracker.Change change) {
    IrcEvent.AccountState state =
        change.state() == Ircv3AccountTagTracker.AccountState.LOGGED_IN
            ? IrcEvent.AccountState.LOGGED_IN
            : IrcEvent.AccountState.LOGGED_OUT;

    log.trace(
        "[{}] account-tag observed via tags: nick={} cmd={} target={} state={} account={}",
        serverId,
        change.nick(),
        command,
        target,
        state,
        change.accountName());
    sink.accept(
        new ServerIrcEvent(
            serverId,
            new IrcEvent.UserAccountStateObserved(
                at, change.nick(), state, change.accountName())));
  }
}
