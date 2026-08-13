package cafe.woden.ircclient.ui.chat.embed;

import java.util.Objects;

/** Feature-safe outcome for applying an embed after root-owned Swing work finishes. */
public record EmbedApplicationResult(boolean appended, int nextInsertAt, String blockedUrl) {

  public EmbedApplicationResult {
    nextInsertAt = Math.max(0, nextInsertAt);
    blockedUrl = Objects.toString(blockedUrl, "").trim();
  }

  public static EmbedApplicationResult appended(int nextInsertAt) {
    return new EmbedApplicationResult(true, nextInsertAt, "");
  }

  public static EmbedApplicationResult skipped(int insertAt) {
    return new EmbedApplicationResult(false, insertAt, "");
  }

  public static EmbedApplicationResult blocked(int insertAt, String url) {
    return new EmbedApplicationResult(false, insertAt, url);
  }

  public boolean hasBlockedUrl() {
    return !blockedUrl.isBlank();
  }
}
