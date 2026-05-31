package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.mutateMap;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns nick color settings under {@code ircafe.ui}. */
class RuntimeConfigNickColorStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigNickColorStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigNickColorStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberColoringEnabled(boolean enabled) {
    mutateUi("nick coloring enabled setting", ui -> ui.put("nickColoringEnabled", enabled));
  }

  synchronized void rememberMinContrast(double minContrast) {
    double mc = (minContrast > 0) ? minContrast : 3.0;
    mutateUi("nick color contrast setting", ui -> ui.put("nickColorMinContrast", mc));
  }

  synchronized void rememberOverrides(Map<String, String> overrides) {
    mutateUi(
        "nick color overrides",
        ui -> {
          if (overrides == null || overrides.isEmpty()) {
            ui.remove("nickColorOverrides");
            return;
          }

          Map<String, Object> out = new LinkedHashMap<>();
          for (Map.Entry<String, String> e : overrides.entrySet()) {
            String nick = Objects.toString(e.getKey(), "").trim();
            String color = Objects.toString(e.getValue(), "").trim();
            if (nick.isEmpty() || color.isEmpty()) continue;
            out.put(nick, color);
          }
          ui.put("nickColorOverrides", out);
        });
  }

  private void mutateUi(String description, Consumer<Map<String, Object>> mutation) {
    mutateMap(file, documentStore, log, description, mutation, "ircafe", "ui");
  }
}
