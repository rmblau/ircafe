package cafe.woden.ircclient.ui.servers;

import cafe.woden.ircclient.config.IrcProperties;
import cafe.woden.ircclient.ui.localization.UiMessages;

/** Pure presentation rules for server-editor proxy test feedback. */
final class ServerEditorProxyTestPresentationPolicy {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private ServerEditorProxyTestPresentationPolicy() {}

  static ProxyTestSuccessPresentation successPresentation(
      boolean tls, IrcProperties.Proxy proxy, long elapsedMs) {
    return new ProxyTestSuccessPresentation(
        MESSAGES.text("servers.editor.proxy.status.ok", elapsedMs),
        MESSAGES.text(
            "servers.editor.proxy.test.success.message",
            MESSAGES.text(tls ? "common.yes" : "common.no"),
            proxySummary(proxy),
            elapsedMs));
  }

  static ProxyTestFailurePresentation failurePresentation(String shortMessage, String longMessage) {
    return new ProxyTestFailurePresentation(
        MESSAGES.text("servers.editor.proxy.status.failedWithReason", shortMessage),
        MESSAGES.text("servers.editor.proxy.test.failure.message", longMessage));
  }

  static ProxyTestFailurePresentation unexpectedFailurePresentation(String longMessage) {
    return new ProxyTestFailurePresentation(
        MESSAGES.text("servers.editor.proxy.status.failed"),
        MESSAGES.text("servers.editor.proxy.test.failure.message", longMessage));
  }

  private static String proxySummary(IrcProperties.Proxy proxy) {
    return proxy.enabled()
        ? proxy.host() + ":" + proxy.port()
        : MESSAGES.text("servers.editor.proxy.summary.disabled");
  }

  record ProxyTestSuccessPresentation(String statusText, String dialogMessage) {}

  record ProxyTestFailurePresentation(String statusText, String dialogMessage) {}
}
