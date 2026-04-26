package cafe.woden.ircclient.ui.chat.transcript.runtime;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.function.Supplier;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

/** Owns per-target transcript documents and runtime state lifecycle operations. */
public final class ChatTranscriptTargetStateSupport {

  private final Map<TargetRef, StyledDocument> docs;
  private final Map<TargetRef, ChatTranscriptState> stateByTarget;
  private final Supplier<ChatTranscriptMessageCatalogSupport.State> messageCatalogStateSupplier;
  private final Object snapshotLock;

  public ChatTranscriptTargetStateSupport(
      Map<TargetRef, StyledDocument> docs,
      Map<TargetRef, ChatTranscriptState> stateByTarget,
      Supplier<ChatTranscriptMessageCatalogSupport.State> messageCatalogStateSupplier,
      Object snapshotLock) {
    this.docs = Objects.requireNonNull(docs, "docs");
    this.stateByTarget = Objects.requireNonNull(stateByTarget, "stateByTarget");
    this.messageCatalogStateSupplier =
        Objects.requireNonNull(messageCatalogStateSupplier, "messageCatalogStateSupplier");
    this.snapshotLock = Objects.requireNonNull(snapshotLock, "snapshotLock");
  }

  public void ensureTargetExists(TargetRef ref) {
    docs.computeIfAbsent(ref, r -> new DefaultStyledDocument());
    stateByTarget.computeIfAbsent(ref, r -> newTranscriptState());
  }

  public ChatTranscriptState newTranscriptState() {
    return new ChatTranscriptState(
        messageCatalogStateSupplier.get(),
        new ChatTranscriptFilteredLinesSupport.State(),
        new ChatTranscriptPresenceFoldSupport.State());
  }

  public void noteEpochMs(TargetRef ref, Long epochMs) {
    if (ref == null || epochMs == null) return;
    ChatTranscriptState state = stateByTarget.get(ref);
    if (state == null) return;
    state.noteEpochMs(epochMs);
  }

  public StyledDocument document(TargetRef ref) {
    ensureTargetExists(ref);
    return docs.get(ref);
  }

  public OptionalLong earliestTimestampEpochMs(TargetRef ref) {
    if (ref == null) return OptionalLong.empty();
    ChatTranscriptState state = stateByTarget.get(ref);
    if (state == null || state.earliestEpochMsSeen() == null) return OptionalLong.empty();
    return OptionalLong.of(state.earliestEpochMsSeen());
  }

  public void closeTarget(TargetRef ref) {
    if (ref == null) return;
    docs.remove(ref);
    stateByTarget.remove(ref);
  }

  public void clearTarget(TargetRef ref) {
    if (ref == null) return;
    ensureTargetExists(ref);

    StyledDocument doc = docs.get(ref);
    if (doc == null) return;

    try {
      doc.remove(0, doc.getLength());
    } catch (Exception ignored) {
    }
    stateByTarget.put(ref, newTranscriptState());
  }

  public List<StyledDocument> snapshotDocuments() {
    synchronized (snapshotLock) {
      return new ArrayList<>(docs.values());
    }
  }
}
