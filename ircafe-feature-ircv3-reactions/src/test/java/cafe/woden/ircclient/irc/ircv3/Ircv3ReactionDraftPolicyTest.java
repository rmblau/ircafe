package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3ReactionDraftPolicyTest {

  @Test
  void clearsReactionPrefillWhenReplyOrReactionSupportIsMissing() {
    String draft = "/quote @+draft/react=:+1:;+reply=abc TAGMSG #ircafe";
    assertEquals("", Ircv3ReactionDraftPolicy.normalizeForCapabilities(draft, false, true));
    assertEquals("", Ircv3ReactionDraftPolicy.normalizeForCapabilities(draft, true, false));
  }

  @Test
  void clearsUnreactionPrefillWhenReactionSupportIsMissing() {
    String draft = "/quote @+draft/unreact=:+1:;+reply=abc TAGMSG #ircafe";
    assertEquals("", Ircv3ReactionDraftPolicy.normalizeForCapabilities(draft, true, false));
  }

  @Test
  void leavesSupportedAndUnrelatedDraftsUnchanged() {
    String supported = "/quote @+draft/react=:+1:;+reply=abc TAGMSG #ircafe";
    assertEquals(
        supported, Ircv3ReactionDraftPolicy.normalizeForCapabilities(supported, true, true));
    assertEquals(
        "/me waves",
        Ircv3ReactionDraftPolicy.normalizeForCapabilities("/me waves", false, false));
  }
}
