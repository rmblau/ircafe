package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.mutateMap;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.readExistingValue;

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

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigUserCommandStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  synchronized List<UserCommandAlias> readAliases() {
    return readExistingValue(
            file, documentStore, log, "user command aliases", "ircafe", "commands", "aliases")
        .filter(List.class::isInstance)
        .map(value -> parseAliases((List<?>) value))
        .orElseGet(List::of);
  }

  synchronized boolean readUnknownCommandAsRawEnabled(boolean defaultValue) {
    return readExistingValue(
            file,
            documentStore,
            log,
            "commands.unknownCommandAsRaw",
            "ircafe",
            "commands",
            "unknownCommandAsRaw")
        .flatMap(RuntimeConfigYamlSupport::asBoolean)
        .orElse(defaultValue);
  }

  synchronized void rememberAliases(List<UserCommandAlias> aliases) {
    mutateMap(
        file,
        documentStore,
        log,
        "user command aliases",
        commands -> commands.put("aliases", serializeAliases(aliases)),
        "ircafe",
        "commands");
  }

  synchronized void rememberUnknownCommandAsRawEnabled(boolean enabled) {
    mutateMap(
        file,
        documentStore,
        log,
        "commands.unknownCommandAsRaw",
        commands -> commands.put("unknownCommandAsRaw", enabled),
        "ircafe",
        "commands");
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
