package cafe.woden.ircclient.config.api;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Runtime-config contract for embedded image and link preview settings. */
@SecondaryPort
@ApplicationLayer
public interface EmbedPreviewRuntimeConfigPort {

  void rememberEmbedPreviewSettings(EmbedPreviewSnapshot settings);

  record EmbedPreviewSnapshot(
      boolean imageEmbedsEnabled,
      boolean imageEmbedsCollapsedByDefault,
      int imageEmbedsMaxWidthPx,
      int imageEmbedsMaxHeightPx,
      boolean imageEmbedsAnimateGifs,
      boolean linkPreviewsEnabled,
      boolean linkPreviewsCollapsedByDefault,
      String embedCardStyleToken) {
    public EmbedPreviewSnapshot {
      embedCardStyleToken = embedCardStyleToken == null ? "" : embedCardStyleToken;
    }
  }
}
