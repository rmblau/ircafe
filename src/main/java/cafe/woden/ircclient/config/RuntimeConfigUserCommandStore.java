package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;

import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import cafe.woden.ircclient.model.UserCommandAlias;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns user-command alias settings under {@code ircafe.commands}. */
class RuntimeConfigUserCommandStore {

  private static final Logger log = LoggerFactory.getLogger(RuntimeConfigUserCommandStore.class);

  private final RuntimeConfigYamlSection commandsSection;

  RuntimeConfigUserCommandStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.commandsSection =
        new RuntimeConfigYamlSection(file, documentStore, log, "ircafe", "commands");
  }

  synchronized List<UserCommandAlias> readAliases() {
    return commandsSection.readExistingValue("user command aliases", "aliases")
        .filter(List.class::isInstance)
        .map(value -> parseAliases((List<?>) value))
        .orElseGet(List::of);
  }

  synchronized boolean readUnknownCommandAsRawEnabled(boolean defaultValue) {
    return commandsSection.readExistingValue("commands.unknownCommandAsRaw", "unknownCommandAsRaw")
        .flatMap(value -> asBoolean(value))
        .orElse(defaultValue);
  }

  synchronized void rememberAliases(List<UserCommandAlias> aliases) {
    commandsSection.mutateMap(
        "user command aliases", commands -> commands.put("aliases", serializeAliases(aliases)));
  }

  synchronized void rememberUnknownCommandAsRawEnabled(boolean enabled) {
    commandsSection.mutateMap(
        "commands.unknownCommandAsRaw", commands -> commands.put("unknownCommandAsRaw", enabled));
  }

  private static List<UserCommandAlias> parseAliases(List<?> raw) {
    List<UserCommandAlias> out = new ArrayList<>();
    for (Object item : raw) {
      if (!(item instanceof Map<?, ?> m)) continue;

      boolean enabled = asBoolean(m.get("enabled")).orElse(Boolean.TRUE);
      String name = Objects.toString(m.get("name"), "").trim();

      // Accept both "template" and legacy/alternate "expansion" key names.
      String template = Objects.toString(m.get("template"), "");
      if (template.isEmpty()) template = Objects.toString(m.get("expansion"), "");

      out.add(new UserCommandAlias(enabled, name, template));
    }
    return List.copyOf(out);
  }

  private static List<Map<String, Object>> serializeAliases(List<UserCommandAlias> aliases) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (aliases != null) {
      for (UserCommandAlias alias : aliases) {
        if (alias == null) continue;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("enabled", alias.enabled());
        m.put("name", Objects.toString(alias.name(), "").trim());
        m.put("template", Objects.toString(alias.template(), ""));
        out.add(m);
      }
    }
    return out;
  }

}
