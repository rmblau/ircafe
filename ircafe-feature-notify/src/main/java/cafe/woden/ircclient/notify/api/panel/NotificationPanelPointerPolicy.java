package cafe.woden.ircclient.notify.api.panel;

/** Feature-owned pointer/cell hit policy for the notifications panel. */
public final class NotificationPanelPointerPolicy {
  public static final int PRIMARY_BUTTON = 1;

  private NotificationPanelPointerPolicy() {}

  public static boolean isChannelCell(int viewRow, int modelColumn, int channelModelColumn) {
    return viewRow >= 0 && modelColumn == channelModelColumn;
  }

  public static boolean shouldActivateChannelCell(
      int mouseButton, boolean popupTrigger, int viewRow, int modelColumn, int channelModelColumn) {
    return mouseButton == PRIMARY_BUTTON
        && !popupTrigger
        && isChannelCell(viewRow, modelColumn, channelModelColumn);
  }
}
