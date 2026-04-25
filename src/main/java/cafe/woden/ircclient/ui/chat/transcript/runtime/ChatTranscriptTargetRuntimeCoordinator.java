package cafe.woden.ircclient.ui.chat.transcript.runtime;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.settings.UiSettings;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.text.StyledDocument;

/** Owns transcript target documents, per-target state, and document restyle orchestration. */
public final class ChatTranscriptTargetRuntimeCoordinator {

  private final Map<TargetRef, StyledDocument> docs = new HashMap<>();
  private final Map<TargetRef, ChatTranscriptState> stateByTarget = new HashMap<>();
  private final ChatTranscriptTargetStateSupport targetStateSupport;
  private final ChatTranscriptRestyleCoordinator restyleCoordinator;

  public ChatTranscriptTargetRuntimeCoordinator(
      Supplier<ChatTranscriptMessageCatalogSupport.State> messageCatalogStateSupplier,
      Object snapshotLock,
      int restyleElementsPerSlice,
      ChatTranscriptRestyleSupport.Context restyleSupportContext,
      Supplier<UiSettings> settingsSupplier,
      Function<UiSettings, Color> outgoingColorResolver) {
    this.targetStateSupport =
        new ChatTranscriptTargetStateSupport(
            docs, stateByTarget, messageCatalogStateSupplier, snapshotLock);
    this.restyleCoordinator =
        new ChatTranscriptRestyleCoordinator(
            restyleElementsPerSlice,
            restyleSupportContext,
            settingsSupplier,
            outgoingColorResolver,
            targetStateSupport::snapshotDocuments);
  }

  public Map<TargetRef, StyledDocument> docs() {
    return docs;
  }

  public Map<TargetRef, ChatTranscriptState> stateByTarget() {
    return stateByTarget;
  }

  public void ensureTargetExists(TargetRef ref) {
    targetStateSupport.ensureTargetExists(ref);
  }

  public ChatTranscriptState newTranscriptState() {
    return targetStateSupport.newTranscriptState();
  }

  public void noteEpochMs(TargetRef ref, Long epochMs) {
    targetStateSupport.noteEpochMs(ref, epochMs);
  }

  public StyledDocument document(TargetRef ref) {
    return targetStateSupport.document(ref);
  }

  public OptionalLong earliestTimestampEpochMs(TargetRef ref) {
    return targetStateSupport.earliestTimestampEpochMs(ref);
  }

  public void restyleAllDocuments() {
    restyleCoordinator.restyleAllDocuments();
  }

  public void restyleAllDocumentsCoalesced() {
    restyleCoordinator.restyleAllDocumentsCoalesced();
  }
}
