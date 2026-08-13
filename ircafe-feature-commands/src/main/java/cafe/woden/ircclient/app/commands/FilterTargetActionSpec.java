package cafe.woden.ircclient.app.commands;

/** Feature-safe action for /filter commands that target names or masks. */
public enum FilterTargetActionSpec {
  DELETE,
  ENABLE,
  DISABLE,
  TOGGLE
}
