package cafe.woden.ircclient.config.runtime.ui;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns image embed and link preview settings under {@code ircafe.ui}. */
public class RuntimeConfigEmbedStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigEmbedStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigEmbedStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  public synchronized void rememberImageEmbedsEnabled(boolean enabled) {
    rememberScalarSetting("imageEmbedsEnabled", enabled, "image embed");
  }

  public synchronized void rememberImageEmbedsCollapsedByDefault(boolean collapsed) {
    rememberScalarSetting("imageEmbedsCollapsedByDefault", collapsed, "image embed collapse");
  }

  public synchronized void rememberImageEmbedsMaxWidthPx(int maxWidthPx) {
    rememberScalarSetting(
        "imageEmbedsMaxWidthPx", Math.max(0, maxWidthPx), "image embed max width");
  }

  public synchronized void rememberImageEmbedsMaxHeightPx(int maxHeightPx) {
    rememberScalarSetting(
        "imageEmbedsMaxHeightPx", Math.max(0, maxHeightPx), "image embed max height");
  }

  public synchronized void rememberImageEmbedsAnimateGifs(boolean animate) {
    rememberScalarSetting("imageEmbedsAnimateGifs", animate, "image embed GIF animation");
  }

  public synchronized void rememberLinkPreviewsEnabled(boolean enabled) {
    rememberScalarSetting("linkPreviewsEnabled", enabled, "link preview");
  }

  public synchronized void rememberLinkPreviewsCollapsedByDefault(boolean collapsed) {
    rememberScalarSetting("linkPreviewsCollapsedByDefault", collapsed, "link preview collapse");
  }

  public synchronized void rememberEmbedCardStyle(String styleToken) {
    String token = Objects.toString(styleToken, "").trim().toLowerCase(Locale.ROOT);
    if (token.isBlank()) token = "default";
    rememberScalarSetting("embedCardStyle", token, "embed card style");
  }

  private void rememberScalarSetting(String key, Object value, String description) {
    uiSection.putValue(description, value, key);
  }
}
