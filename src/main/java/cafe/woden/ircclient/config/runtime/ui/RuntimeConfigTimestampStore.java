package cafe.woden.ircclient.config.runtime.ui;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.getOrCreateMap;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns timestamp settings under {@code ircafe.ui.timestamps}. */
public class RuntimeConfigTimestampStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigTimestampStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigTimestampStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized void rememberEnabled(boolean enabled) {
    rememberSetting("enabled", enabled);
  }

  public synchronized void rememberFormat(String format) {
    String fmt = (format == null || format.isBlank()) ? "HH:mm:ss" : format.trim();
    rememberSetting("format", fmt);
  }

  public synchronized void rememberIncludeChatMessages(boolean includeChatMessages) {
    rememberSetting("includeChatMessages", includeChatMessages);
  }

  public synchronized void rememberIncludePresenceMessages(boolean includePresenceMessages) {
    rememberSetting("includePresenceMessages", includePresenceMessages);
  }

  private void rememberSetting(String key, Object value) {
    uiSection.mutateMap(
        "timestamp " + key + " setting",
        ui -> {
          getOrCreateMap(ui, "timestamps").put(key, value);
          ui.remove("chatMessageTimestampsEnabled");
        });
  }
}
