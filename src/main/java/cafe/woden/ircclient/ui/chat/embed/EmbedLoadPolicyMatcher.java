package cafe.woden.ircclient.ui.chat.embed;

import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicyScope;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.ignore.IgnoreMaskMatcher;
import cafe.woden.ircclient.irc.IrcEvent.AccountState;
import cafe.woden.ircclient.irc.IrcEvent.NickInfo;
import cafe.woden.ircclient.irc.roster.UserListPort;
import cafe.woden.ircclient.model.TargetRef;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Evaluates whether inline image/link loading is allowed for a message URL. */
@Component
@InterfaceLayer
@Lazy
public class EmbedLoadPolicyMatcher {

  private static final Set<Character> VOICE_OR_OP_PREFIXES = Set.of('+', '%', '@', '&', '~');

  private final EmbedLoadPolicyBus policyBus;
  private final UserListPort userListStore;
  private final EmbedLoadPolicyDecisionService decisionService;
  private final EmbedLoadPolicyTagFactsParser tagFactsParser;

  @Autowired
  public EmbedLoadPolicyMatcher(
      EmbedLoadPolicyBus policyBus,
      UserListPort userListStore,
      EmbedLoadPolicyDecisionService decisionService,
      EmbedLoadPolicyTagFactsParser tagFactsParser) {
    this.policyBus = policyBus;
    this.userListStore = userListStore;
    this.decisionService =
        decisionService != null ? decisionService : new EmbedLoadPolicyDecisionService();
    this.tagFactsParser =
        tagFactsParser != null ? tagFactsParser : new EmbedLoadPolicyTagFactsParser();
  }

  public EmbedLoadPolicyMatcher(
      EmbedLoadPolicyBus policyBus,
      UserListPort userListStore,
      EmbedLoadPolicyDecisionService decisionService) {
    this(policyBus, userListStore, decisionService, new EmbedLoadPolicyTagFactsParser());
  }

  public EmbedLoadPolicyMatcher(EmbedLoadPolicyBus policyBus, UserListPort userListStore) {
    this(policyBus, userListStore, new EmbedLoadPolicyDecisionService());
  }

  public boolean allow(
      TargetRef target, String fromNick, Map<String, String> ircv3Tags, String url) {
    String normalizedUrl = Objects.toString(url, "").trim();
    if (target == null || normalizedUrl.isEmpty()) return true;

    EmbedLoadPolicySnapshot policy = policyBus.get();
    EmbedLoadPolicyScope scope = policy.scopeForServer(target.serverId());
    if (scope == null || scope.isDefaultScope()) return true;

    EmbedLoadPolicySenderFacts sender = resolveSenderFacts(target, fromNick, ircv3Tags);
    String channel = target.isChannel() ? target.target() : "";
    return decisionService.allow(toDecisionScope(scope), channel, sender, normalizedUrl);
  }

  public static Optional<String> validatePatternSyntax(String rawPattern) {
    return new EmbedLoadPolicyDecisionService().validatePatternSyntax(rawPattern);
  }

  private static EmbedLoadPolicyDecisionScope toDecisionScope(EmbedLoadPolicyScope scope) {
    if (scope == null) {
      return EmbedLoadPolicyDecisionScope.defaults();
    }
    return new EmbedLoadPolicyDecisionScope(
        scope.userWhitelist(),
        scope.userBlacklist(),
        scope.channelWhitelist(),
        scope.channelBlacklist(),
        scope.requireVoiceOrOp(),
        scope.requireLoggedIn(),
        scope.minAccountAgeDays(),
        scope.linkWhitelist(),
        scope.linkBlacklist(),
        scope.domainWhitelist(),
        scope.domainBlacklist());
  }

  private EmbedLoadPolicySenderFacts resolveSenderFacts(
      TargetRef target, String fromNick, Map<String, String> ircv3Tags) {
    String serverId = Objects.toString(target != null ? target.serverId() : "", "").trim();
    String nick = Objects.toString(fromNick, "").trim();
    String channel = target != null && target.isChannel() ? target.target() : "";

    NickInfo nickInfo = findNickInfo(serverId, channel, nick);
    String hostmask = "";
    boolean loggedIn = false;
    boolean voiceOrOp = false;

    if (nickInfo != null) {
      String hm = Objects.toString(nickInfo.hostmask(), "").trim();
      if (IgnoreMaskMatcher.isUsefulHostmask(hm)) {
        hostmask = hm;
      }
      String prefix = Objects.toString(nickInfo.prefix(), "");
      voiceOrOp = hasVoiceOrOp(prefix);
      AccountState accountState =
          nickInfo.accountState() == null ? AccountState.UNKNOWN : nickInfo.accountState();
      loggedIn = accountState == AccountState.LOGGED_IN;
    }

    if (hostmask.isBlank()) {
      String learned = userListStore.getLearnedHostmask(serverId, nick);
      if (IgnoreMaskMatcher.isUsefulHostmask(learned)) {
        hostmask = learned;
      }
    }

    EmbedLoadPolicyTagFacts tagFacts = tagFactsParser.parse(ircv3Tags);
    if (tagFacts.loggedInKnown()) {
      loggedIn = tagFacts.loggedIn();
    }

    return new EmbedLoadPolicySenderFacts(
        nick, hostmask, loggedIn, voiceOrOp, tagFacts.accountAgeDays());
  }

  private NickInfo findNickInfo(String serverId, String channel, String nick) {
    String sid = Objects.toString(serverId, "").trim();
    String ch = Objects.toString(channel, "").trim();
    String n = Objects.toString(nick, "").trim();
    if (sid.isEmpty() || n.isEmpty()) return null;

    if (!ch.isEmpty()) {
      NickInfo ni = findNickInfoInRoster(userListStore.get(sid, ch), n);
      if (ni != null) return ni;
    }

    for (String serverChannel : userListStore.channelsContainingNick(sid, n)) {
      NickInfo ni = findNickInfoInRoster(userListStore.get(sid, serverChannel), n);
      if (ni != null) return ni;
    }
    return null;
  }

  private static NickInfo findNickInfoInRoster(List<NickInfo> roster, String nick) {
    if (roster == null || roster.isEmpty()) return null;
    String want = Objects.toString(nick, "").trim();
    if (want.isEmpty()) return null;
    for (NickInfo ni : roster) {
      if (ni == null) continue;
      String present = Objects.toString(ni.nick(), "").trim();
      if (!present.isEmpty() && present.equalsIgnoreCase(want)) {
        return ni;
      }
    }
    return null;
  }

  private static boolean hasVoiceOrOp(String prefix) {
    String p = Objects.toString(prefix, "");
    if (p.isEmpty()) return false;
    for (int i = 0; i < p.length(); i++) {
      if (VOICE_OR_OP_PREFIXES.contains(p.charAt(i))) {
        return true;
      }
    }
    return false;
  }
}
