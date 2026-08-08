package cafe.woden.ircclient.irc.ircv3;

import java.util.Objects;

/** Typing-specific projection of RPL_ISUPPORT CLIENTTAGDENY policy. */
public final class Ircv3TypingClientTagPolicy {

  private static final String TYPING_TAG = "typing";

  private Ircv3TypingClientTagPolicy() {}

  public static Observation parseRpl005(String rawLine) {
    String denyValue = Ircv3ClientTagPolicy.parseRpl005ClientTagDenyValue(rawLine);
    if (denyValue == null) {
      return null;
    }
    return new Observation(
        Ircv3ClientTagPolicy.isClientOnlyTagAllowed(denyValue, TYPING_TAG), denyValue);
  }

  public record Observation(boolean allowed, String rawDenyValue) {
    public Observation {
      rawDenyValue = Objects.toString(rawDenyValue, "").trim();
    }
  }
}
