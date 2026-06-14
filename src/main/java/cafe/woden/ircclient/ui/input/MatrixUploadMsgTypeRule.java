package cafe.woden.ircclient.ui.input;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** File-extension to Matrix upload msgtype mapping contributed by upload plugins. */
@InterfaceLayer
public record MatrixUploadMsgTypeRule(String msgType, String[] extensions) {

  public MatrixUploadMsgTypeRule {
    msgType = normalizeMsgType(msgType);
    extensions = normalizeExtensions(extensions);
  }

  @Override
  public String[] extensions() {
    return extensions.clone();
  }

  private static String normalizeMsgType(String value) {
    String normalized = Objects.toString(value, "").trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("msgType must not be blank");
    }
    return normalized;
  }

  private static String[] normalizeExtensions(String[] values) {
    if (values == null || values.length == 0) {
      return new String[0];
    }
    return Arrays.stream(values)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .map(MatrixUploadMsgTypeRule::normalizeExtension)
        .filter(value -> !value.isEmpty())
        .distinct()
        .toArray(String[]::new);
  }

  private static String normalizeExtension(String value) {
    String normalized = value.toLowerCase(Locale.ROOT);
    while (normalized.startsWith(".")) {
      normalized = normalized.substring(1);
    }
    return normalized.trim();
  }
}
