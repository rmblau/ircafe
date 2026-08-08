package cafe.woden.ircclient.app.commands;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/** Feature-safe parsed filter-rule patch that is independent of root filter model classes. */
public record FilterRulePatchSpec(
    String scope,
    boolean scopeSpecified,
    Boolean enabled,
    boolean enabledSpecified,
    Action action,
    boolean actionSpecified,
    Direction direction,
    boolean directionSpecified,
    EnumSet<Kind> kinds,
    boolean kindsSpecified,
    List<String> from,
    boolean fromSpecified,
    String tagsExpression,
    boolean tagsSpecified,
    RegexPattern textRegex,
    boolean textSpecified) {

  public FilterRulePatchSpec {
    scope = Objects.toString(scope, "").trim();
    kinds = kinds == null ? EnumSet.noneOf(Kind.class) : EnumSet.copyOf(kinds);
    from = from == null ? List.of() : List.copyOf(from);
    tagsExpression = Objects.toString(tagsExpression, "").trim();
  }

  public enum Action {
    HIDE,
    DIM,
    HIGHLIGHT
  }

  public enum Direction {
    ANY,
    IN,
    OUT
  }

  public enum Kind {
    CHAT,
    ACTION,
    NOTICE,
    STATUS,
    ERROR,
    PRESENCE,
    SPOILER
  }

  public enum RegexFlag {
    I,
    M,
    S
  }

  public record RegexPattern(String pattern, EnumSet<RegexFlag> flags) {
    public RegexPattern {
      pattern = Objects.toString(pattern, "");
      flags = flags == null ? EnumSet.noneOf(RegexFlag.class) : EnumSet.copyOf(flags);
    }
  }
}
