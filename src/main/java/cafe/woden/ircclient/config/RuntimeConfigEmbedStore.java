package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns image embed and link preview settings under {@code ircafe.ui}. */
class RuntimeConfigEmbedStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigEmbedStore.class);

  private final RuntimeConfigYamlSection uiSection;

  RuntimeConfigEmbedStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "ui");
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
    uiSection.putValue(description, value, key);
  }

}
