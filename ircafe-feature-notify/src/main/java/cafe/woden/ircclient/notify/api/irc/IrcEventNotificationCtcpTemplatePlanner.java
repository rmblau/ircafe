package cafe.woden.ircclient.notify.api.irc;

import java.util.Locale;
import java.util.Set;

/** Feature-owned CTCP notification rule template application policy. */
public final class IrcEventNotificationCtcpTemplatePlanner {
  public static final String CUSTOM = "CUSTOM";
  public static final String VERSION = "VERSION";
  public static final String PING = "PING";
  public static final String TIME = "TIME";
  public static final String CLIENTINFO = "CLIENTINFO";
  public static final String SOURCE = "SOURCE";
  public static final String USERINFO = "USERINFO";

  private static final String CTCP_RECEIVED = "CTCP_RECEIVED";
  private static final String ANY = "ANY";
  private static final String LIKE = "LIKE";

  private static final Set<String> COMMAND_TEMPLATES =
      Set.of(VERSION, PING, TIME, CLIENTINFO, SOURCE, USERINFO);

  private IrcEventNotificationCtcpTemplatePlanner() {}

  /** Builds the feature-safe field plan for the supplied CTCP template identifier. */
  public static IrcEventNotificationCtcpTemplatePlan plan(String templateId) {
    String template = normalizeTemplateId(templateId);
    if (COMMAND_TEMPLATES.contains(template)) {
      return new IrcEventNotificationCtcpTemplatePlan(CTCP_RECEIVED, LIKE, template, ANY, "");
    }
    return new IrcEventNotificationCtcpTemplatePlan(CTCP_RECEIVED, ANY, "", ANY, "");
  }

  private static String normalizeTemplateId(String templateId) {
    String normalized = templateId == null ? "" : templateId.trim().toUpperCase(Locale.ROOT);
    return normalized.isEmpty() ? CUSTOM : normalized;
  }
}
