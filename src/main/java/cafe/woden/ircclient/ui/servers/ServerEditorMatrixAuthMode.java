package cafe.woden.ircclient.ui.servers;

import cafe.woden.ircclient.ui.localization.UiMessages;

enum ServerEditorMatrixAuthMode {
  ACCESS_TOKEN("servers.editor.auth.matrixMode.accessToken"),
  USERNAME_PASSWORD("servers.editor.auth.matrixMode.usernamePassword");

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private final String labelKey;

  ServerEditorMatrixAuthMode(String labelKey) {
    this.labelKey = labelKey;
  }

  @Override
  public String toString() {
    return MESSAGES.text(labelKey);
  }
}
