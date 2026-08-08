package cafe.woden.ircclient.config.runtime.commands;

import static cafe.woden.ircclient.config.runtime.commands.RuntimeConfigUserCommandAliasesCodec.parseAliases;
import static cafe.woden.ircclient.config.runtime.commands.RuntimeConfigUserCommandAliasesCodec.serializeAliases;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.model.UserCommandAlias;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigUserCommandAliasesCodecTest {

  @Test
  void parseAliasesNormalizesValuesAndAcceptsLegacyExpansion() {
    Map<String, Object> current = new LinkedHashMap<>();
    current.put("enabled", "false");
    current.put("name", " hi ");
    current.put("template", "/msg %1 hello");

    Map<String, Object> legacy = new LinkedHashMap<>();
    legacy.put("name", "wave");
    legacy.put("expansion", "/me waves");

    assertEquals(
        List.of(
            new UserCommandAlias(false, "hi", "/msg %1 hello"),
            new UserCommandAlias(true, "wave", "/me waves")),
        parseAliases(List.of(current, "not-an-alias", legacy)));
    assertEquals(List.of(), parseAliases("not-a-list"));
  }

  @Test
  void serializeAliasesSkipsNullsAndUsesCanonicalKeys() {
    assertEquals(
        List.of(
            Map.of("enabled", true, "name", "hi", "template", "/msg %1 hello"),
            Map.of("enabled", false, "name", "wave", "template", "/me waves")),
        serializeAliases(
            java.util.Arrays.asList(
                new UserCommandAlias(true, " hi ", "/msg %1 hello"),
                null,
                new UserCommandAlias(false, "wave", "/me waves"))));
    assertEquals(List.of(), serializeAliases(null));
  }
}
