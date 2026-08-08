package cafe.woden.ircclient.ui.chat.embed;

/** Root-independent text helpers shared by feature-owned preview parsers. */
final class LinkPreviewTextSupport {

  private LinkPreviewTextSupport() {}

  static String trimToSentence(String text, int maxChars) {
    if (text == null) return null;
    String trimmed = text.strip();
    if (trimmed.isEmpty()) return trimmed;
    if (trimmed.length() <= maxChars) return trimmed;

    int hard = Math.min(Math.max(0, maxChars), trimmed.length());
    String prefix = trimmed.substring(0, hard);
    String snapped = snapToNiceBoundary(prefix);
    if (snapped.isBlank()) snapped = prefix.strip();
    return snapped + " …";
  }

  private static String snapToNiceBoundary(String text) {
    if (text == null) return null;
    String trimmed = text.strip();
    if (trimmed.isEmpty()) return trimmed;

    int hard = trimmed.length();
    int lookBack = Math.max(0, hard - 220);
    for (int i = hard - 1; i >= lookBack; i--) {
      char ch = trimmed.charAt(i);
      if (ch == '.' || ch == '!' || ch == '?') {
        char previous = i > 0 ? trimmed.charAt(i - 1) : '\0';
        char next = (i + 1) < trimmed.length() ? trimmed.charAt(i + 1) : '\0';
        if (ch == '.' && Character.isUpperCase(previous) && Character.isUpperCase(next)) {
          continue;
        }
        return trimmed.substring(0, i + 1).strip();
      }
    }
    int whitespace = trimmed.lastIndexOf(' ', hard - 1);
    if (whitespace >= Math.max(40, hard - 120)) {
      return trimmed.substring(0, whitespace).strip();
    }
    return trimmed;
  }
}
