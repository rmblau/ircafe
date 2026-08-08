package cafe.woden.ircclient.irc.ircv3.spi;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Transport-neutral parsed IRC command supplied to inbound runtime providers. */
public record Ircv3InboundCommandRequest(
    String sourceNick,
    String command,
    String rawLine,
    List<String> parameters,
    Map<String, String> tags,
    String connectionHost,
    boolean secureConnection,
    long observedAtEpochMilli,
    boolean messageTagsEnabled,
    boolean batchEnabled,
    boolean chatHistoryEnabled,
    Set<String> pendingCapabilities,
    MultilineState multilineState) {

  public Ircv3InboundCommandRequest(
      String sourceNick,
      String command,
      String rawLine,
      List<String> parameters,
      Map<String, String> tags) {
    this(
        sourceNick,
        command,
        rawLine,
        parameters,
        tags,
        "",
        false,
        0L,
        false,
        false,
        false,
        Set.of(),
        MultilineState.empty());
  }

  public Ircv3InboundCommandRequest(
      String sourceNick,
      String command,
      String rawLine,
      List<String> parameters,
      Map<String, String> tags,
      String connectionHost,
      boolean secureConnection,
      long observedAtEpochMilli) {
    this(
        sourceNick,
        command,
        rawLine,
        parameters,
        tags,
        connectionHost,
        secureConnection,
        observedAtEpochMilli,
        false,
        false,
        false,
        Set.of(),
        MultilineState.empty());
  }

  public Ircv3InboundCommandRequest(
      String sourceNick,
      String command,
      String rawLine,
      List<String> parameters,
      Map<String, String> tags,
      String connectionHost,
      boolean secureConnection,
      long observedAtEpochMilli,
      boolean messageTagsEnabled,
      boolean batchEnabled,
      boolean chatHistoryEnabled,
      Set<String> pendingCapabilities) {
    this(
        sourceNick,
        command,
        rawLine,
        parameters,
        tags,
        connectionHost,
        secureConnection,
        observedAtEpochMilli,
        messageTagsEnabled,
        batchEnabled,
        chatHistoryEnabled,
        pendingCapabilities,
        MultilineState.empty());
  }

  public static Ircv3InboundCommandRequest multilineCapabilityState(
      String action, String normalizedCapabilities, MultilineState state) {
    return new Ircv3InboundCommandRequest(
        "server",
        "CAP",
        "",
        List.of("*", Objects.toString(action, ""), Objects.toString(normalizedCapabilities, "")),
        Map.of(),
        "",
        false,
        0L,
        false,
        false,
        false,
        Set.of(),
        state);
  }

  public Ircv3InboundCommandRequest {
    sourceNick = Objects.toString(sourceNick, "").trim();
    command = Objects.toString(command, "").trim();
    rawLine = Objects.toString(rawLine, "").trim();
    parameters = List.copyOf(Objects.requireNonNullElse(parameters, List.of()));
    tags = Map.copyOf(Objects.requireNonNullElse(tags, Map.of()));
    connectionHost = Objects.toString(connectionHost, "").trim();
    LinkedHashSet<String> normalizedPending = new LinkedHashSet<>();
    for (String capability : Objects.requireNonNullElse(pendingCapabilities, Set.<String>of())) {
      String normalized = Objects.toString(capability, "").trim().toLowerCase(Locale.ROOT);
      if (!normalized.isEmpty()) {
        normalizedPending.add(normalized);
      }
    }
    pendingCapabilities = Set.copyOf(normalizedPending);
    multilineState = multilineState == null ? MultilineState.empty() : multilineState;
  }

  /** Portable current multiline offer and negotiated-limit state. */
  public record MultilineState(
      long finalOfferedMaxBytes,
      long finalOfferedMaxLines,
      long finalNegotiatedMaxBytes,
      long finalNegotiatedMaxLines,
      long draftOfferedMaxBytes,
      long draftOfferedMaxLines,
      long draftNegotiatedMaxBytes,
      long draftNegotiatedMaxLines) {

    public MultilineState {
      finalOfferedMaxBytes = Math.max(0L, finalOfferedMaxBytes);
      finalOfferedMaxLines = Math.max(0L, finalOfferedMaxLines);
      finalNegotiatedMaxBytes = Math.max(0L, finalNegotiatedMaxBytes);
      finalNegotiatedMaxLines = Math.max(0L, finalNegotiatedMaxLines);
      draftOfferedMaxBytes = Math.max(0L, draftOfferedMaxBytes);
      draftOfferedMaxLines = Math.max(0L, draftOfferedMaxLines);
      draftNegotiatedMaxBytes = Math.max(0L, draftNegotiatedMaxBytes);
      draftNegotiatedMaxLines = Math.max(0L, draftNegotiatedMaxLines);
    }

    public static MultilineState empty() {
      return new MultilineState(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }
  }
}
