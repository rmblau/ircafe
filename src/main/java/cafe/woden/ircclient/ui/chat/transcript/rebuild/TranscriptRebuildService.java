package cafe.woden.ircclient.ui.chat.transcript.rebuild;

import cafe.woden.ircclient.app.api.UiPort;
import cafe.woden.ircclient.logging.history.ChatHistoryService;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.chat.transcript.runtime.ChatTranscriptRebuildCoordinator;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Clears a target's transcript and reloads the recent history slice so features like
 * filtering/placeholder folding can be applied to already-existing messages.
 */
@Component
@InterfaceLayer
@Lazy
public class TranscriptRebuildService {

  private final ChatTranscriptRebuildCoordinator coordinator;

  public TranscriptRebuildService(UiPort ui, ChatHistoryService history) {
    this.coordinator =
        new ChatTranscriptRebuildCoordinator(
            ui, history, LoggerFactory.getLogger(TranscriptRebuildService.class));
  }

  /**
   * Rebuilds the transcript for a specific target.
   *
   * @return true if a history reload was kicked off, false if rebuild was skipped.
   */
  public boolean rebuild(TargetRef target) {
    return coordinator.rebuild(target);
  }
}
