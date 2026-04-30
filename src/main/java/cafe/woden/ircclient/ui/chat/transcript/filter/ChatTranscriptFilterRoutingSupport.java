package cafe.woden.ircclient.ui.chat.transcript.filter;

import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptLineMetaSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.filter.FilterContext;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ChatTranscriptFilterRoutingSupport {

  private static final FilterEngine.Effective DEFAULT_EFFECTIVE =
      new FilterEngine.Effective(false, true, true, 3, 250, 12, 10, true);

  @FunctionalInterface
  public interface HiddenAppendHandler {
    void append(TargetRef ref, String previewText, LineMeta hiddenMeta, FilterEngine.Match match);
  }

  @FunctionalInterface
  public interface HiddenInsertHandler {
    int insert(
        TargetRef ref,
        int insertAt,
        String previewText,
        LineMeta hiddenMeta,
        FilterEngine.Match match);
  }

  @FunctionalInterface
  public interface FilteredInsertRunEndHandler {
    void end(TargetRef ref);
  }

  @FunctionalInterface
  public interface PresenceRunBreakHandler {
    void breakRun(TargetRef ref);
  }

  public record HistoryDecision(boolean handled, int nextInsertAt) {
    static HistoryDecision unhandled(int nextInsertAt) {
      return new HistoryDecision(false, nextInsertAt);
    }

    static HistoryDecision handled(int nextInsertAt) {
      return new HistoryDecision(true, nextInsertAt);
    }
  }

  public record VisibleAppend(LineMeta meta, FilterEngine.Match match) {}

  private final FilterEngine filterEngine;
  private final HiddenAppendHandler hiddenAppendHandler;
  private final HiddenInsertHandler hiddenInsertHandler;
  private final FilteredInsertRunEndHandler filteredInsertRunEndHandler;
  private final PresenceRunBreakHandler presenceRunBreakHandler;

  public ChatTranscriptFilterRoutingSupport(
      FilterEngine filterEngine,
      HiddenAppendHandler hiddenAppendHandler,
      HiddenInsertHandler hiddenInsertHandler,
      FilteredInsertRunEndHandler filteredInsertRunEndHandler,
      PresenceRunBreakHandler presenceRunBreakHandler) {
    this.filterEngine = filterEngine;
    this.hiddenAppendHandler = Objects.requireNonNull(hiddenAppendHandler, "hiddenAppendHandler");
    this.hiddenInsertHandler = Objects.requireNonNull(hiddenInsertHandler, "hiddenInsertHandler");
    this.filteredInsertRunEndHandler =
        Objects.requireNonNull(filteredInsertRunEndHandler, "filteredInsertRunEndHandler");
    this.presenceRunBreakHandler =
        Objects.requireNonNull(presenceRunBreakHandler, "presenceRunBreakHandler");
  }

  public FilterEngine.Effective effectiveFor(TargetRef ref) {
    if (filterEngine == null) {
      return DEFAULT_EFFECTIVE;
    }
    try {
      FilterEngine.Effective effective = filterEngine.effectiveFor(ref);
      return effective != null ? effective : DEFAULT_EFFECTIVE;
    } catch (Exception ignored) {
      return DEFAULT_EFFECTIVE;
    }
  }

  public FilterEngine.Match firstMatch(
      TargetRef ref,
      LogKind kind,
      LogDirection direction,
      String fromNick,
      String text,
      Set<String> tags) {
    if (ref == null || filterEngine == null) {
      return null;
    }
    try {
      return filterEngine.firstMatch(
          new FilterContext(ref, kind, direction, fromNick, text, tags != null ? tags : Set.of()));
    } catch (Exception ignored) {
      return null;
    }
  }

  public FilterEngine.Match matchFor(
      TargetRef ref, LineMeta meta, String fallbackFromNick, String text) {
    if (meta == null) {
      return null;
    }
    String filterFrom =
        Objects.toString(meta.fromNick(), "").isBlank() ? fallbackFromNick : meta.fromNick();
    return firstMatch(ref, meta.kind(), meta.direction(), filterFrom, text, meta.tags());
  }

  public FilterEngine.Match hideMatch(
      TargetRef ref,
      LogKind kind,
      LogDirection direction,
      String fromNick,
      String text,
      Set<String> tags) {
    FilterEngine.Match match = firstMatch(ref, kind, direction, fromNick, text, tags);
    return (match != null && match.isHide()) ? match : null;
  }

  public LineMeta prepareVisibleTextAppend(
      TargetRef ref,
      LogKind kind,
      LogDirection direction,
      String fromNick,
      String text,
      long epochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref,
            kind,
            direction,
            fromNick,
            epochMs,
            null,
            messageId,
            ircv3Tags != null ? ircv3Tags : Map.of());
    FilterEngine.Match match = hideMatch(ref, kind, direction, fromNick, text, meta.tags());
    if (handleHiddenTextAppend(ref, fromNick, text, meta, match)) {
      return null;
    }
    presenceRunBreakHandler.breakRun(ref);
    return meta;
  }

  public VisibleAppend prepareVisibleTextAppendWithMatch(
      TargetRef ref,
      LogKind kind,
      LogDirection direction,
      String fromNick,
      String text,
      long epochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref,
            kind,
            direction,
            fromNick,
            epochMs,
            null,
            messageId,
            ircv3Tags != null ? ircv3Tags : Map.of());
    FilterEngine.Match match = firstMatch(ref, kind, direction, fromNick, text, meta.tags());
    if (handleHiddenTextAppend(ref, fromNick, text, meta, match)) {
      return null;
    }
    presenceRunBreakHandler.breakRun(ref);
    return new VisibleAppend(meta, match);
  }

  public VisibleAppend prepareVisibleActionAppend(
      TargetRef ref,
      LogDirection direction,
      String fromNick,
      String action,
      long epochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref,
            LogKind.ACTION,
            direction,
            fromNick,
            epochMs,
            null,
            messageId,
            ircv3Tags != null ? ircv3Tags : Map.of());
    FilterEngine.Match match =
        firstMatch(ref, LogKind.ACTION, direction, fromNick, action, meta.tags());
    if (handleHiddenActionAppend(ref, fromNick, action, meta, match)) {
      return null;
    }
    presenceRunBreakHandler.breakRun(ref);
    return new VisibleAppend(meta, match);
  }

  public boolean handleHiddenAppend(
      TargetRef ref, String previewText, LineMeta hiddenMeta, FilterEngine.Match match) {
    if (ref == null || match == null || !match.isHide()) {
      return false;
    }
    hiddenAppendHandler.append(ref, previewText, hiddenMeta, match);
    return true;
  }

  public boolean handleHiddenTextAppend(
      TargetRef ref, String fromNick, String text, LineMeta hiddenMeta, FilterEngine.Match match) {
    return handleHiddenAppend(
        ref,
        ChatTranscriptFilteredPreviewSupport.previewChatLine(fromNick, text),
        hiddenMeta,
        match);
  }

  public boolean handleHiddenActionAppend(
      TargetRef ref,
      String fromNick,
      String action,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    return handleHiddenAppend(
        ref,
        ChatTranscriptFilteredPreviewSupport.previewActionLine(fromNick, action),
        hiddenMeta,
        match);
  }

  public HistoryDecision handleHiddenHistoryInsert(
      TargetRef ref,
      int insertAt,
      String previewText,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    if (ref == null || match == null || !match.isHide()) {
      return HistoryDecision.unhandled(Math.max(0, insertAt));
    }

    FilterEngine.Effective effective = effectiveFor(ref);
    if (effective.historyPlaceholdersEnabled()) {
      return HistoryDecision.handled(
          hiddenInsertHandler.insert(ref, insertAt, previewText, hiddenMeta, match));
    }

    filteredInsertRunEndHandler.end(ref);
    return HistoryDecision.handled(Math.max(0, insertAt));
  }

  public HistoryDecision handleHiddenTextHistoryInsert(
      TargetRef ref,
      int insertAt,
      String fromNick,
      String text,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    return handleHiddenHistoryInsert(
        ref,
        insertAt,
        ChatTranscriptFilteredPreviewSupport.previewChatLine(fromNick, text),
        hiddenMeta,
        match);
  }

  public HistoryDecision handleHiddenActionHistoryInsert(
      TargetRef ref,
      int insertAt,
      String fromNick,
      String action,
      LineMeta hiddenMeta,
      FilterEngine.Match match) {
    return handleHiddenHistoryInsert(
        ref,
        insertAt,
        ChatTranscriptFilteredPreviewSupport.previewActionLine(fromNick, action),
        hiddenMeta,
        match);
  }
}
