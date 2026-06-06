package cafe.woden.ircclient.ui.servers;

import cafe.woden.ircclient.ui.localization.UiMessages;

enum ServerEditorAuthMode {
  DISABLED("servers.editor.auth.mode.disabled"),
  SASL("servers.editor.auth.mode.sasl"),
  NICKSERV("servers.editor.auth.mode.nickserv");

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private final String labelKey;

  ServerEditorAuthMode(String labelKey) {
    this.labelKey = labelKey;
  }

  @Override
  public String toString() {
    return MESSAGES.text(labelKey);
  }
}
