package cafe.woden.ircclient.irc.pircbotx.parse;

import cafe.woden.ircclient.irc.IrcEvent;
import cafe.woden.ircclient.irc.ServerIrcEvent;
import cafe.woden.ircclient.irc.ircv3.Ircv3ChannelContextRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3MessageMutationRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3ReadMarkerRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.Ircv3Tags;
import cafe.woden.ircclient.irc.ircv3.Ircv3TypingRuntimeSupport;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagRequest;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3InboundTagSignal;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adapts runtime SPI-owned IRCv3 tag signals into root IRC events. */
public final class PircbotxTagSignalSupport {

  private static final Logger log = LoggerFactory.getLogger(PircbotxTagSignalSupport.class);

  private final String serverId;
  private final Consumer<ServerIrcEvent> sink;
  private final Ircv3ChannelContextRuntimeSupport channelContextRuntimeSupport;
  private final Ircv3MessageMutationRuntimeSupport messageMutationRuntimeSupport;
  private final Ircv3ReadMarkerRuntimeSupport readMarkerRuntimeSupport;
  private final Ircv3TypingRuntimeSupport typingRuntimeSupport;

  public PircbotxTagSignalSupport(
      String serverId,
      Consumer<ServerIrcEvent> sink,
      Ircv3ChannelContextRuntimeSupport channelContextRuntimeSupport,
      Ircv3MessageMutationRuntimeSupport messageMutationRuntimeSupport,
      Ircv3ReadMarkerRuntimeSupport readMarkerRuntimeSupport,
      Ircv3TypingRuntimeSupport typingRuntimeSupport) {
    this.serverId = Objects.requireNonNull(serverId, "serverId");
    this.sink = Objects.requireNonNull(sink, "sink");
    this.channelContextRuntimeSupport =
        Objects.requireNonNull(channelContextRuntimeSupport, "channelContextRuntimeSupport");
    this.messageMutationRuntimeSupport =
        Objects.requireNonNull(messageMutationRuntimeSupport, "messageMutationRuntimeSupport");
    this.readMarkerRuntimeSupport =
        Objects.requireNonNull(readMarkerRuntimeSupport, "readMarkerRuntimeSupport");
    this.typingRuntimeSupport =
        Objects.requireNonNull(typingRuntimeSupport, "typingRuntimeSupport");
  }

  public void emitObservedSignals(
      Instant at,
      String nick,
      String rawTarget,
      String command,
      List<String> parsedLine,
      ImmutableMap<String, String> tags) {
    if (tags == null || tags.isEmpty()) return;

    String firstParam = firstParam(parsedLine);
    String messageTarget = !firstParam.isBlank() ? firstParam : stripLeadingColon(rawTarget);
    Ircv3InboundTagRequest request =
        new Ircv3InboundTagRequest(command, nick, messageTarget, parsedLine, tags);
    String conversationTarget = channelContextRuntimeSupport.resolve(request);

    emitSignals(
        at, nick, conversationTarget, messageMutationRuntimeSupport.conversationSignals(request));
    typingRuntimeSupport
        .fromTags(request)
        .ifPresent(
            observed -> {
              if (log.isDebugEnabled()) {
                log.debug(
                    "[{}] IRCv3 +typing tag: from={} target={} state={} cmd={}",
                    serverId,
                    nick,
                    conversationTarget,
                    observed.state(),
                    Objects.toString(command, ""));
              }
              emit(new IrcEvent.UserTypingObserved(at, nick, conversationTarget, observed.state()));
            });
    readMarkerRuntimeSupport
        .fromTags(request)
        .ifPresent(
            observed ->
                emit(
                    new IrcEvent.ReadMarkerObserved(
                        at, nick, conversationTarget, observed.marker())));
  }

  public static String firstTag(ImmutableMap<String, String> tags, String... keys) {
    return Ircv3Tags.firstDecodedTagValue(tags, keys);
  }

  private void emitSignals(
      Instant at, String nick, String conversationTarget, List<Ircv3InboundTagSignal> signals) {
    for (Ircv3InboundTagSignal signal : signals) {
      switch (signal.type()) {
        case REPLY ->
            emit(
                new IrcEvent.MessageReplyObserved(
                    at, nick, conversationTarget, signal.primaryValue()));
        case REACT ->
            emit(
                new IrcEvent.MessageReactObserved(
                    at, nick, conversationTarget, signal.primaryValue(), signal.secondaryValue()));
        case UNREACT ->
            emit(
                new IrcEvent.MessageUnreactObserved(
                    at, nick, conversationTarget, signal.primaryValue(), signal.secondaryValue()));
        case MESSAGE_REDACTION ->
            emit(
                new IrcEvent.MessageRedactionObserved(
                    at, nick, conversationTarget, signal.primaryValue()));
        default -> {
          // Other signal types are consumed by different root adapters.
        }
      }
    }
  }

  private void emit(IrcEvent event) {
    sink.accept(new ServerIrcEvent(serverId, event));
  }

  private static String firstParam(List<String> parsedLine) {
    if (parsedLine == null || parsedLine.isEmpty()) return "";
    return stripLeadingColon(parsedLine.getFirst());
  }

  private static String stripLeadingColon(String raw) {
    String value = Objects.toString(raw, "").trim();
    if (value.startsWith(":")) value = value.substring(1).trim();
    return value;
  }
}
