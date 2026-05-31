package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns CTCP auto-reply settings under {@code ircafe.ui.ctcpReplies}. */
class RuntimeConfigCtcpAutoReplyStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigCtcpAutoReplyStore.class);

  private final RuntimeConfigYamlSection ctcpRepliesSection;

  RuntimeConfigCtcpAutoReplyStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.ctcpRepliesSection =
        new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "ui", "ctcpReplies");
  }

  synchronized boolean readEnabled(boolean defaultValue) {
    return readBoolean("enabled", defaultValue);
  }

  synchronized boolean readVersionEnabled(boolean defaultValue) {
    return readBoolean("version", defaultValue);
  }

  synchronized boolean readPingEnabled(boolean defaultValue) {
    return readBoolean("ping", defaultValue);
  }

  synchronized boolean readTimeEnabled(boolean defaultValue) {
    return readBoolean("time", defaultValue);
  }

  synchronized void rememberEnabled(boolean enabled) {
    rememberBoolean("enabled", enabled);
  }

  synchronized void rememberVersionEnabled(boolean enabled) {
    rememberBoolean("version", enabled);
  }

  synchronized void rememberPingEnabled(boolean enabled) {
    rememberBoolean("ping", enabled);
  }

  synchronized void rememberTimeEnabled(boolean enabled) {
    rememberBoolean("time", enabled);
  }

  private boolean readBoolean(String key, boolean defaultValue) {
    return ctcpRepliesSection.readExistingValue("ui.ctcpReplies." + key, key)
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  private void rememberBoolean(String key, boolean enabled) {
    ctcpRepliesSection.putValue("ui.ctcpReplies." + key, enabled, key);
  }

}
