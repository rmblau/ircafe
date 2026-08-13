package cafe.woden.ircclient.ui.chat.embed;

import java.util.Objects;

/** Feature-owned sender facts used by embed load policy decisions. */
public record EmbedLoadPolicySenderFacts(
    String nick, String hostmask, boolean loggedIn, boolean voiceOrOp, long accountAgeDays) {

  public static final long UNKNOWN_ACCOUNT_AGE_DAYS = -1L;

  public EmbedLoadPolicySenderFacts {
    nick = Objects.toString(nick, "").trim();
    hostmask = Objects.toString(hostmask, "").trim();
    if (accountAgeDays < 0) {
      accountAgeDays = UNKNOWN_ACCOUNT_AGE_DAYS;
    }
  }

  public static EmbedLoadPolicySenderFacts empty() {
    return new EmbedLoadPolicySenderFacts("", "", false, false, UNKNOWN_ACCOUNT_AGE_DAYS);
  }
}
