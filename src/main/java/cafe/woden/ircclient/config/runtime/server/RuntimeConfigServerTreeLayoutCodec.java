package cafe.woden.ircclient.config.runtime.server;

import static cafe.woden.ircclient.config.yaml.RuntimeConfigYamlSupport.asBoolean;

import cafe.woden.ircclient.config.api.ServerTreeBuiltInVisibilityConfigPort.ServerTreeBuiltInNodesVisibility;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeBuiltInLayout;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeBuiltInLayoutNode;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeRootSiblingNode;
import cafe.woden.ircclient.config.api.ServerTreeLayoutConfigPort.ServerTreeRootSiblingOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Pure codec/policy helpers for persisted server-tree layout settings. */
final class RuntimeConfigServerTreeLayoutCodec {

  private RuntimeConfigServerTreeLayoutCodec() {}

  static Map<String, ServerTreeBuiltInNodesVisibility> parseBuiltInNodesVisibilityByServer(
      Object byServerObj) {
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

    return out.isEmpty() ? Map.of() : Map.copyOf(out);
  }

  static Map<String, Object> serializeBuiltInNodesVisibility(
      ServerTreeBuiltInNodesVisibility visibility) {
    ServerTreeBuiltInNodesVisibility v =
        visibility != null ? visibility : ServerTreeBuiltInNodesVisibility.defaults();
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("server", v.server());
    out.put("notifications", v.notifications());
    out.put("logViewer", v.logViewer());
    out.put("monitor", v.monitor());
    out.put("interceptors", v.interceptors());
    return out;
  }

  static Map<String, ServerTreeBuiltInLayout> parseBuiltInLayoutByServer(Object byServerObj) {
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

    return out.isEmpty() ? Map.of() : Map.copyOf(out);
  }

  static ServerTreeBuiltInLayout normalizeBuiltInLayout(ServerTreeBuiltInLayout layout) {
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

  static Map<String, Object> serializeBuiltInLayout(ServerTreeBuiltInLayout layout) {
    ServerTreeBuiltInLayout normalized = normalizeBuiltInLayout(layout);
    Map<String, Object> out = new LinkedHashMap<>();
    List<String> root = builtInLayoutNodeTokens(normalized.rootOrder());
    List<String> other = builtInLayoutNodeTokens(normalized.otherOrder());
    if (!root.isEmpty()) out.put("root", root);
    if (!other.isEmpty()) out.put("other", other);
    return out;
  }

  static Map<String, ServerTreeRootSiblingOrder> parseRootSiblingOrderByServer(Object byServerObj) {
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

    return out.isEmpty() ? Map.of() : Map.copyOf(out);
  }

  static ServerTreeRootSiblingOrder normalizeRootSiblingOrder(ServerTreeRootSiblingOrder order) {
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

  static List<String> rootSiblingNodeTokens(List<ServerTreeRootSiblingNode> order) {
    if (order == null || order.isEmpty()) return List.of();
    ArrayList<String> out = new ArrayList<>(order.size());
    for (ServerTreeRootSiblingNode node : order) {
      if (node == null) continue;
      out.add(node.token());
    }
    return out.isEmpty() ? List.of() : List.copyOf(out);
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
}
