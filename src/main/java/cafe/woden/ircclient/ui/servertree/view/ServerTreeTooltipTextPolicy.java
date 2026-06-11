package cafe.woden.ircclient.ui.servertree.view;

import cafe.woden.ircclient.app.api.ConnectionState;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.servertree.ServerTreeBouncerBackends;
import cafe.woden.ircclient.ui.servertree.model.ServerTreeNodeData;
import cafe.woden.ircclient.ui.servertree.model.ServerTreeQuasselNetworkNodeData;
import cafe.woden.ircclient.ui.servertree.viewmodel.ServerTreeConnectionStateViewModel;
import java.util.Objects;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.stereotype.Component;

/** Stateless tooltip text policy for server-tree nodes once a concrete tree node is known. */
@InterfaceLayer
@Component
public final class ServerTreeTooltipTextPolicy {

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  public String toolTipForNode(
      ServerTreeTooltipProvider.Context context, DefaultMutableTreeNode node) {
    Objects.requireNonNull(context, "context");
    if (node == null) return null;

    if (context.isIrcRootNode(node)) {
      return MESSAGES.text("serverTree.tooltip.root.irc");
    }

    if (context.isApplicationRootNode(node)) {
      return MESSAGES.text("serverTree.tooltip.root.application");
    }

    String networksGroupBackendId = normalizeBackendId(context.backendIdForNetworksGroupNode(node));
    if (!networksGroupBackendId.isEmpty()) {
      return ServerTreeBouncerBackends.networksGroupTooltip(networksGroupBackendId);
    }

    if (context.isInterceptorsGroupNode(node)) {
      return MESSAGES.text("serverTree.tooltip.group.interceptors");
    }
    if (context.isMonitorGroupNode(node)) {
      return MESSAGES.text("serverTree.tooltip.group.monitor");
    }
    if (context.isOtherGroupNode(node)) {
      return MESSAGES.text("serverTree.tooltip.group.other");
    }

    Object userObject = node.getUserObject();
    if (userObject instanceof ServerTreeQuasselNetworkNodeData networkNodeData) {
      String networkTip = tooltipForQuasselNetwork(context, node, networkNodeData);
      if (networkTip != null) {
        return networkTip;
      }
    }

    if (userObject instanceof ServerTreeNodeData nodeData && nodeData.ref != null) {
      String nodeTip = tooltipForNodeData(context, nodeData);
      if (nodeTip != null) {
        return nodeTip;
      }
    }

    if (userObject instanceof String serverId && context.isServerNode(node)) {
      String backendId = normalizeBackendId(context.backendIdForEphemeralServer(serverId));
      if (!backendId.isEmpty()) {
        return ephemeralServerTooltip(context, serverId, backendId);
      }
      return standardServerTooltip(context, serverId);
    }

    return null;
  }

  private String tooltipForQuasselNetwork(
      ServerTreeTooltipProvider.Context context,
      DefaultMutableTreeNode node,
      ServerTreeQuasselNetworkNodeData networkNodeData) {
    if (context.isQuasselEmptyStateNode(node) || networkNodeData.emptyState()) {
      return MESSAGES.text("serverTree.tooltip.quassel.empty");
    }
    if (!context.isQuasselNetworkNode(node)) {
      return null;
    }

    String serverId = Objects.toString(networkNodeData.serverId(), "").trim();
    String token = Objects.toString(networkNodeData.networkToken(), "").trim();
    String tip = Objects.toString(context.quasselNetworkTooltip(serverId, token), "").trim();
    if (!tip.isEmpty()) return tip;

    String state =
        Boolean.FALSE.equals(networkNodeData.enabled())
            ? MESSAGES.text("serverTree.tooltip.quassel.state.disabled")
            : Boolean.TRUE.equals(networkNodeData.connected())
                ? MESSAGES.text("serverTree.tooltip.quassel.state.connected")
                : Boolean.FALSE.equals(networkNodeData.connected())
                    ? MESSAGES.text("serverTree.tooltip.quassel.state.disconnected")
                    : MESSAGES.text("serverTree.tooltip.quassel.state.unknown");
    return MESSAGES.text(
        "serverTree.tooltip.quassel.network", networkNodeData.label(), state, token);
  }

  private String tooltipForNodeData(
      ServerTreeTooltipProvider.Context context, ServerTreeNodeData nodeData) {
    if (nodeData.ref.isChannel() && nodeData.hasDetachedWarning()) {
      return MESSAGES.text("serverTree.tooltip.channel.detached", nodeData.detachedWarning);
    }
    if (nodeData.ref.isApplicationUnhandledErrors()) {
      return MESSAGES.text("serverTree.tooltip.application.unhandledErrors");
    }
    if (nodeData.ref.isApplicationAssertjSwing()) {
      return MESSAGES.text("serverTree.tooltip.application.assertjSwing");
    }
    if (nodeData.ref.isApplicationJhiccup()) {
      return MESSAGES.text("serverTree.tooltip.application.jhiccup");
    }
    if (nodeData.ref.isApplicationInboundDedup()) {
      return MESSAGES.text("serverTree.tooltip.application.inboundDedup");
    }
    if (nodeData.ref.isApplicationPlugins()) {
      return MESSAGES.text("serverTree.tooltip.application.plugins");
    }
    if (nodeData.ref.isApplicationJfr()) {
      return context.isApplicationJfrActive()
          ? MESSAGES.text("serverTree.tooltip.application.jfr.active")
          : MESSAGES.text("serverTree.tooltip.application.jfr.disabled");
    }
    if (nodeData.ref.isApplicationSpring()) {
      return MESSAGES.text("serverTree.tooltip.application.spring");
    }
    if (nodeData.ref.isApplicationTerminal()) {
      return MESSAGES.text("serverTree.tooltip.application.terminal");
    }
    if (context.isBouncerControlStatusNode(nodeData)) {
      return MESSAGES.text("serverTree.tooltip.bouncerControl");
    }
    if (nodeData.ref.isInterceptor()) {
      return MESSAGES.text("serverTree.tooltip.target.interceptor");
    }
    if (nodeData.ref.isWeechatFilters()) {
      return MESSAGES.text("serverTree.tooltip.target.filters");
    }
    if (nodeData.ref.isIgnores()) {
      return MESSAGES.text("serverTree.tooltip.target.ignores");
    }
    return null;
  }

  private String ephemeralServerTooltip(
      ServerTreeTooltipProvider.Context context, String serverId, String backendId) {
    String backend = normalizeBackendId(backendId);
    ConnectionState state = context.connectionStateForServer(serverId);
    boolean desired = context.desiredOnlineForServer(serverId);
    String stateTip =
        MESSAGES.text(
            "serverTree.tooltip.connection.state",
            ServerTreeConnectionStateViewModel.stateLabel(state));
    String intentTip =
        MESSAGES.text(
            "serverTree.tooltip.connection.intent",
            ServerTreeConnectionStateViewModel.desiredIntentLabel(desired));
    String queueTip = ServerTreeConnectionStateViewModel.intentQueueTip(state, desired);
    String diagnostics = context.connectionDiagnosticsTipForServer(serverId);

    String origin = Objects.toString(context.originByServerId(backend, serverId), "").trim();
    String display = context.serverDisplayName(serverId);
    boolean auto = !origin.isEmpty() && context.isAutoConnectEnabled(backend, origin, display);

    String tip = stateTip + intentTip;
    if (!queueTip.isBlank()) tip += " " + queueTip;
    if (!diagnostics.isBlank()) tip += diagnostics;
    tip += " " + ServerTreeBouncerBackends.ephemeralDiscoveryTooltip(backend);
    if (auto) tip += " " + MESSAGES.text("serverTree.tooltip.connection.autoConnectEnabled");
    if (!origin.isEmpty())
      tip += " " + MESSAGES.text("serverTree.tooltip.connection.origin", origin);
    if (display != null && !display.isBlank()) {
      tip += " " + MESSAGES.text("serverTree.tooltip.connection.network", display);
    }
    return tip;
  }

  private String standardServerTooltip(ServerTreeTooltipProvider.Context context, String serverId) {
    ConnectionState state = context.connectionStateForServer(serverId);
    boolean desired = context.desiredOnlineForServer(serverId);
    String queueTip = ServerTreeConnectionStateViewModel.intentQueueTip(state, desired);
    String diagnostics = context.connectionDiagnosticsTipForServer(serverId);
    String action = ServerTreeConnectionStateViewModel.actionHint(state);
    String backendDisplayName =
        Objects.toString(context.backendDisplayNameForServer(serverId), "").trim();
    String base =
        MESSAGES.text(
            "serverTree.tooltip.connection.stateAndIntent",
            ServerTreeConnectionStateViewModel.stateLabel(state),
            ServerTreeConnectionStateViewModel.desiredIntentLabel(desired));
    if (!backendDisplayName.isEmpty()) {
      base += " " + MESSAGES.text("serverTree.tooltip.connection.backend", backendDisplayName);
    }
    if (!queueTip.isBlank() && !diagnostics.isBlank()) {
      return base + " " + queueTip + diagnostics + " " + action;
    }
    if (!queueTip.isBlank()) return base + " " + queueTip + " " + action;
    if (!diagnostics.isBlank()) return base + diagnostics + " " + action;
    return base + " " + action;
  }

  private static String normalizeBackendId(String backendId) {
    return Objects.toString(backendId, "").trim().toLowerCase(java.util.Locale.ROOT);
  }
}
