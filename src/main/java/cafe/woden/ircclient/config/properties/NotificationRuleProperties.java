package cafe.woden.ircclient.config.properties;

import cafe.woden.ircclient.util.HexColorSupport;
import java.util.Objects;

/**
 * Config-backed notification rule definition.
 *
 * <p>These rules are intended for user-configured keyword / regex matches.
 */
public record NotificationRuleProperties(
    Boolean enabled,
    String label,
    Type type,
    String pattern,
    Boolean caseSensitive,
    Boolean wholeWord,
    /** Optional per-rule highlight color as a hex string (e.g. "#FF00FF"). */
    String highlightFg) {

  public enum Type {
    WORD,
    REGEX
  }

  public NotificationRuleProperties {
    if (enabled == null) enabled = true;

    if (type == null) type = Type.WORD;

    if (caseSensitive == null) caseSensitive = false;
    if (wholeWord == null) wholeWord = true;

    String p = Objects.toString(pattern, "").trim();
    pattern = p;

    String l = Objects.toString(label, "").trim();
    if (l.isEmpty() && !p.isEmpty()) l = p;
    label = l;

    highlightFg = HexColorSupport.normalizeHexColorLenient(highlightFg);

    // No pattern means no match; treat as disabled.
    if (pattern.isEmpty()) enabled = false;
  }
}
