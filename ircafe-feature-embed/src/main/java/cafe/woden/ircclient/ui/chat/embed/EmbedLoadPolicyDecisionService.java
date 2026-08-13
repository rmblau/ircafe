package cafe.woden.ircclient.ui.chat.embed;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Feature-owned embed/link load decision rules over already-resolved policy and sender facts. */
@Component
@InterfaceLayer
@Lazy
public class EmbedLoadPolicyDecisionService {

  public boolean allow(
      EmbedLoadPolicyDecisionScope scope,
      String channel,
      EmbedLoadPolicySenderFacts sender,
      String url) {
    EmbedLoadPolicyDecisionScope effectiveScope =
        scope != null ? scope : EmbedLoadPolicyDecisionScope.defaults();
    if (effectiveScope.defaultScope()) {
      return true;
    }

    EmbedLoadPolicySenderFacts effectiveSender =
        sender != null ? sender : EmbedLoadPolicySenderFacts.empty();
    String normalizedUrl = Objects.toString(url, "").trim();
    String normalizedChannel = Objects.toString(channel, "").trim();

    if (!allowByUserRules(
        effectiveScope.userWhitelist(), effectiveScope.userBlacklist(), effectiveSender)) {
      return false;
    }
    if (!allowBySimpleRules(
        effectiveScope.channelWhitelist(), effectiveScope.channelBlacklist(), normalizedChannel)) {
      return false;
    }

    if (effectiveScope.requireVoiceOrOp() && !effectiveSender.voiceOrOp()) {
      return false;
    }
    if (effectiveScope.requireLoggedIn() && !effectiveSender.loggedIn()) {
      return false;
    }
    if (effectiveScope.minAccountAgeDays() > 0) {
      long ageDays = effectiveSender.accountAgeDays();
      if (ageDays < 0 || ageDays < effectiveScope.minAccountAgeDays()) {
        return false;
      }
    }

    if (!allowBySimpleRules(
        effectiveScope.linkWhitelist(), effectiveScope.linkBlacklist(), normalizedUrl)) {
      return false;
    }

    String domain = extractDomain(normalizedUrl);
    return allowBySimpleRules(
        effectiveScope.domainWhitelist(), effectiveScope.domainBlacklist(), domain);
  }

  public Optional<String> validatePatternSyntax(String rawPattern) {
    String pattern = Objects.toString(rawPattern, "").trim();
    if (pattern.isEmpty()) {
      return Optional.empty();
    }

    if (startsWithIgnoreCase(pattern, "nick:") || startsWithIgnoreCase(pattern, "host:")) {
      pattern = pattern.substring(5).trim();
    }
    if (pattern.isEmpty()) {
      return Optional.of("empty pattern");
    }

    if (startsWithIgnoreCase(pattern, "re:")) {
      return validateRegexPattern(pattern.substring(3).trim());
    }
    if (startsWithIgnoreCase(pattern, "regex:")) {
      return validateRegexPattern(pattern.substring(6).trim());
    }
    if (startsWithIgnoreCase(pattern, "glob:")) {
      pattern = pattern.substring(5).trim();
    }
    if (pattern.isEmpty()) {
      return Optional.of("empty pattern");
    }
    return Optional.empty();
  }

  private static boolean allowByUserRules(
      List<String> whitelist, List<String> blacklist, EmbedLoadPolicySenderFacts sender) {
    String nick = sender.nick();
    String hostmask = sender.hostmask();

    if (matchesAnyUserPattern(blacklist, nick, hostmask)) {
      return false;
    }
    if (whitelist == null || whitelist.isEmpty()) {
      return true;
    }
    return matchesAnyUserPattern(whitelist, nick, hostmask);
  }

  private static boolean matchesAnyUserPattern(
      List<String> patterns, String nick, String hostmask) {
    if (patterns == null || patterns.isEmpty()) {
      return false;
    }
    for (String pattern : patterns) {
      if (matchesUserPattern(pattern, nick, hostmask)) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesUserPattern(String rawPattern, String nick, String hostmask) {
    String pattern = Objects.toString(rawPattern, "").trim();
    if (pattern.isEmpty()) {
      return false;
    }

    UserMatchTarget target = UserMatchTarget.ANY;
    if (startsWithIgnoreCase(pattern, "nick:")) {
      target = UserMatchTarget.NICK;
      pattern = pattern.substring(5).trim();
    } else if (startsWithIgnoreCase(pattern, "host:")) {
      target = UserMatchTarget.HOST;
      pattern = pattern.substring(5).trim();
    }

    return switch (target) {
      case NICK -> matchesPattern(pattern, nick);
      case HOST -> matchesHostPattern(pattern, hostmask);
      case ANY -> matchesPattern(pattern, nick) || matchesHostPattern(pattern, hostmask);
    };
  }

  private static boolean allowBySimpleRules(
      List<String> whitelist, List<String> blacklist, String candidate) {
    if (matchesAnyPattern(blacklist, candidate)) {
      return false;
    }
    if (whitelist == null || whitelist.isEmpty()) {
      return true;
    }
    return matchesAnyPattern(whitelist, candidate);
  }

  private static boolean matchesAnyPattern(List<String> patterns, String candidate) {
    if (patterns == null || patterns.isEmpty()) {
      return false;
    }
    for (String pattern : patterns) {
      if (matchesPattern(pattern, candidate)) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesPattern(String rawPattern, String candidate) {
    String pattern = Objects.toString(rawPattern, "").trim();
    String value = Objects.toString(candidate, "").trim();
    if (pattern.isEmpty() || value.isEmpty()) {
      return false;
    }

    if (startsWithIgnoreCase(pattern, "re:")) {
      return matchesRegex(pattern.substring(3).trim(), value);
    }
    if (startsWithIgnoreCase(pattern, "regex:")) {
      return matchesRegex(pattern.substring(6).trim(), value);
    }
    if (startsWithIgnoreCase(pattern, "glob:")) {
      pattern = pattern.substring(5).trim();
    }
    if (pattern.isEmpty()) {
      return false;
    }
    return globMatches(pattern, value);
  }

  private static boolean matchesHostPattern(String rawPattern, String hostmask) {
    String hm = Objects.toString(hostmask, "").trim();
    if (hm.isEmpty()) {
      return false;
    }
    if (matchesPattern(rawPattern, hm)) {
      return true;
    }

    int at = hm.indexOf('@');
    if (at >= 0 && at < hm.length() - 1) {
      String hostOnly = hm.substring(at + 1).trim();
      if (matchesPattern(rawPattern, hostOnly)) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesRegex(String regexBody, String value) {
    String body = Objects.toString(regexBody, "").trim();
    if (body.isEmpty()) {
      return false;
    }
    try {
      return Pattern.compile(body, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
          .matcher(value)
          .find();
    } catch (Exception ignored) {
      return false;
    }
  }

  private static Optional<String> validateRegexPattern(String regexBody) {
    String body = Objects.toString(regexBody, "").trim();
    if (body.isEmpty()) {
      return Optional.of("empty regex");
    }
    try {
      Pattern.compile(body, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
      return Optional.empty();
    } catch (Exception ex) {
      String message = Objects.toString(ex.getMessage(), "").trim();
      if (message.isEmpty()) {
        message = "invalid regex";
      }
      return Optional.of(message);
    }
  }

  private static boolean startsWithIgnoreCase(String value, String prefix) {
    String v = Objects.toString(value, "");
    String p = Objects.toString(prefix, "");
    if (v.length() < p.length()) {
      return false;
    }
    return v.regionMatches(true, 0, p, 0, p.length());
  }

  private static String extractDomain(String rawUrl) {
    String url = Objects.toString(rawUrl, "").trim();
    if (url.isEmpty()) {
      return "";
    }
    try {
      URI uri = URI.create(url);
      String host = Objects.toString(uri.getHost(), "").trim();
      if (host.isEmpty()) {
        return "";
      }
      return host.toLowerCase(Locale.ROOT);
    } catch (Exception ignored) {
      return "";
    }
  }

  private static boolean globMatches(String glob, String text) {
    String ptn = Objects.toString(glob, "").trim().toLowerCase(Locale.ROOT);
    String txt = Objects.toString(text, "").trim().toLowerCase(Locale.ROOT);
    if (ptn.isEmpty() || txt.isEmpty()) {
      return false;
    }

    int p = 0;
    int t = 0;
    int starIdx = -1;
    int match = 0;

    while (t < txt.length()) {
      if (p < ptn.length() && (ptn.charAt(p) == '?' || ptn.charAt(p) == txt.charAt(t))) {
        p++;
        t++;
      } else if (p < ptn.length() && ptn.charAt(p) == '*') {
        starIdx = p;
        match = t;
        p++;
      } else if (starIdx != -1) {
        p = starIdx + 1;
        match++;
        t = match;
      } else {
        return false;
      }
    }

    while (p < ptn.length() && ptn.charAt(p) == '*') {
      p++;
    }
    return p == ptn.length();
  }

  private enum UserMatchTarget {
    ANY,
    NICK,
    HOST
  }
}
