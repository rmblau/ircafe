package cafe.woden.ircclient.config.yaml;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jmolecules.architecture.layered.InfrastructureLayer;
import org.slf4j.Logger;

/**
 * Binds runtime-config YAML helper operations to a common document section.
 *
 * <p>Focused stores can keep their domain-specific normalization logic while sharing the repetitive
 * {@code ircafe.*} path prefix and document-store wiring.
 */
@InfrastructureLayer
public final class RuntimeConfigYamlSection {

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;
  private final Logger log;
  private final String[] basePath;

  public RuntimeConfigYamlSection(
      Path file, RuntimeConfigDocumentStore documentStore, Logger log, String... basePath) {
    if (basePath == null || basePath.length == 0) {
      throw new IllegalArgumentException("Runtime config YAML section path must not be empty");
    }
    this.file = file;
    this.documentStore = documentStore;
    this.log = log;
    this.basePath = Arrays.copyOf(basePath, basePath.length);
  }

  public Optional<Object> readValue(String description, String... path) {
    return RuntimeConfigYamlSupport.readValue(
        file, documentStore, log, description, resolve(path));
  }

  public Optional<Object> readExistingValue(String description, String... path) {
    return RuntimeConfigYamlSupport.readExistingValue(
        file, documentStore, log, description, resolve(path));
  }

  public void putValue(String description, Object value, String... path) {
    RuntimeConfigYamlSupport.putValue(
        file, documentStore, log, description, value, resolve(path));
  }

  public void removeValue(String description, String... path) {
    RuntimeConfigYamlSupport.removeValue(file, documentStore, log, description, resolve(path));
  }

  public void mutateMap(
      String description, Consumer<Map<String, Object>> mutation, String... path) {
    RuntimeConfigYamlSupport.mutateMap(
        file, documentStore, log, description, mutation, resolve(path));
  }

  public void mutateMapIfChanged(
      String description, Function<Map<String, Object>, Boolean> mutation, String... path) {
    RuntimeConfigYamlSupport.mutateMapIfChanged(
        file, documentStore, log, description, mutation, resolve(path));
  }

  public void mutateMapAndRemoveIfEmpty(
      String description, Consumer<Map<String, Object>> mutation, String... path) {
    RuntimeConfigYamlSupport.mutateMapAndRemoveIfEmpty(
        file, documentStore, log, description, mutation, resolve(path));
  }

  public void mutateExistingMapAndRemoveIfEmpty(
      String description, Function<Map<String, Object>, Boolean> mutation, String... path) {
    RuntimeConfigYamlSupport.mutateExistingMapAndRemoveIfEmpty(
        file, documentStore, log, description, mutation, resolve(path));
  }

  public void mutateDocument(
      String description, Function<Map<String, Object>, Boolean> mutation) {
    RuntimeConfigYamlSupport.mutateDocument(file, documentStore, log, description, mutation);
  }

  private String[] resolve(String... path) {
    int childLength = path == null ? 0 : path.length;
    String[] resolved = Arrays.copyOf(basePath, basePath.length + childLength);
    if (childLength > 0) {
      System.arraycopy(path, 0, resolved, basePath.length, childLength);
    }
    return resolved;
  }
}
