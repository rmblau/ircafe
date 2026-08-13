package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Parses IRCv3 BATCH lifecycle control lines without depending on an IRC client library. */
public final class Ircv3HistoryBatchControlParser {

  private Ircv3HistoryBatchControlParser() {}

  public sealed interface Control permits Start, End {}

  public record Start(String batchId, String type, String target) implements Control {
    public Start {
      batchId = Objects.toString(batchId, "");
      type = Objects.toString(type, "");
      target = Objects.toString(target, "");
    }

    public boolean isChatHistory() {
      return type.toLowerCase(Locale.ROOT).contains("chathistory");
    }
  }

  public record End(String batchId) implements Control {
    public End {
      batchId = Objects.toString(batchId, "");
    }
  }

  public record Result(boolean batchCommand, Control control) {
    public static Result notBatch() {
      return new Result(false, null);
    }

    public static Result ignoredBatch() {
      return new Result(true, null);
    }
  }

  public static Result parse(String rawLine) {
    String line = Objects.toString(rawLine, "").trim();
    if (line.isEmpty()) return Result.notBatch();

    String trailing = "";
    int trailingIndex = line.indexOf(" :");
    if (trailingIndex >= 0) {
      trailing = line.substring(trailingIndex + 2).trim();
      line = line.substring(0, trailingIndex).trim();
    }

    List<String> tokens = new ArrayList<>(List.of(line.split("\\s+")));
    int index = 0;
    if (index < tokens.size() && tokens.get(index).startsWith("@")) index++;
    if (index < tokens.size() && tokens.get(index).startsWith(":")) index++;
    if (index >= tokens.size() || !"BATCH".equalsIgnoreCase(tokens.get(index))) {
      return Result.notBatch();
    }
    index++;
    if (index >= tokens.size()) return Result.ignoredBatch();

    String marker = tokens.get(index);
    if (marker == null || marker.length() < 2) return Result.ignoredBatch();
    if (marker.startsWith("+")) {
      String batchId = marker.substring(1);
      String type = index + 1 < tokens.size() ? tokens.get(index + 1) : "";
      String target = index + 2 < tokens.size() ? tokens.get(index + 2) : trailing;
      if (batchId.isBlank()) return Result.ignoredBatch();
      return new Result(true, new Start(batchId, type, stripLeadingColon(target)));
    }
    if (marker.startsWith("-")) {
      String batchId = marker.substring(1);
      if (batchId.isBlank()) return Result.ignoredBatch();
      return new Result(true, new End(batchId));
    }
    return Result.ignoredBatch();
  }

  private static String stripLeadingColon(String value) {
    String normalized = Objects.toString(value, "").trim();
    if (normalized.startsWith(":")) normalized = normalized.substring(1).trim();
    return normalized;
  }
}
