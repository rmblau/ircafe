package cafe.woden.ircclient.config;

import cafe.woden.ircclient.util.HexColorSupport;
import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

/** User-configured notification rule. */
@ValueObject
public record NotificationRule(
    String label,
    Type type,
    String pattern,
    boolean enabled,
    boolean caseSensitive,
    boolean wholeWord,
    /** Optional per-rule highlight color as a hex string (e.g. "#FF00FF"). */
    String highlightFg) {

  public enum Type {
    WORD,
    REGEX
  }

  public NotificationRule {
    if (type == null) type = Type.WORD;

    String p = Objects.toString(pattern, "").trim();
    pattern = p;

    String l = Objects.toString(label, "").trim();
    if (l.isEmpty() && !p.isEmpty()) l = p;
    label = l;

    highlightFg = HexColorSupport.normalizeHexColorLenient(highlightFg);

    // Empty patterns don't match anything.
    if (pattern.isEmpty()) enabled = false;
  }
}
