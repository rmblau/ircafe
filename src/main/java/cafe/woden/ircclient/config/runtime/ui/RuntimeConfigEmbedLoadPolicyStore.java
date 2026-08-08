package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns advanced embed/link loading policy settings under {@code ircafe.ui.embedLoadPolicy}. */
public class RuntimeConfigEmbedLoadPolicyStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigEmbedLoadPolicyStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigEmbedLoadPolicyStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized EmbedLoadPolicySnapshot read() {
    return uiSection
        .readValue("embed/link load policy", "embedLoadPolicy")
        .filter(Map.class::isInstance)
        .map(Map.class::cast)
        .map(RuntimeConfigEmbedLoadPolicyCodec::parseSnapshot)
        .orElseGet(EmbedLoadPolicySnapshot::defaults);
  }

  public synchronized void remember(EmbedLoadPolicySnapshot snapshot) {
    Map<String, Object> policy = RuntimeConfigEmbedLoadPolicyCodec.serializeSnapshot(snapshot);
    uiSection.mutateMap(
        "embed/link load policy",
        ui -> {
          if (policy.isEmpty()) ui.remove("embedLoadPolicy");
          else ui.put("embedLoadPolicy", policy);
        });
  }
}
