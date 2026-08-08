package cafe.woden.ircclient.notifications.api;

import cafe.woden.ircclient.model.IrcEventNotificationRule;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationActionRule;
import cafe.woden.ircclient.notify.api.irc.IrcEventNotificationMatchRule;
import java.util.List;
import java.util.Objects;

/** Adapts root IRC-event notification rules into feature-owned notify policy values. */
public final class IrcEventNotificationRuleAdapters {

  private IrcEventNotificationRuleAdapters() {}

  public static List<IrcEventNotificationMatchRule> toMatchRules(
      List<IrcEventNotificationRule> rules) {
    if (rules == null || rules.isEmpty()) return List.of();
    return rules.stream()
        .filter(Objects::nonNull)
        .map(IrcEventNotificationRuleAdapters::toMatchRule)
        .toList();
  }

  public static IrcEventNotificationMatchRule toMatchRule(IrcEventNotificationRule rule) {
    if (rule == null) return null;
    return new IrcEventNotificationMatchRule(
        rule.enabled(),
        enumName(rule.eventType()),
        enumName(rule.sourceMode()),
        rule.sourcePattern(),
        enumName(rule.channelScope()),
        rule.channelPatterns(),
        enumName(rule.ctcpCommandMode()),
        rule.ctcpCommandPattern(),
        enumName(rule.ctcpValueMode()),
        rule.ctcpValuePattern());
  }

  public static IrcEventNotificationActionRule toActionRule(IrcEventNotificationRule rule) {
    if (rule == null) return null;
    return new IrcEventNotificationActionRule(
        rule.notificationsNodeEnabled(),
        rule.toastEnabled(),
        enumName(rule.focusScope()),
        rule.statusBarEnabled(),
        rule.soundEnabled(),
        rule.soundId(),
        rule.soundUseCustom(),
        rule.soundCustomPath(),
        rule.scriptEnabled(),
        rule.scriptPath(),
        rule.scriptArgs(),
        rule.scriptWorkingDirectory());
  }

  public static IrcEventNotificationRule.FocusScope toFocusScope(
      String value, IrcEventNotificationRule.FocusScope fallback) {
    IrcEventNotificationRule.FocusScope safeFallback =
        fallback != null ? fallback : IrcEventNotificationRule.FocusScope.BACKGROUND_ONLY;
    if (value == null || value.isBlank()) return safeFallback;
    try {
      return IrcEventNotificationRule.FocusScope.valueOf(value.trim());
    } catch (Exception ignored) {
      return safeFallback;
    }
  }

  private static String enumName(Enum<?> value) {
    return value == null ? null : value.name();
  }
}
