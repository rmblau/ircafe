package cafe.woden.ircclient.ui.chat.embed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/** Feature-safe aggregate result for one embed append pass. */
public record EmbedAppendResult(int appendedCount, List<String> blockedUrls) {

  public EmbedAppendResult {
    appendedCount = Math.max(0, appendedCount);
    blockedUrls = normalize(blockedUrls);
  }

  public static EmbedAppendResult empty() {
    return new EmbedAppendResult(0, List.of());
  }

  public static EmbedAppendResult of(int appendedCount, Collection<String> blockedUrls) {
    return new EmbedAppendResult(
        appendedCount, blockedUrls == null ? List.of() : new ArrayList<>(blockedUrls));
  }

  public boolean hasBlockedUrls() {
    return !blockedUrls.isEmpty();
  }

  private static List<String> normalize(Collection<String> urls) {
    if (urls == null || urls.isEmpty()) {
      return List.of();
    }
    LinkedHashSet<String> normalized = new LinkedHashSet<>();
    for (String url : urls) {
      String value = Objects.toString(url, "").trim();
      if (!value.isBlank()) {
        normalized.add(value);
      }
    }
    return normalized.isEmpty() ? List.of() : List.copyOf(normalized);
  }
}
