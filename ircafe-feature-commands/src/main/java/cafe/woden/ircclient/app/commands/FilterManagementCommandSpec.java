package cafe.woden.ircclient.app.commands;

import java.util.Objects;

/** Feature-safe values for filter listing, export, and rule ordering commands. */
public sealed interface FilterManagementCommandSpec
    permits FilterManagementCommandSpec.ListRules,
        FilterManagementCommandSpec.Export,
        FilterManagementCommandSpec.Move {

  record ListRules(String format) implements FilterManagementCommandSpec {
    public ListRules {
      format = normalizeDefault(format, "table");
    }
  }

  record Export(String format, String file) implements FilterManagementCommandSpec {
    public Export {
      format = normalizeDefault(format, "all");
      file = normalizeOptional(file);
    }
  }

  record Move(
      String name,
      FilterMoveModeSpec mode,
      Integer positionOneBased,
      Integer amount,
      String other)
      implements FilterManagementCommandSpec {
    public Move {
      name = Objects.toString(name, "").trim();
      mode = Objects.requireNonNullElse(mode, FilterMoveModeSpec.TO);
      other = normalizeOptional(other);
    }
  }

  private static String normalizeOptional(String value) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isBlank() ? null : normalized;
  }

  private static String normalizeDefault(String value, String fallback) {
    String normalized = Objects.toString(value, "").trim();
    return normalized.isBlank() ? fallback : normalized;
  }
}
