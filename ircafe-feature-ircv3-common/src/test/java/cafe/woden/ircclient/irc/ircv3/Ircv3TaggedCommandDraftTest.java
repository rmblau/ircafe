package cafe.woden.ircclient.irc.ircv3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Ircv3TaggedCommandDraftTest {

  @Test
  void parsesNormalizedClientOnlyTagKeys() {
    Ircv3TaggedCommandDraft draft =
        Ircv3TaggedCommandDraft.parse("  /quote @+draft/react=:+1:;+reply=abc TAGMSG #ircafe")
            .orElseThrow();

    assertTrue(draft.hasAnyTag("draft/react"));
    assertTrue(draft.hasAnyTag("+REPLY"));
    assertFalse(draft.hasAnyTag("draft/unreact"));
  }

  @Test
  void removesSelectedTagsWhilePreservingOtherTagsAndCommand() {
    Ircv3TaggedCommandDraft draft =
        Ircv3TaggedCommandDraft.parse("  /quote @+reply=abc;+label=42 TAGMSG #ircafe")
            .orElseThrow();

    assertEquals("  /quote @+label=42 TAGMSG #ircafe", draft.withoutTags("reply"));
  }

  @Test
  void removesTagSectionWhenNoTagsRemain() {
    Ircv3TaggedCommandDraft draft =
        Ircv3TaggedCommandDraft.parse("/quote @+reply=abc PRIVMSG #ircafe :hello").orElseThrow();

    assertEquals("/quote PRIVMSG #ircafe :hello", draft.withoutTags("reply"));
  }

  @Test
  void rejectsNonQuoteAndTagOnlyDrafts() {
    assertTrue(Ircv3TaggedCommandDraft.parse("/msg #ircafe hello").isEmpty());
    assertTrue(Ircv3TaggedCommandDraft.parse("/quote @+reply=abc").isEmpty());
  }
}
