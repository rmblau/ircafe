package cafe.woden.ircclient.config.yaml;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.slf4j.Logger;

/** Binds per-server YAML helper operations to one runtime-config document. */
@InfrastructureLayer
public final class RuntimeConfigServerYamlSection {

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;
  private final Logger log;
  private final String description;

  public RuntimeConfigServerYamlSection(
      Path file, RuntimeConfigDocumentStore documentStore, Logger log, String description) {
    this.file = file;
    this.documentStore = documentStore;
    this.log = log;
    this.description = description;
  }

  public Optional<Map<String, Object>> readExistingServer(String serverId) {
    return RuntimeConfigServerYamlSupport.readExistingServer(
        file, documentStore, log, description, serverId);
  }

  public void mutateExistingServer(String serverId, Consumer<Map<String, Object>> mutation) {
    RuntimeConfigServerYamlSupport.mutateExistingServer(
        file, documentStore, log, description, serverId, mutation);
  }
}
