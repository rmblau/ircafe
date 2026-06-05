package cafe.woden.ircclient.ui.application;

import cafe.woden.ircclient.diagnostics.JfrRuntimeEventsService;
import cafe.woden.ircclient.diagnostics.RuntimeDiagnosticEvent;
import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import cafe.woden.ircclient.ui.util.UiFontKeys;
import cafe.woden.ircclient.util.VirtualThreads;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 * Dedicated diagnostics UI for the Application -> JFR node.
 *
 * <p>Layout is split into a {@code Status} tab (gauges) and a {@code JFR Events} tab (event table +
 * row actions).
 */
public final class JfrDiagnosticsPanel extends JPanel {
  private static final DateTimeFormatter TIME_FMT =
      DateTimeFormatter.ofPattern("HH:mm:ss")
          .withLocale(Locale.ROOT)
          .withZone(ZoneId.systemDefault());

  private static final long GB = 1000L * 1000L * 1000L;
  private static final double GC_ALERT_EVENTS_PER_MINUTE = 10.0d;
  private static final int ACTION_ICON_SIZE = 16;
  private static final Dimension ACTION_BUTTON_SIZE = new Dimension(28, 28);
  private static final String GC_EVENT_TYPE = "jdk.GarbageCollection";
  private static final ExecutorService MEMORY_EXPORT_EXECUTOR =
      VirtualThreads.newThreadPerTaskExecutor("ircafe-jfr-memory-export");
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private static final int COL_TIME = 0;
  private static final int COL_LEVEL = 1;
  private static final int COL_TYPE = 2;
  private static final int COL_SUMMARY = 3;

  private final JfrRuntimeEventsService service;
  private final PropertyChangeListener stateListener = __ -> refreshOnEdt();
  private final RuntimeEventsTableModel model = new RuntimeEventsTableModel();
  private final JTable table = new JTable(model);
  private final JCheckBox enabledCheck =
      new JCheckBox(message("jfrDiagnostics.control.enable"));
  private final JCheckBox pauseRowsCheck =
      new JCheckBox(message("jfrDiagnostics.control.pauseRows"));
  private final JTextField streamValue = newSummaryField();
  private final JTextField cpuMachineValue = newSummaryField();
  private final JTextField cpuJvmUserValue = newSummaryField();
  private final JTextField cpuJvmSystemValue = newSummaryField();
  private final JTextField cpuSampleValue = newSummaryField();
  private final JTextField heapUsedValue = newSummaryField();
  private final JTextField heapCommittedValue = newSummaryField();
  private final JTextField heapMaxValue = newSummaryField();
  private final JTextField heapSampleValue = newSummaryField();
  private final JTextField gcCountValue = newSummaryField();
  private final JTextField gcRateValue = newSummaryField();
  private final JTextField gcLastValue = newSummaryField();
  private final JLabel rowsLabel = new JLabel(message("jfrDiagnostics.rowsLabel", 0));
  private final JButton clearAllRowsButton = new JButton();
  private final JButton clearSelectedRowButton = new JButton();
  private final JButton detailsButton = new JButton();
  private final JButton refreshButton = new JButton();
  private final JButton exportMemoryBundleButton = new JButton();
  private final CircularGauge cpuGauge = new CircularGauge(message("jfrDiagnostics.gauge.cpu"));
  private final CircularGauge heapGauge = new CircularGauge(message("jfrDiagnostics.gauge.heap"));
  private final CircularGauge gcGauge = new CircularGauge(message("jfrDiagnostics.gauge.gcRate"));

  private boolean syncingControls;
  private boolean stateListenerRegistered;
  private volatile boolean exportInProgress;

  private static String message(String code, Object... args) {
    return MESSAGES.text(code, args);
  }

  public JfrDiagnosticsPanel(JfrRuntimeEventsService service) {
    super(new BorderLayout(0, 8));
    this.service = service;

    JLabel title = new JLabel(message("jfrDiagnostics.title"));
    title.setBorder(BorderFactory.createEmptyBorder(8, 10, 2, 10));
    title.setFont(title.getFont().deriveFont(Font.BOLD));
    JLabel subtitle = new JLabel(message("jfrDiagnostics.subtitle"));
    subtitle.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
    JPanel header = new JPanel(new BorderLayout());
    header.add(title, BorderLayout.NORTH);
    header.add(subtitle, BorderLayout.SOUTH);
    add(header, BorderLayout.NORTH);

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab(message("jfrDiagnostics.tab.status"), buildStatusTab());
    tabs.addTab(message("jfrDiagnostics.tab.events"), buildEventsTab());
    add(tabs, BorderLayout.CENTER);

    configureEventActionButtons();
    installControlActions();
    installTableInteractions();
    applyServiceAvailability();
    startStateSubscription();
    refreshNow();
  }

  public void refreshNow() {
    syncStatus();
    syncRows();
  }

  @Override
  public void addNotify() {
    super.addNotify();
    startStateSubscription();
  }

  @Override
  public void removeNotify() {
    stopStateSubscription();
    super.removeNotify();
  }

  private JPanel buildStatusTab() {
    JPanel root = new JPanel(new BorderLayout(8, 8));
    root.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));

    JPanel controls = new JPanel(MigLayouts.fillXWrap(0, 2, "[]16[]push[]", "[]"));
    enabledCheck.setOpaque(false);
    pauseRowsCheck.setOpaque(false);
    controls.add(enabledCheck);
    controls.add(pauseRowsCheck);
    root.add(controls, BorderLayout.NORTH);

    JPanel gauges = new JPanel(new GridLayout(1, 3, 10, 0));
    gauges.setOpaque(false);
    gauges.add(wrapGauge(cpuGauge));
    gauges.add(wrapGauge(heapGauge));
    gauges.add(wrapGauge(gcGauge));
    root.add(gauges, BorderLayout.CENTER);

    JPanel detailGrid = new JPanel(new GridLayout(1, 3, 10, 0));
    detailGrid.setOpaque(false);
    detailGrid.add(buildCpuSummaryPanel());
    detailGrid.add(buildHeapSummaryPanel());
    detailGrid.add(buildGcSummaryPanel());
    root.add(detailGrid, BorderLayout.SOUTH);

    return root;
  }

  private JPanel buildEventsTab() {
    JPanel root = new JPanel(new BorderLayout(8, 8));
    root.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));

    table.setFillsViewportHeight(true);
    table.setRowSelectionAllowed(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setShowHorizontalLines(false);
    table.setShowVerticalLines(false);
    table.getTableHeader().setReorderingAllowed(false);
    table.getColumnModel().getColumn(COL_TIME).setPreferredWidth(84);
    table.getColumnModel().getColumn(COL_LEVEL).setPreferredWidth(64);
    table.getColumnModel().getColumn(COL_TYPE).setPreferredWidth(210);
    table.getColumnModel().getColumn(COL_SUMMARY).setPreferredWidth(760);
    table
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) updateRowButtons();
            });

    JPanel controls = new JPanel(MigLayouts.fillXWrap(0, 6, "[]4[]4[]4[]4[]push[]", "[]"));
    controls.add(refreshButton);
    controls.add(detailsButton);
    controls.add(clearSelectedRowButton);
    controls.add(clearAllRowsButton);
    controls.add(exportMemoryBundleButton);
    controls.add(rowsLabel, MigConstraints.alignXRight());

    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(null);

    root.add(controls, BorderLayout.NORTH);
    root.add(scroll, BorderLayout.CENTER);
    return root;
  }

  private void configureEventActionButtons() {
    configureEventActionButton(
        refreshButton,
        "refresh",
        message("jfrDiagnostics.button.refresh.tooltip"),
        message("jfrDiagnostics.button.refresh.accessibleName"));
    configureEventActionButton(
        detailsButton,
        "eye",
        message("jfrDiagnostics.button.details.tooltip"),
        message("jfrDiagnostics.button.details.accessibleName"));
    configureEventActionButton(
        clearSelectedRowButton,
        "close",
        message("jfrDiagnostics.button.removeSelected.tooltip"),
        message("jfrDiagnostics.button.removeSelected.accessibleName"));
    configureEventActionButton(
        clearAllRowsButton,
        "trash",
        message("jfrDiagnostics.button.clearAll.tooltip"),
        message("jfrDiagnostics.button.clearAll.accessibleName"));
    configureEventActionButton(
        exportMemoryBundleButton,
        "copy",
        message("jfrDiagnostics.button.exportMemory.tooltip"),
        message("jfrDiagnostics.button.exportMemory.accessibleName"));
  }

  private void configureEventActionButton(
      JButton button, String iconName, String tooltip, String accessibleName) {
    if (button == null) return;
    button.setText("");
    button.setIcon(SvgIcons.action(iconName, ACTION_ICON_SIZE));
    button.setDisabledIcon(SvgIcons.actionDisabled(iconName, ACTION_ICON_SIZE));
    button.setToolTipText(tooltip);
    button.setFocusable(false);
    button.setPreferredSize(ACTION_BUTTON_SIZE);
    button.getAccessibleContext().setAccessibleName(accessibleName);
  }

  private JPanel buildCpuSummaryPanel() {
    JPanel panel = new JPanel(MigLayouts.twoColumnForm(6, 8, MigLayouts.rows(4, 4)));
    panel.setBorder(
        BorderFactory.createTitledBorder(message("jfrDiagnostics.summary.cpu.title")));
    panel.setOpaque(false);
    addSummaryField(panel, message("jfrDiagnostics.summary.cpu.machine"), cpuMachineValue);
    addSummaryField(panel, message("jfrDiagnostics.summary.cpu.jvmUser"), cpuJvmUserValue);
    addSummaryField(panel, message("jfrDiagnostics.summary.cpu.jvmSystem"), cpuJvmSystemValue);
    addSummaryField(panel, message("jfrDiagnostics.summary.cpu.sample"), cpuSampleValue);
    return panel;
  }

  private JPanel buildHeapSummaryPanel() {
    JPanel panel = new JPanel(MigLayouts.twoColumnForm(6, 8, MigLayouts.rows(4, 4)));
    panel.setBorder(
        BorderFactory.createTitledBorder(message("jfrDiagnostics.summary.heap.title")));
    panel.setOpaque(false);
    addSummaryField(panel, message("jfrDiagnostics.summary.heap.used"), heapUsedValue);
    addSummaryField(panel, message("jfrDiagnostics.summary.heap.committed"), heapCommittedValue);
    addSummaryField(panel, message("jfrDiagnostics.summary.heap.max"), heapMaxValue);
    addSummaryField(panel, message("jfrDiagnostics.summary.heap.sample"), heapSampleValue);
    return panel;
  }

  private JPanel buildGcSummaryPanel() {
    JPanel panel = new JPanel(MigLayouts.twoColumnForm(6, 8, MigLayouts.rows(4, 4)));
    panel.setBorder(
        BorderFactory.createTitledBorder(message("jfrDiagnostics.summary.gc.title")));
    panel.setOpaque(false);
    addSummaryField(panel, message("jfrDiagnostics.summary.gc.eventsWindow"), gcCountValue);
    addSummaryField(panel, message("jfrDiagnostics.summary.gc.rate"), gcRateValue);
    addSummaryField(panel, message("jfrDiagnostics.summary.gc.lastEvent"), gcLastValue);
    addSummaryField(panel, message("jfrDiagnostics.summary.gc.stream"), streamValue);
    return panel;
  }

  private static JTextField newSummaryField() {
    JTextField field = new JTextField(message("jfrDiagnostics.value.unavailable"));
    field.setEditable(false);
    field.setFocusable(false);
    field.setColumns(14);
    return field;
  }

  private static void addSummaryField(JPanel panel, String label, JTextField field) {
    if (panel == null || field == null) return;
    panel.add(new JLabel(Objects.toString(label, "")));
    panel.add(field, MigConstraints.growXMinWidth0());
  }

  private void installControlActions() {
    enabledCheck.addActionListener(
        e -> {
          if (syncingControls || service == null) return;
          service.setEnabled(enabledCheck.isSelected());
          refreshNow();
        });
    pauseRowsCheck.addActionListener(
        e -> {
          if (syncingControls || service == null) return;
          service.setTableLoggingPaused(pauseRowsCheck.isSelected());
          refreshNow();
        });
    refreshButton.addActionListener(
        e -> {
          if (service != null) service.requestImmediateRefresh();
          refreshNow();
        });
    detailsButton.addActionListener(e -> showDetailsForSelectedRow());
    clearAllRowsButton.addActionListener(
        e -> {
          if (service == null) return;
          service.clearEvents();
          refreshNow();
        });
    clearSelectedRowButton.addActionListener(e -> removeSelectedRow());
    exportMemoryBundleButton.addActionListener(e -> exportMemoryDiagnosticsBundle());
  }

  private void installTableInteractions() {
    JPopupMenu popup = new JPopupMenu();
    popup
        .add(new javax.swing.JMenuItem(message("jfrDiagnostics.context.details")))
        .addActionListener(e -> showDetailsForSelectedRow());
    popup
        .add(new javax.swing.JMenuItem(message("jfrDiagnostics.context.removeSelected")))
        .addActionListener(e -> removeSelectedRow());
    popup
        .add(new javax.swing.JMenuItem(message("jfrDiagnostics.context.clearAll")))
        .addActionListener(
            e -> {
              if (service == null) return;
              service.clearEvents();
              refreshNow();
            });
    popup
        .add(new javax.swing.JMenuItem(message("jfrDiagnostics.context.exportMemory")))
        .addActionListener(e -> exportMemoryDiagnosticsBundle());

    table.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            if (e.getClickCount() >= 2) {
              showDetailsForSelectedRow();
            }
          }

          @Override
          public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
          }

          private void maybeShowPopup(MouseEvent e) {
            if (e == null || !e.isPopupTrigger()) return;
            int row = table.rowAtPoint(e.getPoint());
            if (row >= 0) {
              table.setRowSelectionInterval(row, row);
            }
            updateRowButtons();
            popup.show(table, e.getX(), e.getY());
          }
        });
  }

  private JPanel wrapGauge(CircularGauge gauge) {
    JPanel box = new JPanel(new BorderLayout());
    box.setOpaque(false);
    box.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    box.add(gauge, BorderLayout.CENTER);
    return box;
  }

  private void syncStatus() {
    if (service == null) {
      syncingControls = true;
      try {
        enabledCheck.setSelected(false);
        pauseRowsCheck.setSelected(false);
      } finally {
        syncingControls = false;
      }
      cpuGauge.setGauge(message("jfrDiagnostics.value.unavailable"), -1, false, false);
      heapGauge.setGauge(message("jfrDiagnostics.value.unavailable"), -1, false, false);
      gcGauge.setGauge(message("jfrDiagnostics.value.unavailable"), -1, false, false);
      setSummaryUnavailable(message("jfrDiagnostics.status.serviceUnavailable"));
      return;
    }

    JfrRuntimeEventsService.StatusSnapshot s = service.statusSnapshot();
    syncingControls = true;
    try {
      enabledCheck.setSelected(s.enabled());
      pauseRowsCheck.setSelected(s.tableLoggingPaused());
    } finally {
      syncingControls = false;
    }

    streamValue.setText(
        s.streamActive()
            ? message("jfrDiagnostics.status.streamActive")
            : message("jfrDiagnostics.status.streamInactive"));

    int cpuPercent =
        s.cpuMachineTotalRatio() == null
            ? -1
            : Math.max(0, Math.min(100, (int) Math.round(s.cpuMachineTotalRatio() * 100.0d)));
    cpuGauge.setGauge(
        cpuPercent < 0 ? message("jfrDiagnostics.value.unavailable") : cpuPercent + "%",
        cpuPercent,
        cpuPercent >= 90,
        false);
    cpuMachineValue.setText(JfrRuntimeEventsService.formatRatio(s.cpuMachineTotalRatio()));
    cpuJvmUserValue.setText(JfrRuntimeEventsService.formatRatio(s.cpuJvmUserRatio()));
    cpuJvmSystemValue.setText(JfrRuntimeEventsService.formatRatio(s.cpuJvmSystemRatio()));
    cpuSampleValue.setText(formatInstant(s.lastCpuSampleAt()));

    int heapPercent = s.runtimeHeapPercent();
    heapGauge.setGauge(
        heapPercent < 0 ? message("jfrDiagnostics.value.unavailable") : heapPercent + "%",
        heapPercent,
        heapPercent >= 90,
        false);
    heapUsedValue.setText(toGb(s.runtimeUsedBytes()));
    heapCommittedValue.setText(toGb(s.runtimeCommittedBytes()));
    heapMaxValue.setText(
        s.runtimeMaxBytes() > 0
            ? toGb(s.runtimeMaxBytes())
            : message("jfrDiagnostics.value.unavailable"));
    heapSampleValue.setText(formatInstant(s.lastRuntimeSampleAt()));

    int gcGaugePercent =
        Math.max(
            0,
            Math.min(
                100,
                (int)
                    Math.round(
                        (s.gcEventsPerMinute() / Math.max(1.0d, GC_ALERT_EVENTS_PER_MINUTE))
                            * 100.0d)));
    boolean pulse =
        s.lastGcEventAt() != null && s.lastGcEventAt().isAfter(Instant.now().minusSeconds(2));
    gcGauge.setGauge(
        String.format(Locale.ROOT, "%.1f/min", s.gcEventsPerMinute()),
        gcGaugePercent,
        s.gcAlert(),
        pulse);
    gcCountValue.setText(Integer.toString(s.gcEventsInWindow()));
    gcRateValue.setText(
        String.format(
            Locale.ROOT,
            "%.1f/min%s",
            s.gcEventsPerMinute(),
            s.gcAlert() ? message("jfrDiagnostics.status.alertSuffix") : ""));
    gcLastValue.setText(formatInstant(s.lastGcEventAt()));
  }

  private void syncRows() {
    RuntimeDiagnosticEvent selected = selectedEvent();
    List<RuntimeDiagnosticEvent> rows = service != null ? service.recentEvents(800) : List.of();
    if (!rows.isEmpty()) {
      rows =
          rows.stream()
              .filter(row -> !GC_EVENT_TYPE.equalsIgnoreCase(Objects.toString(row.type(), "")))
              .toList();
    }
    model.setRows(rows);
    restoreSelection(selected);
    rowsLabel.setText(message("jfrDiagnostics.rowsLabel", model.getRowCount()));
    updateRowButtons();
  }

  private void applyServiceAvailability() {
    boolean available = service != null;
    enabledCheck.setEnabled(available);
    pauseRowsCheck.setEnabled(available);
    clearAllRowsButton.setEnabled(available);
    refreshButton.setEnabled(available);
    exportMemoryBundleButton.setEnabled(available && !exportInProgress);
  }

  private void setSummaryUnavailable(String streamStatusText) {
    String unavailable = message("jfrDiagnostics.value.unavailable");
    streamValue.setText(Objects.toString(streamStatusText, unavailable));
    cpuMachineValue.setText(unavailable);
    cpuJvmUserValue.setText(unavailable);
    cpuJvmSystemValue.setText(unavailable);
    cpuSampleValue.setText(unavailable);
    heapUsedValue.setText(unavailable);
    heapCommittedValue.setText(unavailable);
    heapMaxValue.setText(unavailable);
    heapSampleValue.setText(unavailable);
    gcCountValue.setText(unavailable);
    gcRateValue.setText(unavailable);
    gcLastValue.setText(unavailable);
  }

  private void restoreSelection(RuntimeDiagnosticEvent selected) {
    if (selected == null) {
      table.clearSelection();
      return;
    }
    int modelRow = model.indexOf(selected);
    if (modelRow < 0) {
      table.clearSelection();
      return;
    }
    int viewRow = table.convertRowIndexToView(modelRow);
    if (viewRow < 0) {
      table.clearSelection();
      return;
    }
    if (table.getSelectedRow() != viewRow) {
      table.setRowSelectionInterval(viewRow, viewRow);
    }
  }

  private void startStateSubscription() {
    if (service == null) return;
    if (stateListenerRegistered) return;
    service.addStateListener(stateListener);
    stateListenerRegistered = true;
  }

  private void stopStateSubscription() {
    if (service == null) return;
    if (!stateListenerRegistered) return;
    service.removeStateListener(stateListener);
    stateListenerRegistered = false;
  }

  private void refreshOnEdt() {
    if (SwingUtilities.isEventDispatchThread()) {
      refreshNow();
    } else {
      SwingUtilities.invokeLater(this::refreshNow);
    }
  }

  private void updateRowButtons() {
    boolean hasSelection = selectedEvent() != null;
    detailsButton.setEnabled(hasSelection);
    clearSelectedRowButton.setEnabled(hasSelection && service != null);
    exportMemoryBundleButton.setEnabled(service != null && !exportInProgress);
  }

  private RuntimeDiagnosticEvent selectedEvent() {
    int row = table.getSelectedRow();
    if (row < 0) return null;
    int modelRow = table.convertRowIndexToModel(row);
    return model.rowAt(modelRow);
  }

  private void removeSelectedRow() {
    RuntimeDiagnosticEvent event = selectedEvent();
    if (event == null || service == null) return;
    service.removeEvent(event);
    refreshNow();
  }

  private void showDetailsForSelectedRow() {
    RuntimeDiagnosticEvent event = selectedEvent();
    if (event == null) return;

    JPanel content = buildDetailPanel(event);
    content.setPreferredSize(new Dimension(860, 560));
    JOptionPane.showMessageDialog(
        SwingUtilities.getWindowAncestor(this),
        content,
        message("jfrDiagnostics.details.title"),
        JOptionPane.INFORMATION_MESSAGE);
  }

  private void exportMemoryDiagnosticsBundle() {
    if (service == null || exportInProgress) return;
    Object[] options = {
      message("jfrDiagnostics.export.option.lightBundle"),
      message("jfrDiagnostics.export.option.fullBundle"),
      message("common.button.cancel")
    };
    int choice =
        JOptionPane.showOptionDialog(
            SwingUtilities.getWindowAncestor(this),
            message("jfrDiagnostics.export.prompt"),
            message("jfrDiagnostics.export.title"),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]);
    if (choice < 0 || choice >= 2) return;
    boolean includeHeapDump = choice == 1;

    setExportInProgress(true);
    CompletableFuture.supplyAsync(
            () -> service.captureMemoryDiagnosticsBundle(includeHeapDump), MEMORY_EXPORT_EXECUTOR)
        .whenComplete(
            (report, error) ->
                SwingUtilities.invokeLater(
                    () -> {
                      setExportInProgress(false);
                      if (error != null) {
                        showMultilineDialog(
                            message("jfrDiagnostics.export.error.title"),
                            message(
                                "jfrDiagnostics.export.error.message",
                                Objects.toString(error.getMessage(), "")),
                            JOptionPane.ERROR_MESSAGE);
                        return;
                      }

                      if (report == null) {
                        showMultilineDialog(
                            message("jfrDiagnostics.export.error.title"),
                            message("jfrDiagnostics.export.error.noReport"),
                            JOptionPane.ERROR_MESSAGE);
                        return;
                      }

                      if (report.success()) {
                        showMultilineDialog(
                            message("jfrDiagnostics.export.complete.title"),
                            report.summary(),
                            JOptionPane.INFORMATION_MESSAGE);
                      } else {
                        showMultilineDialog(
                            message("jfrDiagnostics.export.error.title"),
                            report.summary(),
                            JOptionPane.ERROR_MESSAGE);
                      }
                    }));
  }

  private void setExportInProgress(boolean inProgress) {
    exportInProgress = inProgress;
    exportMemoryBundleButton.setEnabled(service != null && !exportInProgress);
    exportMemoryBundleButton.setToolTipText(
        inProgress
            ? message("jfrDiagnostics.button.exportMemory.inProgress.tooltip")
            : message("jfrDiagnostics.button.exportMemory.tooltip"));
    exportMemoryBundleButton.repaint();
  }

  private void showMultilineDialog(String title, String body, int messageType) {
    JTextArea text = new JTextArea(Objects.toString(body, ""));
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    text.setCaretPosition(0);
    JScrollPane scroll = new JScrollPane(text);
    scroll.setPreferredSize(new Dimension(860, 520));
    JOptionPane.showMessageDialog(
        SwingUtilities.getWindowAncestor(this), scroll, title, messageType);
  }

  private static JPanel buildDetailPanel(RuntimeDiagnosticEvent event) {
    JPanel root = new JPanel(new BorderLayout(0, 10));

    JPanel fields = new JPanel(MigLayouts.twoColumnForm(10, MigLayouts.rows(4, 4)));
    addDetailRow(
        fields,
        message("jfrDiagnostics.details.field.time"),
        event.at() == null ? "" : TIME_FMT.format(event.at()));
    addDetailRow(
        fields,
        message("jfrDiagnostics.details.field.level"),
        Objects.toString(event.level(), ""));
    addDetailRow(
        fields,
        message("jfrDiagnostics.details.field.eventType"),
        Objects.toString(event.type(), ""));
    addDetailRow(
        fields,
        message("jfrDiagnostics.details.field.summary"),
        Objects.toString(event.summary(), ""));

    Map<String, String> parsed = parseKeyValueLines(event.details());
    addDetailRowIfPresent(
        fields, message("jfrDiagnostics.details.field.timestamp"), parsed.get("timestamp"));
    addDetailRowIfPresent(
        fields, message("jfrDiagnostics.details.field.sourceType"), parsed.get("sourceType"));
    addDetailRowIfPresent(
        fields, message("jfrDiagnostics.details.field.contextId"), parsed.get("contextId"));
    addDetailRowIfPresent(
        fields,
        message("jfrDiagnostics.details.field.contextName"),
        parsed.get("contextDisplayName"));
    addDetailRowIfPresent(
        fields,
        message("jfrDiagnostics.details.field.availability"),
        parsed.get("availabilityState"));
    addDetailRowIfPresent(
        fields,
        message("jfrDiagnostics.details.field.payloadType"),
        parsed.get("payloadType"));

    JTextArea text = new JTextArea(Objects.toString(event.details(), ""));
    text.setEditable(false);
    text.setLineWrap(false);
    text.setWrapStyleWord(false);
    text.setCaretPosition(0);

    JPanel detailsPanel = new JPanel(new BorderLayout(0, 6));
    detailsPanel.add(
        new JLabel(message("jfrDiagnostics.details.section.details")), BorderLayout.NORTH);
    detailsPanel.add(new JScrollPane(text), BorderLayout.CENTER);

    root.add(fields, BorderLayout.NORTH);
    root.add(detailsPanel, BorderLayout.CENTER);
    return root;
  }

  private static void addDetailRow(JPanel panel, String label, String value) {
    panel.add(new JLabel(Objects.toString(label, "")));
    JTextArea v = new JTextArea(Objects.toString(value, ""));
    v.setEditable(false);
    v.setLineWrap(true);
    v.setWrapStyleWord(true);
    v.setOpaque(false);
    v.setBorder(null);
    v.setFocusable(false);
    panel.add(v, MigConstraints.growXMinWidth0());
  }

  private static void addDetailRowIfPresent(JPanel panel, String label, String value) {
    String v = Objects.toString(value, "").trim();
    if (v.isEmpty()) return;
    addDetailRow(panel, label, v);
  }

  private static Map<String, String> parseKeyValueLines(String details) {
    String raw = Objects.toString(details, "");
    if (raw.isBlank()) return Map.of();
    LinkedHashMap<String, String> out = new LinkedHashMap<>();
    String[] lines = raw.split("\\R");
    for (String line : lines) {
      String s = Objects.toString(line, "").trim();
      if (s.isEmpty()) continue;
      int idx = s.indexOf('=');
      if (idx <= 0 || idx >= (s.length() - 1)) continue;
      String key = s.substring(0, idx).trim();
      String value = s.substring(idx + 1).trim();
      if (key.isEmpty() || value.isEmpty()) continue;
      out.putIfAbsent(key, value);
    }
    return out;
  }

  private static String formatInstant(Instant at) {
    return at == null ? message("jfrDiagnostics.value.unavailable") : TIME_FMT.format(at);
  }

  private static String toGb(long bytes) {
    double gb = bytes / (double) GB;
    return String.format(Locale.ROOT, "%.2f GB", gb);
  }

  private static final class RuntimeEventsTableModel extends AbstractTableModel {
    private final List<RuntimeDiagnosticEvent> rows = new ArrayList<>();

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public int getColumnCount() {
      return 4;
    }

    @Override
    public String getColumnName(int column) {
      return switch (column) {
        case COL_TIME -> message("jfrDiagnostics.column.time");
        case COL_LEVEL -> message("jfrDiagnostics.column.level");
        case COL_TYPE -> message("jfrDiagnostics.column.event");
        case COL_SUMMARY -> message("jfrDiagnostics.column.summary");
        default -> "";
      };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      RuntimeDiagnosticEvent row = rowAt(rowIndex);
      if (row == null) return "";
      return switch (columnIndex) {
        case COL_TIME -> row.at() == null ? "" : TIME_FMT.format(row.at());
        case COL_LEVEL -> Objects.toString(row.level(), "");
        case COL_TYPE -> Objects.toString(row.type(), "");
        case COL_SUMMARY -> Objects.toString(row.summary(), "");
        default -> "";
      };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    RuntimeDiagnosticEvent rowAt(int row) {
      if (row < 0 || row >= rows.size()) return null;
      return rows.get(row);
    }

    int indexOf(RuntimeDiagnosticEvent event) {
      if (event == null || rows.isEmpty()) return -1;
      for (int i = 0; i < rows.size(); i++) {
        if (event.equals(rows.get(i))) return i;
      }
      return -1;
    }

    void setRows(List<RuntimeDiagnosticEvent> nextRows) {
      rows.clear();
      if (nextRows != null && !nextRows.isEmpty()) {
        rows.addAll(nextRows);
      }
      fireTableDataChanged();
    }
  }

  private static final class CircularGauge extends JComponent {
    private final String title;
    private String valueLabel = message("jfrDiagnostics.value.unavailable");
    private int valuePercent = -1;
    private boolean alert;
    private boolean pulse;

    private CircularGauge(String title) {
      this.title = Objects.toString(title, "");
      setOpaque(false);
      setPreferredSize(new Dimension(170, 170));
      setMinimumSize(new Dimension(130, 130));
    }

    private void setGauge(String valueLabel, int valuePercent, boolean alert, boolean pulse) {
      this.valueLabel = Objects.toString(valueLabel, message("jfrDiagnostics.value.unavailable"));
      this.valuePercent = valuePercent;
      this.alert = alert;
      this.pulse = pulse;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (!(g instanceof Graphics2D g2)) return;

      Object oldAa = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      Stroke oldStroke = g2.getStroke();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int pad = 12;
      int size = Math.min(getWidth(), getHeight()) - (pad * 2);
      if (size < 20) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAa);
        return;
      }
      int x = (getWidth() - size) / 2;
      int y = (getHeight() - size) / 2;
      int stroke = Math.max(10, size / 9);

      Color track = uiColor(UiColorKeys.PROGRESS_BAR_BACKGROUND, new Color(230, 230, 230));
      Color text = uiColor(UiColorKeys.LABEL_FOREGROUND, new Color(40, 40, 40));
      Color ok = new Color(46, 170, 85);
      Color warn = new Color(227, 171, 32);
      Color bad = new Color(220, 77, 66);

      Color arcColor =
          alert
              ? bad
              : (valuePercent >= 0 && valuePercent >= 80)
                  ? warn
                  : (valuePercent >= 0)
                      ? ok
                      : uiColor(UiColorKeys.LABEL_DISABLED_FOREGROUND, new Color(140, 140, 140));

      g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.setColor(track);
      g2.drawArc(x, y, size, size, 225, -270);
      if (valuePercent >= 0) {
        int extent = (int) Math.round(-270.0d * Math.max(0, Math.min(100, valuePercent)) / 100.0d);
        g2.setColor(arcColor);
        g2.drawArc(x, y, size, size, 225, extent);
      }

      if (pulse) {
        g2.setStroke(new BasicStroke(Math.max(2f, stroke * 0.18f)));
        g2.setColor(new Color(246, 199, 48, 180));
        g2.drawOval(x - 3, y - 3, size + 6, size + 6);
      }

      Font baseFont = getFont();
      if (baseFont == null) baseFont = javax.swing.UIManager.getFont(UiFontKeys.LABEL_FONT);
      if (baseFont == null) baseFont = new Font("Dialog", Font.PLAIN, 12);

      g2.setColor(text);
      g2.setFont(baseFont.deriveFont(Font.BOLD, Math.max(14f, size * 0.11f)));
      FontMetrics titleMetrics = g2.getFontMetrics();
      int titleX = (getWidth() - titleMetrics.stringWidth(title)) / 2;
      int titleY = y + Math.max(16, stroke);
      g2.drawString(title, titleX, titleY);

      g2.setFont(baseFont.deriveFont(Font.BOLD, Math.max(18f, size * 0.18f)));
      FontMetrics valueMetrics = g2.getFontMetrics();
      int valueX = (getWidth() - valueMetrics.stringWidth(valueLabel)) / 2;
      int valueY = y + (size / 2) + (valueMetrics.getAscent() / 3);
      g2.drawString(valueLabel, valueX, valueY);

      g2.setStroke(oldStroke);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAa);
    }

    private static Color uiColor(String key, Color fallback) {
      Color c = javax.swing.UIManager.getColor(key);
      return c != null ? c : fallback;
    }
  }
}
