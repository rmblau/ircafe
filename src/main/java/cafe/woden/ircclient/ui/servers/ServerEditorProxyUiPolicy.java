package cafe.woden.ircclient.ui.servers;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.ui.localization.UiMessages;

/** Pure UI-state rules for the server-editor proxy controls. */
final class ServerEditorProxyUiPolicy {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private ServerEditorProxyUiPolicy() {}

  static ProxyUiState uiState(
      boolean overrideSelected, boolean proxyEnabled, IrcProperties.Proxy globalProxy) {
    String hint =
        !overrideSelected
            ? globalProxy.enabled()
                ? MESSAGES.text(
                    "servers.editor.proxy.hint.inheritingEnabled",
                    globalProxy.host(),
                    globalProxy.port())
                : MESSAGES.text("servers.editor.proxy.hint.inheritingDisabled")
            : MESSAGES.text("servers.editor.proxy.hint.override");

    boolean proxyDetailsEnabled = overrideSelected && proxyEnabled;
    return new ProxyUiState(
        hint,
        overrideSelected,
        proxyDetailsEnabled,
        overrideSelected,
        overrideSelected,
        overrideSelected);
  }

  record ProxyUiState(
      String hint,
      boolean proxyEnabledToggleEnabled,
      boolean proxyDetailsEnabled,
      boolean remoteDnsEnabled,
      boolean connectTimeoutEnabled,
      boolean readTimeoutEnabled) {}
}
