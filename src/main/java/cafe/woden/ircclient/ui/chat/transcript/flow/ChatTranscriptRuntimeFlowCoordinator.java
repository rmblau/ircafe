package cafe.woden.ircclient.ui.chat.transcript.flow;

import cafe.woden.ircclient.app.api.PresenceEvent;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.fold.HistoryDividerComponent;
import cafe.woden.ircclient.ui.chat.fold.LoadOlderMessagesComponent;
import cafe.woden.ircclient.ui.chat.transcript.line.LineMeta;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptDocumentLineSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptTextAppendSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptTextInsertSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.awt.Component;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

/** Wraps line lifecycle and presence flow behind one runtime-facing delegate surface. */
public final class ChatTranscriptRuntimeFlowCoordinator {

  private final ChatTranscriptLineLifecycleCoordinator lineLifecycleCoordinator;
  private final ChatTranscriptPresenceFlowCoordinator presenceFlowCoordinator;

  public ChatTranscriptRuntimeFlowCoordinator(Object mutationLock, ChatStyles styles) {
    this.lineLifecycleCoordinator = new ChatTranscriptLineLifecycleCoordinator(mutationLock);
    this.presenceFlowCoordinator = new ChatTranscriptPresenceFlowCoordinator(styles);
  }

  public void bindPresenceContext(
      ChatTranscriptPresenceFoldSupport presenceFoldSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptFilteredLinesSupport filteredLinesSupport,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      ChatTranscriptPresenceFlowSupport.EnsureTargetExistsHandler ensureTargetExists,
      ChatTranscriptPresenceFlowSupport.EpochNoteHandler noteEpochMs,
      ChatTranscriptPresenceFlowSupport.TimeSource timeSource) {
    presenceFlowCoordinator.bindContext(
        presenceFoldSupport,
        filterRoutingSupport,
        filteredLinesSupport,
        runtimeSettingsSupport,
        docs,
        stateByTarget,
        ensureTargetExists,
        noteEpochMs,
        lineLifecycleCoordinator::appendLine,
        lineLifecycleCoordinator::insertLineAt,
        timeSource);
  }

  public void bindLineLifecycleContexts(
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
    lineLifecycleCoordinator.bindContexts(
        docs,
        stateByTarget,
        ensureTargetExists,
        noteEpochMs,
        filterRoutingSupport,
        endFilteredInsertRun,
        deferRichTextDuringHistoryBatch,
        documentLineSupport,
        textAppendSupportContext,
        textInsertSupportContext,
        runtimeSettingsSupport,
        imageEmbedsEnabled,
        linkPreviewsEnabled,
        newTranscriptState,
        auxiliaryRowsSupport,
        endFilteredRun);
  }

  public void appendLine(
      TargetRef ref,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      boolean allowEmbeds,
      LineMeta meta) {
    lineLifecycleCoordinator.appendLine(ref, from, text, fromStyle, msgStyle, allowEmbeds, meta);
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
    lineLifecycleCoordinator.appendLineWithTail(
        ref, from, text, fromStyle, msgStyle, meta, tailComponent, tailAttrs);
  }

  public int insertLineAt(
      TargetRef ref,
      int insertAt,
      String from,
      String text,
      AttributeSet fromStyle,
      AttributeSet msgStyle,
      LineMeta meta) {
    return lineLifecycleCoordinator.insertLineAt(
        ref, insertAt, from, text, fromStyle, msgStyle, meta);
  }

  public void maybeRenderPendingReadMarker(TargetRef ref, Long lineEpochMs) {
    lineLifecycleCoordinator.maybeRenderPendingReadMarker(ref, lineEpochMs);
  }

  public void breakPresenceRun(TargetRef ref) {
    presenceFlowCoordinator.breakPresenceRun(ref);
  }

  public void shiftCurrentBlock(TargetRef ref, int insertAt, int delta) {
    presenceFlowCoordinator.shiftCurrentBlock(ref, insertAt, delta);
  }

  public void resetAfterHeadTrim(TargetRef ref) {
    presenceFlowCoordinator.resetAfterHeadTrim(ref);
  }

  public LoadOlderMessagesComponent ensureLoadOlderMessagesControl(TargetRef ref) {
    return lineLifecycleCoordinator.ensureLoadOlderMessagesControl(ref);
  }

  public HistoryDividerComponent ensureHistoryDivider(
      TargetRef ref, int insertAt, String labelText) {
    return lineLifecycleCoordinator.ensureHistoryDivider(ref, insertAt, labelText);
  }

  public void markHistoryDividerPending(TargetRef ref, String labelText) {
    lineLifecycleCoordinator.markHistoryDividerPending(ref, labelText);
  }

  public boolean hasContentAfterOffset(TargetRef ref, int offset) {
    return lineLifecycleCoordinator.hasContentAfterOffset(ref, offset);
  }

  public void updateReadMarker(TargetRef ref, long markerEpochMs) {
    lineLifecycleCoordinator.updateReadMarker(ref, markerEpochMs);
  }

  public void clearReadMarker(TargetRef ref) {
    lineLifecycleCoordinator.clearReadMarker(ref);
  }

  public void clearReadMarkersForServer(String serverId) {
    lineLifecycleCoordinator.clearReadMarkersForServer(serverId);
  }

  public int readMarkerJumpOffset(TargetRef ref) {
    return lineLifecycleCoordinator.readMarkerJumpOffset(ref);
  }

  public int loadOlderInsertOffset(TargetRef ref) {
    return lineLifecycleCoordinator.loadOlderInsertOffset(ref);
  }

  public void setLoadOlderMessagesControlState(TargetRef ref, LoadOlderMessagesComponent.State s) {
    lineLifecycleCoordinator.setLoadOlderMessagesControlState(ref, s);
  }

  public void setLoadOlderMessagesControlHandler(
      TargetRef ref, java.util.function.BooleanSupplier onLoad) {
    lineLifecycleCoordinator.setLoadOlderMessagesControlHandler(ref, onLoad);
  }

  public void closeTarget(TargetRef ref) {
    lineLifecycleCoordinator.closeTarget(ref);
  }

  public void clearTarget(TargetRef ref) {
    lineLifecycleCoordinator.clearTarget(ref);
  }

  public void appendPresence(TargetRef ref, PresenceEvent event) {
    presenceFlowCoordinator.appendPresence(ref, event);
  }

  public int insertPresenceFromHistoryAt(
      TargetRef ref, int insertAt, String displayText, long tsEpochMs) {
    return presenceFlowCoordinator.insertPresenceFromHistoryAt(
        ref, insertAt, displayText, tsEpochMs);
  }

  public void appendPresenceFromHistory(TargetRef ref, String displayText, long tsEpochMs) {
    presenceFlowCoordinator.appendPresenceFromHistory(ref, displayText, tsEpochMs);
  }
}
