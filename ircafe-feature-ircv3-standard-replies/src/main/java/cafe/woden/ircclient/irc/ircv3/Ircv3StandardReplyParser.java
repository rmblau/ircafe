package cafe.woden.ircclient.irc.ircv3;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Parses FAIL, WARN, and NOTE command parameters into a transport-independent value. */
public final class Ircv3StandardReplyParser {

  private Ircv3StandardReplyParser() {}

  public enum Kind {
    FAIL,
    WARN,
    NOTE
  }

  public record StandardReply(
      Kind kind, String command, String code, String context, String description) {
    public StandardReply {
      kind = Objects.requireNonNull(kind, "kind");
      command = Objects.toString(command, "").trim();
      code = Objects.toString(code, "").trim();
      context = Objects.toString(context, "").trim();
      description = Objects.toString(description, "").trim();
    }
  }

  public static Optional<StandardReply> parse(String command, List<String> params) {
    Kind kind = kind(command);
    if (kind == null) return Optional.empty();

    String replyCommand = paramAt(params, 0);
    String code = paramAt(params, 1);
    String context = "";
    String description = "";
    if (params != null && params.size() > 2) {
      int trailingIndex = -1;
      for (int i = 2; i < params.size(); i++) {
        if (Objects.toString(params.get(i), "").startsWith(":")) {
          trailingIndex = i;
          break;
        }
      }
      if (trailingIndex < 0) trailingIndex = params.size() - 1;
      description = stripLeadingColon(params.get(trailingIndex));
      if (trailingIndex > 2) context = joinParams(params, 2, trailingIndex);
    }
    return Optional.of(new StandardReply(kind, replyCommand, code, context, description));
  }

  private static Kind kind(String command) {
    if (command == null || command.isBlank()) return null;
    return switch (command.trim().toUpperCase(Locale.ROOT)) {
      case "FAIL" -> Kind.FAIL;
      case "WARN" -> Kind.WARN;
      case "NOTE" -> Kind.NOTE;
      default -> null;
    };
  }

  private static String paramAt(List<String> params, int index) {
    if (params == null || index < 0 || index >= params.size()) return "";
    return stripLeadingColon(params.get(index));
  }

  private static String joinParams(List<String> params, int fromInclusive, int toExclusive) {
    StringBuilder out = new StringBuilder();
    for (int i = Math.max(0, fromInclusive); i < Math.min(params.size(), toExclusive); i++) {
      String part = stripLeadingColon(params.get(i));
      if (part.isBlank()) continue;
      if (!out.isEmpty()) out.append(' ');
      out.append(part);
    }
    return out.toString().trim();
  }

  private static String stripLeadingColon(String raw) {
    String value = Objects.toString(raw, "").trim();
    if (value.startsWith(":")) value = value.substring(1).trim();
    return value;
  }
}
