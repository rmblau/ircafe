package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns image embed and link preview settings under {@code ircafe.ui}. */
class RuntimeConfigEmbedStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigEmbedStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigEmbedStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized void rememberImageEmbedsEnabled(boolean enabled) {
    rememberScalarSetting("imageEmbedsEnabled", enabled, "image embed");
  }

  synchronized void rememberImageEmbedsCollapsedByDefault(boolean collapsed) {
    rememberScalarSetting("imageEmbedsCollapsedByDefault", collapsed, "image embed collapse");
  }

  synchronized void rememberImageEmbedsMaxWidthPx(int maxWidthPx) {
    rememberScalarSetting(
        "imageEmbedsMaxWidthPx", Math.max(0, maxWidthPx), "image embed max width");
  }

  synchronized void rememberImageEmbedsMaxHeightPx(int maxHeightPx) {
    rememberScalarSetting(
        "imageEmbedsMaxHeightPx", Math.max(0, maxHeightPx), "image embed max height");
  }

  synchronized void rememberImageEmbedsAnimateGifs(boolean animate) {
    rememberScalarSetting("imageEmbedsAnimateGifs", animate, "image embed GIF animation");
  }

  synchronized void rememberLinkPreviewsEnabled(boolean enabled) {
    rememberScalarSetting("linkPreviewsEnabled", enabled, "link preview");
  }

  synchronized void rememberLinkPreviewsCollapsedByDefault(boolean collapsed) {
    rememberScalarSetting("linkPreviewsCollapsedByDefault", collapsed, "link preview collapse");
  }

  synchronized void rememberEmbedCardStyle(String styleToken) {
    String token = Objects.toString(styleToken, "").trim().toLowerCase(Locale.ROOT);
    if (token.isBlank()) token = "default";
    rememberScalarSetting("embedCardStyle", token, "embed card style");
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
