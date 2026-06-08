package cafe.woden.ircclient.ui.servertree.view;

import cafe.woden.ircclient.app.api.ConnectionState;
import cafe.woden.ircclient.config.api.IrcSessionRuntimeConfigPort;
import cafe.woden.ircclient.irc.ircv3.Ircv3ExtensionCatalog;
import cafe.woden.ircclient.irc.ircv3.Ircv3ExtensionRegistry;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.servertree.ServerTreeConventions;
import cafe.woden.ircclient.ui.servertree.state.ServerRuntimeMetadata;
import cafe.woden.ircclient.ui.servertree.viewmodel.ServerTreeConnectionStateViewModel;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.stereotype.Component;

/** Builds and shows the server "Network Info" dialog for {@link ServerTreeDockable}. */
@InterfaceLayer
@Component
@RequiredArgsConstructor
public final class ServerTreeNetworkInfoDialogBuilder {

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private static String message(String code, Object... args) {
    return MESSAGES.text(code, args);
  }

  public interface Context {
    ConnectionState connectionStateForServer(String serverId);

    boolean desiredOnlineForServer(String serverId);

    String backendIdForServer(String serverId);

    String backendDisplayNameForServer(String serverId);

    String prettyServerLabel(String serverId);

    String connectionDiagnosticsTipForServer(String serverId);

    void requestCapabilityToggle(String serverId, String capability, boolean enable);
  }

  @FunctionalInterface
  public interface CapabilityToggleRequester {
    void request(String serverId, String capability, boolean enable);
  }

  public static Context context(
      Function<String, ConnectionState> connectionStateForServer,
      Predicate<String> desiredOnlineForServer,
      Function<String, String> backendIdForServer,
      Function<String, String> backendDisplayNameForServer,
      Function<String, String> prettyServerLabel,
      Function<String, String> connectionDiagnosticsTipForServer,
      CapabilityToggleRequester capabilityToggleRequester) {
    Objects.requireNonNull(connectionStateForServer, "connectionStateForServer");
    Objects.requireNonNull(desiredOnlineForServer, "desiredOnlineForServer");
    Objects.requireNonNull(backendIdForServer, "backendIdForServer");
    Objects.requireNonNull(backendDisplayNameForServer, "backendDisplayNameForServer");
    Objects.requireNonNull(prettyServerLabel, "prettyServerLabel");
    Objects.requireNonNull(connectionDiagnosticsTipForServer, "connectionDiagnosticsTipForServer");
    Objects.requireNonNull(capabilityToggleRequester, "capabilityToggleRequester");
    return new Context() {
      @Override
      public ConnectionState connectionStateForServer(String serverId) {
        return connectionStateForServer.apply(serverId);
      }

      @Override
      public boolean desiredOnlineForServer(String serverId) {
        return desiredOnlineForServer.test(serverId);
      }

      @Override
      public String backendIdForServer(String serverId) {
        return backendIdForServer.apply(serverId);
      }

      @Override
      public String backendDisplayNameForServer(String serverId) {
        return backendDisplayNameForServer.apply(serverId);
      }

      @Override
      public String prettyServerLabel(String serverId) {
        return prettyServerLabel.apply(serverId);
      }

      @Override
      public String connectionDiagnosticsTipForServer(String serverId) {
        return connectionDiagnosticsTipForServer.apply(serverId);
      }

      @Override
      public void requestCapabilityToggle(String serverId, String capability, boolean enable) {
        capabilityToggleRequester.request(serverId, capability, enable);
      }
    };
  }

  private static final DateTimeFormatter SERVER_META_TIME_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());
  private static final DateTimeFormatter CAP_TRANSITION_TIME_FMT =
      DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

  private final IrcSessionRuntimeConfigPort runtimeConfig;
  @NonNull private final Ircv3ExtensionCatalog ircv3ExtensionCatalog;

  public void open(
      java.awt.Component ownerComponent,
      Context context,
      String serverId,
      ServerRuntimeMetadata metadata) {
    Objects.requireNonNull(context, "context");
    String sid = ServerTreeConventions.normalize(serverId);
    if (sid.isEmpty() || metadata == null) return;

    Window owner = ownerComponent == null ? null : SwingUtilities.getWindowAncestor(ownerComponent);
    String title = message("serverTree.networkInfo.dialog.title", context.prettyServerLabel(sid));

    JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    JPanel body =
        new JPanel(
            MigLayouts.fillWrap(
                12, 1, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.LEADING_GROW_FILL));
    body.add(buildNetworkSummaryPanel(context, sid, metadata), MigConstraints.growX());

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab(
        message("serverTree.networkInfo.tab.overview"), buildOverviewTab(context, sid, metadata));
    tabs.addTab(
        message("serverTree.networkInfo.tab.capabilities", metadata.ircv3Caps.size()),
        buildCapabilitiesInfoPanel(context, sid, metadata));
    tabs.addTab(
        message("serverTree.networkInfo.tab.isupport", metadata.isupport.size()),
        buildIsupportInfoPanel(metadata));
    body.add(tabs, MigConstraints.growPush());

    JScrollPane bodyScroll =
        new JScrollPane(
            body,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    bodyScroll.setBorder(BorderFactory.createEmptyBorder());
    bodyScroll.getVerticalScrollBar().setUnitIncrement(16);
    bodyScroll.setPreferredSize(new Dimension(820, 470));

    JButton close = new JButton(message("common.button.close"));
    close.addActionListener(ev -> dialog.dispose());
    JPanel actions = new JPanel(MigLayouts.fillXGrowTrailing());
    actions.add(new JLabel(""), MigConstraints.growX());
    actions.add(close, MigConstraints.tagOk());

    JPanel content =
        new JPanel(
            MigLayouts.fillWrap(
                0, 1, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.GROW_FILL_TRAILING));
    content.add(bodyScroll, MigConstraints.growPush());
    content.add(actions, MigConstraints.growX());

    dialog.setContentPane(content);
    dialog.getRootPane().setDefaultButton(close);
    dialog.pack();
    int width = Math.max(820, dialog.getWidth());
    int height = Math.max(500, Math.min(560, dialog.getHeight()));
    dialog.setSize(width, height);
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);
  }

  private JPanel buildNetworkSummaryPanel(
      Context context, String serverId, ServerRuntimeMetadata metadata) {
    JPanel panel =
        new JPanel(MigLayouts.fillXWrap(8, 2, "[grow,fill][right]", MigLayouts.rows(2, 4)));
    panel.setBorder(
        BorderFactory.createTitledBorder(message("serverTree.networkInfo.summary.title")));

    ConnectionState state = context.connectionStateForServer(serverId);
    boolean desired = context.desiredOnlineForServer(serverId);

    JLabel title = new JLabel(context.prettyServerLabel(serverId));
    Font base = title.getFont();
    if (base != null) {
      title.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 1.5f));
    }
    panel.add(title, MigConstraints.growX());
    panel.add(
        new JLabel(
            message(
                "serverTree.networkInfo.summary.state",
                ServerTreeConnectionStateViewModel.stateLabel(state))));

    String endpoint = formatConnectedEndpoint(metadata.connectedHost, metadata.connectedPort);
    String nick = fallbackInfoValue(metadata.nick);
    panel.add(
        new JLabel(message("serverTree.networkInfo.summary.networkId", serverId)),
        MigConstraints.span2GrowX());
    panel.add(
        new JLabel(
            message(
                "serverTree.networkInfo.summary.connection",
                endpoint,
                nick,
                ServerTreeConnectionStateViewModel.desiredIntentLabel(desired),
                renderBackendInfo(
                    context.backendDisplayNameForServer(serverId),
                    context.backendIdForServer(serverId)))),
        MigConstraints.span2GrowX());
    return panel;
  }

  private JComponent buildOverviewTab(
      Context context, String serverId, ServerRuntimeMetadata metadata) {
    JPanel overview =
        new JPanel(
            MigLayouts.fillWrap(8, 2, MigLayoutConstraints.GROW_FILL_GAP_12_GROW_FILL, "[top]"));
    overview.add(buildConnectionInfoPanel(context, serverId, metadata), MigConstraints.grow());
    overview.add(buildServerInfoPanel(metadata), MigConstraints.grow());
    return overview;
  }

  private JPanel buildConnectionInfoPanel(
      Context context, String serverId, ServerRuntimeMetadata metadata) {
    JPanel panel = new JPanel(MigLayouts.fillXWrap(8, 2, MigLayoutConstraints.RIGHT_GROW_FILL, ""));
    panel.setBorder(
        BorderFactory.createTitledBorder(message("serverTree.networkInfo.connection.title")));
    for (InfoRow row : connectionInfoRows(context, serverId, metadata)) {
      addInfoRow(panel, row.key(), row.value());
    }
    return panel;
  }

  static List<InfoRow> connectionInfoRows(
      Context context, String serverId, ServerRuntimeMetadata metadata) {
    Objects.requireNonNull(context, "context");
    ServerRuntimeMetadata meta = metadata == null ? new ServerRuntimeMetadata() : metadata;
    ArrayList<InfoRow> rows = new ArrayList<>();
    ConnectionState state = context.connectionStateForServer(serverId);
    boolean desired = context.desiredOnlineForServer(serverId);

    rows.add(new InfoRow(message("serverTree.networkInfo.row.networkId"), serverId));
    rows.add(
        new InfoRow(
            message("serverTree.networkInfo.row.display"), context.prettyServerLabel(serverId)));
    rows.add(
        new InfoRow(
            message("serverTree.networkInfo.row.backend"),
            renderBackendInfo(
                context.backendDisplayNameForServer(serverId),
                context.backendIdForServer(serverId))));
    rows.add(
        new InfoRow(
            message("serverTree.networkInfo.row.state"),
            ServerTreeConnectionStateViewModel.stateLabel(state)));
    rows.add(
        new InfoRow(
            message("serverTree.networkInfo.row.intent"),
            ServerTreeConnectionStateViewModel.desiredIntentLabel(desired)));
    rows.add(
        new InfoRow(
            message("serverTree.networkInfo.row.connectedEndpoint"),
            formatConnectedEndpoint(meta.connectedHost, meta.connectedPort)));
    rows.add(
        new InfoRow(
            message("serverTree.networkInfo.row.currentNick"), fallbackInfoValue(meta.nick)));
    rows.add(
        new InfoRow(
            message("serverTree.networkInfo.row.connectedAt"),
            meta.connectedAt == null
                ? message("common.value.unknown.parenthesized")
                : SERVER_META_TIME_FMT.format(meta.connectedAt)));

    String diagnostics = context.connectionDiagnosticsTipForServer(serverId).trim();
    if (!diagnostics.isEmpty()) {
      rows.add(new InfoRow(message("serverTree.networkInfo.row.diagnostics"), diagnostics));
    }
    return List.copyOf(rows);
  }

  private JPanel buildServerInfoPanel(ServerRuntimeMetadata metadata) {
    JPanel panel = new JPanel(MigLayouts.fillXWrap(8, 2, MigLayoutConstraints.RIGHT_GROW_FILL, ""));
    panel.setBorder(
        BorderFactory.createTitledBorder(message("serverTree.networkInfo.server.title")));
    addInfoRow(
        panel,
        message("serverTree.networkInfo.row.serverName"),
        fallbackInfoValue(metadata.serverName));
    addInfoRow(
        panel,
        message("serverTree.networkInfo.row.version"),
        fallbackInfoValue(metadata.serverVersion));
    addInfoRow(
        panel,
        message("serverTree.networkInfo.row.userModes"),
        fallbackInfoValue(metadata.userModes));
    addInfoRow(
        panel,
        message("serverTree.networkInfo.row.channelModes"),
        fallbackInfoValue(metadata.channelModes));
    return panel;
  }

  private JPanel buildCapabilitiesInfoPanel(
      Context context, String serverId, ServerRuntimeMetadata metadata) {
    JPanel panel =
        new JPanel(
            MigLayouts.fillWrap(
                8, 1, MigLayoutConstraints.GROW_FILL, "[]6[]6[]6[]6[grow,fill]6[grow,fill]"));
    panel.add(buildCapabilityCountsRow(metadata), MigConstraints.growX());
    panel.add(new JLabel(capabilityStatusSummary(metadata)), MigConstraints.growX());
    panel.add(
        new JLabel(message("serverTree.networkInfo.capabilities.toggleHint")),
        MigConstraints.growX());
    panel.add(buildCapabilityFeatureSummaryPanel(metadata), MigConstraints.growX());

    TreeMap<String, ServerRuntimeMetadata.CapabilityState> sortedObserved =
        new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    sortedObserved.putAll(metadata.ircv3Caps);
    java.util.TreeSet<String> allCapabilities =
        new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    allCapabilities.addAll(sortedObserved.keySet());
    for (String capability : ircv3ExtensionCatalog.requestableCapabilityTokens()) {
      String normalized =
          Objects.toString(capability, "").trim().toLowerCase(java.util.Locale.ROOT);
      if (!normalized.isEmpty()) {
        allCapabilities.add(normalized);
      }
    }

    if (allCapabilities.isEmpty()) {
      panel.add(
          new JLabel(message("serverTree.networkInfo.capabilities.empty")),
          MigConstraints.grow());
      panel.add(buildCapabilityTransitionsPanel(metadata), MigConstraints.grow());
      return panel;
    }

    Object[][] rows = new Object[allCapabilities.size()][4];
    int idx = 0;
    for (String capName : allCapabilities) {
      ServerRuntimeMetadata.CapabilityState state = sortedObserved.get(capName);
      rows[idx][0] = capName;
      rows[idx][1] =
          state == null ? message("serverTree.networkInfo.capabilities.notSeen") : state.label;
      rows[idx][2] = isCapabilityRequested(capName);
      rows[idx][3] = fallbackInfoValue(metadata.ircv3CapLastSubcommand.get(capName));
      idx++;
    }

    DefaultTableModel model =
        new DefaultTableModel(rows, new String[] {
              message("serverTree.networkInfo.capabilities.column.capability"),
              message("serverTree.networkInfo.capabilities.column.state"),
              message("serverTree.networkInfo.capabilities.column.requested"),
              message("serverTree.networkInfo.capabilities.column.lastCap")
            }) {
          @Override
          public boolean isCellEditable(int row, int column) {
            if (column != 2) {
              return false;
            }
            String capability = Objects.toString(getValueAt(row, 0), "");
            return !requestTokenForCapabilityFromCatalog(capability).isEmpty();
          }

          @Override
          public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 2) {
              return Boolean.class;
            }
            return String.class;
          }
        };
    model.addTableModelListener(
        event -> {
          if (event == null || event.getType() != TableModelEvent.UPDATE) {
            return;
          }
          if (event.getColumn() != 2 && event.getColumn() != TableModelEvent.ALL_COLUMNS) {
            return;
          }
          int from = Math.max(0, event.getFirstRow());
          int to = Math.max(from, event.getLastRow());
          for (int row = from; row <= to; row++) {
            String cap =
                Objects.toString(model.getValueAt(row, 0), "")
                    .trim()
                    .toLowerCase(java.util.Locale.ROOT);
            String requestToken = requestTokenForCapabilityFromCatalog(cap);
            if (requestToken.isEmpty()) {
              continue;
            }
            boolean enable = Boolean.TRUE.equals(model.getValueAt(row, 2));
            context.requestCapabilityToggle(serverId, requestToken, enable);
          }
        });

    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.setAutoCreateRowSorter(true);
    table.setColumnSelectionAllowed(false);
    table.getTableHeader().setReorderingAllowed(false);
    JScrollPane scroll = new JScrollPane(table);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    panel.add(scroll, MigConstraints.grow());
    panel.add(buildCapabilityTransitionsPanel(metadata), MigConstraints.grow());
    return panel;
  }

  private JComponent buildCapabilityFeatureSummaryPanel(ServerRuntimeMetadata metadata) {
    JPanel panel =
        new JPanel(
            MigLayouts.fillWrap(
                0, 1, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.LEADING_GROW_FILL));
    panel.setBorder(
        BorderFactory.createTitledBorder(
            message("serverTree.networkInfo.featureReadiness.title")));

    List<CapabilityFeatureStatus> statuses =
        computeCapabilityFeatureStatuses(metadata, ircv3ExtensionCatalog.visibleFeatures());
    if (statuses.isEmpty()) {
      panel.add(
          new JLabel(message("serverTree.networkInfo.featureReadiness.empty")),
          MigConstraints.growX());
      return panel;
    }

    Object[][] rows = new Object[statuses.size()][3];
    for (int i = 0; i < statuses.size(); i++) {
      CapabilityFeatureStatus status = statuses.get(i);
      rows[i][0] = status.feature();
      rows[i][1] = status.status();
      rows[i][2] = status.detail();
    }

    JTable table = buildReadOnlyTable(new String[] {
            message("serverTree.networkInfo.featureReadiness.column.feature"),
            message("serverTree.networkInfo.featureReadiness.column.status"),
            message("serverTree.networkInfo.featureReadiness.column.details")
          }, rows);
    JScrollPane scroll = new JScrollPane(table);
    scroll.setPreferredSize(new Dimension(1, 140));
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    panel.add(scroll, MigConstraints.growX());
    return panel;
  }

  static List<CapabilityFeatureStatus> computeCapabilityFeatureStatuses(
      ServerRuntimeMetadata metadata) {
    return computeCapabilityFeatureStatuses(metadata, Ircv3ExtensionRegistry.visibleFeatures());
  }

  private static List<CapabilityFeatureStatus> computeCapabilityFeatureStatuses(
      ServerRuntimeMetadata metadata,
      List<Ircv3ExtensionRegistry.FeatureDefinition> capabilityFeatures) {
    Set<String> enabled = new LinkedHashSet<>();
    if (metadata != null) {
      for (Map.Entry<String, ServerRuntimeMetadata.CapabilityState> entry :
          metadata.ircv3Caps.entrySet()) {
        if (!ServerRuntimeMetadata.CapabilityState.ENABLED.equals(entry.getValue())) {
          continue;
        }
        String cap = normalizeCapability(entry.getKey());
        if (!cap.isEmpty()) {
          enabled.add(cap);
        }
      }
    }

    List<Ircv3ExtensionRegistry.FeatureDefinition> features =
        List.copyOf(Objects.requireNonNullElse(capabilityFeatures, List.of()));
    List<CapabilityFeatureStatus> out = new ArrayList<>(features.size());
    for (Ircv3ExtensionRegistry.FeatureDefinition feature : features) {
      List<String> missing = new ArrayList<>();
      int satisfiedRequired = 0;

      for (String required : feature.requiredAll()) {
        String cap = normalizeCapability(required);
        if (cap.isEmpty()) {
          continue;
        }
        if (enabled.contains(cap)) {
          satisfiedRequired++;
        } else {
          missing.add(cap);
        }
      }

      boolean hasRequiredAny = !feature.requiredAny().isEmpty();
      boolean anySatisfied = !hasRequiredAny;
      if (hasRequiredAny) {
        for (String candidate : feature.requiredAny()) {
          String cap = normalizeCapability(candidate);
          if (!cap.isEmpty() && enabled.contains(cap)) {
            anySatisfied = true;
            break;
          }
        }
      }
      if (!feature.requiredAny().isEmpty() && !anySatisfied) {
        missing.add(
            message(
                "serverTree.networkInfo.featureReadiness.oneOf",
                String.join(", ", feature.requiredAny())));
      }

      String status;
      if (missing.isEmpty()) {
        status = message("serverTree.networkInfo.featureReadiness.status.ready");
      } else if (satisfiedRequired > 0 || (hasRequiredAny && anySatisfied)) {
        status = message("serverTree.networkInfo.featureReadiness.status.partial");
      } else {
        status = message("serverTree.networkInfo.featureReadiness.status.unavailable");
      }

      String detail =
          missing.isEmpty()
              ? message("serverTree.networkInfo.featureReadiness.detail.ready")
              : message(
                  "serverTree.networkInfo.featureReadiness.detail.missing",
                  String.join(", ", missing));
      out.add(new CapabilityFeatureStatus(feature.label(), status, detail));
    }
    return out;
  }

  private static String normalizeCapability(String capability) {
    return Objects.toString(capability, "").trim().toLowerCase(java.util.Locale.ROOT);
  }

  static record CapabilityFeatureStatus(String feature, String status, String detail) {}

  static record InfoRow(String key, String value) {}

  private JComponent buildCapabilityTransitionsPanel(ServerRuntimeMetadata metadata) {
    JPanel panel =
        new JPanel(
            MigLayouts.fillWrap(
                0, 1, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.LEADING_GROW_FILL));
    panel.setBorder(
        BorderFactory.createTitledBorder(message("serverTree.networkInfo.capTransitions.title")));
    if (metadata.ircv3CapTransitions.isEmpty()) {
      panel.add(
          new JLabel(message("serverTree.networkInfo.capTransitions.empty")),
          MigConstraints.growX());
      return panel;
    }

    int size = metadata.ircv3CapTransitions.size();
    int start = Math.max(0, size - 80);
    Object[][] rows = new Object[size - start][4];
    int out = 0;
    for (int i = size - 1; i >= start; i--) {
      ServerRuntimeMetadata.CapabilityTransition transition = metadata.ircv3CapTransitions.get(i);
      rows[out][0] = CAP_TRANSITION_TIME_FMT.format(transition.at());
      rows[out][1] = transition.subcommand();
      rows[out][2] = transition.capability();
      rows[out][3] = transition.state().label;
      out++;
    }

    JTable table = buildReadOnlyTable(new String[] {
            message("serverTree.networkInfo.capTransitions.column.time"),
            message("serverTree.networkInfo.capTransitions.column.cap"),
            message("serverTree.networkInfo.capTransitions.column.capability"),
            message("serverTree.networkInfo.capTransitions.column.state")
          }, rows);
    JScrollPane scroll = new JScrollPane(table);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    panel.add(scroll, MigConstraints.grow());
    return panel;
  }

  private JPanel buildIsupportInfoPanel(ServerRuntimeMetadata metadata) {
    JPanel panel =
        new JPanel(
            MigLayouts.fillWrap(
                8, 1, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.GROW_FILL));
    if (metadata.isupport.isEmpty()) {
      panel.add(
          new JLabel(message("serverTree.networkInfo.isupport.empty")), MigConstraints.grow());
      return panel;
    }

    TreeMap<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    sorted.putAll(metadata.isupport);
    Object[][] rows = new Object[sorted.size()][2];
    int idx = 0;
    for (Map.Entry<String, String> entry : sorted.entrySet()) {
      String val = Objects.toString(entry.getValue(), "");
      rows[idx][0] = entry.getKey();
      rows[idx][1] = val.isBlank() ? message("serverTree.networkInfo.isupport.present") : val;
      idx++;
    }

    JTable table = buildReadOnlyTable(new String[] {
            message("serverTree.networkInfo.isupport.column.token"),
            message("serverTree.networkInfo.isupport.column.value")
          }, rows);
    JScrollPane scroll = new JScrollPane(table);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    panel.add(scroll, MigConstraints.grow());
    return panel;
  }

  private JPanel buildCapabilityCountsRow(ServerRuntimeMetadata metadata) {
    Map<ServerRuntimeMetadata.CapabilityState, Integer> counts =
        new java.util.EnumMap<>(ServerRuntimeMetadata.CapabilityState.class);
    for (ServerRuntimeMetadata.CapabilityState state :
        ServerRuntimeMetadata.CapabilityState.values()) {
      counts.put(state, 0);
    }
    for (ServerRuntimeMetadata.CapabilityState state : metadata.ircv3Caps.values()) {
      if (state == null) {
        continue;
      }
      counts.put(state, counts.getOrDefault(state, 0) + 1);
    }

    JPanel row =
        new JPanel(
            MigLayouts.fillXWrap(0, 4, "[grow,fill]8[grow,fill]8[grow,fill]8[grow,fill]", "[]"));
    row.add(
        buildCountChip(
            message("serverTree.networkInfo.capabilityCount.enabled"),
            counts.getOrDefault(ServerRuntimeMetadata.CapabilityState.ENABLED, 0)),
        MigConstraints.growX());
    row.add(
        buildCountChip(
            message("serverTree.networkInfo.capabilityCount.available"),
            counts.getOrDefault(ServerRuntimeMetadata.CapabilityState.AVAILABLE, 0)),
        MigConstraints.growX());
    row.add(
        buildCountChip(
            message("serverTree.networkInfo.capabilityCount.disabled"),
            counts.getOrDefault(ServerRuntimeMetadata.CapabilityState.DISABLED, 0)),
        MigConstraints.growX());
    row.add(
        buildCountChip(
            message("serverTree.networkInfo.capabilityCount.removed"),
            counts.getOrDefault(ServerRuntimeMetadata.CapabilityState.REMOVED, 0)),
        MigConstraints.growX());
    return row;
  }

  private boolean isCapabilityRequested(String capability) {
    String cap = requestTokenForCapabilityFromCatalog(capability);
    if (cap.isEmpty()) {
      return false;
    }

    if (runtimeConfig == null) {
      return true;
    }
    try {
      return runtimeConfig.isIrcv3CapabilityEnabled(cap, true);
    } catch (Exception ignored) {
      return true;
    }
  }

  private static String requestTokenForCapability(String capability) {
    return Ircv3ExtensionRegistry.requestTokenFor(capability);
  }

  private String requestTokenForCapabilityFromCatalog(String capability) {
    return ircv3ExtensionCatalog.requestTokenFor(capability);
  }

  private String capabilityStatusSummary(ServerRuntimeMetadata metadata) {
    if (metadata == null || metadata.ircv3Caps.isEmpty()) {
      return message("serverTree.networkInfo.capabilities.requestedButNotEnabled.none");
    }

    List<String> pending = new ArrayList<>();
    TreeMap<String, ServerRuntimeMetadata.CapabilityState> sorted =
        new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    sorted.putAll(metadata.ircv3Caps);
    for (Map.Entry<String, ServerRuntimeMetadata.CapabilityState> entry : sorted.entrySet()) {
      String cap = entry.getKey();
      ServerRuntimeMetadata.CapabilityState state = entry.getValue();
      if (!isCapabilityRequested(cap)) {
        continue;
      }
      if (ServerRuntimeMetadata.CapabilityState.ENABLED.equals(state)) {
        continue;
      }
      String label = state == null ? message("common.value.unknown") : state.label;
      pending.add(cap + " [" + label + "]");
    }

    if (pending.isEmpty()) {
      return message("serverTree.networkInfo.capabilities.requestedButNotEnabled.none");
    }
    int limit = Math.min(8, pending.size());
    String joined = String.join(", ", pending.subList(0, limit));
    if (pending.size() > limit) {
      joined =
          message(
              "serverTree.networkInfo.capabilities.summary.more",
              joined,
              pending.size() - limit);
    }
    return message("serverTree.networkInfo.capabilities.requestedButNotEnabled", joined);
  }

  private static JPanel buildCountChip(String label, int count) {
    JPanel chip =
        new JPanel(MigLayouts.wrap(6, 1, MigLayoutConstraints.GROW_FILL, MigLayouts.rows(2, 0)));
    Color border = UIManager.getColor(UiColorKeys.SEPARATOR_FOREGROUND);
    if (border == null) {
      border = UIManager.getColor(UiColorKeys.COMPONENT_BORDER_COLOR);
    }
    if (border == null) {
      border = Color.GRAY;
    }
    chip.setBorder(BorderFactory.createLineBorder(withAlpha(border, 180)));

    JLabel countLabel = new JLabel(Integer.toString(Math.max(0, count)));
    Font font = countLabel.getFont();
    if (font != null) {
      countLabel.setFont(font.deriveFont(Font.BOLD, font.getSize2D() + 1f));
    }
    JLabel textLabel = new JLabel(label);
    Color muted = UIManager.getColor(UiColorKeys.LABEL_DISABLED_FOREGROUND);
    if (muted != null) {
      textLabel.setForeground(muted);
    }

    chip.add(countLabel, MigConstraints.alignXCenter());
    chip.add(textLabel, MigConstraints.alignXCenter());
    return chip;
  }

  private static JTable buildReadOnlyTable(String[] columns, Object[][] rows) {
    DefaultTableModel model =
        new DefaultTableModel(rows, columns) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.setAutoCreateRowSorter(true);
    table.setRowSelectionAllowed(false);
    table.setColumnSelectionAllowed(false);
    table.getTableHeader().setReorderingAllowed(false);
    return table;
  }

  private static void addInfoRow(JPanel panel, String key, String value) {
    panel.add(new JLabel(key + ":"), MigConstraints.alignYTop());
    JLabel valueLabel = new JLabel(fallbackInfoValue(value));
    valueLabel.setToolTipText(fallbackInfoValue(value));
    panel.add(valueLabel, MigConstraints.growXWrap());
  }

  private static String renderBackendInfo(String backendDisplayName, String backendId) {
    String displayName = Objects.toString(backendDisplayName, "").trim();
    String id = Objects.toString(backendId, "").trim();
    if (displayName.isEmpty()) {
      return fallbackInfoValue(id);
    }
    if (id.isEmpty() || displayName.equalsIgnoreCase(id)) {
      return displayName;
    }
    return message("serverTree.networkInfo.backend.withId", displayName, id);
  }

  private static String fallbackInfoValue(String value) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? message("common.value.unknown.parenthesized") : v;
  }

  private static String formatConnectedEndpoint(String host, int port) {
    String h = Objects.toString(host, "").trim();
    if (h.isEmpty() && port <= 0) {
      return message("common.value.unknown.parenthesized");
    }
    if (h.isEmpty()) {
      return ":" + port;
    }
    if (port <= 0) {
      return h;
    }
    return h + ":" + port;
  }

  private static Color withAlpha(Color color, int alpha) {
    Color base = color == null ? Color.GRAY : color;
    int a = Math.max(0, Math.min(255, alpha));
    return new Color(base.getRed(), base.getGreen(), base.getBlue(), a);
  }
}
