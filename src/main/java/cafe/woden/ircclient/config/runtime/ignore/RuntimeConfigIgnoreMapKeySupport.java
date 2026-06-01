package cafe.woden.ircclient.config.runtime.ignore;

import java.util.Objects;

final class RuntimeConfigIgnoreMapKeySupport {
  private RuntimeConfigIgnoreMapKeySupport() {}

  static String persistedMaskMapKey(String mask) {
    String key = unwrapPersistedMaskMapKey(mask);
    if (key.isEmpty() || isSimpleConfigMapKey(key)) return key;
    return "[" + key.replace("]", "\\]") + "]";
  }

  static String unwrapPersistedMaskMapKey(String raw) {
    String key = Objects.toString(raw, "").trim();
    if (key.length() >= 2 && key.startsWith("[") && key.endsWith("]")) {
      return key.substring(1, key.length() - 1).replace("\\]", "]");
    }
    return key;
  }

  static boolean maskMapKeysMatch(String left, String right) {
    return unwrapPersistedMaskMapKey(left).equalsIgnoreCase(unwrapPersistedMaskMapKey(right));
  }

  private static boolean isSimpleConfigMapKey(String key) {
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      boolean simple =
          (c >= 'a' && c <= 'z')
              || (c >= 'A' && c <= 'Z')
              || (c >= '0' && c <= '9')
              || c == '-'
              || c == '.';
      if (!simple) return false;
    }
    return true;
  }
}
