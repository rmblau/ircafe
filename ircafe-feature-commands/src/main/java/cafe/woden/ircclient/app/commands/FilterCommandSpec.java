package cafe.woden.ircclient.app.commands;

import java.util.Objects;

/** Feature-safe parsed values for the complete app-owned {@code /filter} command family. */
public sealed interface FilterCommandSpec
    permits FilterCommandSpec.Help,
        FilterCommandSpec.Error,
        FilterCommandSpec.RuleMutation,
        FilterCommandSpec.Display,
        FilterCommandSpec.Management,
        FilterCommandSpec.Lifecycle {

  record Help() implements FilterCommandSpec {}

  record Error(String message) implements FilterCommandSpec {
    public Error {
      message = Objects.toString(message, "").trim();
    }
  }

  record RuleMutation(FilterRuleMutationCommandSpec command) implements FilterCommandSpec {
    public RuleMutation {
      command = Objects.requireNonNull(command, "command");
    }
  }

  record Display(FilterDisplayCommandSpec command) implements FilterCommandSpec {
    public Display {
      command = Objects.requireNonNull(command, "command");
    }
  }

  record Management(FilterManagementCommandSpec command) implements FilterCommandSpec {
    public Management {
      command = Objects.requireNonNull(command, "command");
    }
  }

  record Lifecycle(FilterLifecycleCommandSpec command) implements FilterCommandSpec {
    public Lifecycle {
      command = Objects.requireNonNull(command, "command");
    }
  }
}
