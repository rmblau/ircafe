package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.runtime.ui.RuntimeConfigCtcpAutoReplySettingsCodec.Setting;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
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
    return readBoolean(Setting.ENABLED, defaultValue);
  }

  public synchronized boolean readVersionEnabled(boolean defaultValue) {
    return readBoolean(Setting.VERSION, defaultValue);
  }

  public synchronized boolean readPingEnabled(boolean defaultValue) {
    return readBoolean(Setting.PING, defaultValue);
  }

  public synchronized boolean readTimeEnabled(boolean defaultValue) {
    return readBoolean(Setting.TIME, defaultValue);
  }

  public synchronized void rememberEnabled(boolean enabled) {
    rememberBoolean(Setting.ENABLED, enabled);
  }

  public synchronized void rememberVersionEnabled(boolean enabled) {
    rememberBoolean(Setting.VERSION, enabled);
  }

  public synchronized void rememberPingEnabled(boolean enabled) {
    rememberBoolean(Setting.PING, enabled);
  }

  public synchronized void rememberTimeEnabled(boolean enabled) {
    rememberBoolean(Setting.TIME, enabled);
  }

  private boolean readBoolean(Setting setting, boolean defaultValue) {
    return ctcpRepliesSection
        .readExistingValue(setting.description(), setting.key())
        .flatMap(RuntimeConfigCtcpAutoReplySettingsCodec::readBoolean)
        .orElse(defaultValue);
  }

  private void rememberBoolean(Setting setting, boolean enabled) {
    ctcpRepliesSection.putValue(setting.description(), enabled, setting.key());
  }
}
