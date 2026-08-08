package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;

/** Capability-specific policy for staged {@code reply} and legacy {@code draft/reply} tags. */
public final class Ircv3ReplyDraftPolicy {

  private static final String REPLY = "reply";
  private static final String DRAFT_REPLY = "draft/reply";

  private Ircv3ReplyDraftPolicy() {}

  /** Removes reply tags from a staged tagged command when reply support is unavailable. */
  public static String normalizeForCapability(String draft, boolean replySupported) {
    String raw = Objects.toString(draft, "");
    if (replySupported || raw.isBlank()) return raw;

    return Ircv3TaggedCommandDraft.parse(raw)
        .map(taggedDraft -> taggedDraft.withoutTags(REPLY, DRAFT_REPLY))
        .orElse(raw);
  }
}
