package cafe.woden.ircclient.ui.userlist;

import cafe.woden.ircclient.ignore.IgnoreMaskMatcher;
import cafe.woden.ircclient.irc.IrcEvent.AccountState;
import cafe.woden.ircclient.irc.IrcEvent.AwayState;
import cafe.woden.ircclient.irc.IrcEvent.NickInfo;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
public class UserListNickTooltipBuilder {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  public String build(NickInfo nickInfo, boolean ignored, boolean softIgnored) {
    if (nickInfo == null) return null;

    String nick = Objects.toString(nickInfo.nick(), "").trim();
    if (nick.isEmpty()) return null;

    String hostmask = Objects.toString(nickInfo.hostmask(), "").trim();
    String realName = Objects.toString(nickInfo.realName(), "").trim();
    AwayState away = (nickInfo.awayState() == null) ? AwayState.UNKNOWN : nickInfo.awayState();
    AccountState account =
        (nickInfo.accountState() == null) ? AccountState.UNKNOWN : nickInfo.accountState();

    boolean hasHostmask = IgnoreMaskMatcher.isUsefulHostmask(hostmask);

    StringBuilder sb = new StringBuilder(128);
    sb.append("<html>");
    sb.append("<b>").append(escapeHtml(nick)).append("</b>");

    if (!realName.isEmpty()) {
      sb.append("<br>")
          .append(italic("userList.tooltip.name"))
          .append(": ")
          .append(escapeHtml(realName));
    }

    if (hasHostmask) {
      sb.append("<br>")
          .append("<span style='font-family:monospace'>")
          .append(escapeHtml(hostmask))
          .append("</span>");
    } else {
      sb.append("<br>").append(italic("userList.tooltip.hostmask.pending"));
    }

    if (away == AwayState.AWAY) {
      String reason = nickInfo.awayMessage();
      if (reason != null && !reason.isBlank()) {
        sb.append("<br>")
            .append(italic("userList.tooltip.away"))
            .append(": ")
            .append(escapeHtml(reason));
      } else {
        sb.append("<br>").append(italic("userList.tooltip.away"));
      }
    }

    if (account == AccountState.LOGGED_IN) {
      String accountName = nickInfo.accountName();
      if (accountName != null && !accountName.isBlank()) {
        sb.append("<br>")
            .append(italic("userList.tooltip.account"))
            .append(": ")
            .append(escapeHtml(accountName.trim()));
      } else {
        sb.append("<br>")
            .append(italic("userList.tooltip.account"))
            .append(": ")
            .append(italic("userList.tooltip.account.loggedIn"));
      }
    } else if (account == AccountState.LOGGED_OUT) {
      sb.append("<br>")
          .append(italic("userList.tooltip.account"))
          .append(": ")
          .append(italic("userList.tooltip.account.loggedOut"));
    } else {
      sb.append("<br>")
          .append(italic("userList.tooltip.account"))
          .append(": ")
          .append(italic("userList.tooltip.account.unknown"));
    }

    if (ignored && softIgnored) {
      sb.append("<br>").append(escapeHtml(MESSAGES.text("userList.tooltip.ignore.hardAndSoft")));
    } else if (ignored) {
      sb.append("<br>").append(escapeHtml(MESSAGES.text("userList.tooltip.ignore.hard")));
    } else if (softIgnored) {
      sb.append("<br>").append(escapeHtml(MESSAGES.text("userList.tooltip.ignore.soft")));
    }

    sb.append("</html>");
    return sb.toString();
  }

  private static String italic(String code) {
    return "<i>" + escapeHtml(MESSAGES.text(code)) + "</i>";
  }

  private static String escapeHtml(String s) {
    if (s == null || s.isEmpty()) return "";
    StringBuilder sb = new StringBuilder(s.length() + 16);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '&':
          sb.append("&amp;");
          break;
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '"':
          sb.append("&quot;");
          break;
        case '\'':
          sb.append("&#39;");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }
}
