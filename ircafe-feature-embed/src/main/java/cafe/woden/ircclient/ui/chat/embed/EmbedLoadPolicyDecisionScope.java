package cafe.woden.ircclient.ui.chat.embed;

import java.util.List;
import java.util.Objects;

/** Feature-owned, config-port-independent embed loading policy scope. */
public record EmbedLoadPolicyDecisionScope(
    List<String> userWhitelist,
    List<String> userBlacklist,
    List<String> channelWhitelist,
    List<String> channelBlacklist,
    boolean requireVoiceOrOp,
    boolean requireLoggedIn,
    long minAccountAgeDays,
    List<String> linkWhitelist,
    List<String> linkBlacklist,
    List<String> domainWhitelist,
    List<String> domainBlacklist) {

  public EmbedLoadPolicyDecisionScope {
    userWhitelist = copy(userWhitelist);
    userBlacklist = copy(userBlacklist);
    channelWhitelist = copy(channelWhitelist);
    channelBlacklist = copy(channelBlacklist);
    linkWhitelist = copy(linkWhitelist);
    linkBlacklist = copy(linkBlacklist);
    domainWhitelist = copy(domainWhitelist);
    domainBlacklist = copy(domainBlacklist);
    if (minAccountAgeDays < 0) {
      minAccountAgeDays = 0;
    }
  }

  public static EmbedLoadPolicyDecisionScope defaults() {
    return new EmbedLoadPolicyDecisionScope(
        List.of(), List.of(), List.of(), List.of(), false, false, 0, List.of(), List.of(),
        List.of(), List.of());
  }

  public boolean defaultScope() {
    return userWhitelist.isEmpty()
        && userBlacklist.isEmpty()
        && channelWhitelist.isEmpty()
        && channelBlacklist.isEmpty()
        && !requireVoiceOrOp
        && !requireLoggedIn
        && minAccountAgeDays <= 0
        && linkWhitelist.isEmpty()
        && linkBlacklist.isEmpty()
        && domainWhitelist.isEmpty()
        && domainBlacklist.isEmpty();
  }

  private static List<String> copy(List<String> values) {
    return values == null
        ? List.of()
        : List.copyOf(values.stream().filter(Objects::nonNull).toList());
  }
}
