package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns nick color settings under {@code ircafe.ui}. */
public final class RuntimeConfigNickColorStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigNickColorStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigNickColorStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized void rememberColoringEnabled(boolean enabled) {
    uiSection.mutateMap(
        "nick coloring enabled setting", ui -> ui.put("nickColoringEnabled", enabled));
  }

  public synchronized void rememberMinContrast(double minContrast) {
    double mc = (minContrast > 0) ? minContrast : 3.0;
    uiSection.mutateMap("nick color contrast setting", ui -> ui.put("nickColorMinContrast", mc));
  }

  public synchronized void rememberOverrides(Map<String, String> overrides) {
    uiSection.mutateMap(
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

}
