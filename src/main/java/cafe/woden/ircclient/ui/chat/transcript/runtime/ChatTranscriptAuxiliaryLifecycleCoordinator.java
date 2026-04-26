package cafe.woden.ircclient.ui.chat.transcript.runtime;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.fold.HistoryDividerComponent;
import cafe.woden.ircclient.ui.chat.fold.LoadOlderMessagesComponent;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

/** Coordinates transcript auxiliary-row lifecycle operations under the transcript mutation lock. */
public final class ChatTranscriptAuxiliaryLifecycleCoordinator {

  private final Object mutationLock;
  private Context lifecycleContext;

  public ChatTranscriptAuxiliaryLifecycleCoordinator(Object mutationLock) {
    this.mutationLock = Objects.requireNonNull(mutationLock, "mutationLock");
  }

  public void bindContext(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport,
      Consumer<TargetRef> ensureTargetExists,
      Consumer<TargetRef> endFilteredRun) {
    this.lifecycleContext =
        new Context(docs, stateByTarget, auxiliaryRowsSupport, ensureTargetExists, endFilteredRun);
  }

  public LoadOlderMessagesComponent ensureLoadOlderMessagesControl(TargetRef ref) {
    synchronized (mutationLock) {
      Context context = requireLifecycleContext();
      context.ensureTargetExists().accept(ref);
      context.endFilteredRun().accept(ref);
      StyledDocument doc = context.docs().get(ref);
      ChatTranscriptState state = context.stateByTarget().get(ref);
      return context
          .auxiliaryRowsSupport()
          .ensureLoadOlderMessagesControl(ref, doc, state == null ? null : state.auxiliaryRows());
    }
  }

  public HistoryDividerComponent ensureHistoryDivider(
      TargetRef ref, int insertAt, String labelText) {
    synchronized (mutationLock) {
      Context context = requireLifecycleContext();
      context.ensureTargetExists().accept(ref);
      StyledDocument doc = context.docs().get(ref);
      ChatTranscriptState state = context.stateByTarget().get(ref);
      return context
          .auxiliaryRowsSupport()
          .ensureHistoryDivider(
              ref, doc, state == null ? null : state.auxiliaryRows(), insertAt, labelText);
    }
  }

  public void markHistoryDividerPending(TargetRef ref, String labelText) {
    synchronized (mutationLock) {
      if (ref == null) return;
      Context context = requireLifecycleContext();
      context.ensureTargetExists().accept(ref);
      ChatTranscriptState state = context.stateByTarget().get(ref);
      context
          .auxiliaryRowsSupport()
          .markHistoryDividerPending(state == null ? null : state.auxiliaryRows(), labelText);
    }
  }

  public boolean hasContentAfterOffset(TargetRef ref, int offset) {
    synchronized (mutationLock) {
      if (ref == null) return false;
      Context context = requireLifecycleContext();
      context.ensureTargetExists().accept(ref);
      StyledDocument doc = context.docs().get(ref);
      return doc != null && doc.getLength() > Math.max(0, offset);
    }
  }

  public void flushPendingHistoryDividerIfNeeded(TargetRef ref, StyledDocument doc) {
    synchronized (mutationLock) {
      if (ref == null || doc == null) return;
      Context context = requireLifecycleContext();
      ChatTranscriptState state = context.stateByTarget().get(ref);
      context
          .auxiliaryRowsSupport()
          .flushPendingHistoryDividerIfNeeded(
              ref, doc, state == null ? null : state.auxiliaryRows());
    }
  }

  public void updateReadMarker(TargetRef ref, long markerEpochMs) {
    synchronized (mutationLock) {
      if (ref == null) return;
      Context context = requireLifecycleContext();
      context.ensureTargetExists().accept(ref);
      StyledDocument doc = context.docs().get(ref);
      ChatTranscriptState state = context.stateByTarget().get(ref);
      context
          .auxiliaryRowsSupport()
          .updateReadMarker(ref, doc, state == null ? null : state.auxiliaryRows(), markerEpochMs);
    }
  }

  public void clearReadMarker(TargetRef ref) {
    synchronized (mutationLock) {
      if (ref == null) return;
      Context context = requireLifecycleContext();
      context.ensureTargetExists().accept(ref);
      StyledDocument doc = context.docs().get(ref);
      ChatTranscriptState state = context.stateByTarget().get(ref);
      context
          .auxiliaryRowsSupport()
          .clearReadMarker(ref, doc, state == null ? null : state.auxiliaryRows());
    }
  }

  public void clearReadMarkersForServer(String serverId) {
    synchronized (mutationLock) {
      Context context = requireLifecycleContext();
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
  }

  public int readMarkerJumpOffset(TargetRef ref) {
    synchronized (mutationLock) {
      Context context = requireLifecycleContext();
      StyledDocument doc = context.docs().get(ref);
      ChatTranscriptState state = context.stateByTarget().get(ref);
      return context
          .auxiliaryRowsSupport()
          .readMarkerJumpOffset(doc, state == null ? null : state.auxiliaryRows());
    }
  }

  public void maybeRenderPendingReadMarker(TargetRef ref, Long lineEpochMs) {
    synchronized (mutationLock) {
      Context context = requireLifecycleContext();
      ChatTranscriptState state = context.stateByTarget().get(ref);
      StyledDocument doc = context.docs().get(ref);
      context
          .auxiliaryRowsSupport()
          .maybeRenderPendingReadMarker(
              ref, doc, state == null ? null : state.auxiliaryRows(), lineEpochMs);
    }
  }

  public int loadOlderInsertOffset(TargetRef ref) {
    synchronized (mutationLock) {
      Context context = requireLifecycleContext();
      StyledDocument doc = context.docs().get(ref);
      ChatTranscriptState state = context.stateByTarget().get(ref);
      return context
          .auxiliaryRowsSupport()
          .loadOlderInsertOffset(doc, state == null ? null : state.auxiliaryRows());
    }
  }

  public void setLoadOlderMessagesControlState(TargetRef ref, LoadOlderMessagesComponent.State s) {
    synchronized (mutationLock) {
      Context context = requireLifecycleContext();
      ChatTranscriptState state = context.stateByTarget().get(ref);
      context
          .auxiliaryRowsSupport()
          .setLoadOlderMessagesControlState(state == null ? null : state.auxiliaryRows(), s);
    }
  }

  public void setLoadOlderMessagesControlHandler(
      TargetRef ref, java.util.function.BooleanSupplier onLoad) {
    synchronized (mutationLock) {
      Context context = requireLifecycleContext();
      ChatTranscriptState state = context.stateByTarget().get(ref);
      context
          .auxiliaryRowsSupport()
          .setLoadOlderMessagesControlHandler(state == null ? null : state.auxiliaryRows(), onLoad);
    }
  }

  private Context requireLifecycleContext() {
    if (lifecycleContext == null) {
      throw new IllegalStateException("Auxiliary lifecycle coordinator context not bound");
    }
    return lifecycleContext;
  }

  private record Context(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport,
      Consumer<TargetRef> ensureTargetExists,
      Consumer<TargetRef> endFilteredRun) {}
}
