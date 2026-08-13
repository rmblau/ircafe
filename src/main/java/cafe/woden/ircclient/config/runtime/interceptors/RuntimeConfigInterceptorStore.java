package cafe.woden.ircclient.config.runtime.interceptors;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.model.InterceptorDefinition;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted interceptor definitions under {@code ircafe.ui.interceptors}. */
public class RuntimeConfigInterceptorStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigInterceptorStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigInterceptorStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized Map<String, List<InterceptorDefinition>> readDefinitions() {
    return uiSection
        .readExistingValue("interceptor definitions", "interceptors", "servers")
        .map(RuntimeConfigInterceptorDefinitionsCodec::parseByServer)
        .orElseGet(Map::of);
  }

  public synchronized void rememberDefinitions(
      Map<String, List<InterceptorDefinition>> defsByServer) {
    uiSection.mutateMapAndRemoveIfEmpty(
        "interceptor definitions",
        interceptors -> {
          Map<String, Object> serversOut =
              RuntimeConfigInterceptorDefinitionsCodec.serializeByServer(defsByServer);
          if (serversOut.isEmpty()) {
            interceptors.remove("servers");
          } else {
            interceptors.put("servers", serversOut);
          }
        },
        "interceptors");
  }
}
