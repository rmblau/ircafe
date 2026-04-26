package cafe.woden.ircclient.ui.chat.transcript.message;

import cafe.woden.ircclient.model.TargetRef;

/** Handles user actions from a rendered reaction chip in the transcript. */
@FunctionalInterface
public interface ReactionChipActionHandler {
  void onReactionAction(
      TargetRef target, String messageId, String reactionToken, boolean unreactRequested);
}
