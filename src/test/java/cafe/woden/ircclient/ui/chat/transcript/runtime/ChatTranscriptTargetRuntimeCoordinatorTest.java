package cafe.woden.ircclient.ui.chat.transcript.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.ChatStyles;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageStateSupport;
import org.junit.jupiter.api.Test;

class ChatTranscriptTargetRuntimeCoordinatorTest {

  @Test
  void documentCreatesTargetAndTracksEarliestEpoch() {
    ChatTranscriptMessageCatalogSupport messageCatalogSupport =
        new ChatTranscriptMessageCatalogSupport(
            new ChatTranscriptMessageStateSupport.Context(120, "[redacted]", () -> 1L));
    ChatTranscriptTargetRuntimeCoordinator coordinator =
        new ChatTranscriptTargetRuntimeCoordinator(
            () -> messageCatalogSupport.createState(32, 32),
            new Object(),
            180,
            new ChatTranscriptRestyleSupport.Context(
                new ChatStyles(null), null, (attrs, action) -> {}),
            () -> null,
            settings -> null,
            null);
    TargetRef ref = new TargetRef("srv", "#chan");

    assertNotNull(coordinator.document(ref));
    coordinator.noteEpochMs(ref, 2_000L);
    coordinator.noteEpochMs(ref, 1_000L);

    assertEquals(1_000L, coordinator.earliestTimestampEpochMs(ref).orElseThrow());
    assertNotNull(coordinator.docs().get(ref));
    assertNotNull(coordinator.stateByTarget().get(ref));
  }
}
