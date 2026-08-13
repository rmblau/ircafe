package cafe.woden.ircclient.config.runtime.notifications;

import cafe.woden.ircclient.config.properties.PushyProperties;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns Pushy notification settings under {@code ircafe.pushy}. */
public class RuntimeConfigPushyStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigPushyStore.class);
  private final RuntimeConfigYamlSection pushySection;

  public RuntimeConfigPushyStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.pushySection = RuntimeConfigYamlSection.ircafe(file, documentStore, log, "pushy");
  }

  public synchronized void rememberSettings(PushyProperties settings) {
    pushySection.mutateMap(
        "pushy settings",
        pushy -> {
          Map<String, Object> normalized =
              RuntimeConfigPushySettingsCodec.mergeSettings(pushy, settings);
          pushy.clear();
          pushy.putAll(normalized);
        });
  }
}
