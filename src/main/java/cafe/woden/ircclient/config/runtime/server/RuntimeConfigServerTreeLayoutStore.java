package cafe.woden.ircclient.config.runtime.server;

import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.normalizeBuiltInLayout;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.normalizeRootSiblingOrder;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.parseBuiltInLayoutByServer;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.parseBuiltInNodesVisibilityByServer;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.parseRootSiblingOrderByServer;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.rootSiblingNodeTokens;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.serializeBuiltInLayout;
import static cafe.woden.ircclient.config.runtime.server.RuntimeConfigServerTreeLayoutCodec.serializeBuiltInNodesVisibility;

import cafe.woden.ircclient.config.api.ServerTreeBuiltInVisibilityConfigPort.ServerTreeBuiltInNodesVisibility;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeBuiltInLayout;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeRootSiblingOrder;
import cafe.woden.ircclient.config.yaml.RuntimeConfigDocumentStore;
import cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSection;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns movable built-in server tree layout settings under {@code ircafe.ui.serverTree}. */
public class RuntimeConfigServerTreeLayoutStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigServerTreeLayoutStore.class);

  private final RuntimeConfigYamlSection uiSection;

  public RuntimeConfigServerTreeLayoutStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.uiSection = RuntimeConfigYamlSection.ircafeUi(file, documentStore, log);
  }

  /**
   * Reads persisted per-server visibility for built-in server tree nodes.
   *
   * <p>Stored under {@code ircafe.ui.serverTree.builtInNodesByServer.<serverId>}.
   */
  public synchronized Map<String, ServerTreeBuiltInNodesVisibility> readBuiltInNodesVisibility() {
    Object byServerObj = readServerTreeSection("built-in node visibility", "builtInNodesByServer");
    return parseBuiltInNodesVisibilityByServer(byServerObj);
  }

  /**
   * Persists per-server visibility for built-in server tree nodes.
   *
   * <p>When all flags are {@code true}, the server entry is removed to keep config compact.
   */
  public synchronized void rememberBuiltInNodesVisibility(
      String serverId, ServerTreeBuiltInNodesVisibility visibility) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;

    ServerTreeBuiltInNodesVisibility v =
        visibility != null ? visibility : ServerTreeBuiltInNodesVisibility.defaults();

    mutateServerTreeByServer(
        "built-in node visibility",
        "builtInNodesByServer",
        byServer -> {
          if (v.isDefaultVisible()) {
            byServer.remove(sid);
          } else {
            byServer.put(sid, serializeBuiltInNodesVisibility(v));
          }
        });
  }

  /**
   * Reads persisted per-server layout for movable built-in server tree nodes.
   *
   * <p>Stored under {@code ircafe.ui.serverTree.builtInLayoutByServer.<serverId>}.
   */
  public synchronized Map<String, ServerTreeBuiltInLayout> readBuiltInLayoutByServer() {
    Object byServerObj = readServerTreeSection("built-in layout", "builtInLayoutByServer");
    return parseBuiltInLayoutByServer(byServerObj);
  }

  /**
   * Persists per-server layout for movable built-in server tree nodes.
   *
   * <p>When layout matches defaults, the server entry is removed to keep config compact.
   */
  public synchronized void rememberBuiltInLayout(String serverId, ServerTreeBuiltInLayout layout) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;

    ServerTreeBuiltInLayout next =
        normalizeBuiltInLayout(layout == null ? ServerTreeBuiltInLayout.defaults() : layout);

    mutateServerTreeByServer(
        "built-in layout",
        "builtInLayoutByServer",
        byServer -> {
          if (next.isDefaultLayout()) {
            byServer.remove(sid);
          } else {
            byServer.put(sid, serializeBuiltInLayout(next));
          }
        });
  }

  /**
   * Reads persisted per-server order for top-level server sibling nodes.
   *
   * <p>Stored under {@code ircafe.ui.serverTree.rootSiblingOrderByServer.<serverId>}.
   */
  public synchronized Map<String, ServerTreeRootSiblingOrder> readRootSiblingOrderByServer() {
    Object byServerObj = readServerTreeSection("root sibling order", "rootSiblingOrderByServer");
    return parseRootSiblingOrderByServer(byServerObj);
  }

  /**
   * Persists per-server order for top-level server sibling nodes.
   *
   * <p>When order matches defaults, the server entry is removed to keep config compact.
   */
  public synchronized void rememberRootSiblingOrder(
      String serverId, ServerTreeRootSiblingOrder order) {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;

    ServerTreeRootSiblingOrder next =
        normalizeRootSiblingOrder(order == null ? ServerTreeRootSiblingOrder.defaults() : order);

    mutateServerTreeByServer(
        "root sibling order",
        "rootSiblingOrderByServer",
        byServer -> {
          if (next.isDefaultOrder()) {
            byServer.remove(sid);
          } else {
            byServer.put(sid, rootSiblingNodeTokens(next.order()));
          }
        });
  }

  private Object readServerTreeSection(String description, String key) {
    return uiSection
        .readExistingValue("server-tree " + description, "serverTree", key)
        .orElse(null);
  }

  private void mutateServerTreeByServer(
      String description, String key, java.util.function.Consumer<Map<String, Object>> mutation) {
    uiSection.mutateMapAndRemoveIfEmpty("server-tree " + description, mutation, "serverTree", key);
  }
}
