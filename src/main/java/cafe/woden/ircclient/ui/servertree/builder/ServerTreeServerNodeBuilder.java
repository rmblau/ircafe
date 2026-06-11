package cafe.woden.ircclient.ui.servertree.builder;

import cafe.woden.ircclient.model.InterceptorDefinition;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.servertree.model.ServerTreeNodeData;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.stereotype.Component;

/** Builds server tree nodes/leaves before dockable-specific layout placement is applied. */
@InterfaceLayer
@Component
public final class ServerTreeServerNodeBuilder {

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  public record BuildSpec(
      String serverId,
      String statusLabel,
      String channelListLabel,
      String weechatFiltersLabel,
      String ignoresLabel,
      String dccTransfersLabel,
      String logViewerLabel,
      String monitorGroupLabel,
      String interceptorsGroupLabel,
      String otherGroupLabel,
      boolean serverNodeVisible,
      boolean notificationsNodeVisible,
      boolean logViewerNodeVisible,
      boolean dccTransfersNodeVisible,
      int notificationsCount,
      int interceptorGroupHitCount,
      List<InterceptorDefinition> interceptors) {}

  public record BuildResult(
      DefaultMutableTreeNode serverNode,
      DefaultMutableTreeNode privateMessagesNode,
      DefaultMutableTreeNode otherNode,
      DefaultMutableTreeNode monitorNode,
      DefaultMutableTreeNode interceptorsNode,
      TargetRef statusRef,
      TargetRef notificationsRef,
      TargetRef logViewerRef,
      TargetRef channelListRef,
      TargetRef weechatFiltersRef,
      TargetRef ignoresRef,
      TargetRef dccTransfersRef,
      Map<TargetRef, DefaultMutableTreeNode> leavesByTarget) {}

  public BuildResult build(BuildSpec spec) {
    Objects.requireNonNull(spec, "spec");
    String id = normalizeServerId(spec.serverId());

    DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(id);
    DefaultMutableTreeNode pmNode =
        new DefaultMutableTreeNode(MESSAGES.text("serverTree.node.privateMessages.lowercase"));
    Map<TargetRef, DefaultMutableTreeNode> leaves = new LinkedHashMap<>();

    TargetRef statusRef = new TargetRef(id, "status");
    if (spec.serverNodeVisible()) {
      leaves.put(
          statusRef,
          new DefaultMutableTreeNode(
              new ServerTreeNodeData(statusRef, Objects.toString(spec.statusLabel(), ""))));
    }

    TargetRef notificationsRef = TargetRef.notifications(id);
    ServerTreeNodeData notificationsData =
        new ServerTreeNodeData(notificationsRef, MESSAGES.text("serverTree.node.notifications"));
    notificationsData.highlightUnread = Math.max(0, spec.notificationsCount());
    if (spec.notificationsNodeVisible()) {
      leaves.put(notificationsRef, new DefaultMutableTreeNode(notificationsData));
    }

    TargetRef logViewerRef = TargetRef.logViewer(id);
    if (spec.logViewerNodeVisible()) {
      leaves.put(
          logViewerRef,
          new DefaultMutableTreeNode(
              new ServerTreeNodeData(
                  logViewerRef,
                  Objects.toString(
                      spec.logViewerLabel(), MESSAGES.text("serverTree.node.logViewer")))));
    }

    TargetRef channelListRef = TargetRef.channelList(id);
    DefaultMutableTreeNode channelListLeaf =
        new DefaultMutableTreeNode(
            new ServerTreeNodeData(
                channelListRef,
                Objects.toString(
                    spec.channelListLabel(), MESSAGES.text("serverTree.node.channelList"))));
    serverNode.add(channelListLeaf);
    leaves.put(channelListRef, channelListLeaf);

    TargetRef weechatFiltersRef = TargetRef.weechatFilters(id);
    leaves.put(
        weechatFiltersRef,
        new DefaultMutableTreeNode(
            new ServerTreeNodeData(
                weechatFiltersRef,
                Objects.toString(
                    spec.weechatFiltersLabel(), MESSAGES.text("serverTree.node.filters")))));

    TargetRef ignoresRef = TargetRef.ignores(id);
    leaves.put(
        ignoresRef,
        new DefaultMutableTreeNode(
            new ServerTreeNodeData(
                ignoresRef,
                Objects.toString(spec.ignoresLabel(), MESSAGES.text("serverTree.node.ignores")))));

    TargetRef dccTransfersRef = TargetRef.dccTransfers(id);
    if (spec.dccTransfersNodeVisible()) {
      DefaultMutableTreeNode dccTransfersLeaf =
          new DefaultMutableTreeNode(
              new ServerTreeNodeData(
                  dccTransfersRef,
                  Objects.toString(
                      spec.dccTransfersLabel(), MESSAGES.text("serverTree.node.dccTransfers"))));
      serverNode.add(dccTransfersLeaf);
      leaves.put(dccTransfersRef, dccTransfersLeaf);
    }

    TargetRef monitorGroupRef = TargetRef.monitorGroup(id);
    TargetRef interceptorsGroupRef = TargetRef.interceptorsGroup(id);

    ServerTreeNodeData interceptorsData =
        new ServerTreeNodeData(
            interceptorsGroupRef, Objects.toString(spec.interceptorsGroupLabel(), ""));
    interceptorsData.unread = Math.max(0, spec.interceptorGroupHitCount());

    ServerTreeNodeData monitorData =
        new ServerTreeNodeData(monitorGroupRef, Objects.toString(spec.monitorGroupLabel(), ""));
    DefaultMutableTreeNode monitorNode = new DefaultMutableTreeNode(monitorData);
    DefaultMutableTreeNode interceptorsNode = new DefaultMutableTreeNode(interceptorsData);
    leaves.put(monitorGroupRef, monitorNode);
    leaves.put(interceptorsGroupRef, interceptorsNode);

    List<InterceptorDefinition> definitions =
        spec.interceptors() == null ? List.of() : spec.interceptors();
    for (InterceptorDefinition definition : definitions) {
      if (definition == null) continue;
      TargetRef ref = TargetRef.interceptor(id, definition.id());
      String label = Objects.toString(definition.name(), "").trim();
      if (label.isEmpty()) label = MESSAGES.text("serverTree.node.interceptor");
      DefaultMutableTreeNode leaf = new DefaultMutableTreeNode(new ServerTreeNodeData(ref, label));
      interceptorsNode.add(leaf);
      leaves.put(ref, leaf);
    }

    DefaultMutableTreeNode otherNode =
        new DefaultMutableTreeNode(
            new ServerTreeNodeData(null, Objects.toString(spec.otherGroupLabel(), "")));
    serverNode.add(otherNode);
    serverNode.add(pmNode);

    return new BuildResult(
        serverNode,
        pmNode,
        otherNode,
        monitorNode,
        interceptorsNode,
        statusRef,
        notificationsRef,
        logViewerRef,
        channelListRef,
        weechatFiltersRef,
        ignoresRef,
        dccTransfersRef,
        Map.copyOf(leaves));
  }

  private static String normalizeServerId(String serverId) {
    String id = Objects.toString(serverId, "").trim();
    return id.isEmpty() ? "(server)" : id;
  }
}
