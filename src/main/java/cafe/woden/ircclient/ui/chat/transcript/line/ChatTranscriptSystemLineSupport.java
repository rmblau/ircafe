package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.ui.filter.FilterEngine;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

final class ChatTranscriptSystemLineSupport {

  @FunctionalInterface
  interface EnsureTargetExistsHandler {
    void ensure(TargetRef ref);
  }

  @FunctionalInterface
  interface EpochNoteHandler {
    void note(TargetRef ref, Long epochMs);
  }

  @FunctionalInterface
  interface AppendLineHandler {
    void append(
        TargetRef ref,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        boolean allowEmbeds,
        LineMeta meta);
  }

  @FunctionalInterface
  interface InsertLineHandler {
    int insert(
        TargetRef ref,
        int insertAt,
        String from,
        String text,
        AttributeSet fromStyle,
        AttributeSet msgStyle,
        LineMeta meta);
  }

  @FunctionalInterface
  interface ReplyContextAppender {
    void append(TargetRef ref, String from, String replyToMsgId, long tsEpochMs);
  }

  @FunctionalInterface
  interface DocumentLookup {
    StyledDocument get(TargetRef ref);
  }

  @FunctionalInterface
  interface StateLookup {
    ChatTranscriptState get(TargetRef ref);
  }

  @FunctionalInterface
  interface TimeSource {
    long now();
  }

  record LineStyles(AttributeSet fromStyle, AttributeSet messageStyle) {
    LineStyles {
      Objects.requireNonNull(fromStyle, "fromStyle");
      Objects.requireNonNull(messageStyle, "messageStyle");
    }
  }

  @FunctionalInterface
  interface LineStylesResolver {
    LineStyles resolve(TargetRef ref);
  }

  private final ChatTranscriptFilterRoutingSupport filterRoutingSupport;
  private final EnsureTargetExistsHandler ensureTargetExists;
  private final EpochNoteHandler noteEpochMs;
  private final AppendLineHandler appendLine;
  private final InsertLineHandler insertLine;
  private final ReplyContextAppender appendReplyContext;
  private final DocumentLookup documentLookup;
  private final StateLookup stateLookup;
  private final ChatTranscriptReactionSummarySupport reactionSummarySupport;
  private final LineStylesResolver noticeStyles;
  private final LineStylesResolver statusStyles;
  private final LineStylesResolver errorStyles;
  private final TimeSource timeSource;

  ChatTranscriptSystemLineSupport(
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      EnsureTargetExistsHandler ensureTargetExists,
      EpochNoteHandler noteEpochMs,
      AppendLineHandler appendLine,
      InsertLineHandler insertLine,
      ReplyContextAppender appendReplyContext,
      DocumentLookup documentLookup,
      StateLookup stateLookup,
      ChatTranscriptReactionSummarySupport reactionSummarySupport,
      LineStylesResolver noticeStyles,
      LineStylesResolver statusStyles,
      LineStylesResolver errorStyles,
      TimeSource timeSource) {
    this.filterRoutingSupport = Objects.requireNonNull(filterRoutingSupport, "filterRoutingSupport");
    this.ensureTargetExists = Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
    this.noteEpochMs = Objects.requireNonNull(noteEpochMs, "noteEpochMs");
    this.appendLine = Objects.requireNonNull(appendLine, "appendLine");
    this.insertLine = Objects.requireNonNull(insertLine, "insertLine");
    this.appendReplyContext = Objects.requireNonNull(appendReplyContext, "appendReplyContext");
    this.documentLookup = Objects.requireNonNull(documentLookup, "documentLookup");
    this.stateLookup = Objects.requireNonNull(stateLookup, "stateLookup");
    this.reactionSummarySupport =
        Objects.requireNonNull(reactionSummarySupport, "reactionSummarySupport");
    this.noticeStyles = Objects.requireNonNull(noticeStyles, "noticeStyles");
    this.statusStyles = Objects.requireNonNull(statusStyles, "statusStyles");
    this.errorStyles = Objects.requireNonNull(errorStyles, "errorStyles");
    this.timeSource = Objects.requireNonNull(timeSource, "timeSource");
  }

  void appendNotice(TargetRef ref, String from, String text) {
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.NOTICE, LogDirection.IN, from, text, timeSource.now(), "", Map.of());
    if (meta == null) {
      return;
    }
    LineStyles styles = noticeStyles.resolve(ref);
    appendLine.append(ref, from, text, styles.fromStyle(), styles.messageStyle(), true, meta);
  }

  void appendStatus(TargetRef ref, String from, String text) {
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref,
            LogKind.STATUS,
            LogDirection.SYSTEM,
            from,
            text,
            timeSource.now(),
            "",
            Map.of());
    if (meta == null) {
      return;
    }
    LineStyles styles = statusStyles.resolve(ref);
    appendLine.append(ref, from, text, styles.fromStyle(), styles.messageStyle(), true, meta);
  }

  void appendError(TargetRef ref, String from, String text) {
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref,
            LogKind.ERROR,
            LogDirection.SYSTEM,
            from,
            text,
            timeSource.now(),
            "",
            Map.of());
    if (meta == null) {
      return;
    }
    LineStyles styles = errorStyles.resolve(ref);
    appendLine.append(ref, from, text, styles.fromStyle(), styles.messageStyle(), true, meta);
  }

  void appendNoticeFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    appendNoticeFromHistory(ref, from, text, tsEpochMs, "", Map.of());
  }

  void appendNoticeFromHistory(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists.ensure(ref);
    noteEpochMs.note(ref, tsEpochMs);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(
        documentLookup.get(ref), messageId)) {
      return;
    }
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.NOTICE, LogDirection.IN, from, text, tsEpochMs, messageId, ircv3Tags);
    if (meta == null) {
      return;
    }
    LineStyles styles = noticeStyles.resolve(ref);
    appendLine.append(ref, from, text, styles.fromStyle(), styles.messageStyle(), false, meta);
  }

  void appendStatusFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    ensureTargetExists.ensure(ref);
    noteEpochMs.note(ref, tsEpochMs);
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.STATUS, LogDirection.SYSTEM, from, text, tsEpochMs, "", Map.of());
    if (meta == null) {
      return;
    }
    LineStyles styles = statusStyles.resolve(ref);
    appendLine.append(ref, from, text, styles.fromStyle(), styles.messageStyle(), false, meta);
  }

  void appendErrorFromHistory(TargetRef ref, String from, String text, long tsEpochMs) {
    ensureTargetExists.ensure(ref);
    noteEpochMs.note(ref, tsEpochMs);
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.ERROR, LogDirection.SYSTEM, from, text, tsEpochMs, "", Map.of());
    if (meta == null) {
      return;
    }
    LineStyles styles = errorStyles.resolve(ref);
    appendLine.append(ref, from, text, styles.fromStyle(), styles.messageStyle(), false, meta);
  }

  int insertNoticeFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    return insertFromHistoryAt(
        ref,
        insertAt,
        from,
        text,
        tsEpochMs,
        messageId,
        ircv3Tags,
        LogKind.NOTICE,
        LogDirection.IN,
        noticeStyles);
  }

  int insertStatusFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return insertFromHistoryAt(
        ref,
        insertAt,
        from,
        text,
        tsEpochMs,
        "",
        Map.of(),
        LogKind.STATUS,
        LogDirection.SYSTEM,
        statusStyles);
  }

  int insertErrorFromHistoryAt(
      TargetRef ref, int insertAt, String from, String text, long tsEpochMs) {
    return insertFromHistoryAt(
        ref,
        insertAt,
        from,
        text,
        tsEpochMs,
        "",
        Map.of(),
        LogKind.ERROR,
        LogDirection.SYSTEM,
        errorStyles);
  }

  private int insertFromHistoryAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags,
      LogKind kind,
      LogDirection direction,
      LineStylesResolver styleResolver) {
    ensureTargetExists.ensure(ref);
    StyledDocument doc = documentLookup.get(ref);
    noteEpochMs.note(ref, tsEpochMs);
    if (doc == null) {
      return Math.max(0, insertAt);
    }
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(doc, messageId)) {
      return Math.max(0, insertAt);
    }

    Map<String, String> safeTags = ircv3Tags != null ? ircv3Tags : Map.of();
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, kind, direction, from, tsEpochMs, null, messageId, safeTags);
    FilterEngine.Match match =
        filterRoutingSupport.hideMatch(ref, kind, direction, from, text, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        filterRoutingSupport.handleHiddenTextHistoryInsert(ref, insertAt, from, text, meta, match);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    LineStyles styles = styleResolver.resolve(ref);
    return insertLine.insert(ref, insertAt, from, text, styles.fromStyle(), styles.messageStyle(), meta);
  }

  void appendNoticeAt(TargetRef ref, String from, String text, long tsEpochMs) {
    appendNoticeAt(ref, from, text, tsEpochMs, "", Map.of());
  }

  void appendNoticeAt(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists.ensure(ref);
    noteEpochMs.note(ref, tsEpochMs);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(
        documentLookup.get(ref), messageId)) {
      return;
    }
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.NOTICE, LogDirection.IN, from, text, tsEpochMs, messageId, ircv3Tags);
    if (meta == null) {
      return;
    }
    ChatTranscriptOutgoingFollowUpSupport.Plan followUp =
        ChatTranscriptOutgoingFollowUpSupport.plan(messageId, ircv3Tags);
    followUp.runReplyContext(
        replyToMsgId -> appendReplyContext.append(ref, from, replyToMsgId, tsEpochMs));

    LineStyles styles = noticeStyles.resolve(ref);
    appendLine.append(ref, from, text, styles.fromStyle(), styles.messageStyle(), true, meta);

    StyledDocument doc = documentLookup.get(ref);
    ChatTranscriptState st = stateLookup.get(ref);
    followUp.applyPostAppend(
        ref,
        doc,
        reactionSummarySupport,
        st == null ? null : st.reactionSummary,
        from,
        tsEpochMs);
  }

  void appendStatusAt(
      TargetRef ref,
      String from,
      String text,
      long tsEpochMs,
      String messageId,
      Map<String, String> ircv3Tags) {
    ensureTargetExists.ensure(ref);
    noteEpochMs.note(ref, tsEpochMs);
    if (ChatTranscriptAppendGuardSupport.shouldSkipAppendByMessageId(
        documentLookup.get(ref), messageId)) {
      return;
    }
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.STATUS, LogDirection.SYSTEM, from, text, tsEpochMs, messageId, ircv3Tags);
    if (meta == null) {
      return;
    }
    LineStyles styles = statusStyles.resolve(ref);
    appendLine.append(ref, from, text, styles.fromStyle(), styles.messageStyle(), true, meta);
  }

  void appendStatusAt(TargetRef ref, String from, String text, long tsEpochMs) {
    appendStatusAt(ref, from, text, tsEpochMs, "", Map.of());
  }

  void appendErrorAt(TargetRef ref, String from, String text, long tsEpochMs) {
    ensureTargetExists.ensure(ref);
    noteEpochMs.note(ref, tsEpochMs);
    LineMeta meta =
        filterRoutingSupport.prepareVisibleTextAppend(
            ref, LogKind.ERROR, LogDirection.SYSTEM, from, text, tsEpochMs, "", Map.of());
    if (meta == null) {
      return;
    }
    LineStyles styles = errorStyles.resolve(ref);
    appendLine.append(ref, from, text, styles.fromStyle(), styles.messageStyle(), true, meta);
  }
}
