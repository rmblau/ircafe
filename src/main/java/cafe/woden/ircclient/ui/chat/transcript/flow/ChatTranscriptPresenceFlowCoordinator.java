package cafe.woden.ircclient.ui.chat.transcript.flow;

import cafe.woden.ircclient.app.api.PresenceEvent;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilterRoutingSupport;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRuntimeSettingsSupport;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptState;
import java.util.Map;
import javax.swing.text.StyledDocument;

/** Wraps presence rendering, fold state, and head-trim reset policy behind one collaborator. */
public final class ChatTranscriptPresenceFlowCoordinator {

  private final ChatTranscriptPresenceFlowSupport support;
  private ChatTranscriptPresenceFlowSupport.Context context;

  public ChatTranscriptPresenceFlowCoordinator(ChatStyles styles) {
    this.support = new ChatTranscriptPresenceFlowSupport(styles);
  }

  public void bindContext(
      ChatTranscriptPresenceFoldSupport presenceFoldSupport,
      ChatTranscriptFilterRoutingSupport filterRoutingSupport,
      ChatTranscriptFilteredLinesSupport filteredLinesSupport,
      ChatTranscriptRuntimeSettingsSupport runtimeSettingsSupport,
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      ChatTranscriptPresenceFlowSupport.EnsureTargetExistsHandler ensureTargetExists,
      ChatTranscriptPresenceFlowSupport.EpochNoteHandler noteEpochMs,
      ChatTranscriptPresenceFlowSupport.AppendLineHandler appendLine,
      ChatTranscriptPresenceFlowSupport.InsertLineHandler insertLine,
      ChatTranscriptPresenceFlowSupport.TimeSource timeSource) {
    this.context =
        new ChatTranscriptPresenceFlowSupport.Context(
            filterRoutingSupport,
            presenceFoldSupport,
            filteredLinesSupport,
            runtimeSettingsSupport,
            docs,
            stateByTarget,
            ensureTargetExists,
            noteEpochMs,
            appendLine,
            insertLine,
            timeSource);
  }

  public void appendPresence(TargetRef ref, PresenceEvent event) {
    support.appendPresence(requireBoundContext(), ref, event);
  }

  public int insertPresenceFromHistoryAt(
      TargetRef ref, int insertAt, String displayText, long tsEpochMs) {
    return support.insertPresenceFromHistoryAt(
        requireBoundContext(), ref, insertAt, displayText, tsEpochMs);
  }

  public void appendPresenceFromHistory(TargetRef ref, String displayText, long tsEpochMs) {
    support.appendPresenceFromHistory(requireBoundContext(), ref, displayText, tsEpochMs);
  }

  public void breakPresenceRun(TargetRef ref) {
    support.breakPresenceRun(requireBoundContext(), ref);
  }

  public void shiftCurrentBlock(TargetRef ref, int insertAt, int delta) {
    support.shiftCurrentBlock(requireBoundContext(), ref, insertAt, delta);
  }

  public void resetAfterHeadTrim(TargetRef ref) {
    if (ref == null) {
      return;
    }
    ChatTranscriptPresenceFlowSupport.Context context = requireBoundContext();
    ChatTranscriptState state = context.stateByTarget().get(ref);
    if (state == null) {
      return;
    }
    state.resetAfterHeadTrim(context.presenceFoldSupport(), context.filteredLinesSupport());
  }

  private ChatTranscriptPresenceFlowSupport.Context requireBoundContext() {
    if (context == null) {
      throw new IllegalStateException("Presence flow coordinator context not bound");
    }
    return context;
  }
}
