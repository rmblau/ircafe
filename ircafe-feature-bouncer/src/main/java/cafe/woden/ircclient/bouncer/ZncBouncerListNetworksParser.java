package cafe.woden.ircclient.bouncer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Feature-owned parser for ZNC {@code *status ListNetworks} output. */
public final class ZncBouncerListNetworksParser {

  /** Parsed network table row. */
  public record ParsedRow(String name, Boolean onIrc) {}

  /** Parse one tolerant table or tokenized network row. */
  public ParsedRow parseRow(String messageText) {
    if (messageText == null) return null;
    String source = messageText.trim();
    if (source.isEmpty()) return null;

    if ((source.startsWith("+") && source.endsWith("+") && source.indexOf('-') >= 0)
        || source.startsWith("--")
        || source.toLowerCase(Locale.ROOT).contains("listnetworks")) {
      return null;
    }

    if (source.startsWith("|") && source.contains("|")) {
      String[] parts = source.split("\\|");
      List<String> cells = new ArrayList<>();
      for (String part : parts) {
        if (part == null) continue;
        String cell = part.trim();
        if (!cell.isEmpty()) cells.add(cell);
      }
      if (cells.isEmpty()) return null;

      String first = cells.getFirst();
      if (first.equalsIgnoreCase("network") || first.equalsIgnoreCase("name")) return null;

      String name = first.trim();
      if (name.isEmpty()) return null;
      Boolean onIrc = cells.size() >= 2 ? parseYesNo(cells.get(1)) : null;
      return new ParsedRow(name, onIrc);
    }

    String[] tokens = source.split("\\s+");
    if (tokens.length >= 1) {
      String name = tokens[0].trim();
      if (!name.isEmpty() && !name.startsWith("[") && !name.startsWith("(")) {
        Boolean onIrc = tokens.length >= 2 ? parseYesNo(tokens[1]) : null;
        return new ParsedRow(name, onIrc);
      }
    }

    return null;
  }

  /** Return whether the message is a tolerant ListNetworks completion marker. */
  public boolean isDoneLine(String messageText) {
    if (messageText == null) return false;
    String source = messageText.trim().toLowerCase(Locale.ROOT);
    if (source.isEmpty()) return false;
    return source.contains("end") && source.contains("list")
        || source.contains("done") && source.contains("list")
        || source.contains("use /znc") && source.contains("listnetworks")
        || source.contains("listnetworks") && source.contains("complete");
  }

  private Boolean parseYesNo(String value) {
    if (value == null) return null;
    String source = value.trim().toLowerCase(Locale.ROOT);
    if (source.isEmpty()) return null;
    if (source.equals("yes")
        || source.equals("y")
        || source.equals("on")
        || source.equals("true")
        || source.equals("1")) return Boolean.TRUE;
    if (source.equals("no")
        || source.equals("n")
        || source.equals("off")
        || source.equals("false")
        || source.equals("0")) return Boolean.FALSE;
    if (source.contains("connect") && !source.contains("dis")) return Boolean.TRUE;
    if (source.contains("disconnect")) return Boolean.FALSE;
    return null;
  }
}
