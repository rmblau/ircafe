package cafe.woden.ircclient.config.runtime.server;

import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.parseBuiltInLayoutByServer;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.parseBuiltInNodesVisibilityByServer;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.parseRootSiblingOrderByServer;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.serializeBuiltInNodesVisibility;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cafe.woden.ircclient.config.api.ServerTreeBuiltInVisibilityConfigPort.ServerTreeBuiltInNodesVisibility;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeBuiltInLayout;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeBuiltInLayoutNode;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeRootSiblingNode;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeRootSiblingOrder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeConfigServerTreeLayoutCodecTest {

  @Test
  void parseBuiltInNodesVisibilityDefaultsMissingFieldsToVisibleAndSerializesAllFlags() {
    Map<String, Object> byServer = new LinkedHashMap<>();
    byServer.put("libera", Map.of("notifications", false, "interceptors", false));
    byServer.put(" ", Map.of("server", false));
    byServer.put("oftc", "malformed");

    Map<String, ServerTreeBuiltInNodesVisibility> parsed =
        parseBuiltInNodesVisibilityByServer(byServer);

    ServerTreeBuiltInNodesVisibility visibility =
        new ServerTreeBuiltInNodesVisibility(true, false, true, true, false);
    assertEquals(Map.of("libera", visibility), parsed);
    assertEquals(
        Map.of(
            "server",
            true,
            "notifications",
            false,
            "logViewer",
            true,
            "monitor",
            true,
            "interceptors",
            false),
        serializeBuiltInNodesVisibility(visibility));
  }

  @Test
  void parseBuiltInLayoutByServerNormalizesDuplicatesUnknownsAndMissingDefaults() {
    Map<String, Object> raw = new LinkedHashMap<>();
    raw.put(
        "root",
        List.of(
            "monitor",
            "server",
            "monitor",
            "unknown-node",
            ServerTreeBuiltInLayoutNode.NOTIFICATIONS));
    raw.put("other", List.of("filters", "server"));
    Map<String, Object> byServer = Map.of("libera", raw);

    Map<String, ServerTreeBuiltInLayout> parsed = parseBuiltInLayoutByServer(byServer);

    assertEquals(
        new ServerTreeBuiltInLayout(
            List.of(
                ServerTreeBuiltInLayoutNode.MONITOR,
                ServerTreeBuiltInLayoutNode.SERVER,
                ServerTreeBuiltInLayoutNode.NOTIFICATIONS),
            List.of(
                ServerTreeBuiltInLayoutNode.FILTERS,
                ServerTreeBuiltInLayoutNode.LOG_VIEWER,
                ServerTreeBuiltInLayoutNode.IGNORES,
                ServerTreeBuiltInLayoutNode.INTERCEPTORS)),
        parsed.get("libera"));
  }

  @Test
  void parseRootSiblingOrderByServerAcceptsMapShapeAndCompletesDefaults() {
    Map<String, Object> byServer =
        Map.of(
            "libera",
            Map.of("order", List.of("other", "privateMessages", "other", "unknown-node")),
            "oftc",
            List.of("channelList", "notifications", "other", "privateMessages"));

    Map<String, ServerTreeRootSiblingOrder> parsed = parseRootSiblingOrderByServer(byServer);

    assertEquals(
        Map.of(
            "libera",
            new ServerTreeRootSiblingOrder(
                List.of(
                    ServerTreeRootSiblingNode.OTHER,
                    ServerTreeRootSiblingNode.PRIVATE_MESSAGES,
                    ServerTreeRootSiblingNode.CHANNEL_LIST,
                    ServerTreeRootSiblingNode.NOTIFICATIONS))),
        parsed);
  }
}
