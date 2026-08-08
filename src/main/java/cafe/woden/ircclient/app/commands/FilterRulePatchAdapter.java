package cafe.woden.ircclient.app.commands;

import cafe.woden.ircclient.model.FilterAction;
import cafe.woden.ircclient.model.FilterDirection;
import cafe.woden.ircclient.model.LogKind;
import cafe.woden.ircclient.model.RegexFlag;
import cafe.woden.ircclient.model.RegexSpec;
import java.util.EnumSet;

/** Adapts feature-owned filter patch values into the root filter model. */
final class FilterRulePatchAdapter {

  FilterCommand.FilterRulePatch toRoot(FilterRulePatchSpec source) {
    if (source == null) return FilterCommand.FilterRulePatch.empty();

    return new FilterCommand.FilterRulePatch(
        source.scope(),
        source.scopeSpecified(),
        source.enabled(),
        source.enabledSpecified(),
        mapAction(source.action()),
        source.actionSpecified(),
        mapDirection(source.direction()),
        source.directionSpecified(),
        mapKinds(source.kinds()),
        source.kindsSpecified(),
        source.from(),
        source.fromSpecified(),
        source.tagsExpression(),
        source.tagsSpecified(),
        mapRegex(source.textRegex()),
        source.textSpecified());
  }

  private static FilterAction mapAction(FilterRulePatchSpec.Action action) {
    return action == null ? null : FilterAction.valueOf(action.name());
  }

  private static FilterDirection mapDirection(FilterRulePatchSpec.Direction direction) {
    return direction == null ? null : FilterDirection.valueOf(direction.name());
  }

  private static EnumSet<LogKind> mapKinds(EnumSet<FilterRulePatchSpec.Kind> kinds) {
    EnumSet<LogKind> mapped = EnumSet.noneOf(LogKind.class);
    if (kinds != null) {
      kinds.forEach(kind -> mapped.add(LogKind.valueOf(kind.name())));
    }
    return mapped;
  }

  private static RegexSpec mapRegex(FilterRulePatchSpec.RegexPattern regex) {
    if (regex == null) return null;
    EnumSet<RegexFlag> flags = EnumSet.noneOf(RegexFlag.class);
    regex.flags().forEach(flag -> flags.add(RegexFlag.valueOf(flag.name())));
    return new RegexSpec(regex.pattern(), flags);
  }
}
