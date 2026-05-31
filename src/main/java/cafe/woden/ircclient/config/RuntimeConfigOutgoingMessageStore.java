package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns outgoing message presentation settings under {@code ircafe.ui}. */
class RuntimeConfigOutgoingMessageStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigOutgoingMessageStore.class);

  private final RuntimeConfigYamlSection uiSection;

  RuntimeConfigOutgoingMessageStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "ui");
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
    uiSection.putValue(description, value, key);
  }
}
