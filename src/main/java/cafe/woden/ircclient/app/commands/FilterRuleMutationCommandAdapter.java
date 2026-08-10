package cafe.woden.ircclient.app.commands;

/** Adapts feature-owned filter rule mutation values to the root command model. */
final class FilterRuleMutationCommandAdapter {

  private final FilterRulePatchAdapter patchAdapter = new FilterRulePatchAdapter();

  FilterCommand toRoot(FilterRuleMutationCommandSpec spec) {
    return switch (spec) {
      case FilterRuleMutationCommandSpec.Add add ->
          new FilterCommand.Add(add.name(), patchAdapter.toRoot(add.patch()));
      case FilterRuleMutationCommandSpec.AddReplace addReplace ->
          new FilterCommand.AddReplace(addReplace.name(), patchAdapter.toRoot(addReplace.patch()));
      case FilterRuleMutationCommandSpec.Set set ->
          new FilterCommand.Set(set.name(), patchAdapter.toRoot(set.patch()));
    };
  }
}
