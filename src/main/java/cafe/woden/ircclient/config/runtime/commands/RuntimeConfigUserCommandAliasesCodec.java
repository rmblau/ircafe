package cafe.woden.ircclient.config.runtime.commands;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;

import cafe.woden.ircclient.model.UserCommandAlias;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Pure codec helpers for persisted user-command aliases. */
final class RuntimeConfigUserCommandAliasesCodec {

  private RuntimeConfigUserCommandAliasesCodec() {}

  static List<UserCommandAlias> parseAliases(Object rawAliases) {
    if (!(rawAliases instanceof List<?> raw)) return List.of();

    List<UserCommandAlias> out = new ArrayList<>();
    for (Object item : raw) {
      if (!(item instanceof Map<?, ?> alias)) continue;

      boolean enabled = asBoolean(alias.get("enabled")).orElse(Boolean.TRUE);
      String name = Objects.toString(alias.get("name"), "").trim();

      // Accept both "template" and legacy/alternate "expansion" key names.
      String template = Objects.toString(alias.get("template"), "");
      if (template.isEmpty()) template = Objects.toString(alias.get("expansion"), "");

      out.add(new UserCommandAlias(enabled, name, template));
    }
    return List.copyOf(out);
  }

  static List<Map<String, Object>> serializeAliases(List<UserCommandAlias> aliases) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (aliases == null) return out;

    for (UserCommandAlias alias : aliases) {
      if (alias == null) continue;
      Map<String, Object> serialized = new LinkedHashMap<>();
      serialized.put("enabled", alias.enabled());
      serialized.put("name", Objects.toString(alias.name(), "").trim());
      serialized.put("template", Objects.toString(alias.template(), ""));
      out.add(serialized);
    }
    return out;
  }
}
