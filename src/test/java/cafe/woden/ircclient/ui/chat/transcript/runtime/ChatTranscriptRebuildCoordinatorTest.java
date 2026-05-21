package cafe.woden.ircclient.ui.chat.transcript.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.logging.history.ChatHistoryService;
import cafe.woden.ircclient.model.TargetRef;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.slf4j.Logger;

class ChatTranscriptRebuildCoordinatorTest {

  @Test
  void skipsNullAndUiOnlyTargets() {
    UiPort ui = mock(UiPort.class);
    ChatHistoryService history = mock(ChatHistoryService.class);
    ChatTranscriptRebuildCoordinator coordinator = newCoordinator(ui, history);

    assertFalse(coordinator.rebuild(null));
    assertFalse(coordinator.rebuild(TargetRef.applicationTerminal()));

    verifyNoInteractions(ui, history);
  }

  @Test
  void skipsWhenRecentHistoryCannotBeReloaded() {
    UiPort ui = mock(UiPort.class);
    ChatHistoryService history = mock(ChatHistoryService.class);
    ChatTranscriptRebuildCoordinator coordinator = newCoordinator(ui, history);
    TargetRef target = new TargetRef("srv", "#chan");

    when(history.canReloadRecent(target)).thenReturn(false);

    assertFalse(coordinator.rebuild(target));

    verify(history).canReloadRecent(target);
    verify(history, never()).reset(target);
    verify(history, never()).reloadRecent(target);
    verifyNoInteractions(ui);
  }

  @Test
  void clearsAndReloadsWhenRecentHistoryCanBeReloaded() {
    UiPort ui = mock(UiPort.class);
    ChatHistoryService history = mock(ChatHistoryService.class);
    ChatTranscriptRebuildCoordinator coordinator = newCoordinator(ui, history);
    TargetRef target = new TargetRef("srv", "#chan");

    when(history.canReloadRecent(target)).thenReturn(true);

    assertTrue(coordinator.rebuild(target));

    InOrder inOrder = inOrder(history, ui);
    inOrder.verify(history).canReloadRecent(target);
    inOrder.verify(history).reset(target);
    inOrder.verify(ui).clearTranscript(target);
    inOrder.verify(history).reloadRecent(target);
  }

  private static ChatTranscriptRebuildCoordinator newCoordinator(
      UiPort ui, ChatHistoryService history) {
    return new ChatTranscriptRebuildCoordinator(ui, history, mock(Logger.class));
  }
}
