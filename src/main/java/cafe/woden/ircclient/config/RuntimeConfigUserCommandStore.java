package cafe.woden.ircclient.config;

import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.asBoolean;
import static cafe.woden.ircclient.config.RuntimeConfigYamlSupport.getOrCreateMapPath;

import cafe.woden.ircclient.model.UserCommandAlias;
import java.nio.file.Files;
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
    try {
      if (file.toString().isBlank()) return List.of();
      if (!Files.exists(file)) return List.of();

      Map<String, Object> doc = documentStore.load();
      Object aliasesObj =
          RuntimeConfigDocumentPathReader.readValue(doc, "ircafe", "commands", "aliases")
              .orElse(null);
      if (!(aliasesObj instanceof List<?> raw)) return List.of();

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
    } catch (Exception e) {
      log.warn("[ircafe] Could not read user command aliases from '{}'", file, e);
      return List.of();
    }
  }

  synchronized boolean readUnknownCommandAsRawEnabled(boolean defaultValue) {
    try {
      if (file.toString().isBlank()) return defaultValue;
      if (!Files.exists(file)) return defaultValue;

      Map<String, Object> doc = documentStore.load();
      return RuntimeConfigDocumentPathReader.readValue(
              doc, "ircafe", "commands", "unknownCommandAsRaw")
          .flatMap(RuntimeConfigYamlSupport::asBoolean)
          .orElse(defaultValue);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read commands.unknownCommandAsRaw from '{}'", file, e);
      return defaultValue;
    }
  }

  synchronized void rememberAliases(List<UserCommandAlias> aliases) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> commands = getOrCreateMapPath(doc, "ircafe", "commands");

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

      commands.put("aliases", out);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist user command aliases to '{}'", file, e);
    }
  }

  synchronized void rememberUnknownCommandAsRawEnabled(boolean enabled) {
    try {
      if (file.toString().isBlank()) return;

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> commands = getOrCreateMapPath(doc, "ircafe", "commands");

      commands.put("unknownCommandAsRaw", enabled);
      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist commands.unknownCommandAsRaw to '{}'", file, e);
    }
  }

}
