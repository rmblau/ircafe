package cafe.woden.ircclient.app.commands;

import java.util.List;

/** Feature-safe values for filter lifecycle and name/mask commands. */
public sealed interface FilterLifecycleCommandSpec
    permits FilterLifecycleCommandSpec.Rename,
        FilterLifecycleCommandSpec.Recreate,
        FilterLifecycleCommandSpec.Targets {

  record Rename(String name, String newName) implements FilterLifecycleCommandSpec {}

  record Recreate(String name) implements FilterLifecycleCommandSpec {}

  record Targets(FilterTargetActionSpec action, List<String> namesOrMasks)
      implements FilterLifecycleCommandSpec {
    public Targets {
      namesOrMasks = namesOrMasks == null ? List.of() : List.copyOf(namesOrMasks);
    }
  }
}
