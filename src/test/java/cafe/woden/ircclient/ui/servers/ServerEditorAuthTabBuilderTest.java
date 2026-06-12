package cafe.woden.ircclient.ui.servers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class ServerEditorAuthTabBuilderTest {
  private static final int USABLE_AUTH_FIELD_WIDTH = 160;
  private static final String DISABLED_CARD_ID = "auth-disabled";
  private static final String SASL_CARD_ID = "auth-sasl";
  private static final String NICKSERV_CARD_ID = "auth-nickserv";
  private static final ServerEditorBackendProfiles BACKEND_PROFILES =
      ServerEditorBackendProfiles.builtIns();

  @Test
  void buildInitializesHintLabelsAndAuthCards() {
    TestWidgets widgets = newTestWidgets();

    JPanel panel = build(widgets);

    assertEquals(" ", widgets.matrixAuthHintLabel().getText());
    assertEquals(" ", widgets.saslHintLabel().getText());
    assertEquals(" ", widgets.nickservHintLabel().getText());
    assertEquals(
        "foreground:$Label.disabledForeground",
        widgets.matrixAuthHintLabel().getClientProperty(FlatClientProperties.STYLE));
    assertEquals(
        "<html>No authentication on connect.</html>", widgets.authDisabledHintLabel().getText());
    assertEquals(3, widgets.authModeCardPanel().getComponentCount());
    assertTrue(panel.isAncestorOf(widgets.authModeCardPanel()));
  }

  @Test
  void authModeSwitchKeepsSharedFieldsUsableAtMinimumWidth() {
    TestWidgets widgets = newTestWidgets();
    widgets.authModeCombo().setPrototypeDisplayValue(ServerEditorAuthMode.NICKSERV);
    widgets.saslMechanismCombo().setPrototypeDisplayValue("ECDSA-NIST256P-CHALLENGE");

    JPanel panel = build(widgets);
    ServerEditorAuthPanelUiApplier.AuthPanelWidgets panelWidgets = authPanelWidgets(widgets, panel);

    applyAuthMode(panelWidgets, ServerEditorAuthMode.SASL);
    layoutAtMinimumWidth(panel);
    assertSharedAuthControlsUsable(widgets, "SASL");

    applyAuthMode(panelWidgets, ServerEditorAuthMode.NICKSERV);
    layoutAtMinimumWidth(panel);

    assertEquals(ServerEditorAuthMode.NICKSERV, widgets.authModeCombo().getSelectedItem());
    assertSharedAuthControlsUsable(widgets, "NickServ");
  }

  private static TestWidgets newTestWidgets() {
    return new TestWidgets(
        new JLabel("Matrix auth"),
        new JComboBox<>(ServerEditorMatrixAuthMode.values()),
        new JLabel("Username"),
        new JTextField(),
        new JLabel("Server password"),
        new JPasswordField(),
        new JLabel("Method"),
        new JComboBox<>(ServerEditorAuthMode.values()),
        new JLabel("stale"),
        new JPanel(new CardLayout()),
        new JLabel("stale"),
        new JTextField(),
        new JPasswordField(),
        new JComboBox<>(new String[] {"PLAIN", "ECDSA-NIST256P-CHALLENGE"}),
        new JCheckBox(),
        new JLabel("stale"),
        new JTextField(),
        new JPasswordField(),
        new JCheckBox(),
        new JLabel("stale"));
  }

  private static JPanel build(TestWidgets widgets) {
    return ServerEditorAuthTabBuilder.build(
        new ServerEditorAuthTabBuilder.AuthTabWidgets(
            widgets.matrixAuthModeLabel(),
            widgets.matrixAuthModeCombo(),
            widgets.matrixAuthUserLabel(),
            widgets.matrixAuthUserField(),
            widgets.serverPasswordLabel(),
            widgets.serverPasswordField(),
            widgets.authModeLabel(),
            widgets.authModeCombo(),
            widgets.matrixAuthHintLabel(),
            widgets.authModeCardPanel(),
            DISABLED_CARD_ID,
            SASL_CARD_ID,
            NICKSERV_CARD_ID,
            "No authentication on connect.",
            widgets.authDisabledHintLabel(),
            widgets.saslUserField(),
            widgets.saslPasswordField(),
            widgets.saslMechanismCombo(),
            widgets.saslContinueOnFailureBox(),
            widgets.saslHintLabel(),
            widgets.nickservServiceField(),
            widgets.nickservPasswordField(),
            widgets.nickservDelayJoinBox(),
            widgets.nickservHintLabel()));
  }

  private static ServerEditorAuthPanelUiApplier.AuthPanelWidgets authPanelWidgets(
      TestWidgets widgets, JPanel refreshTarget) {
    return new ServerEditorAuthPanelUiApplier.AuthPanelWidgets(
        new ServerEditorAuthModeUiApplier.AuthModeWidgets(
            widgets.authModeCombo(),
            widgets.authModeCardPanel(),
            DISABLED_CARD_ID,
            SASL_CARD_ID,
            NICKSERV_CARD_ID),
        new ServerEditorAuthUiApplier.MatrixAuthWidgets(
            widgets.authModeLabel(),
            widgets.authModeCombo(),
            widgets.authModeCardPanel(),
            widgets.matrixAuthModeLabel(),
            widgets.matrixAuthModeCombo(),
            widgets.matrixAuthHintLabel(),
            widgets.matrixAuthUserLabel(),
            widgets.matrixAuthUserField(),
            widgets.serverPasswordLabel(),
            widgets.serverPasswordField(),
            refreshTarget),
        new ServerEditorAuthUiApplier.SaslWidgets(
            widgets.saslMechanismCombo(),
            widgets.saslContinueOnFailureBox(),
            widgets.saslUserField(),
            widgets.saslPasswordField(),
            widgets.saslHintLabel()),
        new ServerEditorAuthUiApplier.NickservWidgets(
            widgets.nickservServiceField(),
            widgets.nickservPasswordField(),
            widgets.nickservDelayJoinBox(),
            widgets.nickservHintLabel()));
  }

  private static void applyAuthMode(
      ServerEditorAuthPanelUiApplier.AuthPanelWidgets panelWidgets, ServerEditorAuthMode authMode) {
    ServerEditorAuthPanelUiApplier.apply(
        new ServerEditorAuthPanelUiApplier.RefreshRequest(
            BACKEND_PROFILES.profileForBackendId("irc"),
            authMode,
            ServerEditorMatrixAuthMode.ACCESS_TOKEN,
            "PLAIN"),
        panelWidgets);
  }

  private static void layoutAtMinimumWidth(JPanel panel) {
    Dimension minimum = panel.getMinimumSize();
    Dimension preferred = panel.getPreferredSize();
    panel.setSize(minimum.width, Math.max(minimum.height, preferred.height));
    panel.doLayout();
  }

  private static void assertSharedAuthControlsUsable(TestWidgets widgets, String modeName) {
    assertTrue(
        widgets.serverPasswordField().getWidth() >= USABLE_AUTH_FIELD_WIDTH,
        () ->
            modeName
                + " server password field width was "
                + widgets.serverPasswordField().getWidth());
    assertTrue(
        widgets.authModeCombo().getWidth() >= USABLE_AUTH_FIELD_WIDTH,
        () -> modeName + " auth method combo width was " + widgets.authModeCombo().getWidth());
  }

  private record TestWidgets(
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
