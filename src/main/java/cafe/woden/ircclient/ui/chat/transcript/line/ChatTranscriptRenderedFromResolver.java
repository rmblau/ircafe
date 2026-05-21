package cafe.woden.ircclient.ui.chat.transcript.line;

import cafe.woden.ircclient.model.TargetRef;

/** Resolves the sender label rendered into a transcript line. */
@FunctionalInterface
public interface ChatTranscriptRenderedFromResolver {
  String render(TargetRef ref, String from);
}
