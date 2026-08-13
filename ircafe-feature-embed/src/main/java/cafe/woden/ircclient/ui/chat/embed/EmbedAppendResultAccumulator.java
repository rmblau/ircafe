package cafe.woden.ircclient.ui.chat.embed;

import java.util.LinkedHashSet;

/** Aggregates append-loop outcomes without depending on Swing or root app types. */
public final class EmbedAppendResultAccumulator {

  private int appendedCount;
  private int nextInsertAt;
  private final LinkedHashSet<String> blockedUrls = new LinkedHashSet<>();

  private EmbedAppendResultAccumulator(int insertAt) {
    this.nextInsertAt = Math.max(0, insertAt);
  }

  public static EmbedAppendResultAccumulator startingAt(int insertAt) {
    return new EmbedAppendResultAccumulator(insertAt);
  }

  public int appendedCount() {
    return appendedCount;
  }

  public int nextInsertAt() {
    return nextInsertAt;
  }

  public boolean canAppendMore(int maxAppends) {
    return appendedCount < Math.max(0, maxAppends);
  }

  public void add(EmbedApplicationResult result) {
    if (result == null) {
      return;
    }
    if (result.appended()) {
      appendedCount++;
      nextInsertAt = result.nextInsertAt();
      return;
    }
    if (result.hasBlockedUrl()) {
      blockedUrls.add(result.blockedUrl());
    }
  }

  public EmbedAppendResult finish() {
    return EmbedAppendResult.of(appendedCount, blockedUrls);
  }
}
