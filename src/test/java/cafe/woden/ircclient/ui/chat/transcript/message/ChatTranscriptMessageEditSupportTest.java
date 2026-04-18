package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChatTranscriptMessageEditSupportTest {

  @Test
  void renderEditedTextAppendsEditedMarkerForNonBlankText() {
    assertEquals("after (edited)", ChatTranscriptMessageEditSupport.renderEditedText("after"));
  }

  @Test
  void renderEditedTextFallsBackToEditedMarkerForBlankText() {
    assertEquals("(edited)", ChatTranscriptMessageEditSupport.renderEditedText("   "));
    assertEquals("(edited)", ChatTranscriptMessageEditSupport.renderEditedText(null));
  }
}
