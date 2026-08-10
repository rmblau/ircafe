package cafe.woden.ircclient.irc.ircv3.spi;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Transport-neutral input for one runtime-rendered outbound IRCv3 command.
 *
 * <p>Use the named factories instead of interpreting unrelated fields for an operation. The class
 * intentionally remains extensible without exposing a public all-arguments constructor to plugin
 * authors.
 */
public final class Ircv3OutboundCommandRequest {

  private final String serverId;
  private final String target;
  private final String primaryValue;
  private final String secondaryValue;
  private final String payload;
  private final Instant timestamp;
  private final Instant secondaryTimestamp;
  private final int limit;
  private final String batchId;
  private final boolean finalCapability;
  private final boolean draftCapability;
  private final long maxBytes;
  private final long maxLines;
  private final long sequence;
  private final List<String> values;

  private Ircv3OutboundCommandRequest(
      String serverId,
      String target,
      String primaryValue,
      String secondaryValue,
      String payload,
      Instant timestamp,
      Instant secondaryTimestamp,
      int limit,
      String batchId,
      boolean finalCapability,
      boolean draftCapability,
      long maxBytes,
      long maxLines,
      long sequence,
      List<String> values) {
    this.serverId = text(serverId);
    this.target = text(target);
    this.primaryValue = text(primaryValue);
    this.secondaryValue = text(secondaryValue);
    this.payload = Objects.toString(payload, "");
    this.timestamp = timestamp;
    this.secondaryTimestamp = secondaryTimestamp;
    this.limit = limit;
    this.batchId = text(batchId);
    this.finalCapability = finalCapability;
    this.draftCapability = draftCapability;
    this.maxBytes = maxBytes;
    this.maxLines = maxLines;
    this.sequence = sequence;
    this.values = normalizeValues(values);
  }

  public static Ircv3OutboundCommandRequest typing(String target, String state) {
    return new Ircv3OutboundCommandRequest(
        "", target, state, "", "", null, null, 0, "", false, false, 0L, 0L, 0L, List.of());
  }

  public static Ircv3OutboundCommandRequest readMarker(String target, Instant timestamp) {
    return new Ircv3OutboundCommandRequest(
        "", target, "", "", "", timestamp, null, 0, "", false, false, 0L, 0L, 0L, List.of());
  }

  public static Ircv3OutboundCommandRequest zncPlayback(
      String target, Instant fromInclusive, Instant toInclusive) {
    return new Ircv3OutboundCommandRequest(
        "",
        target,
        "",
        "",
        "",
        fromInclusive,
        toInclusive,
        0,
        "",
        false,
        false,
        0L,
        0L,
        0L,
        List.of());
  }

  public static Ircv3OutboundCommandRequest chatHistory(
      String target, String primarySelector, String secondarySelector, int limit) {
    return chatHistory(target, primarySelector, secondarySelector, limit, null);
  }

  public static Ircv3OutboundCommandRequest chatHistory(
      String target,
      String primarySelector,
      String secondarySelector,
      int limit,
      Instant fallbackTimestamp) {
    return new Ircv3OutboundCommandRequest(
        "",
        target,
        primarySelector,
        secondarySelector,
        "",
        fallbackTimestamp,
        null,
        limit,
        "",
        false,
        false,
        0L,
        0L,
        0L,
        List.of());
  }

  public static Ircv3OutboundCommandRequest multiline(
      String serverId,
      String target,
      String command,
      String payload,
      String batchId,
      boolean finalCapability,
      boolean draftCapability,
      long maxBytes,
      long maxLines) {
    return new Ircv3OutboundCommandRequest(
        serverId,
        target,
        command,
        "",
        payload,
        null,
        null,
        0,
        batchId,
        finalCapability,
        draftCapability,
        maxBytes,
        maxLines,
        0L,
        List.of());
  }

  public static Ircv3OutboundCommandRequest labeledResponse(
      String serverId, String rawLine, long sequence) {
    return new Ircv3OutboundCommandRequest(
        serverId, "", "", "", rawLine, null, null, 0, "", false, false, 0L, 0L, sequence,
        List.of());
  }

  public static Ircv3OutboundCommandRequest monitor(List<String> nicks, int limit) {
    return new Ircv3OutboundCommandRequest(
        "", "", "", "", "", null, null, limit, "", false, false, 0L, 0L, 0L, nicks);
  }

  public String serverId() {
    return serverId;
  }

  public String target() {
    return target;
  }

  public String primaryValue() {
    return primaryValue;
  }

  public String secondaryValue() {
    return secondaryValue;
  }

  public String payload() {
    return payload;
  }

  public Instant timestamp() {
    return timestamp;
  }

  public Instant secondaryTimestamp() {
    return secondaryTimestamp;
  }

  public int limit() {
    return limit;
  }

  public String batchId() {
    return batchId;
  }

  public boolean finalCapability() {
    return finalCapability;
  }

  public boolean draftCapability() {
    return draftCapability;
  }

  public long maxBytes() {
    return maxBytes;
  }

  public long maxLines() {
    return maxLines;
  }

  public long sequence() {
    return sequence;
  }

  public List<String> values() {
    return values;
  }

  private static String text(String value) {
    return Objects.toString(value, "").trim();
  }

  private static List<String> normalizeValues(List<String> rawValues) {
    if (rawValues == null || rawValues.isEmpty()) {
      return List.of();
    }
    return rawValues.stream()
        .map(Ircv3OutboundCommandRequest::text)
        .filter(value -> !value.isEmpty())
        .toList();
  }
}
