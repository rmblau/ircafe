package cafe.woden.ircclient.irc.ircv3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Transport-neutral parsing and raw-line planning for the {@code /monitor} command family. */
public final class Ircv3MonitorCommandPlanner {
  public static final int DEFAULT_CHUNK_SIZE = 100;

  private Ircv3MonitorCommandPlanner() {}

  public sealed interface Action
      permits Usage, ListRequested, StatusRequested, ClearRequested, Modify {}

  public record Usage() implements Action {}

  public record ListRequested() implements Action {}

  public record StatusRequested() implements Action {}

  public record ClearRequested() implements Action {}

  public record Modify(char sigil, String nickSpec) implements Action {
    public Modify {
      if (sigil != '+' && sigil != '-') {
        throw new IllegalArgumentException("MONITOR modification sigil must be + or -");
      }
      nickSpec = Objects.toString(nickSpec, "").trim();
    }
  }

  public static Action parse(String rawArgs) {
    String raw = Objects.toString(rawArgs, "").trim();
    if (raw.isEmpty()) return new Usage();

    String[] headTail = raw.split("\\s+", 2);
    String operation = headTail[0].trim();
    String remainder = headTail.length > 1 ? headTail[1].trim() : "";

    char sigil = leadingSigil(operation);
    if (sigil != 0) {
      String nickSpec = operation.length() > 1 ? operation.substring(1) : "";
      if (!remainder.isEmpty()) {
        nickSpec = nickSpec.isEmpty() ? remainder : nickSpec + " " + remainder;
      }
      return nickSpec.isBlank() ? new Usage() : new Modify(sigil, nickSpec);
    }

    return switch (operation.toLowerCase(Locale.ROOT)) {
      case "list", "l" -> new ListRequested();
      case "status", "s" -> new StatusRequested();
      case "clear", "c" -> new ClearRequested();
      case "help" -> new Usage();
      default -> new Modify('+', raw);
    };
  }

  public static String simpleRawLine(Action action) {
    if (action instanceof ListRequested) return "MONITOR L";
    if (action instanceof StatusRequested) return "MONITOR S";
    if (action instanceof ClearRequested) return "MONITOR C";
    return "";
  }

  public static List<String> modificationRawLines(
      char sigil, List<String> rawNicks, int negotiatedLimit) {
    if (sigil != '+' && sigil != '-') return List.of();
    List<String> nicks = normalizeNicks(rawNicks);
    if (nicks.isEmpty()) return List.of();

    int chunkSize = negotiatedLimit > 0 ? negotiatedLimit : DEFAULT_CHUNK_SIZE;
    ArrayList<String> lines = new ArrayList<>((nicks.size() + chunkSize - 1) / chunkSize);
    for (int start = 0; start < nicks.size(); start += chunkSize) {
      int end = Math.min(start + chunkSize, nicks.size());
      lines.add("MONITOR " + sigil + String.join(",", nicks.subList(start, end)));
    }
    return List.copyOf(lines);
  }

  private static List<String> normalizeNicks(List<String> rawNicks) {
    if (rawNicks == null || rawNicks.isEmpty()) return List.of();
    ArrayList<String> out = new ArrayList<>(rawNicks.size());
    for (String rawNick : rawNicks) {
      String nick = Objects.toString(rawNick, "").trim();
      if (!nick.isEmpty()) out.add(nick);
    }
    return List.copyOf(out);
  }

  private static char leadingSigil(String rawOperation) {
    String operation = Objects.toString(rawOperation, "").trim();
    if (operation.isEmpty()) return 0;
    char first = operation.charAt(0);
    return first == '+' || first == '-' ? first : 0;
  }
}
