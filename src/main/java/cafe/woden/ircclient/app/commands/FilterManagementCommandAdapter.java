package cafe.woden.ircclient.app.commands;

/** Adapts feature-owned filter management values to the root command model. */
final class FilterManagementCommandAdapter {

  FilterCommand toRoot(FilterManagementCommandSpec spec) {
    return switch (spec) {
      case FilterManagementCommandSpec.ListRules list ->
          new FilterCommand.ListRules(list.format());
      case FilterManagementCommandSpec.Export export ->
          new FilterCommand.Export(export.format(), export.file());
      case FilterManagementCommandSpec.Move move ->
          new FilterCommand.Move(
              move.name(),
              toRoot(move.mode()),
              move.positionOneBased(),
              move.amount(),
              move.other());
    };
  }

  private static FilterCommand.MoveMode toRoot(FilterMoveModeSpec mode) {
    return switch (mode) {
      case TO -> FilterCommand.MoveMode.TO;
      case TOP -> FilterCommand.MoveMode.TOP;
      case BOTTOM -> FilterCommand.MoveMode.BOTTOM;
      case UP -> FilterCommand.MoveMode.UP;
      case DOWN -> FilterCommand.MoveMode.DOWN;
      case BEFORE -> FilterCommand.MoveMode.BEFORE;
      case AFTER -> FilterCommand.MoveMode.AFTER;
    };
  }
}
