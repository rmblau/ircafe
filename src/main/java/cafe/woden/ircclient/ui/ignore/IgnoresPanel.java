package cafe.woden.ircclient.ui.ignore;

import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.awt.BorderLayout;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Lightweight center-card launcher for per-server ignore management. */
public final class IgnoresPanel extends JPanel {

  private final UiMessages messages;
  private final JLabel serverLabel;
  private final JButton openButton;
  private TargetRef targetRef;
  private Consumer<TargetRef> openIgnoreDialogHandler = ref -> {};

  public IgnoresPanel() {
    this(UiMessages.bundledDefaults());
  }

  public IgnoresPanel(UiMessages messages) {
    super(new BorderLayout());
    this.messages = messages == null ? UiMessages.bundledDefaults() : messages;
    this.serverLabel = new JLabel(message("ignoreLists.panel.server.none"));
    this.openButton = new JButton(message("ignoreLists.panel.open"));
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

    JLabel heading = new JLabel(message("ignoreLists.panel.heading"));
    JLabel copy = new JLabel(message("ignoreLists.panel.copy"));

    openButton.setIcon(SvgIcons.action("ban", 16));
    openButton.setDisabledIcon(SvgIcons.actionDisabled("ban", 16));
    openButton.setEnabled(false);
    openButton.addActionListener(
        e -> {
          TargetRef ref = targetRef;
          if (ref == null) return;
          String sid = Objects.toString(ref.serverId(), "").trim();
          if (!isValidServerId(sid)) return;
          openIgnoreDialogHandler.accept(ref);
        });

    JPanel stack = new JPanel();
    stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
    stack.add(heading);
    stack.add(Box.createVerticalStrut(8));
    stack.add(copy);
    stack.add(Box.createVerticalStrut(12));
    stack.add(serverLabel);
    stack.add(Box.createVerticalStrut(12));
    stack.add(openButton);

    add(stack, BorderLayout.NORTH);
  }

  public void setServerId(String serverId) {
    String sid = Objects.toString(serverId, "").trim();
    setTarget(sid.isEmpty() ? null : TargetRef.ignores(sid));
  }

  public void setTarget(TargetRef targetRef) {
    this.targetRef = targetRef;
    if (targetRef == null) {
      serverLabel.setText(message("ignoreLists.panel.server.none"));
      openButton.setEnabled(false);
      return;
    }
    String sid = Objects.toString(targetRef.serverId(), "").trim();
    String token = Objects.toString(targetRef.networkQualifierToken(), "").trim();
    if (sid.isEmpty()) {
      serverLabel.setText(message("ignoreLists.panel.server.none"));
      openButton.setEnabled(false);
      return;
    }
    if (token.isEmpty()) {
      serverLabel.setText(message("ignoreLists.panel.server", sid));
    } else {
      serverLabel.setText(message("ignoreLists.panel.server.network", sid, token));
    }
    openButton.setEnabled(isValidServerId(sid));
  }

  public void setOnOpenIgnoreDialog(Consumer<TargetRef> handler) {
    this.openIgnoreDialogHandler = handler == null ? ref -> {} : handler;
  }

  private String message(String code, Object... args) {
    return messages.text(code, args);
  }

  private static boolean isValidServerId(String rawServerId) {
    String sid = Objects.toString(rawServerId, "").trim();
    if (sid.isEmpty()) return false;
    for (int i = 0; i < sid.length(); i++) {
      if (Character.isWhitespace(sid.charAt(i))) return false;
    }
    return true;
  }
}
