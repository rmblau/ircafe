package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;

/** Capability-specific policy for staged {@code draft/react} and {@code draft/unreact} commands. */
public final class Ircv3ReactionDraftPolicy {

  private static final String DRAFT_REACT = "draft/react";
  private static final String DRAFT_UNREACT = "draft/unreact";

  private Ircv3ReactionDraftPolicy() {}

  /**
   * Clears a staged reaction command when reaction support or its required reply target metadata is
   * unavailable.
   */
  public static String normalizeForCapabilities(
      String draft, boolean replySupported, boolean reactionSupported) {
    String raw = Objects.toString(draft, "");
    if (raw.isBlank() || (replySupported && reactionSupported)) return raw;

    boolean isReactionDraft =
        Ircv3TaggedCommandDraft.parse(raw)
            .map(taggedDraft -> taggedDraft.hasAnyTag(DRAFT_REACT, DRAFT_UNREACT))
            .orElse(false);
    return isReactionDraft ? "" : raw;
  }
}
