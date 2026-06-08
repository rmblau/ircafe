package cafe.woden.ircclient.ui.userlist;

import cafe.woden.ircclient.ignore.IgnoreListService;
import cafe.woden.ircclient.ignore.IgnoreMaskMatcher;
import cafe.woden.ircclient.ignore.IgnoreStatusService;
import cafe.woden.ircclient.irc.IrcEvent.NickInfo;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.awt.Window;
import java.util.Objects;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
public final class UserListIgnorePromptHandler {
  private final IgnoreListService ignoreListService;
  private final IgnoreStatusService ignoreStatusService;
  private final Dialogs dialogs;
  private final UiMessages messages;

  @Autowired
  public UserListIgnorePromptHandler(
      IgnoreListService ignoreListService,
      IgnoreStatusService ignoreStatusService,
      UiMessages messages) {
    this(ignoreListService, ignoreStatusService, new JOptionPaneDialogs(), messages);
  }

  UserListIgnorePromptHandler(
      IgnoreListService ignoreListService,
      IgnoreStatusService ignoreStatusService,
      Dialogs dialogs) {
    this(ignoreListService, ignoreStatusService, dialogs, UiMessages.bundledDefaults());
  }

  UserListIgnorePromptHandler(
      IgnoreListService ignoreListService,
      IgnoreStatusService ignoreStatusService,
      Dialogs dialogs,
      UiMessages messages) {
    this.ignoreListService = ignoreListService;
    this.ignoreStatusService = ignoreStatusService;
    this.dialogs = dialogs;
    this.messages = Objects.requireNonNull(messages, "messages");
  }

  public boolean prompt(
      java.awt.Component parent,
      TargetRef active,
      NickInfo nickInfo,
      String nick,
      boolean removing,
      boolean soft) {
    try {
      if (active == null || active.serverId() == null || active.serverId().isBlank()) return false;
      String normalizedNick = Objects.toString(nick, "").trim();
      if (normalizedNick.isEmpty()) return false;

      String hostmask = nickInfo == null ? "" : Objects.toString(nickInfo.hostmask(), "").trim();
      String seedBase =
          (ignoreStatusService == null)
              ? (IgnoreMaskMatcher.isUsefulHostmask(hostmask) ? hostmask : normalizedNick)
              : ignoreStatusService.bestSeedForMask(active.serverId(), normalizedNick, hostmask);
      String seed = IgnoreListService.normalizeMaskOrNickToHostmask(seedBase);
      IgnoreDialogCopy copy = dialogCopy(removing, soft);
      java.awt.Component owner = dialogOwner(parent);

      String input = dialogs.showInput(owner, copy.prompt(), copy.title(), seed);
      if (input == null) return false;
      String arg = input.trim();
      if (arg.isEmpty()) return false;

      boolean changed = applyChange(active.serverId(), arg, removing, soft);
      String stored = IgnoreListService.normalizeMaskOrNickToHostmask(arg);
      dialogs.showInfo(owner, resultMessage(changed, stored, removing, soft), copy.title());
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  private boolean applyChange(String serverId, String arg, boolean removing, boolean soft) {
    if (removing) {
      if (soft) {
        return ignoreListService != null && ignoreListService.removeSoftMask(serverId, arg);
      }
      return ignoreListService != null && ignoreListService.removeMask(serverId, arg);
    }
    if (soft) {
      return ignoreListService != null && ignoreListService.addSoftMask(serverId, arg);
    }
    return ignoreListService != null && ignoreListService.addMask(serverId, arg);
  }

  private String resultMessage(boolean changed, String stored, boolean removing, boolean soft) {
    if (soft) {
      if (removing) {
        return message(
            changed
                ? "userList.ignore.result.soft.removed"
                : "userList.ignore.result.soft.notFound",
            stored);
      }
      return message(
          changed ? "userList.ignore.result.soft.added" : "userList.ignore.result.soft.exists",
          stored);
    }
    if (removing) {
      return message(
          changed ? "userList.ignore.result.hard.removed" : "userList.ignore.result.hard.notFound",
          stored);
    }
    return message(
        changed ? "userList.ignore.result.hard.added" : "userList.ignore.result.hard.exists",
        stored);
  }

  private IgnoreDialogCopy dialogCopy(boolean removing, boolean soft) {
    if (soft) {
      return removing
          ? new IgnoreDialogCopy(
              message("userList.ignore.soft.remove.title"),
              message("userList.ignore.soft.remove.prompt"))
          : new IgnoreDialogCopy(
              message("userList.ignore.soft.add.title"),
              message("userList.ignore.soft.add.prompt"));
    }
    return removing
        ? new IgnoreDialogCopy(
            message("userList.ignore.hard.remove.title"),
            message("userList.ignore.hard.remove.prompt"))
        : new IgnoreDialogCopy(
            message("userList.ignore.hard.add.title"), message("userList.ignore.hard.add.prompt"));
  }

  private String message(String code, Object... args) {
    return messages.text(code, args);
  }

  private static java.awt.Component dialogOwner(java.awt.Component parent) {
    if (parent == null) return null;
    Window owner = SwingUtilities.getWindowAncestor(parent);
    return owner != null ? owner : parent;
  }

  private record IgnoreDialogCopy(String title, String prompt) {}

  interface Dialogs {
    String showInput(java.awt.Component parent, String prompt, String title, String seed);

    void showInfo(java.awt.Component parent, String message, String title);
  }

  static final class JOptionPaneDialogs implements Dialogs {
    @Override
    public String showInput(java.awt.Component parent, String prompt, String title, String seed) {
      return (String)
          JOptionPane.showInputDialog(
              parent, prompt, title, JOptionPane.PLAIN_MESSAGE, null, null, seed);
    }

    @Override
    public void showInfo(java.awt.Component parent, String message, String title) {
      JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
  }
}
