package cafe.woden.ircclient.irc.ircv3;

import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandOperation;
import cafe.woden.ircclient.irc.ircv3.spi.Ircv3OutboundCommandRequest;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Validates runtime-provider CHATHISTORY plans before application or backend use. */
@Component
@InfrastructureLayer
public final class Ircv3ChatHistoryRuntimeSupport {

  private static final int MAX_RAW_LINE_LENGTH = 4096;
  private static final int MAX_LIMIT = 200;

  private final Ircv3OutboundCommandRuntimeCatalog catalog;

  @Autowired
  public Ircv3ChatHistoryRuntimeSupport(Ircv3OutboundCommandRuntimeCatalog catalog) {
    this.catalog = Objects.requireNonNull(catalog, "catalog");
  }

  public Plan before(String target, String selector, int limit, Instant fallbackTimestamp) {
    return plan(
        Ircv3OutboundCommandOperation.CHAT_HISTORY_BEFORE,
        target,
        selector,
        "",
        limit,
        fallbackTimestamp);
  }

  public Plan latest(String target, String selector, int limit) {
    return plan(
        Ircv3OutboundCommandOperation.CHAT_HISTORY_LATEST, target, selector, "", limit, null);
  }

  public Plan between(String target, String startSelector, String endSelector, int limit) {
    return plan(
        Ircv3OutboundCommandOperation.CHAT_HISTORY_BETWEEN,
        target,
        startSelector,
        endSelector,
        limit,
        null);
  }

  public Plan around(String target, String selector, int limit) {
    return plan(
        Ircv3OutboundCommandOperation.CHAT_HISTORY_AROUND, target, selector, "", limit, null);
  }

  public boolean available() {
    return catalog.supports(Ircv3OutboundCommandOperation.CHAT_HISTORY_BEFORE)
        && catalog.supports(Ircv3OutboundCommandOperation.CHAT_HISTORY_LATEST)
        && catalog.supports(Ircv3OutboundCommandOperation.CHAT_HISTORY_BETWEEN)
        && catalog.supports(Ircv3OutboundCommandOperation.CHAT_HISTORY_AROUND);
  }

  private Plan plan(
      Ircv3OutboundCommandOperation operation,
      String target,
      String primarySelector,
      String secondarySelector,
      int limit,
      Instant fallbackTimestamp) {
    String requestedTarget = requireToken(target, "target");
    validateRequestedSelectors(operation, primarySelector, secondarySelector);
    String rawLine =
        catalog.buildSingle(
            operation,
            Ircv3OutboundCommandRequest.chatHistory(
                requestedTarget, primarySelector, secondarySelector, limit, fallbackTimestamp));
    if (rawLine.isEmpty()) {
      throw new IllegalStateException("CHATHISTORY runtime provider did not render " + operation);
    }
    return parse(operation, requestedTarget, rawLine);
  }

  private static Plan parse(
      Ircv3OutboundCommandOperation operation, String requestedTarget, String rawLine) {
    String line = Objects.toString(rawLine, "").trim();
    if (!validRawLine(line)) {
      throw new IllegalStateException("CHATHISTORY runtime provider returned an unsafe raw line");
    }

    String[] tokens = line.split("\\s+");
    int expectedTokens = operation == Ircv3OutboundCommandOperation.CHAT_HISTORY_BETWEEN ? 6 : 5;
    if (tokens.length != expectedTokens
        || !"CHATHISTORY".equalsIgnoreCase(tokens[0])
        || !expectedMode(operation).equalsIgnoreCase(tokens[1])) {
      throw new IllegalStateException(
          "CHATHISTORY runtime provider returned an invalid " + operation + " command");
    }

    String renderedTarget = requireToken(tokens[2], "provider target");
    if (!requestedTarget.equals(renderedTarget)) {
      throw new IllegalStateException("CHATHISTORY runtime provider changed the requested target");
    }

    String primary = requireSelector(tokens[3], operationAllowsWildcard(operation));
    String secondary = "";
    int limitIndex = 4;
    if (operation == Ircv3OutboundCommandOperation.CHAT_HISTORY_BETWEEN) {
      secondary = requireSelector(tokens[4], true);
      limitIndex = 5;
    }
    int renderedLimit = parseLimit(tokens[limitIndex]);
    return new Plan(operation, line, renderedTarget, primary, secondary, renderedLimit);
  }

  private static void validateRequestedSelectors(
      Ircv3OutboundCommandOperation operation, String primarySelector, String secondarySelector) {
    switch (operation) {
      case CHAT_HISTORY_BEFORE -> validateRequestedSelector(primarySelector, true, false);
      case CHAT_HISTORY_LATEST -> validateRequestedSelector(primarySelector, true, true);
      case CHAT_HISTORY_AROUND -> validateRequestedSelector(primarySelector, false, false);
      case CHAT_HISTORY_BETWEEN -> {
        validateRequestedSelector(primarySelector, false, true);
        validateRequestedSelector(secondarySelector, false, true);
      }
      default -> throw new IllegalArgumentException("Not a CHATHISTORY operation: " + operation);
    }
  }

  private static void validateRequestedSelector(
      String raw, boolean blankAllowed, boolean wildcardAllowed) {
    String selector = Objects.toString(raw, "").trim();
    if (selector.isEmpty()) {
      if (blankAllowed) {
        return;
      }
      throw new IllegalArgumentException("selector is blank");
    }
    if (wildcardAllowed && "*".equals(selector)) {
      return;
    }

    selector = requireToken(selector, "selector");
    int equals = selector.indexOf('=');
    if (equals <= 0 || equals == selector.length() - 1) {
      throw new IllegalArgumentException("selector must be msgid=... or timestamp=...");
    }
    String key = selector.substring(0, equals).toLowerCase(Locale.ROOT);
    if (!"msgid".equals(key) && !"timestamp".equals(key)) {
      throw new IllegalArgumentException("unsupported CHATHISTORY selector");
    }
  }

  private static String expectedMode(Ircv3OutboundCommandOperation operation) {
    return switch (operation) {
      case CHAT_HISTORY_BEFORE -> "BEFORE";
      case CHAT_HISTORY_LATEST -> "LATEST";
      case CHAT_HISTORY_BETWEEN -> "BETWEEN";
      case CHAT_HISTORY_AROUND -> "AROUND";
      default -> throw new IllegalArgumentException("Not a CHATHISTORY operation: " + operation);
    };
  }

  private static boolean operationAllowsWildcard(Ircv3OutboundCommandOperation operation) {
    return operation == Ircv3OutboundCommandOperation.CHAT_HISTORY_LATEST
        || operation == Ircv3OutboundCommandOperation.CHAT_HISTORY_BETWEEN;
  }

  private static String requireToken(String raw, String label) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty()) {
      throw new IllegalArgumentException(label + " is blank");
    }
    if (containsUnsafe(value) || value.chars().anyMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException(label + " contains whitespace or controls");
    }
    return value;
  }

  private static String requireSelector(String raw, boolean wildcardAllowed) {
    String selector = requireToken(raw, "selector");
    if (wildcardAllowed && "*".equals(selector)) {
      return selector;
    }
    int equals = selector.indexOf('=');
    if (equals <= 0 || equals == selector.length() - 1) {
      throw new IllegalStateException("CHATHISTORY runtime provider returned an invalid selector");
    }
    String key = selector.substring(0, equals).toLowerCase(Locale.ROOT);
    if (!"msgid".equals(key) && !"timestamp".equals(key)) {
      throw new IllegalStateException(
          "CHATHISTORY runtime provider returned an unsupported selector");
    }
    return key + selector.substring(equals);
  }

  private static int parseLimit(String raw) {
    try {
      int parsed = Integer.parseInt(raw);
      if (parsed <= 0 || parsed > MAX_LIMIT) {
        throw new IllegalStateException(
            "CHATHISTORY runtime provider returned an out-of-range limit");
      }
      return parsed;
    } catch (NumberFormatException error) {
      throw new IllegalStateException(
          "CHATHISTORY runtime provider returned an invalid limit", error);
    }
  }

  private static boolean validRawLine(String rawLine) {
    return !rawLine.isBlank()
        && rawLine.length() <= MAX_RAW_LINE_LENGTH
        && !containsUnsafe(rawLine);
  }

  private static boolean containsUnsafe(String value) {
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (ch == '\r' || ch == '\n' || ch == '\0') {
        return true;
      }
    }
    return false;
  }

  public record Plan(
      Ircv3OutboundCommandOperation operation,
      String rawLine,
      String target,
      String primarySelector,
      String secondarySelector,
      int limit) {

    public Plan {
      Objects.requireNonNull(operation, "operation");
      Objects.requireNonNull(rawLine, "rawLine");
      Objects.requireNonNull(target, "target");
      Objects.requireNonNull(primarySelector, "primarySelector");
      Objects.requireNonNull(secondarySelector, "secondarySelector");
    }

    public String selectorSummary() {
      return secondarySelector.isEmpty()
          ? primarySelector
          : primarySelector + " .. " + secondarySelector;
    }
  }
}
