package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Ircv3ReplyDraftPolicyTest {

  @Test
  void removesUnsupportedReplyTagWhilePreservingOtherTagsAndCommand() {
    assertEquals(
        "  /quote @+label=42 TAGMSG #ircafe",
        Ircv3ReplyDraftPolicy.normalizeForCapability(
            "  /quote @+reply=abc;+label=42 TAGMSG #ircafe", false));
  }

  @Test
  void removesLegacyDraftReplyTag() {
    assertEquals(
        "/quote PRIVMSG #ircafe :hello",
        Ircv3ReplyDraftPolicy.normalizeForCapability(
            "/quote @+draft/reply=abc PRIVMSG #ircafe :hello", false));
  }

  @Test
  void leavesSupportedAndUnrelatedDraftsUnchanged() {
    String supported = "/quote @+reply=abc PRIVMSG #ircafe :hello";
    assertEquals(supported, Ircv3ReplyDraftPolicy.normalizeForCapability(supported, true));
    assertEquals(
        "/msg #ircafe hello",
        Ircv3ReplyDraftPolicy.normalizeForCapability("/msg #ircafe hello", false));
  }
}
