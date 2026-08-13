package cafe.woden.ircclient.app.commands;

/** Adapts feature-owned filter lifecycle values to the root command model. */
final class FilterLifecycleCommandAdapter {

  FilterCommand toRoot(FilterLifecycleCommandSpec spec) {
    return switch (spec) {
      case FilterLifecycleCommandSpec.Rename rename ->
          new FilterCommand.Rename(rename.name(), rename.newName());
      case FilterLifecycleCommandSpec.Recreate recreate ->
          new FilterCommand.Recreate(recreate.name());
      case FilterLifecycleCommandSpec.Targets targets -> adaptTargets(targets);
    };
  }

  private static FilterCommand adaptTargets(FilterLifecycleCommandSpec.Targets targets) {
    return switch (targets.action()) {
      case DELETE -> new FilterCommand.Del(targets.namesOrMasks());
      case ENABLE -> new FilterCommand.Enable(targets.namesOrMasks());
      case DISABLE -> new FilterCommand.Disable(targets.namesOrMasks());
      case TOGGLE -> new FilterCommand.Toggle(targets.namesOrMasks());
    };
  }
}
