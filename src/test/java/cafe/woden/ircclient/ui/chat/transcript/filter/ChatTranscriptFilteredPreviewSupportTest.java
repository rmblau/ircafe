package cafe.woden.ircclient.ui.chat.transcript;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.ui.chat.transcript.filter.ChatTranscriptFilteredPreviewSupport;
import org.junit.jupiter.api.Test;

class ChatTranscriptFilteredPreviewSupportTest {

  @Test
  void previewChatLinePrefixesSenderWhenPresent() {
    assertEquals(
        "alice: hello", ChatTranscriptFilteredPreviewSupport.previewChatLine("alice", "hello"));
    assertEquals("hello", ChatTranscriptFilteredPreviewSupport.previewChatLine("   ", "hello"));
    assertEquals("", ChatTranscriptFilteredPreviewSupport.previewChatLine(null, null));
  }

  @Test
  void previewActionLinePreservesCurrentActionSpacingRules() {
    assertEquals(
        "* alice waves", ChatTranscriptFilteredPreviewSupport.previewActionLine("alice", "waves"));
    assertEquals("*  waves", ChatTranscriptFilteredPreviewSupport.previewActionLine(null, "waves"));
    assertEquals("* alice ", ChatTranscriptFilteredPreviewSupport.previewActionLine("alice", null));
  }
}
