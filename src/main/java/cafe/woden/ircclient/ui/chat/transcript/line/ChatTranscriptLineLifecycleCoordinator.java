package cafe.woden.ircclient.ui.chat.transcript.line;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.fold.HistoryDividerComponent;
import cafe.woden.ircclient.ui.chat.fold.LoadOlderMessagesComponent;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptLifecycleSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.awt.Component;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

/** Owns the shared visible-line bridge and transcript auxiliary-row lifecycle callbacks. */
public final class ChatTranscriptLineLifecycleCoordinator {

  private final Object mutationLock;
  private final ChatTranscriptLineFlowSupport lineFlowSupport = new ChatTranscriptLineFlowSupport();
  private final ChatTranscriptLifecycleSupport lifecycleSupport =
      new ChatTranscriptLifecycleSupport();
  private ChatTranscriptLineFlowSupport.Context lineFlowContext;
  private ChatTranscriptLifecycleSupport.Context lifecycleContext;

  public ChatTranscriptLineLifecycleCoordinator(Object mutationLock) {
    this.mutationLock = Objects.requireNonNull(mutationLock, "mutationLock");
  }

  public void bindContexts(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      Consumer<TargetRef> ensureTargetExists,
      ChatTranscriptLineFlowSupport.EpochNoteHandler noteEpochMs,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      Consumer<TargetRef> endFilteredInsertRun,
      Predicate<TargetRef> deferRichTextDuringHistoryBatch,
      ChatTranscriptDocumentLineSupport documentLineSupport,
      ChatTranscriptTextAppendSupport.Context textAppendSupportContext,
      ChatTranscriptTextInsertSupport.Context textInsertSupportContext,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      BooleanSupplier imageEmbedsEnabled,
      BooleanSupplier linkPreviewsEnabled,
      Supplier<ChatTranscriptState> newTranscriptState,
      ChatTranscriptAuxiliaryRowsSupport auxiliaryRowsSupport,
      Consumer<TargetRef> endFilteredRun) {
    this.lifecycleContext =
        new ChatTranscriptLifecycleSupport.Context(
            docs,
            stateByTarget,
            newTranscriptState,
            auxiliaryRowsSupport,
            ensureTargetExists,
            endFilteredRun);
    this.lineFlowContext =
        new ChatTranscriptLineFlowSupport.Context(
            docs,
            stateByTarget,
            ensureTargetExists,
            noteEpochMs,
            this::flushPendingHistoryDividerIfNeeded,
            filterRoutingSupport,
            endFilteredInsertRun,
            deferRichTextDuringHistoryBatch,
            documentLineSupport,
            textAppendSupportContext,
            textInsertSupportContext,
            runtimeSettingsSupport,
            imageEmbedsEnabled,
            linkPreviewsEnabled);
  }

  public void appendLine(
      TargetRef ref,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      boolean allowEmbeds,
      LineMeta meta) {
    synchronized (mutationLock) {
      lineFlowSupport.appendLine(
          requireLineFlowContext(),
          ref,
          from,
          text,
          fromStyle,
          msgStyle,
          allowEmbeds,
          meta,
          null,
          null);
    }
  }

  public void appendLineWithTail(
      TargetRef ref,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      LineMeta meta,
      Component tailComponent,
      AttributeSet tailAttrs) {
    synchronized (mutationLock) {
      lineFlowSupport.appendLine(
          requireLineFlowContext(),
          ref,
          from,
          text,
          fromStyle,
          msgStyle,
          true,
          meta,
          tailComponent,
          tailAttrs);
    }
  }

  public int insertLineAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      LineMeta meta) {
    synchronized (mutationLock) {
      return lineFlowSupport.insertLineAt(
          requireLineFlowContext(), ref, insertAt, from, text, fromStyle, msgStyle, meta);
    }
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

  public void closeTarget(TargetRef ref) {
    synchronized (mutationLock) {
      lifecycleSupport.closeTarget(requireLifecycleContext(), ref);
    }
  }

  public void clearTarget(TargetRef ref) {
    synchronized (mutationLock) {
      lifecycleSupport.clearTarget(requireLifecycleContext(), ref);
    }
  }

  private ChatTranscriptLineFlowSupport.Context requireLineFlowContext() {
    requireContextsBound();
    return lineFlowContext;
  }

  private ChatTranscriptLifecycleSupport.Context requireLifecycleContext() {
    requireContextsBound();
    return lifecycleContext;
  }

  private void requireContextsBound() {
    if (lineFlowContext == null || lifecycleContext == null) {
      throw new IllegalStateException("Line/lifecycle coordinator contexts not bound");
    }
  }
}
