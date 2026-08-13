package cafe.woden.ircclient.app.commands;

/** Adapts the complete feature-owned filter command value to the root command model. */
final class FilterCommandAdapter {

  private final FilterRuleMutationCommandAdapter ruleMutationAdapter =
      new FilterRuleMutationCommandAdapter();
  private final FilterDisplayCommandAdapter displayAdapter = new FilterDisplayCommandAdapter();
  private final FilterManagementCommandAdapter managementAdapter =
      new FilterManagementCommandAdapter();
  private final FilterLifecycleCommandAdapter lifecycleAdapter =
      new FilterLifecycleCommandAdapter();

  FilterCommand toRoot(FilterCommandSpec spec) {
    return switch (spec) {
      case FilterCommandSpec.Help ignored -> new FilterCommand.Help();
      case FilterCommandSpec.Error error -> new FilterCommand.Error(error.message());
      case FilterCommandSpec.RuleMutation mutation ->
          ruleMutationAdapter.toRoot(mutation.command());
      case FilterCommandSpec.Display display -> displayAdapter.toRoot(display.command());
      case FilterCommandSpec.Management management ->
          managementAdapter.toRoot(management.command());
      case FilterCommandSpec.Lifecycle lifecycle -> lifecycleAdapter.toRoot(lifecycle.command());
    };
  }
}
