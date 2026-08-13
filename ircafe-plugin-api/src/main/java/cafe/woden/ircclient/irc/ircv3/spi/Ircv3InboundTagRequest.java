package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Transport-neutral tagged-message input supplied to inbound IRCv3 runtime providers. */
public record Ircv3InboundTagRequest(
    String command,
    String sourceNick,
    String rawTarget,
    List<String> parameters,
    Map<String, String> tags,
    String rawLine,
    long observedAtEpochMilli,
    List<String> selfNickAliases,
    boolean selfAuthored) {

  public Ircv3InboundTagRequest(
      String command,
      String sourceNick,
      String rawTarget,
      List<String> parameters,
      Map<String, String> tags) {
    this(command, sourceNick, rawTarget, parameters, tags, "", 0L, List.of(), false);
  }

  public Ircv3InboundTagRequest(
      String command,
      String sourceNick,
      String rawTarget,
      List<String> parameters,
      Map<String, String> tags,
      String rawLine) {
    this(command, sourceNick, rawTarget, parameters, tags, rawLine, 0L, List.of(), false);
  }

  public Ircv3InboundTagRequest(
      String command,
      String sourceNick,
      String rawTarget,
      List<String> parameters,
      Map<String, String> tags,
      String rawLine,
      long observedAtEpochMilli) {
    this(
        command,
        sourceNick,
        rawTarget,
        parameters,
        tags,
        rawLine,
        observedAtEpochMilli,
        List.of(),
        false);
  }

  public Ircv3InboundTagRequest(
      String command,
      String sourceNick,
      String rawTarget,
      List<String> parameters,
      Map<String, String> tags,
      String rawLine,
      long observedAtEpochMilli,
      List<String> selfNickAliases) {
    this(
        command,
        sourceNick,
        rawTarget,
        parameters,
        tags,
        rawLine,
        observedAtEpochMilli,
        selfNickAliases,
        false);
  }

  public static Ircv3InboundTagRequest historyBootstrap(
      String target, String message, boolean selfAuthored) {
    return new Ircv3InboundTagRequest(
        "PRIVMSG",
        "",
        target,
        List.of(Objects.toString(message, "")),
        Map.of(),
        "",
        0L,
        List.of(),
        selfAuthored);
  }

  public Ircv3InboundTagRequest {
    command = Objects.toString(command, "").trim();
    sourceNick = Objects.toString(sourceNick, "").trim();
    rawTarget = Objects.toString(rawTarget, "").trim();
    parameters = List.copyOf(Objects.requireNonNullElse(parameters, List.of()));
    tags = Map.copyOf(Objects.requireNonNullElse(tags, Map.of()));
    rawLine = Objects.toString(rawLine, "").trim();
    selfNickAliases = List.copyOf(Objects.requireNonNullElse(selfNickAliases, List.of()));
  }

  public boolean isMessageLikeCommand() {
    String normalized = command.toUpperCase(Locale.ROOT);
    return normalized.equals("PRIVMSG")
        || normalized.equals("NOTICE")
        || normalized.equals("TAGMSG");
  }
}
