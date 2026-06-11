package cafe.woden.ircclient.ui.servertree.view;

import cafe.woden.ircclient.ui.controls.ConnectButton;
import cafe.woden.ircclient.ui.controls.DisconnectButton;
import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.servers.ServerDialogs;
import java.awt.Dimension;
import java.awt.Window;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Owns header button wiring and connection-control tooltip state for the server tree. */
public final class ServerTreeHeaderControls {

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();
  private static final int HEADER_BUTTON_SIZE = 26;

  private final JComponent owner;
  private final ServerDialogs serverDialogs;
  private final ConnectButton connectBtn;
  private final DisconnectButton disconnectBtn;
  private final JButton addServerBtn = new JButton();
  private final JPanel panel;

  public ServerTreeHeaderControls(
      JComponent owner,
      ConnectButton connectBtn,
      DisconnectButton disconnectBtn,
      ServerDialogs serverDialogs) {
    this.owner = Objects.requireNonNull(owner, "owner");
    this.connectBtn = Objects.requireNonNull(connectBtn, "connectBtn");
    this.disconnectBtn = Objects.requireNonNull(disconnectBtn, "disconnectBtn");
    this.serverDialogs = serverDialogs;
    configureButtons();
    this.panel = buildPanel();
  }

  public JPanel panel() {
    return panel;
  }

  public void setStatusText(String text) {
    String normalized = Objects.toString(text, "").trim();
    connectBtn.setToolTipText(
        connectionTooltip("serverTree.header.connectAll.tooltip", normalized));
    disconnectBtn.setToolTipText(
        connectionTooltip("serverTree.header.disconnectAll.tooltip", normalized));
  }

  public void setConnectionControlsEnabled(boolean connectEnabled, boolean disconnectEnabled) {
    connectBtn.setEnabled(connectEnabled);
    disconnectBtn.setEnabled(disconnectEnabled);
  }

  private void configureButtons() {
    addServerBtn.setText("");
    addServerBtn.setIcon(SvgIcons.action("plus", 16));
    addServerBtn.setDisabledIcon(SvgIcons.actionDisabled("plus", 16));
    addServerBtn.setToolTipText(message("serverTree.header.addServer.tooltip"));
    addServerBtn.setFocusable(false);
    addServerBtn.setPreferredSize(new Dimension(HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE));
    addServerBtn.setEnabled(serverDialogs != null);
    addServerBtn.addActionListener(
        ev -> {
          if (serverDialogs == null) return;
          Window window = SwingUtilities.getWindowAncestor(owner);
          serverDialogs.openAddServer(window);
        });

    connectBtn.setText("");
    connectBtn.setIcon(SvgIcons.action("check", 16));
    connectBtn.setDisabledIcon(SvgIcons.actionDisabled("check", 16));
    connectBtn.setToolTipText(message("serverTree.header.connectAll.tooltip"));
    connectBtn.setFocusable(false);
    connectBtn.setPreferredSize(new Dimension(HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE));

    disconnectBtn.setText("");
    disconnectBtn.setIcon(SvgIcons.action("exit", 16));
    disconnectBtn.setDisabledIcon(SvgIcons.actionDisabled("exit", 16));
    disconnectBtn.setToolTipText(message("serverTree.header.disconnectAll.tooltip"));
    disconnectBtn.setFocusable(false);
    disconnectBtn.setPreferredSize(new Dimension(HEADER_BUTTON_SIZE, HEADER_BUTTON_SIZE));
  }

  private static String connectionTooltip(String baseKey, String statusText) {
    String normalized = Objects.toString(statusText, "").trim();
    if (normalized.isEmpty()) {
      return message(baseKey);
    }
    return message("serverTree.header.connectionTooltip.withStatus", message(baseKey), normalized);
  }

  private static String message(String key, Object... args) {
    return MESSAGES.text(key, args);
  }

  private JPanel buildPanel() {
    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
    header.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
    header.add(addServerBtn);
    header.add(Box.createHorizontalStrut(6));
    header.add(connectBtn);
    header.add(Box.createHorizontalStrut(6));
    header.add(disconnectBtn);
    header.add(Box.createHorizontalGlue());
    return header;
  }
}
