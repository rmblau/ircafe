package cafe.woden.ircclient.ui.chat.transcript.runtime;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.fold.HistoryDividerComponent;
import cafe.woden.ircclient.ui.chat.fold.LoadOlderMessagesComponent;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.text.StyledDocument;

/** Coordinates transcript auxiliary-row lifecycle operations under the transcript mutation lock. */
public final class ChatTranscriptAuxiliaryLifecycleCoordinator {

  private final Object mutationLock;
  private final ChatTranscriptLifecycleSupport lifecycleSupport =
      new ChatTranscriptLifecycleSupport();
  private ChatTranscriptLifecycleSupport.Context lifecycleContext;

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
        new ChatTranscriptLifecycleSupport.Context(
            docs,
            stateByTarget,
            auxiliaryRowsSupport,
            ensureTargetExists,
            endFilteredRun);
  }

  public LoadOlderMessagesComponent ensureLoadOlderMessagesControl(TargetRef ref) {
    synchronized (mutationLock) {
      return lifecycleSupport.ensureLoadOlderMessagesControl(requireLifecycleContext(), ref);
    }
  }

  public HistoryDividerComponent ensureHistoryDivider(
      TargetRef ref, int insertAt, String labelText) {
    synchronized (mutationLock) {
      return lifecycleSupport.ensureHistoryDivider(
          requireLifecycleContext(), ref, insertAt, labelText);
    }
  }

  public void markHistoryDividerPending(TargetRef ref, String labelText) {
    synchronized (mutationLock) {
      lifecycleSupport.markHistoryDividerPending(requireLifecycleContext(), ref, labelText);
    }
  }

  public boolean hasContentAfterOffset(TargetRef ref, int offset) {
    synchronized (mutationLock) {
      return lifecycleSupport.hasContentAfterOffset(requireLifecycleContext(), ref, offset);
    }
  }

  public void flushPendingHistoryDividerIfNeeded(TargetRef ref, StyledDocument doc) {
    synchronized (mutationLock) {
      lifecycleSupport.flushPendingHistoryDividerIfNeeded(requireLifecycleContext(), ref, doc);
    }
  }

  public void updateReadMarker(TargetRef ref, long markerEpochMs) {
    synchronized (mutationLock) {
      lifecycleSupport.updateReadMarker(requireLifecycleContext(), ref, markerEpochMs);
    }
  }

  public void clearReadMarker(TargetRef ref) {
    synchronized (mutationLock) {
      lifecycleSupport.clearReadMarker(requireLifecycleContext(), ref);
    }
  }

  public void clearReadMarkersForServer(String serverId) {
    synchronized (mutationLock) {
      lifecycleSupport.clearReadMarkersForServer(requireLifecycleContext(), serverId);
    }
  }

  public int readMarkerJumpOffset(TargetRef ref) {
    synchronized (mutationLock) {
      return lifecycleSupport.readMarkerJumpOffset(requireLifecycleContext(), ref);
    }
  }

  public void maybeRenderPendingReadMarker(TargetRef ref, Long lineEpochMs) {
    synchronized (mutationLock) {
      lifecycleSupport.maybeRenderPendingReadMarker(requireLifecycleContext(), ref, lineEpochMs);
    }
  }

  public int loadOlderInsertOffset(TargetRef ref) {
    synchronized (mutationLock) {
      return lifecycleSupport.loadOlderInsertOffset(requireLifecycleContext(), ref);
    }
  }

  public void setLoadOlderMessagesControlState(TargetRef ref, LoadOlderMessagesComponent.State s) {
    synchronized (mutationLock) {
      lifecycleSupport.setLoadOlderMessagesControlState(requireLifecycleContext(), ref, s);
    }
  }

  public void setLoadOlderMessagesControlHandler(
      TargetRef ref, java.util.function.BooleanSupplier onLoad) {
    synchronized (mutationLock) {
      lifecycleSupport.setLoadOlderMessagesControlHandler(requireLifecycleContext(), ref, onLoad);
    }
  }

  private ChatTranscriptLifecycleSupport.Context requireLifecycleContext() {
    if (lifecycleContext == null) {
      throw new IllegalStateException("Auxiliary lifecycle coordinator context not bound");
    }
    return lifecycleContext;
  }
}
