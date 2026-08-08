package cafe.woden.ircclient.app.commands;

import java.util.Objects;

/** Feature-safe values for adding, replacing, and updating filter rules. */
public sealed interface FilterRuleMutationCommandSpec
    permits FilterRuleMutationCommandSpec.Add,
        FilterRuleMutationCommandSpec.AddReplace,
        FilterRuleMutationCommandSpec.Set {

  record Add(String name, FilterRulePatchSpec patch) implements FilterRuleMutationCommandSpec {
    public Add {
      name = Objects.toString(name, "");
      patch = Objects.requireNonNull(patch, "patch");
    }
  }

  record AddReplace(String name, FilterRulePatchSpec patch)
      implements FilterRuleMutationCommandSpec {
    public AddReplace {
      name = Objects.toString(name, "");
      patch = Objects.requireNonNull(patch, "patch");
    }
  }

  record Set(String name, FilterRulePatchSpec patch) implements FilterRuleMutationCommandSpec {
    public Set {
      name = Objects.toString(name, "");
      patch = Objects.requireNonNull(patch, "patch");
    }
  }
}
