package cafe.woden.ircclient.ui.servers;

import cafe.woden.ircclient.ui.localization.UiMessages;

/** Pure UI-state rules for server-editor auth controls. */
final class ServerEditorAuthUiPolicy {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private ServerEditorAuthUiPolicy() {}

  static MatrixUiState matrixUiState(
      ServerEditorBackendProfile profile, ServerEditorMatrixAuthMode matrixAuthMode) {
    boolean directAuthControlsVisible = profile != null && profile.directAuthEnabled();
    boolean matrixAuthBackend = profile != null && profile.matrixAuthSupported();
    if (!matrixAuthBackend) {
      return new MatrixUiState(directAuthControlsVisible, true, false, false, false, "", "", null);
    }

    boolean usernamePassword = matrixAuthMode == ServerEditorMatrixAuthMode.USERNAME_PASSWORD;
    String hint;
    String passwordLabel;
    String passwordPlaceholder;
    if (usernamePassword) {
      passwordLabel = MESSAGES.text("servers.editor.auth.password");
      passwordPlaceholder = MESSAGES.text("servers.editor.placeholder.matrixPassword");
      hint = MESSAGES.text("servers.editor.auth.matrixMode.usernamePassword.hint");
    } else {
      passwordLabel = MESSAGES.text("servers.editor.auth.accessToken");
      passwordPlaceholder = MESSAGES.text("servers.editor.placeholder.matrixAccessToken");
      hint = MESSAGES.text("servers.editor.auth.matrixMode.accessToken.hint");
    }

    return new MatrixUiState(
        directAuthControlsVisible,
        false,
        true,
        usernamePassword,
        usernamePassword,
        passwordLabel,
        passwordPlaceholder,
        hint);
  }

  static SaslUiState saslUiState(ServerEditorAuthMode authMode, String mechanism) {
    boolean enabled = authMode == ServerEditorAuthMode.SASL;
    ServerEditorAuthPolicy.SaslMechanismMetadata metadata =
        ServerEditorAuthPolicy.saslMechanismMetadata(mechanism);

    return new SaslUiState(
        enabled,
        enabled,
        enabled,
        enabled,
        enabled && metadata.secretEnabled(),
        metadata.secretPlaceholder(),
        metadata.hint());
  }

  static NickservUiState nickservUiState(ServerEditorAuthMode authMode) {
    String hint = MESSAGES.text("servers.editor.auth.nickserv.hint");
    return new NickservUiState(authMode == ServerEditorAuthMode.NICKSERV, hint);
  }

  record MatrixUiState(
      boolean authModeControlsVisible,
      boolean authModeCardVisible,
      boolean matrixAuthControlsVisible,
      boolean matrixAuthUserVisible,
      boolean matrixAuthUserEnabled,
      String serverPasswordLabel,
      String serverPasswordPlaceholder,
      String hint) {}

  record SaslUiState(
      boolean hintVisible,
      boolean mechanismEnabled,
      boolean continueOnFailureEnabled,
      boolean userEnabled,
      boolean secretEnabled,
      String secretPlaceholder,
      String hint) {}

  record NickservUiState(boolean enabled, String hint) {}
}
