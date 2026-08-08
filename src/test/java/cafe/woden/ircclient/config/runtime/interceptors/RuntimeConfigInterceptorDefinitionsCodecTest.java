package cafe.woden.ircclient.config.runtime.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafe.woden.ircclient.model.InterceptorDefinition;
import cafe.woden.ircclient.model.InterceptorRule;
import cafe.woden.ircclient.model.InterceptorRuleMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigInterceptorDefinitionsCodecTest {

  @Test
  void parseByServerMigratesLegacyInterceptorShape() {
    Map<String, Object> legacyDef = new LinkedHashMap<>();
    legacyDef.put("id", " watcher ");
    legacyDef.put("name", " Legacy watcher ");
    legacyDef.put("channelsCsv", " #ops ");
    legacyDef.put("mode", "regex");
    legacyDef.put("pattern", " ping.* ");

    Map<String, Object> servers = Map.of(" libera ", List.of(legacyDef));

    Map<String, List<InterceptorDefinition>> parsed =
        RuntimeConfigInterceptorDefinitionsCodec.parseByServer(servers);

    InterceptorDefinition def = parsed.get("libera").getFirst();
    assertEquals("watcher", def.id());
    assertEquals("Legacy watcher", def.name());
    assertEquals("libera", def.scopeServerId());
    assertEquals("#ops", def.channelIncludes());
    assertEquals(InterceptorRuleMode.REGEX, def.rules().getFirst().messageMode());
    assertEquals("ping.*", def.rules().getFirst().messagePattern());
  }

  @Test
  void serializeByServerSkipsBlankServersAndKeepsAnyServerScope() {
    InterceptorDefinition def =
        new InterceptorDefinition(
            "id-1",
            "Watcher",
            true,
            "",
            InterceptorRuleMode.ALL,
            "",
            InterceptorRuleMode.NONE,
            "",
            false,
            true,
            false,
            "",
            false,
            "",
            false,
            "",
            "",
            "",
            List.of(
                new InterceptorRule(
                    true,
                    "Message",
                    "message",
                    InterceptorRuleMode.LIKE,
                    "ping",
                    InterceptorRuleMode.ALL,
                    "",
                    InterceptorRuleMode.ALL,
                    "")));

    Map<String, Object> serialized =
        RuntimeConfigInterceptorDefinitionsCodec.serializeByServer(
            Map.of(" ", List.of(def), "libera", List.of(def)));

    assertFalse(serialized.containsKey(" "));
    assertTrue(serialized.containsKey("libera"));

    @SuppressWarnings("unchecked")
    Map<String, Object> serializedDef =
        ((List<Map<String, Object>>) serialized.get("libera")).getFirst();
    assertEquals("", serializedDef.get("scopeServerId"));
    assertEquals("NOTIF_1", serializedDef.get("actionSoundId"));
  }
}
