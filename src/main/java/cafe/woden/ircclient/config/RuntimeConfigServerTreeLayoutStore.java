package cafe.woden.ircclient.config;

import cafe.woden.ircclient.config.api.ServerTreeBuiltInVisibilityConfigPort.ServerTreeBuiltInNodesVisibility;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeBuiltInLayout;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeBuiltInLayoutNode;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeRootSiblingNode;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeRootSiblingOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns movable built-in server tree layout settings under {@code ircafe.ui.serverTree}. */
class RuntimeConfigServerTreeLayoutStore {

  private static final Logger log =
      LoggerFactory.getLogger(RuntimeConfigServerTreeLayoutStore.class);

  private final Path file;
  private final RuntimeConfigDocumentStore documentStore;

  RuntimeConfigServerTreeLayoutStore(Path file, RuntimeConfigDocumentStore documentStore) {
    this.file = file;
    this.documentStore = documentStore;
  }

  /**
   * Reads persisted per-server visibility for built-in server tree nodes.
   *
   * <p>Stored under {@code ircafe.ui.serverTree.builtInNodesByServer.<serverId>}.
   */
  synchronized Map<String, ServerTreeBuiltInNodesVisibility> readBuiltInNodesVisibility() {
    try {
      if (file.toString().isBlank()) return Map.of();
      if (!Files.exists(file)) return Map.of();

      Map<String, Object> doc = documentStore.load();
      Object byServerObj = readServerTreeValue(doc, "builtInNodesByServer").orElse(null);
      if (!(byServerObj instanceof Map<?, ?> byServer)) return Map.of();

      LinkedHashMap<String, ServerTreeBuiltInNodesVisibility> out = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : byServer.entrySet()) {
        String sid = Objects.toString(entry.getKey(), "").trim();
        if (sid.isEmpty()) continue;
        if (!(entry.getValue() instanceof Map<?, ?> raw)) continue;

        ServerTreeBuiltInNodesVisibility d = ServerTreeBuiltInNodesVisibility.defaults();
        boolean server = asBoolean(raw.get("server")).orElse(d.server());
        boolean notifications = asBoolean(raw.get("notifications")).orElse(d.notifications());
        boolean logViewer = asBoolean(raw.get("logViewer")).orElse(d.logViewer());
        boolean monitor = asBoolean(raw.get("monitor")).orElse(d.monitor());
        boolean interceptors = asBoolean(raw.get("interceptors")).orElse(d.interceptors());

        out.put(
            sid,
            new ServerTreeBuiltInNodesVisibility(
                server, notifications, logViewer, monitor, interceptors));
      }

      if (out.isEmpty()) return Map.of();
      return Map.copyOf(out);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read server-tree built-in node visibility from '{}'", file, e);
      return Map.of();
    }
  }

  /**
   * Persists per-server visibility for built-in server tree nodes.
   *
   * <p>When all flags are {@code true}, the server entry is removed to keep config compact.
   */
  synchronized void rememberBuiltInNodesVisibility(
      String serverId, ServerTreeBuiltInNodesVisibility visibility) {
    try {
      if (file.toString().isBlank()) return;
      String sid = Objects.toString(serverId, "").trim();
      if (sid.isEmpty()) return;

      ServerTreeBuiltInNodesVisibility v =
          visibility != null ? visibility : ServerTreeBuiltInNodesVisibility.defaults();

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> serverTree = serverTreeMap(doc);
      Map<String, Object> byServer = getOrCreateMap(serverTree, "builtInNodesByServer");

      if (v.isDefaultVisible()) {
        byServer.remove(sid);
      } else {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("server", v.server());
        out.put("notifications", v.notifications());
        out.put("logViewer", v.logViewer());
        out.put("monitor", v.monitor());
        out.put("interceptors", v.interceptors());
        byServer.put(sid, out);
      }

      if (byServer.isEmpty()) serverTree.remove("builtInNodesByServer");
      pruneEmptyServerTree(doc, serverTree);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist server-tree built-in node visibility to '{}'", file, e);
    }
  }

  /**
   * Reads persisted per-server layout for movable built-in server tree nodes.
   *
   * <p>Stored under {@code ircafe.ui.serverTree.builtInLayoutByServer.<serverId>}.
   */
  synchronized Map<String, ServerTreeBuiltInLayout> readBuiltInLayoutByServer() {
    try {
      if (file.toString().isBlank()) return Map.of();
      if (!Files.exists(file)) return Map.of();

      Map<String, Object> doc = documentStore.load();
      Object byServerObj = readServerTreeValue(doc, "builtInLayoutByServer").orElse(null);
      if (!(byServerObj instanceof Map<?, ?> byServer)) return Map.of();

      LinkedHashMap<String, ServerTreeBuiltInLayout> out = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : byServer.entrySet()) {
        String sid = Objects.toString(entry.getKey(), "").trim();
        if (sid.isEmpty()) continue;
        if (!(entry.getValue() instanceof Map<?, ?> raw)) continue;

        List<ServerTreeBuiltInLayoutNode> root =
            parseBuiltInLayoutNodeOrder(raw.get("root"), List.of());
        List<ServerTreeBuiltInLayoutNode> other =
            parseBuiltInLayoutNodeOrder(raw.get("other"), List.of());
        ServerTreeBuiltInLayout layout =
            normalizeBuiltInLayout(new ServerTreeBuiltInLayout(root, other));
        if (layout.isDefaultLayout()) continue;
        out.put(sid, layout);
      }

      if (out.isEmpty()) return Map.of();
      return Map.copyOf(out);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read server-tree built-in layout from '{}'", file, e);
      return Map.of();
    }
  }

  /**
   * Persists per-server layout for movable built-in server tree nodes.
   *
   * <p>When layout matches defaults, the server entry is removed to keep config compact.
   */
  synchronized void rememberBuiltInLayout(String serverId, ServerTreeBuiltInLayout layout) {
    try {
      if (file.toString().isBlank()) return;
      String sid = Objects.toString(serverId, "").trim();
      if (sid.isEmpty()) return;

      ServerTreeBuiltInLayout next =
          normalizeBuiltInLayout(layout == null ? ServerTreeBuiltInLayout.defaults() : layout);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> serverTree = serverTreeMap(doc);
      Map<String, Object> byServer = getOrCreateMap(serverTree, "builtInLayoutByServer");

      if (next.isDefaultLayout()) {
        byServer.remove(sid);
      } else {
        Map<String, Object> out = new LinkedHashMap<>();
        List<String> root = builtInLayoutNodeTokens(next.rootOrder());
        List<String> other = builtInLayoutNodeTokens(next.otherOrder());
        if (!root.isEmpty()) out.put("root", root);
        if (!other.isEmpty()) out.put("other", other);
        byServer.put(sid, out);
      }

      if (byServer.isEmpty()) serverTree.remove("builtInLayoutByServer");
      pruneEmptyServerTree(doc, serverTree);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist server-tree built-in layout to '{}'", file, e);
    }
  }

  /**
   * Reads persisted per-server order for top-level server sibling nodes.
   *
   * <p>Stored under {@code ircafe.ui.serverTree.rootSiblingOrderByServer.<serverId>}.
   */
  synchronized Map<String, ServerTreeRootSiblingOrder> readRootSiblingOrderByServer() {
    try {
      if (file.toString().isBlank()) return Map.of();
      if (!Files.exists(file)) return Map.of();

      Map<String, Object> doc = documentStore.load();
      Object byServerObj = readServerTreeValue(doc, "rootSiblingOrderByServer").orElse(null);
      if (!(byServerObj instanceof Map<?, ?> byServer)) return Map.of();

      LinkedHashMap<String, ServerTreeRootSiblingOrder> out = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : byServer.entrySet()) {
        String sid = Objects.toString(entry.getKey(), "").trim();
        if (sid.isEmpty()) continue;

        List<ServerTreeRootSiblingNode> parsed =
            parseRootSiblingNodeOrder(entry.getValue(), List.of());
        ServerTreeRootSiblingOrder order =
            normalizeRootSiblingOrder(new ServerTreeRootSiblingOrder(parsed));
        if (order.isDefaultOrder()) continue;
        out.put(sid, order);
      }

      if (out.isEmpty()) return Map.of();
      return Map.copyOf(out);
    } catch (Exception e) {
      log.warn("[ircafe] Could not read server-tree root sibling order from '{}'", file, e);
      return Map.of();
    }
  }

  /**
   * Persists per-server order for top-level server sibling nodes.
   *
   * <p>When order matches defaults, the server entry is removed to keep config compact.
   */
  synchronized void rememberRootSiblingOrder(String serverId, ServerTreeRootSiblingOrder order) {
    try {
      if (file.toString().isBlank()) return;
      String sid = Objects.toString(serverId, "").trim();
      if (sid.isEmpty()) return;

      ServerTreeRootSiblingOrder next =
          normalizeRootSiblingOrder(order == null ? ServerTreeRootSiblingOrder.defaults() : order);

      Map<String, Object> doc = documentStore.loadOrEmpty();
      Map<String, Object> serverTree = serverTreeMap(doc);
      Map<String, Object> byServer = getOrCreateMap(serverTree, "rootSiblingOrderByServer");

      if (next.isDefaultOrder()) {
        byServer.remove(sid);
      } else {
        byServer.put(sid, rootSiblingNodeTokens(next.order()));
      }

      if (byServer.isEmpty()) serverTree.remove("rootSiblingOrderByServer");
      pruneEmptyServerTree(doc, serverTree);

      documentStore.write(doc);
    } catch (Exception e) {
      log.warn("[ircafe] Could not persist server-tree root sibling order to '{}'", file, e);
    }
  }

  private static ServerTreeBuiltInLayout normalizeBuiltInLayout(ServerTreeBuiltInLayout layout) {
    ServerTreeBuiltInLayout defaults = ServerTreeBuiltInLayout.defaults();
    List<ServerTreeBuiltInLayoutNode> defaultOther = defaults.otherOrder();

    List<ServerTreeBuiltInLayoutNode> rawRoot =
        layout == null ? List.of() : parseBuiltInLayoutNodeOrder(layout.rootOrder(), List.of());
    List<ServerTreeBuiltInLayoutNode> rawOther =
        layout == null ? List.of() : parseBuiltInLayoutNodeOrder(layout.otherOrder(), List.of());

    ArrayList<ServerTreeBuiltInLayoutNode> root = new ArrayList<>();
    java.util.EnumSet<ServerTreeBuiltInLayoutNode> seen =
        java.util.EnumSet.noneOf(ServerTreeBuiltInLayoutNode.class);
    for (ServerTreeBuiltInLayoutNode node : rawRoot) {
      if (node == null || seen.contains(node)) continue;
      root.add(node);
      seen.add(node);
    }

    ArrayList<ServerTreeBuiltInLayoutNode> other = new ArrayList<>();
    for (ServerTreeBuiltInLayoutNode node : rawOther) {
      if (node == null || seen.contains(node)) continue;
      other.add(node);
      seen.add(node);
    }

    for (ServerTreeBuiltInLayoutNode node : defaultOther) {
      if (node == null || seen.contains(node)) continue;
      other.add(node);
      seen.add(node);
    }

    return new ServerTreeBuiltInLayout(List.copyOf(root), List.copyOf(other));
  }

  private static List<ServerTreeBuiltInLayoutNode> parseBuiltInLayoutNodeOrder(
      Object rawOrder, List<ServerTreeBuiltInLayoutNode> fallback) {
    ArrayList<ServerTreeBuiltInLayoutNode> out = new ArrayList<>();

    if (rawOrder instanceof List<?> list) {
      for (Object entry : list) {
        ServerTreeBuiltInLayoutNode node =
            ServerTreeBuiltInLayoutNode.fromToken(Objects.toString(entry, ""));
        if (node == null || out.contains(node)) continue;
        out.add(node);
      }
    } else if (rawOrder instanceof ServerTreeBuiltInLayoutNode node) {
      out.add(node);
    } else if (rawOrder instanceof String token) {
      ServerTreeBuiltInLayoutNode node = ServerTreeBuiltInLayoutNode.fromToken(token);
      if (node != null) out.add(node);
    }

    if (out.isEmpty()) return fallback == null ? List.of() : List.copyOf(fallback);
    return List.copyOf(out);
  }

  private static List<String> builtInLayoutNodeTokens(List<ServerTreeBuiltInLayoutNode> order) {
    if (order == null || order.isEmpty()) return List.of();
    ArrayList<String> out = new ArrayList<>(order.size());
    for (ServerTreeBuiltInLayoutNode node : order) {
      if (node == null) continue;
      out.add(node.token());
    }
    return out.isEmpty() ? List.of() : List.copyOf(out);
  }

  private static ServerTreeRootSiblingOrder normalizeRootSiblingOrder(
      ServerTreeRootSiblingOrder order) {
    ServerTreeRootSiblingOrder defaults = ServerTreeRootSiblingOrder.defaults();
    List<ServerTreeRootSiblingNode> raw =
        order == null ? List.of() : parseRootSiblingNodeOrder(order.order(), List.of());

    ArrayList<ServerTreeRootSiblingNode> out = new ArrayList<>();
    for (ServerTreeRootSiblingNode node : raw) {
      if (node == null || out.contains(node)) continue;
      out.add(node);
    }
    for (ServerTreeRootSiblingNode node : defaults.order()) {
      if (node == null || out.contains(node)) continue;
      out.add(node);
    }

    return new ServerTreeRootSiblingOrder(List.copyOf(out));
  }

  private static List<ServerTreeRootSiblingNode> parseRootSiblingNodeOrder(
      Object rawOrder, List<ServerTreeRootSiblingNode> fallback) {
    Object raw = rawOrder;
    if (raw instanceof Map<?, ?> map) {
      raw = map.get("order");
    }

    ArrayList<ServerTreeRootSiblingNode> out = new ArrayList<>();
    if (raw instanceof List<?> list) {
      for (Object entry : list) {
        ServerTreeRootSiblingNode node =
            ServerTreeRootSiblingNode.fromToken(Objects.toString(entry, ""));
        if (node == null || out.contains(node)) continue;
        out.add(node);
      }
    } else if (raw instanceof ServerTreeRootSiblingNode node) {
      out.add(node);
    } else if (raw instanceof String token) {
      ServerTreeRootSiblingNode node = ServerTreeRootSiblingNode.fromToken(token);
      if (node != null) out.add(node);
    }

    if (out.isEmpty()) return fallback == null ? List.of() : List.copyOf(fallback);
    return List.copyOf(out);
  }

  private static List<String> rootSiblingNodeTokens(List<ServerTreeRootSiblingNode> order) {
    if (order == null || order.isEmpty()) return List.of();
    ArrayList<String> out = new ArrayList<>(order.size());
    for (ServerTreeRootSiblingNode node : order) {
      if (node == null) continue;
      out.add(node.token());
    }
    return out.isEmpty() ? List.of() : List.copyOf(out);
  }

  private Optional<Object> readServerTreeValue(Map<String, Object> doc, String key) {
    Object ircafeObj = doc.get("ircafe");
    if (!(ircafeObj instanceof Map<?, ?> ircafe)) return Optional.empty();

    Object uiObj = ircafe.get("ui");
    if (!(uiObj instanceof Map<?, ?> ui)) return Optional.empty();

    Object serverTreeObj = ui.get("serverTree");
    if (!(serverTreeObj instanceof Map<?, ?> serverTree)) return Optional.empty();

    return Optional.ofNullable(serverTree.get(key));
  }

  private static void pruneEmptyServerTree(
      Map<String, Object> doc, Map<String, Object> serverTree) {
    if (!serverTree.isEmpty()) return;
    Object uiObj = readMap(doc, "ircafe").map(ircafe -> ircafe.get("ui")).orElse(null);
    if (uiObj instanceof Map<?, ?> ui) {
      @SuppressWarnings("unchecked")
      Map<String, Object> mutableUi = (Map<String, Object>) ui;
      mutableUi.remove("serverTree");
    }
  }

  private static Map<String, Object> serverTreeMap(Map<String, Object> doc) {
    return getOrCreateMapPath(doc, "ircafe", "ui", "serverTree");
  }

  private static Map<String, Object> getOrCreateMapPath(Map<String, Object> root, String... path) {
    Map<String, Object> current = root;
    for (String segment : path) {
      current = getOrCreateMap(current, segment);
    }
    return current;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }

  @SuppressWarnings("unchecked")
  private static Optional<Map<String, Object>> readMap(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Map<?, ?> m) return Optional.of((Map<String, Object>) m);
    return Optional.empty();
  }

  private static Optional<Boolean> asBoolean(Object value) {
    if (value instanceof Boolean b) return Optional.of(b);
    if (value instanceof String s) {
      String t = s.trim();
      if (t.equalsIgnoreCase("true")) return Optional.of(Boolean.TRUE);
      if (t.equalsIgnoreCase("false")) return Optional.of(Boolean.FALSE);
    }
    if (value instanceof Number n) {
      int i = n.intValue();
      if (i == 0) return Optional.of(Boolean.FALSE);
      if (i == 1) return Optional.of(Boolean.TRUE);
    }
    return Optional.empty();
  }
}
