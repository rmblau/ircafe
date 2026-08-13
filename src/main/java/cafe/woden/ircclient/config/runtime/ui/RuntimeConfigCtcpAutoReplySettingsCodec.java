package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.util.Optional;

/** Pure helpers for persisted CTCP auto-reply setting values. */
final class RuntimeConfigCtcpAutoReplySettingsCodec {

  enum Setting {
    ENABLED("enabled"),
    VERSION("version"),
    PING("ping"),
    TIME("time");

    private final String key;

    Setting(String key) {
      this.key = key;
    }

    String key() {
      return key;
    }

    String description() {
      return "ui.ctcpReplies." + key;
    }
  }

  private RuntimeConfigCtcpAutoReplySettingsCodec() {}

  static Optional<Boolean> readBoolean(Object raw) {
    return RuntimeConfigYamlSupport.asBoolean(raw);
  }
}
