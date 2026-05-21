package cafe.woden.ircclient.ui.servers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class ServerEditorValidationUiApplierTest {

  @Test
  void applyDecoratesErrorsWarningsAndSaveState() {
    JTextField idField = new JTextField();
    JTextField hostField = new JTextField();
    JTextField portField = new JTextField();
    JPasswordField serverPasswordField = new JPasswordField();
    JTextField matrixAuthUserField = new JTextField();
    JTextField loginField = new JTextField();
    JTextField nickField = new JTextField();
    JTextField saslUserField = new JTextField();
    JPasswordField saslSecretField = new JPasswordField();
    JTextField nickservServiceField = new JTextField();
    JPasswordField nickservPasswordField = new JPasswordField();
    JTextField proxyHostField = new JTextField();
    JTextField proxyPortField = new JTextField();
    JTextField proxyUserField = new JTextField();
    JPasswordField proxyPasswordField = new JPasswordField();
    JTextField proxyConnectTimeoutField = new JTextField();
    JTextField proxyReadTimeoutField = new JTextField();
    JButton saveButton = new JButton();

    ServerEditorValidationUiApplier.apply(
        new ServerEditorValidationPolicy.ValidationState(
            new ServerEditorConnectionPolicy.ConnectionValidation(true, false, true, false),
            new ServerEditorAuthPolicy.MatrixValidation(true, true, true),
            ServerEditorAuthMode.SASL,
            new ServerEditorAuthPolicy.SaslValidation(true, true, true),
            new ServerEditorAuthPolicy.NickservValidation(true, true),
            new ServerEditorProxyValidationPolicy.ProxyValidation(
                true, true, true, true, true, false, true),
            false),
        new ServerEditorValidationUiApplier.ValidationWidgets(
            idField,
            hostField,
            portField,
            serverPasswordField,
            matrixAuthUserField,
            loginField,
            nickField,
            saslUserField,
            saslSecretField,
            nickservServiceField,
            nickservPasswordField,
            proxyHostField,
            proxyPortField,
            proxyUserField,
            proxyPasswordField,
            proxyConnectTimeoutField,
            proxyReadTimeoutField,
            saveButton));

    assertEquals(
        FlatClientProperties.OUTLINE_ERROR,
        idField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(hostField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_ERROR,
        portField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_ERROR,
        serverPasswordField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_ERROR,
        matrixAuthUserField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_ERROR,
        saslUserField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_ERROR,
        saslSecretField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_ERROR,
        nickservPasswordField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_ERROR,
        proxyHostField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_ERROR,
        proxyPortField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_WARNING,
        proxyUserField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_WARNING,
        proxyPasswordField.getClientProperty(FlatClientProperties.OUTLINE));
    assertEquals(
        FlatClientProperties.OUTLINE_WARNING,
        proxyConnectTimeoutField.getClientProperty(FlatClientProperties.OUTLINE));
    assertFalse(saveButton.isEnabled());
    assertEquals("Fix highlighted fields to enable Save.", saveButton.getToolTipText());
  }

  @Test
  void applyClearsInactiveSectionsAndAllowsSave() {
    JTextField matrixAuthUserField = new JTextField();
    matrixAuthUserField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
    JTextField saslUserField = new JTextField();
    saslUserField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
    JPasswordField saslSecretField = new JPasswordField();
    saslSecretField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
    JTextField nickservServiceField = new JTextField();
    nickservServiceField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
    JPasswordField nickservPasswordField = new JPasswordField();
    nickservPasswordField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
    JTextField proxyHostField = new JTextField();
    proxyHostField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
    JTextField proxyPortField = new JTextField();
    proxyPortField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
    JTextField proxyUserField = new JTextField();
    proxyUserField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_WARNING);
    JPasswordField proxyPasswordField = new JPasswordField();
    proxyPasswordField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_WARNING);
    JTextField proxyConnectTimeoutField = new JTextField();
    proxyConnectTimeoutField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_WARNING);
    JTextField proxyReadTimeoutField = new JTextField();
    proxyReadTimeoutField.putClientProperty(
        FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_WARNING);
    JButton saveButton = new JButton();

    ServerEditorValidationUiApplier.apply(
        new ServerEditorValidationPolicy.ValidationState(
            new ServerEditorConnectionPolicy.ConnectionValidation(false, false, false, false),
            new ServerEditorAuthPolicy.MatrixValidation(false, false, false),
            ServerEditorAuthMode.DISABLED,
            new ServerEditorAuthPolicy.SaslValidation(false, false, false),
            new ServerEditorAuthPolicy.NickservValidation(false, false),
            new ServerEditorProxyValidationPolicy.ProxyValidation(
                false, false, false, false, false, false, false),
            true),
        new ServerEditorValidationUiApplier.ValidationWidgets(
            new JTextField(),
            new JTextField(),
            new JTextField(),
            new JPasswordField(),
            matrixAuthUserField,
            new JTextField(),
            new JTextField(),
            saslUserField,
            saslSecretField,
            nickservServiceField,
            nickservPasswordField,
            proxyHostField,
            proxyPortField,
            proxyUserField,
            proxyPasswordField,
            proxyConnectTimeoutField,
            proxyReadTimeoutField,
            saveButton));

    assertNull(matrixAuthUserField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(saslUserField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(saslSecretField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(nickservServiceField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(nickservPasswordField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(proxyHostField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(proxyPortField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(proxyUserField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(proxyPasswordField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(proxyConnectTimeoutField.getClientProperty(FlatClientProperties.OUTLINE));
    assertNull(proxyReadTimeoutField.getClientProperty(FlatClientProperties.OUTLINE));
    assertTrue(saveButton.isEnabled());
    assertNull(saveButton.getToolTipText());
  }
}
