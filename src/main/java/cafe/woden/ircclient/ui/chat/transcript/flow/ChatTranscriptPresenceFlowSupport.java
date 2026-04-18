package cafe.woden.ircclient.ui.chat.transcript;

import cafe.woden.ircclient.app.api.PresenceEvent;
import cafe.woden.ircclient.app.api.PresenceKind;
import cafe.woden.ircclient.model.LogDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.filter.FilterEngine;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

final class ChatTranscriptPresenceFlowSupport {

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
  interface TimeSource {
    long now();
  }

  record Context(
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptPresenceFoldSupport presenceFoldSupport,
      ChatTranscriptFilteredLinesSupport filteredLinesSupport,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      EnsureTargetExistsHandler ensureTargetExists,
      EpochNoteHandler noteEpochMs,
      AppendLineHandler appendLine,
      InsertLineHandler insertLine,
      TimeSource timeSource) {
    Context {
      Objects.requireNonNull(filterRoutingSupport, "filterRoutingSupport");
      Objects.requireNonNull(presenceFoldSupport, "presenceFoldSupport");
      Objects.requireNonNull(filteredLinesSupport, "filteredLinesSupport");
      Objects.requireNonNull(runtimeSettingsSupport, "runtimeSettingsSupport");
      Objects.requireNonNull(docs, "docs");
      Objects.requireNonNull(stateByTarget, "stateByTarget");
      Objects.requireNonNull(ensureTargetExists, "ensureTargetExists");
      Objects.requireNonNull(noteEpochMs, "noteEpochMs");
      Objects.requireNonNull(appendLine, "appendLine");
      Objects.requireNonNull(insertLine, "insertLine");
      Objects.requireNonNull(timeSource, "timeSource");
    }
  }

  private final ChatStyles styles;

  ChatTranscriptPresenceFlowSupport(ChatStyles styles) {
    this.styles = Objects.requireNonNull(styles, "styles");
  }

  void appendPresence(Context context, TargetRef ref, PresenceEvent event) {
    if (ref == null || event == null) {
      return;
    }

    long eventEpochMs = context.timeSource().now();
    String presenceFrom = resolvePresenceFrom(event);
    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.PRESENCE, LogDirection.SYSTEM, presenceFrom, eventEpochMs, event);

    FilterEngine.Match match =
        context.filterRoutingSupport().firstMatch(
            ref,
            LogKind.PRESENCE,
            LogDirection.SYSTEM,
            presenceFrom,
            event.displayText(),
            meta.tags());
    if (context.filterRoutingSupport().handleHiddenAppend(ref, event.displayText(), meta, match)) {
      return;
    }

    ChatTranscriptState state = context.stateByTarget().get(ref);
    if (state != null) {
      context.filteredLinesSupport().endAppendRun(state.filteredLines);
    }

    context.ensureTargetExists().ensure(ref);

    StyledDocument doc = context.docs().get(ref);
    state = context.stateByTarget().get(ref);
    if (doc == null || state == null) {
      return;
    }

    context.presenceFoldSupport().appendPresence(
        ref,
        doc,
        state.presenceFolds,
        event,
        eventEpochMs,
        meta,
        match,
        context.runtimeSettingsSupport().timestampsIncludePresenceMessages(),
        context.runtimeSettingsSupport().presenceFoldsEnabled());
  }

  int insertPresenceFromHistoryAt(Context context, TargetRef ref, int insertAt, String displayText, long tsEpochMs) {
    context.ensureTargetExists().ensure(ref);
    StyledDocument doc = context.docs().get(ref);
    context.noteEpochMs().note(ref, tsEpochMs);
    if (doc == null) {
      return Math.max(0, insertAt);
    }

    LineMeta meta =
        ChatTranscriptLineMetaSupport.create(
            ref, LogKind.PRESENCE, LogDirection.SYSTEM, null, tsEpochMs, null);
    FilterEngine.Match match =
        context.filterRoutingSupport().hideMatch(
            ref, LogKind.PRESENCE, LogDirection.SYSTEM, null, displayText, meta.tags());
    ChatTranscriptFilterRoutingSupport.HistoryDecision hidden =
        context.filterRoutingSupport().handleHiddenTextHistoryInsert(
            ref, insertAt, null, displayText, meta, match);
    if (hidden.handled()) {
      return hidden.nextInsertAt();
    }

    return context
        .insertLine()
        .insert(ref, insertAt, null, displayText, styles.status(), styles.status(), meta);
  }

  void appendPresenceFromHistory(Context context, TargetRef ref, String displayText, long tsEpochMs) {
    context.ensureTargetExists().ensure(ref);
    context.noteEpochMs().note(ref, tsEpochMs);
    LineMeta meta =
        context.filterRoutingSupport().prepareVisibleTextAppend(
            ref, LogKind.PRESENCE, LogDirection.SYSTEM, null, displayText, tsEpochMs, "", Map.of());
    if (meta == null) {
      return;
    }
    context
        .appendLine()
        .append(ref, null, displayText, styles.presence(), styles.presence(), false, meta);
  }

  void breakPresenceRun(Context context, TargetRef ref) {
    if (ref == null) {
      return;
    }
    ChatTranscriptState state = context.stateByTarget().get(ref);
    if (state == null) {
      return;
    }
    context.presenceFoldSupport().clearCurrentBlock(state.presenceFolds);
    context.filteredLinesSupport().endAppendRun(state.filteredLines);
  }

  void shiftCurrentBlock(Context context, TargetRef ref, int insertAt, int delta) {
    if (ref == null || delta == 0) {
      return;
    }
    ChatTranscriptState state = context.stateByTarget().get(ref);
    context.presenceFoldSupport().shiftCurrentBlock(
        state == null ? null : state.presenceFolds, insertAt, delta);
  }

  static String resolvePresenceFrom(PresenceEvent event) {
    if (event == null) {
      return null;
    }
    try {
      return event.kind() == PresenceKind.NICK ? event.oldNick() : event.nick();
    } catch (Exception ignored) {
      return event.nick();
    }
  }
}
