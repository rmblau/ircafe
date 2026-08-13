package cafe.woden.ircclient.notify.api.text;

/** Builds deterministic sample-message snippets around plain notification rule matches. */
public final class NotificationTextMatchSnippetPlanner {
  private static final int DEFAULT_CONTEXT_CHARS = 30;

  private NotificationTextMatchSnippetPlanner() {}

  public static String snippetAround(String message, int start, int end) {
    return snippetAround(message, start, end, DEFAULT_CONTEXT_CHARS);
  }

  public static String snippetAround(String message, int start, int end, int contextChars) {
    if (message == null) return "";
    int len = message.length();
    int normalizedStart = Math.max(0, Math.min(start, len));
    int normalizedEnd = Math.max(normalizedStart, Math.min(end, len));
    int normalizedContext = Math.max(0, contextChars);

    int snippetStart = Math.max(0, normalizedStart - normalizedContext);
    int snippetEnd = Math.min(len, normalizedEnd + normalizedContext);

    String prefix = snippetStart > 0 ? "…" : "";
    String suffix = snippetEnd < len ? "…" : "";

    String before = message.substring(snippetStart, normalizedStart);
    String match = message.substring(normalizedStart, normalizedEnd);
    String after = message.substring(normalizedEnd, snippetEnd);

    return prefix
        + collapseWhitespace(before)
        + "["
        + collapseWhitespace(match)
        + "]"
        + collapseWhitespace(after)
        + suffix;
  }

  private static String collapseWhitespace(String value) {
    if (value == null || value.isEmpty()) return "";
    return value.replaceAll("\\s+", " ");
  }
}
