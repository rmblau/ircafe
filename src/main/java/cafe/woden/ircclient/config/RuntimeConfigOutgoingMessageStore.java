package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns outgoing message presentation settings under {@code ircafe.ui}. */
class RuntimeConfigOutgoingMessageStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigOutgoingMessageStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigOutgoingMessageStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberClientLineColorEnabled(boolean enabled) {
    rememberScalarSetting("clientLineColorEnabled", enabled, "outgoing message color enabled");
  }

  synchronized void rememberClientLineColor(String hex) {
    rememberScalarSetting(
        "clientLineColor", Objects.toString(hex, "").trim(), "outgoing message color");
  }

  synchronized void rememberOutgoingDeliveryIndicatorsEnabled(boolean enabled) {
    rememberScalarSetting(
        "outgoingDeliveryIndicatorsEnabled", enabled, "outgoing delivery indicators");
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ui = getOrCreateMapPath(doc, "ircafe", "ui");

      ui.put(key, value);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist {} setting to '{}'", description, file, e);
    }
  }

}
