package cafe.woden.ircclient.config.runtime.ui;

import java.util.Locale;
import java.util.Objects;

/** Pure normalization helpers for persisted image embed and link preview settings. */
final class RuntimeConfigEmbedSettingsCodec {

  private RuntimeConfigEmbedSettingsCodec() {}

  static int normalizeImageDimensionPx(int value) {
    return Math.max(0, value);
  }

  static String normalizeEmbedCardStyle(String styleToken) {
    String token = Objects.toString(styleToken, "").trim().toLowerCase(Locale.ROOT);
    return token.isBlank() ? "default" : token;
  }
}
