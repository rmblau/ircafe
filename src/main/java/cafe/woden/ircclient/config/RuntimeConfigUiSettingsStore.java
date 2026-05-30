package cafe.woden.ircclient.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns persisted UI settings and startup theme recovery state under {@code ircafe.ui}. */
class RuntimeConfigUiSettingsStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigUiSettingsStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigUiSettingsStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberUiSettings(String theme, String chatFontFamily, int chatFontSize) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ui = getOrCreateMap(ircafe, "ui");

      if (theme != null && !theme.isBlank()) ui.put("theme", theme);
      if (chatFontFamily != null && !chatFontFamily.isBlank()) {
        ui.put("chatFontFamily", chatFontFamily);
      }
      if (chatFontSize > 0) ui.put("chatFontSize", chatFontSize);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist UI config to '{}'", file, e);
    }
  }

  synchronized Optional<String> readStartupThemePending() {
    try {
      if (file.toString().isBlank()) return Optional.empty();
      if (!Files.exists(file)) return Optional.empty();

      Map<String, Object> doc = documentStore.load();
      String theme =
          RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "ui", "startupThemePending")
              .map(raw -> Objects.toString(raw, "").trim())
              .orElse("");
      if (theme.isEmpty()) return Optional.empty();
      return Optional.of(theme);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read ui.startupThemePending from '{}'", file, e);
      return Optional.empty();
    }
  }

  synchronized void rememberStartupThemePending(String theme) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> ircafe = getOrCreateMap(doc, "ircafe");
      Map<String, Object> ui = getOrCreateMap(ircafe, "ui");

      String normalized = Objects.toString(theme, "").trim();
      if (normalized.isEmpty()) {
        ui.remove("startupThemePending");
      } else {
        ui.put("startupThemePending", normalized);
      }

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist ui.startupThemePending to '{}'", file, e);
    }
  }

  synchronized void clearStartupThemePending() {
    rememberStartupThemePending(null);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }
}
