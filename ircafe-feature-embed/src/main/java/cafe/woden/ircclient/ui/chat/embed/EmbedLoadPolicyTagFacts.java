package cafe.woden.ircclient.ui.chat.embed;

/** Feature-safe sender facts derived from IRCv3 tags used by embed load policy checks. */
public record EmbedLoadPolicyTagFacts(
    boolean loggedInKnown, boolean loggedIn, long accountAgeDays) {

  public EmbedLoadPolicyTagFacts {
    if (accountAgeDays < 0) {
      accountAgeDays = EmbedLoadPolicySenderFacts.UNKNOWN_ACCOUNT_AGE_DAYS;
    }
  }

  public static EmbedLoadPolicyTagFacts empty() {
    return new EmbedLoadPolicyTagFacts(
        false, false, EmbedLoadPolicySenderFacts.UNKNOWN_ACCOUNT_AGE_DAYS);
  }

  public static EmbedLoadPolicyTagFacts of(
      boolean loggedInKnown, boolean loggedIn, long accountAgeDays) {
    return new EmbedLoadPolicyTagFacts(loggedInKnown, loggedIn, accountAgeDays);
  }
}
