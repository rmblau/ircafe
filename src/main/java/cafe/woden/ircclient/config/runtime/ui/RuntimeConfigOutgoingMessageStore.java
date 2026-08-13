package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns outgoing message presentation settings under {@code ircafe.ui}. */
public class RuntimeConfigOutgoingMessageStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigOutgoingMessageStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigOutgoingMessageStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized void rememberClientLineColorEnabled(boolean enabled) {
    rememberScalarSetting("clientLineColorEnabled", enabled, "outgoing message color enabled");
  }

  public synchronized void rememberClientLineColor(String hex) {
    rememberScalarSetting(
        "clientLineColor",
        RuntimeConfigOutgoingMessageSettingsCodec.normalizeClientLineColor(hex),
        "outgoing message color");
  }

  public synchronized void rememberOutgoingDeliveryIndicatorsEnabled(boolean enabled) {
    rememberScalarSetting(
        "outgoingDeliveryIndicatorsEnabled", enabled, "outgoing delivery indicators");
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    uiSection.putValue(description, value, key);
  }
}
