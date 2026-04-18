package cafe.woden.ircclient.ui.chat.transcript;

import java.util.Objects;

final class ChatTranscriptState {

  final ChatTranscriptMessageCatalogSupport.State messageCatalog;
  final ChatTranscriptFilteredLinesSupport.State filteredLines;
  final ChatTranscriptPresenceFoldSupport.State presenceFolds;
  final ChatTranscriptAuxiliaryRowsSupport.State auxiliaryRows;
  final ChatTranscriptReactionSummarySupport.State reactionSummary;

  Long earliestEpochMsSeen;

  ChatTranscriptState(
      ChatTranscriptMessageCatalogSupport.State messageCatalog,
      ChatTranscriptFilteredLinesSupport.State filteredLines,
      ChatTranscriptPresenceFoldSupport.State presenceFolds) {
    this.messageCatalog = Objects.requireNonNull(messageCatalog, "messageCatalog");
    this.filteredLines = Objects.requireNonNull(filteredLines, "filteredLines");
    this.presenceFolds = Objects.requireNonNull(presenceFolds, "presenceFolds");
    this.auxiliaryRows = new ChatTranscriptAuxiliaryRowsSupport.State();
    this.reactionSummary = new ChatTranscriptReactionSummarySupport.State();
  }

  void resetAfterHeadTrim(
      ChatTranscriptPresenceFoldSupport presenceFoldSupport,
      ChatTranscriptFilteredLinesSupport filteredLinesSupport) {
    earliestEpochMsSeen = null;
    presenceFoldSupport.reset(presenceFolds);
    filteredLinesSupport.reset(filteredLines);
    auxiliaryRows.reset();
    reactionSummary.clear();
  }
}
