package cafe.woden.ircclient.ui.logviewer;

import cafe.woden.ircclient.config.properties.ConfigPropertyKeys;
import cafe.woden.ircclient.logging.viewer.ChatLogViewerMatchMode;
import cafe.woden.ircclient.logging.viewer.ChatLogViewerQuery;
import cafe.woden.ircclient.logging.viewer.ChatLogViewerResult;
import cafe.woden.ircclient.logging.viewer.ChatLogViewerRow;
import cafe.woden.ircclient.logging.viewer.ChatLogViewerService;
import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import cafe.woden.ircclient.ui.util.PopupMenuThemeSupport;
import cafe.woden.ircclient.util.VirtualThreads;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Per-server log viewer panel backed by the chat log database.
 *
 * <p>Searches and exports are performed off the EDT.
 */
public final class LogViewerPanel extends JPanel implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(LogViewerPanel.class);
  private static final String VIEWER_BUTTON_SIZE_GROUP = "viewer-btn";
  private static final int CHANNEL_PICKER_LIMIT = 3000;
  private static final ChatLogViewerMatchMode[] TEXT_MATCH_MODES = {
    ChatLogViewerMatchMode.CONTAINS, ChatLogViewerMatchMode.GLOB, ChatLogViewerMatchMode.REGEX
  };
  private static final ChatLogViewerMatchMode[] CHANNEL_MATCH_MODES = {
    ChatLogViewerMatchMode.ANY,
    ChatLogViewerMatchMode.CONTAINS,
    ChatLogViewerMatchMode.GLOB,
    ChatLogViewerMatchMode.REGEX,
    ChatLogViewerMatchMode.LIST
  };

  private static final DateTimeFormatter TS_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  private static final int COL_TIME = 0;
  private static final int COL_NICK = 1;
  private static final int COL_MESSAGE = 2;
  private static final int COL_CHANNEL = 3;
  private static final int COL_HOSTMASK = 4;
  private static final int COL_KIND = 5;
  private static final int COL_DIRECTION = 6;
  private static final int COL_MESSAGE_ID = 7;
  private static final int COL_TAGS = 8;
  private static final int COL_META = 9;

  private static final int[] OPTIONAL_COLUMNS = {
    COL_HOSTMASK, COL_KIND, COL_DIRECTION, COL_MESSAGE_ID, COL_TAGS, COL_META
  };

  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private final ChatLogViewerService service;
  private final java.util.function.Function<String, List<String>> openChannelsProvider;
  private final ExecutorService exec;
  private final boolean ownsExecutor;
  private final AtomicLong requestSeq = new AtomicLong(0);

  private Future<?> runningTask;

  private final JLabel title = new JLabel(message("logViewer.title"));
  private final JLabel subtitle = new JLabel(message("logViewer.subtitle.initial"));
  private final JLabel status = new JLabel(" ");
  private final JPanel north = new JPanel(new BorderLayout());
  private final JPanel footer = new JPanel(new BorderLayout());

  private final javax.swing.JTextField nickField = new javax.swing.JTextField();
  private final JComboBox<ChatLogViewerMatchMode> nickMode =
      new JComboBox<>(TEXT_MATCH_MODES.clone());
  private final javax.swing.JTextField messageField = new javax.swing.JTextField();
  private final JComboBox<ChatLogViewerMatchMode> messageMode =
      new JComboBox<>(TEXT_MATCH_MODES.clone());
  private final javax.swing.JTextField hostmaskField = new javax.swing.JTextField();
  private final JComboBox<ChatLogViewerMatchMode> hostmaskMode =
      new JComboBox<>(TEXT_MATCH_MODES.clone());
  private final javax.swing.JTextField channelField = new javax.swing.JTextField();
  private final JComboBox<ChatLogViewerMatchMode> channelMode =
      new JComboBox<>(CHANNEL_MATCH_MODES.clone());
  private final JButton channelListButton = new JButton(message("logViewer.button.pickChannels"));
  private final JCheckBox includeServerEvents = new JCheckBox(message("logViewer.option.serverEvents"));
  private final JCheckBox includeProtocolDetails = new JCheckBox(message("logViewer.option.protocolDebug"));
  private final JComboBox<DateRangePreset> datePreset = new JComboBox<>(DateRangePreset.values());

  private final JLabel fromLabel = new JLabel(message("logViewer.label.from"));
  private final JLabel toLabel = new JLabel(message("logViewer.label.to"));
  private final JSpinner fromSpinner;
  private final JSpinner toSpinner;
  private final JSpinner limitSpinner = new JSpinner(new SpinnerNumberModel(500, 10, 10_000, 10));

  private final JButton searchButton = new JButton(message("logViewer.button.search"));
  private final JButton resetButton = new JButton(message("logViewer.button.reset"));
  private final JButton exportButton = new JButton(message("logViewer.button.export"));
  private final JButton columnsButton = new JButton(message("logViewer.button.columns"));
  private final JPanel advancedPanel =
      new JPanel(
          MigLayouts.fillXWithHideMode(
              "2 18 0 0", 3, "[right][grow,fill][pref!][grow,fill]", "[]"));

  private final LogViewerTableModel model = new LogViewerTableModel();
  private final JTable table = new JTable(model);
  private final TableRowSorter<LogViewerTableModel> sorter = new TableRowSorter<>(model);
  private final LinkedHashMap<Integer, TableColumn> allColumnByModelIndex = new LinkedHashMap<>();
  private final LinkedHashSet<String> channelPickerSelectedKeys = new LinkedHashSet<>();

  private JPopupMenu channelPickerPopup;
  private JTextField channelPickerFilterField;
  private JLabel channelPickerSummaryLabel;
  private JList<ChannelOption> channelPickerList;
  private DefaultListModel<ChannelOption> channelPickerModel;
  private List<ChannelOption> channelPickerAllOptions = List.of();

  private volatile String serverId = "";

  private static String message(String code, Object... args) {
    return MESSAGES.text(code, args);
  }

  public LogViewerPanel(ChatLogViewerService service) {
    this(service, sid -> List.of());
  }

  public LogViewerPanel(
      ChatLogViewerService service,
      java.util.function.Function<String, List<String>> openChannelsProvider) {
    this(
        service,
        openChannelsProvider,
        VirtualThreads.newSingleThreadExecutor("ircafe-log-viewer"),
        true);
  }

  public LogViewerPanel(
      ChatLogViewerService service,
      java.util.function.Function<String, List<String>> openChannelsProvider,
      ExecutorService exec) {
    this(service, openChannelsProvider, exec, false);
  }

  private LogViewerPanel(
      ChatLogViewerService service,
      java.util.function.Function<String, List<String>> openChannelsProvider,
      ExecutorService exec,
      boolean ownsExecutor) {
    super(new BorderLayout());
    this.service = Objects.requireNonNull(service, "service");
    this.openChannelsProvider =
        (openChannelsProvider == null) ? sid -> List.of() : openChannelsProvider;
    this.exec = Objects.requireNonNull(exec, "exec");
    if (this.exec.isShutdown()) {
      throw new IllegalArgumentException("exec must be active");
    }
    this.ownsExecutor = ownsExecutor;

    Date now = new Date();
    Date dayAgo = new Date(Math.max(0L, now.getTime() - Duration.ofDays(1).toMillis()));
    fromSpinner = new JSpinner(new SpinnerDateModel(dayAgo, null, null, java.util.Calendar.MINUTE));
    toSpinner = new JSpinner(new SpinnerDateModel(now, null, null, java.util.Calendar.MINUTE));
    fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "yyyy-MM-dd HH:mm:ss"));
    toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "yyyy-MM-dd HH:mm:ss"));
    datePreset.setSelectedItem(DateRangePreset.ALL_TIME);

    buildHeader();
    add(north, BorderLayout.NORTH);
    buildTable();
    buildFilters();
    buildStatusBar();
    add(footer, BorderLayout.SOUTH);
    installListeners();
    refreshAvailability();
  }

  public void setServerId(String serverId) {
    String sid = Objects.toString(serverId, "").trim();
    if (Objects.equals(this.serverId, sid)) return;
    this.serverId = sid;
    hideChannelPickerPopup();
    updateHeader();
    model.setRows(List.of());
    updateButtons(false);
    if (!sid.isEmpty() && service.enabled()) {
      runSearch(true);
    }
  }

  @Override
  public void close() {
    requestSeq.incrementAndGet();
    Future<?> f = runningTask;
    if (f != null) {
      try {
        f.cancel(true);
      } catch (Exception ignored) {
      }
    }
    if (ownsExecutor) {
      exec.shutdownNow();
    }
    SwingUtilities.invokeLater(
        () -> {
          if (channelPickerPopup != null) {
            channelPickerPopup.setVisible(false);
            channelPickerPopup = null;
          }
        });
  }

  private void buildHeader() {
    title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD));
    configureTopActionButtons();

    JPanel header =
        new JPanel(
            MigLayouts.fillXWrap(
                "8 10 6 10",
                7,
                "[grow,fill][right][pref!][pref!][pref!][pref!][pref!]",
                MigLayouts.rows(2, 2)));
    header.add(title, MigConstraints.growX());
    header.add(new JLabel(message("logViewer.label.maxRows")), "");
    header.add(limitSpinner, MigConstraints.width(84));
    header.add(searchButton, MigConstraints.sizeGroup(VIEWER_BUTTON_SIZE_GROUP));
    header.add(resetButton, MigConstraints.sizeGroup(VIEWER_BUTTON_SIZE_GROUP));
    header.add(columnsButton, MigConstraints.sizeGroup(VIEWER_BUTTON_SIZE_GROUP));
    header.add(exportButton, MigConstraints.sizeGroup(VIEWER_BUTTON_SIZE_GROUP));
    header.add(subtitle, MigConstraints.spanXGrowX(7));
    north.add(header, BorderLayout.CENTER);
  }

  private void buildFilters() {
    JPanel filters =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                "4 10 8 10", 1, 3, MigLayoutConstraints.GROW_FILL, MigLayouts.rows(3, 2)));

    nickMode.setRenderer(modeRenderer());
    messageMode.setRenderer(modeRenderer());
    hostmaskMode.setRenderer(modeRenderer());
    channelMode.setRenderer(modeRenderer());
    datePreset.setRenderer(datePresetRenderer());
    nickMode.setSelectedItem(ChatLogViewerMatchMode.CONTAINS);
    messageMode.setSelectedItem(ChatLogViewerMatchMode.CONTAINS);
    hostmaskMode.setSelectedItem(ChatLogViewerMatchMode.CONTAINS);
    channelMode.setSelectedItem(ChatLogViewerMatchMode.CONTAINS);

    nickField.setToolTipText(message("logViewer.tooltip.nick"));
    nickField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, message("logViewer.placeholder.nick"));
    messageField.setToolTipText(message("logViewer.tooltip.message"));
    messageField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, message("logViewer.placeholder.message"));
    hostmaskField.setToolTipText(message("logViewer.tooltip.hostmask"));
    hostmaskField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, message("logViewer.placeholder.hostmask"));
    channelField.setToolTipText(message("logViewer.tooltip.channel"));
    channelField.putClientProperty(
        FlatClientProperties.PLACEHOLDER_TEXT, message("logViewer.placeholder.channel"));
    channelListButton.setToolTipText(message("logViewer.tooltip.chooseChannels"));
    configureInlineActionButton(channelListButton, "channel", message("logViewer.tooltip.chooseChannels"));
    datePreset.setToolTipText(message("logViewer.tooltip.dateWindow"));
    includeServerEvents.setToolTipText(message("logViewer.tooltip.serverEvents"));
    includeProtocolDetails.setToolTipText(message("logViewer.tooltip.protocolDebug"));

    JPanel quickRow =
        new JPanel(
            MigLayouts.fillXWithHideMode(
                0, 3, "[right][grow,fill][pref!][right][grow,fill][pref!][pref!]", "[]"));
    quickRow.add(new JLabel(message("logViewer.label.nick")), "");
    quickRow.add(nickField, MigConstraints.pushXGrowX());
    quickRow.add(nickMode, MigConstraints.width(88));
    quickRow.add(new JLabel(message("logViewer.label.channel")), "");
    quickRow.add(channelField, MigConstraints.pushXGrowX());
    quickRow.add(channelMode, MigConstraints.width(88));
    quickRow.add(channelListButton, MigConstraints.width(34));
    filters.add(quickRow, MigConstraints.growX());

    JPanel messageRow =
        new JPanel(
            MigLayouts.fillXWithHideMode(0, 3, "[right][grow,fill][pref!][grow,fill]", "[]"));
    messageRow.add(new JLabel(message("logViewer.label.message")), "");
    messageRow.add(messageField, MigConstraints.pushXGrowX());
    messageRow.add(messageMode, MigConstraints.width(88));
    messageRow.add(new JLabel(""), MigConstraints.pushXGrowX());
    filters.add(messageRow, MigConstraints.growX());

    JPanel dateRow =
        new JPanel(
            MigLayouts.fillXWithHideMode(
                0, 3, "[right][pref!][right][pref!][right][pref!][grow,fill]", "[]"));
    dateRow.add(new JLabel(message("logViewer.label.range")), "");
    dateRow.add(datePreset, MigConstraints.width(100));
    dateRow.add(fromLabel, "");
    dateRow.add(fromSpinner, MigConstraints.width(145));
    dateRow.add(toLabel, "");
    dateRow.add(toSpinner, MigConstraints.width(145));
    dateRow.add(new JLabel(""), MigConstraints.pushXGrowX());
    filters.add(dateRow, MigConstraints.growX());

    advancedPanel.add(new JLabel(message("logViewer.label.hostmask")), "");
    advancedPanel.add(hostmaskField, MigConstraints.pushXGrowX());
    advancedPanel.add(hostmaskMode, MigConstraints.width(88));
    advancedPanel.add(new JLabel(""), MigConstraints.pushXGrowX());
    filters.add(advancedPanel, MigConstraints.growX());

    JPanel visibilityRow =
        new JPanel(MigLayouts.fillXWithHideMode(0, 3, "[right][pref!][pref!][grow,fill]", "[]"));
    visibilityRow.add(new JLabel(message("logViewer.label.show")), "");
    visibilityRow.add(includeServerEvents, "");
    visibilityRow.add(includeProtocolDetails, "");
    visibilityRow.add(new JLabel(""), MigConstraints.pushXGrowX());
    filters.add(visibilityRow, MigConstraints.growX());

    footer.add(filters, BorderLayout.NORTH);
    updateChannelFilterUi();
    updateDatePresetUi();
  }

  private void buildTable() {
    table.setFillsViewportHeight(true);
    table.setRowSelectionAllowed(true);
    table.setShowHorizontalLines(false);
    table.setShowVerticalLines(false);
    table.getTableHeader().setReorderingAllowed(false);
    table.getTableHeader().setToolTipText(message("logViewer.tooltip.sortColumn"));

    table.setRowSorter(sorter);
    sorter.setSortsOnUpdates(true);
    sorter.setSortKeys(List.of(new RowSorter.SortKey(COL_TIME, SortOrder.DESCENDING)));
    for (int i = 0; i < model.getColumnCount(); i++) {
      sorter.setSortable(i, true);
    }

    captureColumns();
    for (int col : OPTIONAL_COLUMNS) {
      hideColumn(col);
    }
    applyPreferredWidths();

    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(null);
    add(scroll, BorderLayout.CENTER);
  }

  private void buildStatusBar() {
    JPanel south = new JPanel(new BorderLayout());
    south.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
    south.add(status, BorderLayout.CENTER);
    footer.add(south, BorderLayout.SOUTH);
  }

  private void installListeners() {
    datePreset.addActionListener(e -> updateDatePresetUi());
    searchButton.addActionListener(e -> runSearch(false));
    resetButton.addActionListener(e -> resetFiltersAndSearch());
    exportButton.addActionListener(e -> exportVisibleRows());
    columnsButton.addActionListener(e -> showColumnsMenu(columnsButton));
    channelListButton.addActionListener(e -> showChannelListPickerDialog());
    includeServerEvents.addActionListener(e -> runSearch(false));
    includeProtocolDetails.addActionListener(e -> runSearch(false));

    nickField.addActionListener(e -> runSearch(false));
    messageField.addActionListener(e -> runSearch(false));
    hostmaskField.addActionListener(e -> runSearch(false));
    channelField.addActionListener(e -> runSearch(false));
    channelMode.addActionListener(e -> updateChannelFilterUi());
    table.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            if (e.getClickCount() != 2) return;

            int viewRow = table.rowAtPoint(e.getPoint());
            if (viewRow < 0) return;
            table.setRowSelectionInterval(viewRow, viewRow);
            openRowDetailsDialog(viewRow);
          }
        });
  }

  private void openRowDetailsDialog(int viewRow) {
    if (viewRow < 0) return;
    int modelRow = table.convertRowIndexToModel(viewRow);
    ChatLogViewerRow row = model.rowAt(modelRow);
    if (row == null) return;
    showRowDetailsDialog(row);
  }

  private void showRowDetailsDialog(ChatLogViewerRow row) {
    if (row == null) return;

    Window owner = SwingUtilities.getWindowAncestor(this);
    String sid = Objects.toString(row.serverId(), "").trim();
    String title =
        sid.isEmpty()
            ? message("logViewer.details.title")
            : message("logViewer.details.title.server", sid);

    JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    JPanel content = new JPanel(MigLayouts.singleColumnFill(12, "[][grow,fill][]"));

    JPanel form = new JPanel(MigLayouts.twoColumnForm(0));
    addReadOnlyFormRow(form, message("logViewer.details.field.rowId"), row.id() > 0L ? String.valueOf(row.id()) : "");
    addReadOnlyFormRow(form, message("logViewer.details.field.time"), formatEpochMs(row.tsEpochMs()));
    addReadOnlyFormRow(form, message("logViewer.details.field.server"), row.serverId());
    addReadOnlyFormRow(form, message("logViewer.details.field.target"), row.target());
    addReadOnlyFormRow(form, message("logViewer.details.field.nick"), row.fromNick());
    addReadOnlyFormRow(form, message("logViewer.details.field.hostmask"), row.hostmask());
    addReadOnlyFormRow(
        form,
        message("logViewer.details.field.direction"),
        row.direction() == null ? "" : row.direction().name());
    addReadOnlyFormRow(form, message("logViewer.details.field.kind"), row.kind() == null ? "" : row.kind().name());
    addReadOnlyFormRow(form, message("logViewer.details.field.messageId"), row.messageId());
    addReadOnlyFormRow(form, message("logViewer.details.field.messageTags"), formatTags(row.ircv3Tags()));
    addReadOnlyFormRow(form, message("logViewer.details.field.meta"), row.metaJson());
    content.add(form, MigConstraints.growX());

    JPanel messagePanel = new JPanel(MigLayouts.singleColumnFill(0, "[]4[grow,fill]"));
    messagePanel.add(new JLabel(message("logViewer.details.message")), MigConstraints.growX());
    JTextArea messageArea = new JTextArea(Objects.toString(row.text(), ""));
    messageArea.setEditable(false);
    messageArea.setLineWrap(true);
    messageArea.setWrapStyleWord(true);
    messageArea.setCaretPosition(0);
    JScrollPane messageScroll = new JScrollPane(messageArea);
    messagePanel.add(messageScroll, MigConstraints.growPushMinHeight(140));
    content.add(messagePanel, MigConstraints.growPush());

    JButton close = new JButton(message("common.button.close"));
    close.addActionListener(ev -> dialog.dispose());
    JPanel actions = new JPanel(MigLayouts.fillXGrowTrailing());
    actions.add(new JLabel(""), MigConstraints.growX());
    actions.add(close, MigConstraints.tagOk());
    content.add(actions, MigConstraints.growX());

    dialog.setContentPane(content);
    dialog.getRootPane().setDefaultButton(close);
    dialog.pack();
    dialog.setSize(Math.max(760, dialog.getWidth()), Math.max(520, dialog.getHeight()));
    dialog.setLocationRelativeTo(owner == null ? this : owner);
    dialog.setVisible(true);
  }

  private static void addReadOnlyFormRow(JPanel panel, String label, String value) {
    if (panel == null) return;
    panel.add(new JLabel(Objects.toString(label, "")));
    JTextField field = new JTextField(Objects.toString(value, ""));
    field.setEditable(false);
    field.setCaretPosition(0);
    panel.add(field, MigConstraints.growX());
  }

  private void refreshAvailability() {
    if (!service.enabled()) {
      title.setText(message("logViewer.title"));
      subtitle.setText(message("logViewer.subtitle.disabled"));
      status.setText(message("logViewer.status.enableLogging", ConfigPropertyKeys.LOGGING_ENABLED_TRUE));
      setControlsEnabled(false);
      model.setRows(List.of());
      return;
    }
    setControlsEnabled(true);
    updateButtons(false);
  }

  private void setControlsEnabled(boolean enabled) {
    nickField.setEnabled(enabled);
    nickMode.setEnabled(enabled);
    messageField.setEnabled(enabled);
    messageMode.setEnabled(enabled);
    hostmaskField.setEnabled(enabled);
    hostmaskMode.setEnabled(enabled);
    channelMode.setEnabled(enabled);
    datePreset.setEnabled(enabled);
    limitSpinner.setEnabled(enabled);
    includeServerEvents.setEnabled(enabled);
    includeProtocolDetails.setEnabled(enabled);
    fromSpinner.setEnabled(enabled && selectedDatePreset() == DateRangePreset.CUSTOM);
    toSpinner.setEnabled(enabled && selectedDatePreset() == DateRangePreset.CUSTOM);
    updateChannelFilterUi();
    updateButtons(false);
  }

  private void updateHeader() {
    String sid = serverId;
    if (sid.isEmpty()) {
      title.setText(message("logViewer.title"));
      subtitle.setText(message("logViewer.subtitle.selectServer"));
      status.setText(" ");
      return;
    }
    title.setText(message("logViewer.title.server", sid));
    subtitle.setText(message("logViewer.subtitle.server", sid));
    status.setText(" ");
  }

  private void resetFiltersAndSearch() {
    nickField.setText("");
    messageField.setText("");
    hostmaskField.setText("");
    channelField.setText("");
    nickMode.setSelectedItem(ChatLogViewerMatchMode.CONTAINS);
    messageMode.setSelectedItem(ChatLogViewerMatchMode.CONTAINS);
    hostmaskMode.setSelectedItem(ChatLogViewerMatchMode.CONTAINS);
    channelMode.setSelectedItem(ChatLogViewerMatchMode.CONTAINS);
    includeServerEvents.setSelected(false);
    includeProtocolDetails.setSelected(false);
    datePreset.setSelectedItem(DateRangePreset.ALL_TIME);
    updateDatePresetUi();
    Date now = new Date();
    fromSpinner.setValue(new Date(Math.max(0L, now.getTime() - Duration.ofDays(1).toMillis())));
    toSpinner.setValue(now);
    limitSpinner.setValue(500);
    runSearch(false);
  }

  private void runSearch(boolean auto) {
    if (!service.enabled()) return;
    String sid = serverId;
    if (sid.isEmpty()) {
      status.setText(message("logViewer.status.selectServer"));
      return;
    }

    ChatLogViewerQuery query = buildQuery(sid);
    long req = requestSeq.incrementAndGet();

    Future<?> prev = runningTask;
    if (prev != null) {
      try {
        prev.cancel(true);
      } catch (Exception ignored) {
      }
    }

    setBusy(true, auto ? message("logViewer.status.loadingLogs") : message("logViewer.status.searchingLogs"));
    runningTask =
        exec.submit(
            () -> {
              long startedNs = System.nanoTime();
              try {
                ChatLogViewerResult result = service.search(query);
                long elapsedMs = Duration.ofNanos(System.nanoTime() - startedNs).toMillis();
                SwingUtilities.invokeLater(() -> applySearchResult(req, query, result, elapsedMs));
              } catch (IllegalArgumentException ex) {
                SwingUtilities.invokeLater(() -> applySearchError(req, ex.getMessage()));
              } catch (Exception ex) {
                log.warn("[ircafe] log viewer search failed", ex);
                SwingUtilities.invokeLater(
                    () -> applySearchError(req, message("logViewer.status.searchFailed.detail", ex.getMessage())));
              }
            });
  }

  private ChatLogViewerQuery buildQuery(String sid) {
    Long fromMs = null;
    Long toMs = null;
    DateRangePreset preset = selectedDatePreset();
    long now = System.currentTimeMillis();
    if (preset == DateRangePreset.CUSTOM) {
      Date from = (Date) fromSpinner.getValue();
      Date to = (Date) toSpinner.getValue();
      if (from != null) fromMs = from.getTime();
      if (to != null) toMs = to.getTime();
    } else if (preset == DateRangePreset.LAST_1_HOUR) {
      fromMs = Math.max(0L, now - Duration.ofHours(1).toMillis());
      toMs = now;
    } else if (preset == DateRangePreset.LAST_24_HOURS) {
      fromMs = Math.max(0L, now - Duration.ofHours(24).toMillis());
      toMs = now;
    } else if (preset == DateRangePreset.LAST_7_DAYS) {
      fromMs = Math.max(0L, now - Duration.ofDays(7).toMillis());
      toMs = now;
    }

    int limit = ((Number) limitSpinner.getValue()).intValue();
    return new ChatLogViewerQuery(
        sid,
        nickField.getText(),
        selectedMode(nickMode),
        messageField.getText(),
        selectedMode(messageMode),
        hostmaskField.getText(),
        selectedMode(hostmaskMode),
        channelField.getText(),
        selectedMode(channelMode),
        includeServerEvents.isSelected(),
        includeProtocolDetails.isSelected(),
        fromMs,
        toMs,
        limit);
  }

  private void applySearchResult(
      long req, ChatLogViewerQuery query, ChatLogViewerResult result, long elapsedMs) {
    if (req != requestSeq.get()) return;

    model.setRows(result.rows());
    sorter.sort();
    updateButtons(false);

    int shown = result.rows().size();
    int limit = query.limit();
    String scannedSegment =
        result.scannedRows() > 0
            ? message("logViewer.status.searchResult.scanned", result.scannedRows())
            : "";
    String limitSegment = "";
    if (result.truncated()) {
      limitSegment =
          result.scanCapped()
              ? message("logViewer.status.searchResult.limitedByScanCap")
              : message("logViewer.status.searchResult.limited");
    }
    status.setText(
        message("logViewer.status.searchResult", shown, scannedSegment, limitSegment, elapsedMs));

    subtitle.setText(message("logViewer.subtitle.rows", shown, limit));
    setBusy(false, "");
  }

  private void applySearchError(long req, String message) {
    if (req != requestSeq.get()) return;
    setBusy(false, "");
    status.setText(Objects.toString(message, message("logViewer.status.searchFailed")));
  }

  private void setBusy(boolean busy, String statusText) {
    Cursor c = busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor();
    setCursor(c);
    table.setCursor(c);
    searchButton.setEnabled(!busy && service.enabled() && !serverId.isEmpty());
    resetButton.setEnabled(!busy && service.enabled());
    columnsButton.setEnabled(!busy && service.enabled());
    exportButton.setEnabled(!busy && service.enabled() && model.getRowCount() > 0);
    if (statusText != null && !statusText.isBlank()) {
      status.setText(statusText);
    }
  }

  private void updateButtons(boolean busy) {
    searchButton.setEnabled(!busy && service.enabled() && !serverId.isEmpty());
    resetButton.setEnabled(!busy && service.enabled());
    columnsButton.setEnabled(!busy && service.enabled());
    exportButton.setEnabled(!busy && service.enabled() && model.getRowCount() > 0);
  }

  private void configureTopActionButtons() {
    limitSpinner.setToolTipText(message("logViewer.tooltip.maxRows"));
    configureTopButton(searchButton, "hourglass", message("logViewer.tooltip.search"));
    configureTopButton(resetButton, "reset", message("logViewer.tooltip.reset"));
    configureTopButton(columnsButton, "settings", message("logViewer.tooltip.columns"));
    configureTopButton(exportButton, "copy", message("logViewer.tooltip.export"));
  }

  private static void configureTopButton(JButton button, String iconName, String tooltip) {
    if (button == null) return;
    button.setText("");
    button.setIcon(SvgIcons.action(iconName, 16));
    button.setDisabledIcon(SvgIcons.actionDisabled(iconName, 16));
    button.setMargin(new Insets(2, 6, 2, 6));
    button.setToolTipText(tooltip);
    button.setFocusable(false);
  }

  private static void configureInlineActionButton(JButton button, String iconName, String tooltip) {
    if (button == null) return;
    button.setText("");
    button.setIcon(SvgIcons.action(iconName, 14));
    button.setDisabledIcon(SvgIcons.actionDisabled(iconName, 14));
    button.setMargin(new Insets(1, 4, 1, 4));
    button.setToolTipText(tooltip);
    button.setFocusable(false);
  }

  private void exportVisibleRows() {
    if (model.getRowCount() <= 0) {
      status.setText(message("logViewer.export.noRows"));
      return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(message("logViewer.export.title"));
    chooser.setSelectedFile(new java.io.File(defaultExportFileName()));
    int result = chooser.showSaveDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) return;

    Path path = chooser.getSelectedFile().toPath();
    if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
      path = path.resolveSibling(path.getFileName().toString() + ".csv");
    }

    ExportSnapshot snapshot = captureExportSnapshot();
    if (snapshot.rows().isEmpty()) {
      status.setText(message("logViewer.export.noVisibleRows"));
      return;
    }

    setBusy(true, message("logViewer.export.inProgress", snapshot.rows().size()));
    final Path outPath = path;
    runningTask =
        exec.submit(
            () -> {
              try {
                writeCsv(outPath, snapshot);
                SwingUtilities.invokeLater(
                    () -> {
                      setBusy(false, "");
                      status.setText(
                          message(
                              "logViewer.export.complete",
                              snapshot.rows().size(),
                              outPath.toAbsolutePath()));
                    });
              } catch (Exception ex) {
                log.warn("[ircafe] log viewer export failed", ex);
                SwingUtilities.invokeLater(
                    () -> {
                      setBusy(false, "");
                      status.setText(message("logViewer.export.failed.status", ex.getMessage()));
                      JOptionPane.showMessageDialog(
                          LogViewerPanel.this,
                          message("logViewer.export.failed.message", ex.getMessage()),
                          message("logViewer.export.failed.title"),
                          JOptionPane.ERROR_MESSAGE);
                    });
              }
            });
  }

  private ExportSnapshot captureExportSnapshot() {
    TableColumnModel cm = table.getColumnModel();
    ArrayList<Integer> modelCols = new ArrayList<>();
    ArrayList<String> headers = new ArrayList<>();
    for (int i = 0; i < cm.getColumnCount(); i++) {
      TableColumn tc = cm.getColumn(i);
      int mi = tc.getModelIndex();
      modelCols.add(mi);
      headers.add(model.getColumnName(mi));
    }

    ArrayList<List<String>> rows = new ArrayList<>();
    for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
      int modelRow = table.convertRowIndexToModel(viewRow);
      ArrayList<String> row = new ArrayList<>(modelCols.size());
      for (int mi : modelCols) {
        row.add(Objects.toString(model.getValueAt(modelRow, mi), ""));
      }
      rows.add(List.copyOf(row));
    }
    return new ExportSnapshot(List.copyOf(headers), List.copyOf(rows));
  }

  private static void writeCsv(Path path, ExportSnapshot snapshot) throws Exception {
    if (path == null) throw new IllegalArgumentException(message("logViewer.export.error.outputPathRequired"));
    if (snapshot == null) throw new IllegalArgumentException(message("logViewer.export.error.snapshotRequired"));
    if (path.getParent() != null) {
      Files.createDirectories(path.getParent());
    }

    try (var out =
        Files.newBufferedWriter(
            path,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE)) {
      out.write(joinCsv(snapshot.headers()));
      out.newLine();
      for (List<String> row : snapshot.rows()) {
        out.write(joinCsv(row));
        out.newLine();
      }
    }
  }

  private static String joinCsv(List<String> cols) {
    if (cols == null || cols.isEmpty()) return "";
    StringBuilder sb = new StringBuilder(cols.size() * 16);
    for (int i = 0; i < cols.size(); i++) {
      if (i > 0) sb.append(',');
      sb.append(csvCell(cols.get(i)));
    }
    return sb.toString();
  }

  private static String csvCell(String value) {
    String s = Objects.toString(value, "");
    boolean needsQuote =
        s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
    if (!needsQuote) return s;
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }

  private String defaultExportFileName() {
    String sid = serverId.isBlank() ? "server" : serverId.replaceAll("[^A-Za-z0-9._-]+", "_");
    String ts = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date());
    return "ircafe-logs-" + sid + "-" + ts + ".csv";
  }

  private void showColumnsMenu(Component invoker) {
    JPopupMenu menu = new JPopupMenu();
    for (int modelIndex : OPTIONAL_COLUMNS) {
      String name = model.getColumnName(modelIndex);
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(name, isColumnVisible(modelIndex));
      item.addActionListener(
          e -> {
            if (item.isSelected()) {
              showColumn(modelIndex);
            } else {
              hideColumn(modelIndex);
            }
          });
      menu.add(item);
    }
    PopupMenuThemeSupport.prepareForDisplay(menu);
    menu.show(invoker, 0, invoker.getHeight());
  }

  private void captureColumns() {
    TableColumnModel cm = table.getColumnModel();
    allColumnByModelIndex.clear();
    for (int i = 0; i < cm.getColumnCount(); i++) {
      TableColumn tc = cm.getColumn(i);
      allColumnByModelIndex.put(tc.getModelIndex(), tc);
    }
  }

  private boolean isColumnVisible(int modelIndex) {
    TableColumnModel cm = table.getColumnModel();
    for (int i = 0; i < cm.getColumnCount(); i++) {
      if (cm.getColumn(i).getModelIndex() == modelIndex) return true;
    }
    return false;
  }

  private void hideColumn(int modelIndex) {
    TableColumnModel cm = table.getColumnModel();
    for (int i = 0; i < cm.getColumnCount(); i++) {
      TableColumn tc = cm.getColumn(i);
      if (tc.getModelIndex() != modelIndex) continue;
      cm.removeColumn(tc);
      return;
    }
  }

  private void showColumn(int modelIndex) {
    if (isColumnVisible(modelIndex)) return;
    TableColumn tc = allColumnByModelIndex.get(modelIndex);
    if (tc == null) return;
    TableColumnModel cm = table.getColumnModel();
    cm.addColumn(tc);
    reorderVisibleColumns();
    applyPreferredWidth(tc.getModelIndex(), tc);
  }

  private void reorderVisibleColumns() {
    TableColumnModel cm = table.getColumnModel();
    int desiredView = 0;
    for (int modelIndex = 0; modelIndex < model.getColumnCount(); modelIndex++) {
      int current = viewIndexForModel(modelIndex, cm);
      if (current < 0) continue;
      if (current != desiredView) {
        cm.moveColumn(current, desiredView);
      }
      desiredView++;
    }
  }

  private static int viewIndexForModel(int modelIndex, TableColumnModel cm) {
    for (int i = 0; i < cm.getColumnCount(); i++) {
      if (cm.getColumn(i).getModelIndex() == modelIndex) return i;
    }
    return -1;
  }

  private void applyPreferredWidths() {
    TableColumnModel cm = table.getColumnModel();
    for (int i = 0; i < cm.getColumnCount(); i++) {
      TableColumn tc = cm.getColumn(i);
      applyPreferredWidth(tc.getModelIndex(), tc);
    }
  }

  private static void applyPreferredWidth(int modelIndex, TableColumn tc) {
    if (tc == null) return;
    switch (modelIndex) {
      case COL_TIME -> tc.setPreferredWidth(165);
      case COL_NICK -> tc.setPreferredWidth(120);
      case COL_MESSAGE -> tc.setPreferredWidth(640);
      case COL_CHANNEL -> tc.setPreferredWidth(160);
      case COL_HOSTMASK -> tc.setPreferredWidth(250);
      case COL_KIND -> tc.setPreferredWidth(90);
      case COL_DIRECTION -> tc.setPreferredWidth(95);
      case COL_MESSAGE_ID -> tc.setPreferredWidth(230);
      case COL_TAGS -> tc.setPreferredWidth(320);
      case COL_META -> tc.setPreferredWidth(420);
      default -> {}
    }
  }

  private void updateChannelFilterUi() {
    boolean enabled = service.enabled() && channelMode.isEnabled();
    ChatLogViewerMatchMode mode = selectedMode(channelMode);
    boolean any = mode == ChatLogViewerMatchMode.ANY;
    boolean list = mode == ChatLogViewerMatchMode.LIST;

    channelField.setEnabled(enabled && !any);
    channelListButton.setEnabled(enabled && !serverId.isBlank());

    if (list) {
      channelField.putClientProperty(
          FlatClientProperties.PLACEHOLDER_TEXT, message("logViewer.placeholder.channelList"));
      channelField.setToolTipText(
          message("logViewer.tooltip.channelList"));
      channelListButton.setToolTipText(
          message("logViewer.tooltip.chooseChannelsFromOpenAndLogs"));
    } else if (any) {
      channelField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, message("logViewer.placeholder.channelAny"));
      channelField.setToolTipText(message("logViewer.tooltip.channelAny"));
      channelListButton.setToolTipText(message("logViewer.tooltip.chooseChannelsSwitchList"));
    } else {
      channelField.putClientProperty(
          FlatClientProperties.PLACEHOLDER_TEXT, message("logViewer.placeholder.channel"));
      channelField.setToolTipText(message("logViewer.tooltip.channel"));
      channelListButton.setToolTipText(message("logViewer.tooltip.chooseChannels"));
    }
  }

  private void showChannelListPickerDialog() {
    if (!service.enabled()) return;
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) {
      status.setText(message("logViewer.status.selectServer"));
      return;
    }

    List<String> openChannels = safeOpenChannels(sid);
    status.setText(message("logViewer.channelPicker.status.loading"));
    exec.submit(
        () -> {
          List<ChannelOption> options = collectChannelOptions(sid, openChannels);
          SwingUtilities.invokeLater(() -> presentChannelListPicker(sid, options, openChannels));
        });
  }

  private List<ChannelOption> collectChannelOptions(String serverId, List<String> openChannels) {
    LinkedHashMap<String, ChannelOption> byKey = new LinkedHashMap<>();

    List<String> open = (openChannels == null) ? List.of() : openChannels;
    for (String channel : open) {
      mergeChannelOption(byKey, channel, true, false);
    }

    try {
      List<String> fromLogs = service.listUniqueChannels(serverId, CHANNEL_PICKER_LIMIT);
      if (fromLogs != null) {
        for (String channel : fromLogs) {
          mergeChannelOption(byKey, channel, false, true);
        }
      }
    } catch (Exception ex) {
      log.debug("[ircafe] log viewer channel options lookup failed for server={}", serverId, ex);
    }

    ArrayList<ChannelOption> out = new ArrayList<>(byKey.values());
    out.sort(
        Comparator.comparing(ChannelOption::open)
            .reversed()
            .thenComparing(ChannelOption::name, String.CASE_INSENSITIVE_ORDER));
    return List.copyOf(out);
  }

  private List<String> safeOpenChannels(String serverId) {
    try {
      List<String> open = openChannelsProvider.apply(serverId);
      if (open == null || open.isEmpty()) return List.of();
      ArrayList<String> out = new ArrayList<>(open.size());
      for (String raw : open) {
        String value = Objects.toString(raw, "").trim();
        if (!isChannelName(value)) continue;
        out.add(value);
      }
      return List.copyOf(out);
    } catch (Exception ignored) {
      return List.of();
    }
  }

  private static void mergeChannelOption(
      Map<String, ChannelOption> byKey, String rawChannel, boolean open, boolean fromLog) {
    if (byKey == null) return;
    String channel = Objects.toString(rawChannel, "").trim();
    if (!isChannelName(channel)) return;

    String key = channel.toLowerCase(Locale.ROOT);
    ChannelOption cur = byKey.get(key);
    if (cur == null) {
      byKey.put(key, new ChannelOption(channel, open, fromLog));
      return;
    }
    String name = cur.name().isBlank() ? channel : cur.name();
    byKey.put(key, new ChannelOption(name, cur.open() || open, cur.fromLog() || fromLog));
  }

  private void presentChannelListPicker(
      String serverId, List<ChannelOption> options, List<String> openChannels) {
    if (!Objects.equals(this.serverId, serverId)) return;
    if (options == null || options.isEmpty()) {
      status.setText(message("logViewer.channelPicker.status.noneAvailable"));
      JOptionPane.showMessageDialog(
          this,
          message("logViewer.channelPicker.noneAvailable.message"),
          message("logViewer.channelPicker.title"),
          JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    channelPickerAllOptions = List.copyOf(options);
    channelPickerSelectedKeys.clear();

    List<String> open = (openChannels == null) ? List.of() : openChannels;
    for (String openChannel : open) {
      channelPickerSelectedKeys.add(normalizedChannelKey(openChannel));
    }
    for (String listed : parseChannelListTokens(channelField.getText())) {
      channelPickerSelectedKeys.add(normalizedChannelKey(listed));
    }

    ensureChannelPickerPopup();
    if (channelPickerPopup == null) {
      status.setText(message("logViewer.channelPicker.status.openFailed"));
      return;
    }

    if (channelPickerFilterField != null) {

      try {
        channelPickerFilterField.setText("");
        channelPickerFilterField.requestFocusInWindow();
      } finally {

      }
    }
    refreshChannelPickerList();

    if (channelPickerPopup.isVisible()) {
      channelPickerPopup.setVisible(false);
    }
    channelPickerPopup.show(channelListButton, 0, channelListButton.getHeight());
    if (channelPickerFilterField != null) {
      channelPickerFilterField.requestFocusInWindow();
      channelPickerFilterField.selectAll();
    }
    status.setText(message("logViewer.channelPicker.status.loaded", options.size()));
  }

  private static boolean isChannelName(String value) {
    String s = Objects.toString(value, "").trim();
    if (s.isEmpty()) return false;
    char first = s.charAt(0);
    return first == '#' || first == '&';
  }

  private static String normalizedChannelKey(String value) {
    return Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
  }

  private static Set<String> parseChannelListTokens(String raw) {
    String text = Objects.toString(raw, "").trim();
    if (text.isEmpty()) return Set.of();
    String[] parts = text.split("[,;\\s]+");
    LinkedHashSet<String> out = new LinkedHashSet<>(parts.length);
    for (String part : parts) {
      String token = Objects.toString(part, "").trim();
      if (!isChannelName(token)) continue;
      out.add(token);
    }
    if (out.isEmpty()) return Set.of();
    return Set.copyOf(out);
  }

  private void ensureChannelPickerPopup() {
    if (channelPickerPopup != null) return;

    JPopupMenu popup = new JPopupMenu();
    popup.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(6, 6, 6, 6)));

    channelPickerFilterField = new JTextField();
    channelPickerFilterField.putClientProperty(
        FlatClientProperties.PLACEHOLDER_TEXT, message("logViewer.channelPicker.placeholder.filter"));
    channelPickerFilterField.setToolTipText(message("logViewer.channelPicker.tooltip.filter"));
    channelPickerFilterField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                refreshChannelPickerList();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                refreshChannelPickerList();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                refreshChannelPickerList();
              }
            });

    JButton clearFilterButton = new JButton(message("common.button.clear"));
    clearFilterButton.setToolTipText(message("logViewer.channelPicker.tooltip.clearFilter"));
    clearFilterButton.addActionListener(e -> channelPickerFilterField.setText(""));

    channelPickerModel = new DefaultListModel<>();
    channelPickerList = new JList<>(channelPickerModel);
    channelPickerList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    channelPickerList.setVisibleRowCount(18);
    channelPickerList.setCellRenderer(channelPickerRenderer());
    channelPickerList.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            int idx = channelPickerList.locationToIndex(e.getPoint());
            if (idx < 0) return;
            java.awt.Rectangle cell = channelPickerList.getCellBounds(idx, idx);
            if (cell == null || !cell.contains(e.getPoint())) return;
            channelPickerList.setSelectedIndex(idx);
            toggleChannelPickerOptionAt(idx);
          }
        });
    InputMap listInputMap = channelPickerList.getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap listActionMap = channelPickerList.getActionMap();
    if (listInputMap != null && listActionMap != null) {
      final String toggleKey = "channel-picker-toggle";
      listInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), toggleKey);
      listActionMap.put(
          toggleKey,
          new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
              int idx = channelPickerList.getSelectedIndex();
              if (idx >= 0) {
                toggleChannelPickerOptionAt(idx);
              }
            }
          });
    }

    channelPickerSummaryLabel = new JLabel(" ");

    JButton clearSelectionButton = new JButton(message("common.button.clear"));
    clearSelectionButton.setToolTipText(message("logViewer.channelPicker.tooltip.clearSelection"));
    clearSelectionButton.addActionListener(
        e -> {
          channelPickerSelectedKeys.clear();
          if (channelPickerList != null) {
            channelPickerList.clearSelection();
          }
          applyChannelPickerSelection(true);
          updateChannelPickerSummary();
        });

    JButton doneButton = new JButton(message("common.button.done"));
    doneButton.setToolTipText(message("logViewer.channelPicker.tooltip.done"));
    doneButton.addActionListener(e -> hideChannelPickerPopup());

    JPanel searchRow =
        new JPanel(MigLayouts.fillXWithHideMode(0, 3, "[right][grow,fill][pref!]", "[]"));
    searchRow.add(new JLabel(message("logViewer.channelPicker.label.filter")), "");
    searchRow.add(channelPickerFilterField, MigConstraints.pushXGrowX());
    searchRow.add(clearFilterButton, "");

    JScrollPane scroll = new JScrollPane(channelPickerList);
    scroll.setPreferredSize(new java.awt.Dimension(420, 280));

    JPanel buttons =
        new JPanel(MigLayouts.fillXWithHideMode(0, 3, "[grow,fill][pref!][pref!]", "[]"));
    buttons.add(channelPickerSummaryLabel, MigConstraints.growX());
    buttons.add(clearSelectionButton, "");
    buttons.add(doneButton, "");

    JPanel root =
        new JPanel(
            MigLayouts.fillWrapWithHideMode(
                4, 1, 3, MigLayoutConstraints.GROW_FILL, "[]8[grow,fill]8[]"));
    root.add(searchRow, MigConstraints.growX());
    root.add(scroll, MigConstraints.growPushMinWidth0MinHeight(0));
    root.add(buttons, MigConstraints.growX());
    installChannelPickerShortcuts(root, doneButton);
    popup.add(root);
    channelPickerPopup = popup;
  }

  private void installChannelPickerShortcuts(JComponent root, JButton doneButton) {
    if (root == null) return;

    InputMap inputMap = root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap actionMap = root.getActionMap();
    if (inputMap == null || actionMap == null) return;

    final String focusFilterKey = "channel-picker-focus-filter";
    final String doneKey = "channel-picker-done";
    final String closeKey = "channel-picker-close";

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), focusFilterKey);
    actionMap.put(
        focusFilterKey,
        new AbstractAction() {
          @Override
          public void actionPerformed(java.awt.event.ActionEvent e) {
            if (channelPickerFilterField == null) return;
            channelPickerFilterField.requestFocusInWindow();
            channelPickerFilterField.selectAll();
          }
        });

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), doneKey);
    actionMap.put(
        doneKey,
        new AbstractAction() {
          @Override
          public void actionPerformed(java.awt.event.ActionEvent e) {
            if (doneButton != null && doneButton.isEnabled()) {
              doneButton.doClick();
            } else {
              hideChannelPickerPopup();
            }
          }
        });

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), closeKey);
    actionMap.put(
        closeKey,
        new AbstractAction() {
          @Override
          public void actionPerformed(java.awt.event.ActionEvent e) {
            hideChannelPickerPopup();
          }
        });
  }

  private void hideChannelPickerPopup() {
    if (channelPickerPopup != null) {
      channelPickerPopup.setVisible(false);
    }
  }

  private javax.swing.ListCellRenderer<? super ChannelOption> channelPickerRenderer() {
    return (list, value, index, isSelected, cellHasFocus) -> {
      JCheckBox box = new JCheckBox();
      ChannelOption option = (value instanceof ChannelOption co) ? co : null;
      String name = (option == null) ? "" : Objects.toString(option.name(), "").trim();
      StringBuilder suffix = new StringBuilder();
      if (option != null && option.open()) suffix.append(message("logViewer.channelPicker.source.open"));
      if (option != null && option.fromLog()) {
        if (!suffix.isEmpty()) suffix.append(", ");
        suffix.append(message("logViewer.channelPicker.source.log"));
      }
      if (suffix.isEmpty()) box.setText(name);
      else box.setText(name + "  (" + suffix + ")");
      box.setSelected(channelPickerSelectedKeys.contains(normalizedChannelKey(name)));
      box.setOpaque(true);
      box.setFocusPainted(false);
      if (isSelected) {
        box.setBackground(list.getSelectionBackground());
        box.setForeground(list.getSelectionForeground());
      } else {
        box.setBackground(list.getBackground());
        box.setForeground(list.getForeground());
      }
      return box;
    };
  }

  private void refreshChannelPickerList() {
    if (channelPickerModel == null || channelPickerList == null) return;
    String q =
        Objects.toString(
                (channelPickerFilterField == null) ? "" : channelPickerFilterField.getText(), "")
            .trim()
            .toLowerCase(Locale.ROOT);
    String[] tokens = q.isEmpty() ? new String[0] : q.split("\\s+");

    try {
      channelPickerModel.clear();
      for (ChannelOption option : channelPickerAllOptions) {
        if (option == null) continue;
        if (!matchesChannelPickerFilter(option, tokens)) continue;
        channelPickerModel.addElement(option);
      }
    } finally {

    }
    if (channelPickerModel.getSize() > 0 && channelPickerList.getSelectedIndex() < 0) {
      channelPickerList.setSelectedIndex(0);
    }
    channelPickerList.repaint();
    updateChannelPickerSummary();
  }

  private static boolean matchesChannelPickerFilter(ChannelOption option, String[] tokens) {
    if (option == null) return false;
    if (tokens == null || tokens.length == 0) return true;
    String name = Objects.toString(option.name(), "").toLowerCase(Locale.ROOT);
    for (String raw : tokens) {
      String token = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
      if (token.isEmpty()) continue;
      if (!name.contains(token)) return false;
    }
    return true;
  }

  private void toggleChannelPickerOptionAt(int index) {
    if (channelPickerModel == null || channelPickerList == null) return;
    if (index < 0 || index >= channelPickerModel.size()) return;
    ChannelOption option = channelPickerModel.get(index);
    if (option == null) return;
    String key = normalizedChannelKey(option.name());
    if (key.isEmpty()) return;
    if (channelPickerSelectedKeys.contains(key)) {
      channelPickerSelectedKeys.remove(key);
    } else {
      channelPickerSelectedKeys.add(key);
    }
    channelPickerList.repaint(channelPickerList.getCellBounds(index, index));
    applyChannelPickerSelection(false);
    updateChannelPickerSummary();
  }

  private void updateChannelPickerSummary() {
    if (channelPickerSummaryLabel == null) return;
    int visible = (channelPickerModel == null) ? 0 : channelPickerModel.getSize();
    int total = channelPickerAllOptions.size();
    int selected = channelPickerSelectedKeys.size();
    channelPickerSummaryLabel.setText(
        message("logViewer.channelPicker.summary", visible, total, selected));
  }

  private void applyChannelPickerAnyMode(boolean updateStatus) {
    channelPickerSelectedKeys.clear();
    if (channelPickerList != null) {
      channelPickerList.clearSelection();
    }
    channelMode.setSelectedItem(ChatLogViewerMatchMode.ANY);
    channelField.setText("");
    updateChannelFilterUi();
    updateChannelPickerSummary();
    if (updateStatus) {
      status.setText(message("logViewer.channelPicker.status.any"));
    }
  }

  private void applyChannelPickerSelection(boolean updateStatus) {
    if (channelPickerSelectedKeys.isEmpty()) {
      applyChannelPickerAnyMode(updateStatus);
      return;
    }

    LinkedHashMap<String, String> selectedNamesByKey = new LinkedHashMap<>();
    for (ChannelOption option : channelPickerAllOptions) {
      if (option == null) continue;
      String name = Objects.toString(option.name(), "").trim();
      if (!isChannelName(name)) continue;
      String key = normalizedChannelKey(name);
      if (!channelPickerSelectedKeys.contains(key)) continue;
      selectedNamesByKey.putIfAbsent(key, name);
    }

    ArrayList<String> channels = new ArrayList<>(selectedNamesByKey.values());
    if (channels.isEmpty()) {
      applyChannelPickerAnyMode(updateStatus);
      return;
    }

    channels.sort(String.CASE_INSENSITIVE_ORDER);
    channelMode.setSelectedItem(ChatLogViewerMatchMode.LIST);
    channelField.setText(String.join(", ", channels));
    updateChannelFilterUi();
    if (updateStatus) {
      status.setText(message("logViewer.channelPicker.status.selected", channels.size()));
    }
  }

  private static DefaultListCellRenderer modeRenderer() {
    return new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(
          javax.swing.JList<?> list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof ChatLogViewerMatchMode mode) {
          setText(modeLabel(mode));
        }
        return this;
      }
    };
  }

  private static String modeLabel(ChatLogViewerMatchMode mode) {
    if (mode == null) return message("logViewer.matchMode.contains");
    return switch (mode) {
      case ANY -> message("logViewer.matchMode.any");
      case CONTAINS -> message("logViewer.matchMode.contains");
      case GLOB -> message("logViewer.matchMode.glob");
      case REGEX -> message("logViewer.matchMode.regex");
      case LIST -> message("logViewer.matchMode.list");
    };
  }

  private static ChatLogViewerMatchMode selectedMode(JComboBox<ChatLogViewerMatchMode> combo) {
    Object o = combo.getSelectedItem();
    if (o instanceof ChatLogViewerMatchMode mode) return mode;
    return ChatLogViewerMatchMode.CONTAINS;
  }

  private static DefaultListCellRenderer datePresetRenderer() {
    return new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(
          javax.swing.JList<?> list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof DateRangePreset preset) {
          setText(preset.label());
        }
        return this;
      }
    };
  }

  private DateRangePreset selectedDatePreset() {
    Object o = datePreset.getSelectedItem();
    if (o instanceof DateRangePreset preset) return preset;
    return DateRangePreset.ALL_TIME;
  }

  private void updateDatePresetUi() {
    boolean custom = selectedDatePreset() == DateRangePreset.CUSTOM;
    fromLabel.setVisible(custom);
    fromSpinner.setVisible(custom);
    toLabel.setVisible(custom);
    toSpinner.setVisible(custom);
    boolean enabled = service.enabled();
    fromSpinner.setEnabled(enabled && custom);
    toSpinner.setEnabled(enabled && custom);
    revalidate();
    repaint();
  }

  private static String formatEpochMs(long epochMs) {
    if (epochMs <= 0L) return "";
    try {
      return TS_FMT.format(java.time.Instant.ofEpochMilli(epochMs));
    } catch (Exception e) {
      return String.valueOf(epochMs);
    }
  }

  private static String formatTags(Map<String, String> tags) {
    if (tags == null || tags.isEmpty()) return "";
    StringBuilder sb = new StringBuilder(tags.size() * 16);
    boolean first = true;
    for (Map.Entry<String, String> e : tags.entrySet()) {
      if (!first) sb.append("; ");
      String key = Objects.toString(e.getKey(), "").trim();
      String val = Objects.toString(e.getValue(), "").trim();
      if (val.isEmpty()) sb.append(key);
      else sb.append(key).append('=').append(val);
      first = false;
    }
    return sb.toString();
  }

  private enum DateRangePreset {
    ALL_TIME("logViewer.datePreset.allTime"),
    LAST_1_HOUR("logViewer.datePreset.last1h"),
    LAST_24_HOURS("logViewer.datePreset.last24h"),
    LAST_7_DAYS("logViewer.datePreset.last7d"),
    CUSTOM("logViewer.datePreset.custom");

    private final String code;

    DateRangePreset(String code) {
      this.code = code;
    }

    private String label() {
      return message(code);
    }
  }

  private static final class LogViewerTableModel extends AbstractTableModel {
    private static final String[] COL_CODES = {
      "logViewer.column.time",
      "logViewer.column.nick",
      "logViewer.column.message",
      "logViewer.column.channel",
      "logViewer.column.hostmask",
      "logViewer.column.kind",
      "logViewer.column.direction",
      "logViewer.column.messageId",
      "logViewer.column.messageTags",
      "logViewer.column.meta"
    };

    private List<ChatLogViewerRow> rows = List.of();

    void setRows(List<ChatLogViewerRow> rows) {
      this.rows = (rows == null) ? List.of() : List.copyOf(rows);
      fireTableDataChanged();
    }

    ChatLogViewerRow rowAt(int rowIndex) {
      if (rowIndex < 0 || rowIndex >= rows.size()) return null;
      return rows.get(rowIndex);
    }

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public int getColumnCount() {
      return COL_CODES.length;
    }

    @Override
    public String getColumnName(int column) {
      return (column >= 0 && column < COL_CODES.length) ? message(COL_CODES[column]) : "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < 0 || rowIndex >= rows.size()) return "";
      ChatLogViewerRow row = rows.get(rowIndex);
      if (row == null) return "";

      return switch (columnIndex) {
        case COL_TIME -> formatEpochMs(row.tsEpochMs());
        case COL_NICK -> row.fromNick();
        case COL_MESSAGE -> row.text();
        case COL_CHANNEL -> row.target();
        case COL_HOSTMASK -> row.hostmask();
        case COL_KIND -> row.kind().name();
        case COL_DIRECTION -> row.direction().name();
        case COL_MESSAGE_ID -> row.messageId();
        case COL_TAGS -> formatTags(row.ircv3Tags());
        case COL_META -> row.metaJson();
        default -> "";
      };
    }
  }

  private record ChannelOption(String name, boolean open, boolean fromLog) {}

  private record ExportSnapshot(List<String> headers, List<List<String>> rows) {}
}
