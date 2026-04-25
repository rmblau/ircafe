package cafe.woden.ircclient.ui.chat.transcript.runtime;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.fold.HistoryDividerComponent;
import cafe.woden.ircclient.ui.chat.fold.LoadOlderMessagesComponent;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.text.StyledDocument;

public final class ChatTranscriptLifecycleSupport {

  public record Context(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      Supplier<ChatTranscriptState> newTranscriptState,
      ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport,
      Consumer<TargetRef> ensureTargetExists,
      Consumer<TargetRef> endFilteredRun) {}

  public LoadOlderMessagesComponent ensureLoadOlderMessagesControl(Context context, TargetRef ref) {
    context.ensureTargetExists().accept(ref);
    context.endFilteredRun().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState state = context.stateByTarget().get(ref);
    return context
        .auxiliaryRowsSupport()
        .ensureLoadOlderMessagesControl(ref, doc, state == null ? null : state.auxiliaryRows());
  }

  public HistoryDividerComponent ensureHistoryDivider(
      Context context, TargetRef ref, int insertAt, String labelText) {
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState state = context.stateByTarget().get(ref);
    return context
        .auxiliaryRowsSupport()
        .ensureHistoryDivider(
            ref, doc, state == null ? null : state.auxiliaryRows(), insertAt, labelText);
  }

  public void markHistoryDividerPending(Context context, TargetRef ref, String labelText) {
    if (ref == null) return;
    context.ensureTargetExists().accept(ref);
    ChatTranscriptState state = context.stateByTarget().get(ref);
    context
        .auxiliaryRowsSupport()
        .markHistoryDividerPending(state == null ? null : state.auxiliaryRows(), labelText);
  }

  public boolean hasContentAfterOffset(Context context, TargetRef ref, int offset) {
    if (ref == null) return false;
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    return doc != null && doc.getLength() > Math.max(0, offset);
  }

  public void flushPendingHistoryDividerIfNeeded(
      Context context, TargetRef ref, StyledDocument doc) {
    if (ref == null || doc == null) return;
    ChatTranscriptState state = context.stateByTarget().get(ref);
    context
        .auxiliaryRowsSupport()
        .flushPendingHistoryDividerIfNeeded(ref, doc, state == null ? null : state.auxiliaryRows());
  }

  public void updateReadMarker(Context context, TargetRef ref, long markerEpochMs) {
    if (ref == null) return;
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState state = context.stateByTarget().get(ref);
    context
        .auxiliaryRowsSupport()
        .updateReadMarker(ref, doc, state == null ? null : state.auxiliaryRows(), markerEpochMs);
  }

  public void clearReadMarker(Context context, TargetRef ref) {
    if (ref == null) return;
    context.ensureTargetExists().accept(ref);
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState state = context.stateByTarget().get(ref);
    context
        .auxiliaryRowsSupport()
        .clearReadMarker(ref, doc, state == null ? null : state.auxiliaryRows());
  }

  public void clearReadMarkersForServer(Context context, String serverId) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;
    ArrayList<TargetRef> targets = new ArrayList<>(context.stateByTarget().keySet());
    for (TargetRef ref : targets) {
      if (ref == null || !sid.equals(Objects.toString(ref.serverId(), "").trim())) continue;
      StyledDocument doc = context.docs().get(ref);
      ChatTranscriptState state = context.stateByTarget().get(ref);
      context
          .auxiliaryRowsSupport()
          .clearReadMarker(ref, doc, state == null ? null : state.auxiliaryRows());
    }
  }

  public void maybeRenderPendingReadMarker(Context context, TargetRef ref, Long lineEpochMs) {
    ChatTranscriptState state = context.stateByTarget().get(ref);
    StyledDocument doc = context.docs().get(ref);
    context
        .auxiliaryRowsSupport()
        .maybeRenderPendingReadMarker(
            ref, doc, state == null ? null : state.auxiliaryRows(), lineEpochMs);
  }

  public int readMarkerJumpOffset(Context context, TargetRef ref) {
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState state = context.stateByTarget().get(ref);
    return context
        .auxiliaryRowsSupport()
        .readMarkerJumpOffset(doc, state == null ? null : state.auxiliaryRows());
  }

  public int loadOlderInsertOffset(Context context, TargetRef ref) {
    StyledDocument doc = context.docs().get(ref);
    ChatTranscriptState state = context.stateByTarget().get(ref);
    return context
        .auxiliaryRowsSupport()
        .loadOlderInsertOffset(doc, state == null ? null : state.auxiliaryRows());
  }

  public void setLoadOlderMessagesControlState(
      Context context, TargetRef ref, LoadOlderMessagesComponent.State newState) {
    ChatTranscriptState state = context.stateByTarget().get(ref);
    context
        .auxiliaryRowsSupport()
        .setLoadOlderMessagesControlState(state == null ? null : state.auxiliaryRows(), newState);
  }

  public void setLoadOlderMessagesControlHandler(
      Context context, TargetRef ref, java.util.function.BooleanSupplier onLoad) {
    ChatTranscriptState state = context.stateByTarget().get(ref);
    context
        .auxiliaryRowsSupport()
        .setLoadOlderMessagesControlHandler(state == null ? null : state.auxiliaryRows(), onLoad);
  }

  public void closeTarget(Context context, TargetRef ref) {
    if (ref == null) return;
    context.docs().remove(ref);
    context.stateByTarget().remove(ref);
  }

  public void clearTarget(Context context, TargetRef ref) {
    if (ref == null) return;
    context.ensureTargetExists().accept(ref);

    StyledDocument doc = context.docs().get(ref);
    if (doc == null) return;

    try {
      doc.remove(0, doc.getLength());
    } catch (Exception ignored) {
    }
    context.stateByTarget().put(ref, context.newTranscriptState().get());
  }
}
