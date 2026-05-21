package cafe.woden.ircclient.ui.chat.transcript.runtime;

import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredLinesSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptAuxiliaryRowsSupport;
import cafe.woden.ircclient.ui.chat.transcript.line.ChatTranscriptPresenceFoldSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptMessageCatalogSupport;
import cafe.woden.ircclient.ui.chat.transcript.message.ChatTranscriptReactionSummarySupport;
import java.util.Objects;

public final class ChatTranscriptState {

  private final ChatTranscriptMessageCatalogSupport.State messageCatalog;
  private final ChatTranscriptFilteredLinesSupport.State filteredLines;
  private final ChatTranscriptPresenceFoldSupport.State presenceFolds;
  private final ChatTranscriptAuxiliaryRowsSupport.State auxiliaryRows;
  private final ChatTranscriptReactionSummarySupport.State reactionSummary;

  private Long earliestEpochMsSeen;

  public ChatTranscriptState(
      ChatTranscriptMessageCatalogSupport.State messageCatalog,
      ChatTranscriptFilteredLinesSupport.State filteredLines,
      ChatTranscriptPresenceFoldSupport.State presenceFolds) {
    this.messageCatalog = Objects.requireNonNull(messageCatalog, "messageCatalog");
    this.filteredLines = Objects.requireNonNull(filteredLines, "filteredLines");
    this.presenceFolds = Objects.requireNonNull(presenceFolds, "presenceFolds");
    this.auxiliaryRows = new ChatTranscriptAuxiliaryRowsSupport.State();
    this.reactionSummary = new ChatTranscriptReactionSummarySupport.State();
  }

  public ChatTranscriptMessageCatalogSupport.State messageCatalog() {
    return messageCatalog;
  }

  public ChatTranscriptFilteredLinesSupport.State filteredLines() {
    return filteredLines;
  }

  public ChatTranscriptPresenceFoldSupport.State presenceFolds() {
    return presenceFolds;
  }

  public ChatTranscriptAuxiliaryRowsSupport.State auxiliaryRows() {
    return auxiliaryRows;
  }

  public ChatTranscriptReactionSummarySupport.State reactionSummary() {
    return reactionSummary;
  }

  public Long earliestEpochMsSeen() {
    return earliestEpochMsSeen;
  }

  public void noteEpochMs(Long epochMs) {
    if (epochMs == null) return;
    Long current = earliestEpochMsSeen;
    if (current == null || epochMs < current) {
      earliestEpochMsSeen = epochMs;
    }
  }

  public void resetAfterHeadTrim(
      ChatTranscriptPresenceFoldSupport presenceFoldSupport,
      ChatTranscriptFilteredLinesSupport filteredLinesSupport) {
    earliestEpochMsSeen = null;
    presenceFoldSupport.reset(presenceFolds);
    filteredLinesSupport.reset(filteredLines);
    auxiliaryRows.reset();
    reactionSummary.clear();
  }
}
