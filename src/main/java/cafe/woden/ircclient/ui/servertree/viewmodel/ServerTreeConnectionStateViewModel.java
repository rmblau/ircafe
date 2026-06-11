package cafe.woden.ircclient.ui.servertree.viewmodel;

import cafe.woden.ircclient.app.api.ConnectionState;
import cafe.woden.ircclient.ui.icons.SvgIcons.Palette;
import cafe.woden.ircclient.ui.localization.UiMessages;

/** UI presentation policy for server-node connection state controls and labels. */
public final class ServerTreeConnectionStateViewModel {

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private ServerTreeConnectionStateViewModel() {}

  public static boolean canConnect(ConnectionState state) {
    ConnectionState st = state == null ? ConnectionState.DISCONNECTED : state;
    return st == ConnectionState.DISCONNECTED || st == ConnectionState.DISCONNECTING;
  }

  public static boolean canDisconnect(ConnectionState state) {
    ConnectionState st = state == null ? ConnectionState.DISCONNECTED : state;
    return st == ConnectionState.CONNECTING
        || st == ConnectionState.CONNECTED
        || st == ConnectionState.RECONNECTING;
  }

  public static String stateLabel(ConnectionState state) {
    ConnectionState st = state == null ? ConnectionState.DISCONNECTED : state;
    return switch (st) {
      case CONNECTED -> MESSAGES.text("serverTree.connection.state.connected");
      case CONNECTING -> MESSAGES.text("serverTree.connection.state.connecting");
      case RECONNECTING -> MESSAGES.text("serverTree.connection.state.reconnecting");
      case DISCONNECTING -> MESSAGES.text("serverTree.connection.state.disconnecting");
      case DISCONNECTED -> MESSAGES.text("serverTree.connection.state.disconnected");
    };
  }

  public static String desiredIntentLabel(boolean desiredOnline) {
    return desiredOnline
        ? MESSAGES.text("serverTree.connection.intent.online")
        : MESSAGES.text("serverTree.connection.intent.offline");
  }

  public static String desiredBadge(ConnectionState state, boolean desiredOnline) {
    ConnectionState st = state == null ? ConnectionState.DISCONNECTED : state;
    boolean online = isOnlineState(st);
    if (desiredOnline && !online) {
      if (st == ConnectionState.DISCONNECTING) {
        return MESSAGES.text("serverTree.connection.badge.connectQueued");
      }
      return MESSAGES.text("serverTree.connection.badge.wantedOnline");
    }
    if (!desiredOnline && online) {
      return MESSAGES.text("serverTree.connection.badge.disconnectQueued");
    }
    return "";
  }

  public static String intentQueueTip(ConnectionState state, boolean desiredOnline) {
    ConnectionState st = state == null ? ConnectionState.DISCONNECTED : state;
    boolean online = isOnlineState(st);
    if (desiredOnline && st == ConnectionState.DISCONNECTING) {
      return MESSAGES.text("serverTree.connection.tip.connectQueued");
    }
    if (desiredOnline && st == ConnectionState.DISCONNECTED) {
      return MESSAGES.text("serverTree.connection.tip.wantedOnline");
    }
    if (!desiredOnline && online) {
      return MESSAGES.text("serverTree.connection.tip.disconnectQueued");
    }
    return "";
  }

  public static String actionHint(ConnectionState state) {
    ConnectionState st = state == null ? ConnectionState.DISCONNECTED : state;
    return canConnect(st)
        ? MESSAGES.text("serverTree.connection.actionHint.connect")
        : canDisconnect(st)
            ? MESSAGES.text("serverTree.connection.actionHint.disconnect")
            : MESSAGES.text("serverTree.connection.actionHint.changing");
  }

  public static String serverNodeIconName(ConnectionState state) {
    ConnectionState st = state == null ? ConnectionState.DISCONNECTED : state;
    return switch (st) {
      case CONNECTED -> "check";
      case CONNECTING, RECONNECTING, DISCONNECTING -> "refresh";
      case DISCONNECTED -> "terminal";
    };
  }

  public static Palette serverNodeIconPalette(ConnectionState state) {
    ConnectionState st = state == null ? ConnectionState.DISCONNECTED : state;
    return switch (st) {
      case CONNECTED, CONNECTING, RECONNECTING -> Palette.TREE;
      case DISCONNECTED, DISCONNECTING -> Palette.TREE_DISABLED;
    };
  }

  public static String serverActionIconName(ConnectionState state) {
    ConnectionState st = state == null ? ConnectionState.DISCONNECTED : state;
    return switch (st) {
      case DISCONNECTED -> "plus";
      case CONNECTED, RECONNECTING -> "exit";
      case CONNECTING, DISCONNECTING -> "refresh";
    };
  }

  private static boolean isOnlineState(ConnectionState state) {
    ConnectionState st = state == null ? ConnectionState.DISCONNECTED : state;
    return st == ConnectionState.CONNECTED
        || st == ConnectionState.CONNECTING
        || st == ConnectionState.RECONNECTING;
  }
}
