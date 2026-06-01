package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns CTCP auto-reply settings under {@code ircafe.ui.ctcpReplies}. */
public class RuntimeConfigCtcpAutoReplyStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigCtcpAutoReplyStore.class);

  private final RuntimeConfigYamlSection ctcpRepliesSection;

  public RuntimeConfigCtcpAutoReplyStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.ctcpRepliesSection =
        RuntimeConfigYamlSection.ircafeUi(file, documentStore, log, "ctcpReplies");
  }

  public synchronized boolean readEnabled(boolean defaultValue) {
    return readBoolean("enabled", defaultValue);
  }

  public synchronized boolean readVersionEnabled(boolean defaultValue) {
    return readBoolean("version", defaultValue);
  }

  public synchronized boolean readPingEnabled(boolean defaultValue) {
    return readBoolean("ping", defaultValue);
  }

  public synchronized boolean readTimeEnabled(boolean defaultValue) {
    return readBoolean("time", defaultValue);
  }

  public synchronized void rememberEnabled(boolean enabled) {
    rememberBoolean("enabled", enabled);
  }

  public synchronized void rememberVersionEnabled(boolean enabled) {
    rememberBoolean("version", enabled);
  }

  public synchronized void rememberPingEnabled(boolean enabled) {
    rememberBoolean("ping", enabled);
  }

  public synchronized void rememberTimeEnabled(boolean enabled) {
    rememberBoolean("time", enabled);
  }

  private boolean readBoolean(String key, boolean defaultValue) {
    return ctcpRepliesSection
        .readExistingValue("ui.ctcpReplies." + key, key)
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  private void rememberBoolean(String key, boolean enabled) {
    ctcpRepliesSection.putValue("ui.ctcpReplies." + key, enabled, key);
  }
}
