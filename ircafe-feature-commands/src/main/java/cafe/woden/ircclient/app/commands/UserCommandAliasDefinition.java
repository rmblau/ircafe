package cafe.woden.ircclient.app.commands;

import java.util.Objects;

/** Feature-safe user-defined slash-command alias definition. */
public record UserCommandAliasDefinition(boolean enabled, String name, String template) {

  public UserCommandAliasDefinition {
    name = Objects.toString(name, "").trim();
    template = Objects.toString(template, "");
  }
}
