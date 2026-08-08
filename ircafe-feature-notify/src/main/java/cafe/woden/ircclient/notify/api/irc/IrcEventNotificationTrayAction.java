package cafe.woden.ircclient.notify.api.irc;

/** Feature-safe tray/status/sound action for a matched IRC event notification rule. */
public record IrcEventNotificationTrayAction(
    boolean enabled,
    boolean showToast,
    boolean showStatusBar,
    String focusScope,
    boolean playSound,
    String soundId,
    boolean soundUseCustom,
    String soundCustomPath) {

  public static IrcEventNotificationTrayAction disabled() {
    return new IrcEventNotificationTrayAction(false, false, false, null, false, null, false, null);
  }
}
