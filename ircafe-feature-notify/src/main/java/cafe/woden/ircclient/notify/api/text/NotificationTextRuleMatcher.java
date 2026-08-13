package cafe.woden.ircclient.notify.api.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Feature-owned matcher for user-configured notification text rules. */
public final class NotificationTextRuleMatcher {
  private static final NotificationTextRuleMatcher EMPTY =
      new NotificationTextRuleMatcher(List.of(), false, false, List.of());

  private final List<CompiledRule> rules;
  private final boolean needsMessageLower;
  private final boolean hasWholeWordRules;
  private final List<NotificationTextRuleCompileFailure> compileFailures;

  private NotificationTextRuleMatcher(
      List<CompiledRule> rules,
      boolean needsMessageLower,
      boolean hasWholeWordRules,
      List<NotificationTextRuleCompileFailure> compileFailures) {
    this.rules = List.copyOf(rules);
    this.needsMessageLower = needsMessageLower;
    this.hasWholeWordRules = hasWholeWordRules;
    this.compileFailures = List.copyOf(compileFailures);
  }

  public static NotificationTextRuleMatcher compile(List<NotificationTextRule> rawRules) {
    if (rawRules == null || rawRules.isEmpty()) return EMPTY;

    List<CompiledRule> compiled = new ArrayList<>(rawRules.size());
    List<NotificationTextRuleCompileFailure> failures = new ArrayList<>();
    boolean needsLower = false;
    boolean hasWholeWord = false;

    for (NotificationTextRule rule : rawRules) {
      if (rule == null) continue;
      if (!rule.enabled()) continue;
      if (rule.pattern().isEmpty()) continue;

      if (rule.type() == NotificationTextRule.Type.REGEX) {
        Pattern pattern = null;
        try {
          int flags = Pattern.UNICODE_CASE;
          if (!rule.caseSensitive()) flags |= Pattern.CASE_INSENSITIVE;
          pattern = Pattern.compile(rule.pattern(), flags);
        } catch (Exception ex) {
          failures.add(new NotificationTextRuleCompileFailure(rule.label(), ex.getMessage()));
        }
        compiled.add(CompiledRule.forRegex(rule, pattern));
      } else {
        if (!rule.caseSensitive() && !rule.wholeWord()) needsLower = true;
        if (rule.wholeWord()) hasWholeWord = true;
        String lower =
            (!rule.caseSensitive() && !rule.wholeWord())
                ? rule.pattern().toLowerCase(Locale.ROOT)
                : null;
        compiled.add(CompiledRule.forWord(rule, lower));
      }
    }

    if (compiled.isEmpty() && failures.isEmpty()) return EMPTY;
    return new NotificationTextRuleMatcher(compiled, needsLower, hasWholeWord, failures);
  }

  public List<NotificationTextMatch> matchAll(String message) {
    if (message == null || message.isBlank()) return List.of();
    if (rules.isEmpty()) return List.of();

    List<NotificationTextMatch> out = new ArrayList<>();
    List<Token> tokens = hasWholeWordRules ? tokenize(message) : List.of();
    String messageLower = needsMessageLower ? message.toLowerCase(Locale.ROOT) : null;

    for (CompiledRule compiledRule : rules) {
      NotificationTextRule rule = compiledRule.rule;
      if (!rule.enabled()) continue;

      if (rule.type() == NotificationTextRule.Type.REGEX) {
        Pattern regex = compiledRule.regex;
        if (regex == null) continue;
        Matcher matcher = regex.matcher(message);
        if (matcher.find()) {
          out.add(
              new NotificationTextMatch(
                  rule.label(),
                  rule.type(),
                  matcher.group(),
                  matcher.start(),
                  matcher.end(),
                  rule.highlightColor()));
        }
        continue;
      }

      String pattern = rule.pattern();
      if (pattern.isEmpty()) continue;

      if (rule.wholeWord()) {
        addWholeWordMatch(out, message, tokens, rule, pattern);
      } else {
        addSubstringMatch(out, message, messageLower, compiledRule, rule, pattern);
      }
    }

    return out.isEmpty() ? List.of() : Collections.unmodifiableList(out);
  }

  public List<NotificationTextRuleCompileFailure> compileFailures() {
    return compileFailures;
  }

  private static void addWholeWordMatch(
      List<NotificationTextMatch> out,
      String message,
      List<Token> tokens,
      NotificationTextRule rule,
      String pattern) {
    int patternLength = pattern.length();
    for (Token token : tokens) {
      int tokenLength = token.end - token.start;
      if (tokenLength != patternLength) continue;

      boolean matches =
          rule.caseSensitive()
              ? message.regionMatches(false, token.start, pattern, 0, patternLength)
              : message.regionMatches(true, token.start, pattern, 0, patternLength);

      if (matches) {
        out.add(
            new NotificationTextMatch(
                rule.label(),
                rule.type(),
                message.substring(token.start, token.end),
                token.start,
                token.end,
                rule.highlightColor()));
        return;
      }
    }
  }

  private static void addSubstringMatch(
      List<NotificationTextMatch> out,
      String message,
      String messageLower,
      CompiledRule compiledRule,
      NotificationTextRule rule,
      String pattern) {
    int index;
    if (rule.caseSensitive()) {
      index = message.indexOf(pattern);
    } else {
      String patternLower = compiledRule.wordLower;
      if (patternLower == null) patternLower = pattern.toLowerCase(Locale.ROOT);
      index =
          (messageLower != null ? messageLower : message.toLowerCase(Locale.ROOT))
              .indexOf(patternLower);
    }

    if (index >= 0) {
      out.add(
          new NotificationTextMatch(
              rule.label(),
              rule.type(),
              message.substring(index, index + pattern.length()),
              index,
              index + pattern.length(),
              rule.highlightColor()));
    }
  }

  private static List<Token> tokenize(String message) {
    int len = message.length();
    if (len == 0) return List.of();

    List<Token> tokens = new ArrayList<>();
    int i = 0;

    while (i < len) {
      while (i < len && !isWordChar(message.charAt(i))) i++;
      if (i >= len) break;
      int start = i;
      while (i < len && isWordChar(message.charAt(i))) i++;
      int end = i;
      tokens.add(new Token(start, end));
    }

    return tokens;
  }

  /**
   * "Word" tokenization for notification rules.
   *
   * <p>We intentionally keep this tighter than IRC nick chars; for more complex matching, users can
   * switch to REGEX rules.
   */
  private static boolean isWordChar(char ch) {
    if (ch >= '0' && ch <= '9') return true;
    if (ch >= 'A' && ch <= 'Z') return true;
    if (ch >= 'a' && ch <= 'z') return true;
    return ch == '_' || ch == '-';
  }

  private record Token(int start, int end) {}

  private static final class CompiledRule {
    private final NotificationTextRule rule;
    private final Pattern regex;
    private final String wordLower;

    private CompiledRule(NotificationTextRule rule, Pattern regex, String wordLower) {
      this.rule = rule;
      this.regex = regex;
      this.wordLower = wordLower;
    }

    static CompiledRule forRegex(NotificationTextRule rule, Pattern regex) {
      return new CompiledRule(rule, regex, null);
    }

    static CompiledRule forWord(NotificationTextRule rule, String wordLower) {
      return new CompiledRule(rule, null, wordLower);
    }
  }
}
