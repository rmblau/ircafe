package cafe.woden.ircclient.irc.pircbotx.parse;

import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3StandardReplyRuntimeSupport;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Adapts runtime SPI-owned IRCv3 standard replies to root {@link IrcEvent}s. */
public final class PircbotxStandardReplySupport {

  private final String serverId;
  private final Consumer<ServerIrcEvent> sink;
  private final Ircv3StandardReplyRuntimeSupport runtimeSupport;

  public PircbotxStandardReplySupport(
      String serverId,
      Consumer<ServerIrcEvent> sink,
      Ircv3StandardReplyRuntimeSupport runtimeSupport) {
    this.serverId = Objects.requireNonNull(serverId, "serverId");
    this.sink = Objects.requireNonNull(sink, "sink");
    this.runtimeSupport = Objects.requireNonNull(runtimeSupport, "runtimeSupport");
  }

  public boolean emitIfSupported(
      Instant at,
      String command,
      String rawLine,
      List<String> parsedLine,
      ImmutableMap<String, String> tags) {
    Ircv3StandardReplyRuntimeSupport.Observation observation =
        runtimeSupport.observe(command, rawLine, parsedLine, tags, "").orElse(null);
    if (observation == null) {
      return false;
    }
    emit(at, rawLine, tags, observation);
    return true;
  }

  private void emit(
      Instant at,
      String rawLine,
      ImmutableMap<String, String> tags,
      Ircv3StandardReplyRuntimeSupport.Observation reply) {
    Map<String, String> ircv3Tags = tags == null ? Map.of() : tags;
    sink.accept(
        new ServerIrcEvent(
            serverId,
            new IrcEvent.StandardReply(
                at,
                toRootKind(reply.kind()),
                reply.command(),
                reply.code(),
                reply.context(),
                reply.description(),
                Objects.toString(rawLine, "").trim(),
                reply.messageId(),
                ircv3Tags)));
  }

  private static IrcEvent.StandardReplyKind toRootKind(Ircv3StandardReplyRuntimeSupport.Kind kind) {
    return switch (kind) {
      case FAIL -> IrcEvent.StandardReplyKind.FAIL;
      case WARN -> IrcEvent.StandardReplyKind.WARN;
      case NOTE -> IrcEvent.StandardReplyKind.NOTE;
    };
  }
}
