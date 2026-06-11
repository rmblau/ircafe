package cafe.woden.ircclient.ui.servers;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Builds the server-editor auth tab and its auth-mode cards from existing widgets. */
final class ServerEditorAuthTabBuilder {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private ServerEditorAuthTabBuilder() {}

  static JPanel build(AuthTabWidgets widgets) {
    JPanel panel =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                8,
                2,
                3,
                MigLayoutConstraints.RIGHT_12_GROW_FILL_MIN_0,
                "[]6[]6[]6[]8[grow,fill,min:0]"));

    panel.add(widgets.matrixAuthModeLabel());
    panel.add(widgets.matrixAuthModeCombo(), MigConstraints.growXMinWidth0Wrap());
    panel.add(widgets.matrixAuthUserLabel());
    panel.add(widgets.matrixAuthUserField(), MigConstraints.growXMinWidth0Wrap());
    panel.add(widgets.serverPasswordLabel());
    panel.add(widgets.serverPasswordField(), MigConstraints.growXMinWidth0Wrap());
    panel.add(widgets.authModeLabel());
    panel.add(widgets.authModeCombo(), MigConstraints.growXMinWidth0Wrap());

    styleHint(widgets.matrixAuthHintLabel(), " ");
    panel.add(widgets.matrixAuthHintLabel(), MigConstraints.span2GrowXMinWidth0Wrap());

    widgets.authModeCardPanel().removeAll();
    widgets.authModeCardPanel().add(buildDisabledCard(widgets), widgets.disabledCardId());
    widgets.authModeCardPanel().add(buildSaslCard(widgets), widgets.saslCardId());
    widgets.authModeCardPanel().add(buildNickservCard(widgets), widgets.nickservCardId());
    panel.add(widgets.authModeCardPanel(), MigConstraints.span2GrowPushMinWidth0());

    return panel;
  }

  private static JPanel buildDisabledCard(AuthTabWidgets widgets) {
    JPanel panel = new JPanel(MigLayouts.fillX("6 0 0 0", "[grow,fill,min:0]", "[]"));
    styleHint(widgets.authDisabledHintLabel(), asHtml(widgets.authDisabledHintText()));
    panel.add(widgets.authDisabledHintLabel(), MigConstraints.growXMinWidth0());
    return panel;
  }

  private static JPanel buildSaslCard(AuthTabWidgets widgets) {
    JPanel panel =
        new JPanel(
            MigLayouts.fillXWrap(
                0, 2, MigLayoutConstraints.RIGHT_12_GROW_FILL_MIN_0, "[]6[]6[]6[]8[]push"));
    panel.add(new JLabel(MESSAGES.text("servers.editor.auth.username")));
    panel.add(widgets.saslUserField(), MigConstraints.growXMinWidth0Wrap());
    panel.add(new JLabel(MESSAGES.text("servers.editor.auth.secret")));
    panel.add(widgets.saslPasswordField(), MigConstraints.growXMinWidth0Wrap());
    panel.add(new JLabel(MESSAGES.text("servers.editor.auth.mechanism")));
    panel.add(widgets.saslMechanismCombo(), MigConstraints.growXMinWidth0Wrap());
    panel.add(
        new JLabel(MESSAGES.text("servers.editor.auth.onFailure")), MigConstraints.alignYTop());
    panel.add(widgets.saslContinueOnFailureBox(), MigConstraints.growXMinWidth0Wrap());

    styleHint(widgets.saslHintLabel(), " ");
    panel.add(widgets.saslHintLabel(), MigConstraints.span2GrowXMinWidth0PushY());
    return panel;
  }

  private static JPanel buildNickservCard(AuthTabWidgets widgets) {
    JPanel panel =
        new JPanel(
            MigLayouts.fillXWrap(
                0, 2, MigLayoutConstraints.RIGHT_12_GROW_FILL_MIN_0, "[]6[]6[]8[]push"));
    panel.add(new JLabel(MESSAGES.text("servers.editor.auth.service")));
    panel.add(widgets.nickservServiceField(), MigConstraints.growXMinWidth0Wrap());
    panel.add(new JLabel(MESSAGES.text("servers.editor.auth.password")));
    panel.add(widgets.nickservPasswordField(), MigConstraints.growXMinWidth0Wrap());
    panel.add(
        new JLabel(MESSAGES.text("servers.editor.auth.delayAutoJoin")), MigConstraints.alignYTop());
    panel.add(widgets.nickservDelayJoinBox(), MigConstraints.growXMinWidth0Wrap());

    styleHint(widgets.nickservHintLabel(), " ");
    panel.add(widgets.nickservHintLabel(), MigConstraints.span2GrowXMinWidth0PushY());
    return panel;
  }

  private static void styleHint(JLabel label, String text) {
    label.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground");
    label.setText(text);
  }

  private static String asHtml(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    return "<html>" + escapeHtml(text) + "</html>";
  }

  private static String escapeHtml(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  record AuthTabWidgets(
      JLabel matrixAuthModeLabel,
      JComboBox<ServerEditorMatrixAuthMode> matrixAuthModeCombo,
      JLabel matrixAuthUserLabel,
      JTextField matrixAuthUserField,
      JLabel serverPasswordLabel,
      JPasswordField serverPasswordField,
      JLabel authModeLabel,
      JComboBox<ServerEditorAuthMode> authModeCombo,
      JLabel matrixAuthHintLabel,
      JPanel authModeCardPanel,
      String disabledCardId,
      String saslCardId,
      String nickservCardId,
      String authDisabledHintText,
      JLabel authDisabledHintLabel,
      JTextField saslUserField,
      JPasswordField saslPasswordField,
      JComboBox<String> saslMechanismCombo,
      JCheckBox saslContinueOnFailureBox,
      JLabel saslHintLabel,
      JTextField nickservServiceField,
      JPasswordField nickservPasswordField,
      JCheckBox nickservDelayJoinBox,
      JLabel nickservHintLabel) {}
}
