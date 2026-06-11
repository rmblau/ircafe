package cafe.woden.ircclient.ui.channellist;

import cafe.woden.ircclient.state.api.ModeVocabulary;
import cafe.woden.ircclient.state.api.NegotiatedModeSemantics;
import cafe.woden.ircclient.ui.backend.BackendUiContext;
import cafe.woden.ircclient.ui.backend.BackendUiProfile;
import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

/** Swing panel for server /LIST results and managed channel state/actions. */
public final class ChannelListPanel extends JPanel {

  @FunctionalInterface
  public interface ChannelModeCommandHandler {
    void accept(String serverId, String channel, String modeSpec);
  }

  private enum AlisActivityState {
    IDLE,
    SPINNER,
    CONFIRMED
  }

  public enum ManagedSortMode {
    ALPHABETICAL,
    MOST_RECENT_ACTIVITY,
    MOST_UNREAD_MESSAGES,
    MOST_UNREAD_NOTIFICATIONS,
    CUSTOM
  }

  public enum ChannelDetailsSource {
    MANAGED,
    SERVER_LIST
  }

  public record ManagedChannelRow(
      String channel,
      boolean detached,
      boolean autoReattach,
      int users,
      int notifications,
      String modes) {

    public ManagedChannelRow(String channel, boolean detached, boolean autoReattach) {
      this(channel, detached, autoReattach, 0, 0, "");
    }

    public ManagedChannelRow {
      channel = Objects.toString(channel, "").trim();
      users = Math.max(0, users);
      notifications = Math.max(0, notifications);
      modes = Objects.toString(modes, "").trim();
    }
  }

  public record ChannelDetails(
      String serverId,
      ChannelDetailsSource source,
      String channel,
      String state,
      String topic,
      String modes,
      String modeSummary,
      int users,
      int notifications,
      boolean autoReattach) {}

  public record BanListEntryRow(String mask, String setBy, String setAt) {
    public BanListEntryRow {
      mask = Objects.toString(mask, "").trim();
      setBy = Objects.toString(setBy, "").trim();
      setAt = Objects.toString(setAt, "").trim();
    }
  }

  public record BanListSnapshot(List<BanListEntryRow> entries, String summary) {
    public static BanListSnapshot empty() {
      return new BanListSnapshot(List.of(), "");
    }

    public BanListSnapshot {
      ArrayList<BanListEntryRow> normalizedEntries = new ArrayList<>();
      if (entries != null) {
        for (BanListEntryRow entry : entries) {
          if (entry != null) normalizedEntries.add(entry);
        }
      }
      entries = List.copyOf(normalizedEntries);
      summary = Objects.toString(summary, "").trim();
    }

    public boolean hasEntries() {
      return !entries.isEmpty();
    }

    public boolean hasSummary() {
      return !summary.isEmpty();
    }
  }

  public record ListEntryRow(String channel, int visibleUsers, String topic) {}

  enum AlisRegistrationFilter {
    ANY,
    REGISTERED_ONLY,
    UNREGISTERED_ONLY
  }

  record AlisSearchOptions(
      boolean includeTopic,
      Integer minUsers,
      Integer maxUsers,
      Integer skipCount,
      boolean showModes,
      boolean showTopicSetter,
      AlisRegistrationFilter registrationFilter) {

    static AlisSearchOptions defaults(boolean includeTopic) {
      return new AlisSearchOptions(
          includeTopic, null, null, null, false, false, AlisRegistrationFilter.ANY);
    }
  }

  record MatrixListOptions(String searchTerm, String sinceToken, Integer limit) {
    static MatrixListOptions defaults() {
      return new MatrixListOptions("", "", MATRIX_LIST_DEFAULT_LIMIT);
    }

    MatrixListOptions {
      searchTerm = normalizeMatrixToken(searchTerm);
      sinceToken = normalizeMatrixToken(sinceToken);
      if (limit == null) {
        limit = Integer.valueOf(MATRIX_LIST_DEFAULT_LIMIT);
      } else {
        limit = Integer.valueOf(normalizeMatrixListLimit(limit.intValue()));
      }
    }
  }

  private static final int LIST_COL_CHANNEL = 0;
  private static final int LIST_COL_USERS = 1;
  private static final int LIST_COL_TOPIC = 2;

  private static final int MANAGED_COL_CHANNEL = 0;
  private static final int MANAGED_COL_STATE = 1;
  private static final int MANAGED_COL_USERS = 2;
  private static final int MANAGED_COL_NOTIFICATIONS = 3;
  private static final int MANAGED_COL_MODES = 4;
  private static final int MANAGED_COL_AUTO_REATTACH = 5;

  private static final int MATRIX_LIST_DEFAULT_LIMIT = 100;
  private static final int MATRIX_LIST_MAX_LIMIT = 200;

  private static final int ACTION_ICON_SIZE = 16;
  private static final Dimension ACTION_BUTTON_SIZE = new Dimension(28, 28);

  private final UiMessages messages = UiMessages.bundledDefaults();

  private final ChannelListUxMode ircListUxMode = new IrcChannelListUxMode();
  private final ChannelListUxMode matrixListUxMode = new MatrixChannelListUxMode();
  private final ChannelListUxMode.Context listUxContext = new ChannelListUxModeContext();
  private final ChannelListTableModel listModel =
      new ChannelListTableModel(
          message("channelList.column.channel"),
          message("channelList.column.users"),
          message("channelList.column.topic"));
  private final JTable listTable = new JTable(listModel);
  private final JTextArea listSubtitle = createSubtitleArea(ircListUxMode.defaultHint());
  private final JTextField filterField = new JTextField();
  private final TableRowSorter<ChannelListTableModel> listSorter = new TableRowSorter<>(listModel);
  private final JButton runListButton = new JButton();
  private final JButton runAlisButton = new JButton();
  private final JButton runMatrixNextButton = new JButton();
  private final JButton listDetailsButton = new JButton();
  private final JButton clearListButton = new JButton();
  private final JPopupMenu listContextMenu = new JPopupMenu();
  private final JMenuItem listJoinSelectMenuItem = new JMenuItem();
  private final JMenuItem listShowDetailsMenuItem =
      new JMenuItem(message("channelList.menu.channelDetails"));

  private final ManagedChannelTableModel managedModel = new ManagedChannelTableModel();
  private final JTable managedTable = new JTable(managedModel);
  private final JButton addChannelButton = new JButton();
  private final JButton attachDetachButton = new JButton();
  private final JButton closeChannelButton = new JButton();
  private final JButton managedDetailsButton = new JButton();
  private final JButton moveUpButton = new JButton();
  private final JButton moveDownButton = new JButton();
  private final JComboBox<ManagedSortMode> sortModeCombo =
      new JComboBox<>(ManagedSortMode.values());
  private final JLabel managedSubtitle = new JLabel(message("channelList.managed.hint"));

  private final Map<String, ArrayList<Row>> rowsByServer = new HashMap<>();
  private final Map<String, String> statusByServer = new HashMap<>();
  private final Map<String, Boolean> loadingByServer = new HashMap<>();
  private final Map<String, ChannelListRequestType> requestTypeByServer = new HashMap<>();
  private final Map<String, ArrayList<ManagedChannelRow>> managedRowsByServer = new HashMap<>();
  private final Map<String, ManagedSortMode> managedSortModeByServer = new HashMap<>();
  private final Map<String, Map<String, ChannelModeSnapshot>> channelModeSnapshotsByServer =
      new HashMap<>();
  private final Icon runAlisDefaultIcon = SvgIcons.action("help", ACTION_ICON_SIZE);
  private final Icon runAlisDefaultDisabledIcon = SvgIcons.actionDisabled("help", ACTION_ICON_SIZE);
  private final Icon runAlisActivityIcon = new AlisActivityIcon();
  private final Timer alisActivityTimer = new Timer(33, e -> onAlisActivityTick());
  private AlisActivityState alisActivityState = AlisActivityState.IDLE;
  private int alisSpinnerAngleDeg;
  private long alisConfirmedStartMs;
  private float alisConfirmedAlpha = 1f;

  private volatile String serverId = "";
  private volatile Consumer<String> onJoinChannel;
  private volatile Runnable onRunListRequest;
  private volatile Consumer<String> onRunAlisRequest;
  private volatile BackendUiProfile backendUiProfile = BackendUiProfile.ircOnly("");
  private volatile Consumer<String> onAddChannelRequest;
  private volatile Consumer<String> onReconnectChannelRequest;
  private volatile Consumer<String> onDisconnectChannelRequest;
  private volatile Consumer<String> onCloseChannelRequest;
  private volatile BiConsumer<String, Boolean> onAutoReattachChanged;
  private volatile Consumer<ManagedSortMode> onManagedSortModeChanged;
  private volatile Consumer<List<String>> onManagedCustomOrderChanged;
  private volatile Consumer<String> onManagedChannelSelected;
  private volatile BiFunction<String, String, String> onChannelTopicRequest;
  private volatile BiFunction<String, String, BanListSnapshot> onChannelBanListSnapshotRequest;
  private volatile BiConsumer<String, String> onChannelBanListRefreshRequest;
  private volatile BiConsumer<String, String> onChannelModeRefreshRequest;
  private volatile ChannelModeCommandHandler onChannelModeSetRequest;
  private volatile BiPredicate<String, String> canEditChannelModes = (server, channel) -> false;
  private volatile Function<String, ModeVocabulary> modeVocabularyProvider =
      server -> ModeVocabulary.fallback();
  private boolean syncingSortModeCombo;
  private ChannelDetailsDialogState channelDetailsDialog;

  public ChannelListPanel() {
    super(new BorderLayout());

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab(message("channelList.tab.managedChannels"), buildManagedChannelsTab());
    tabs.addTab(message("channelList.tab.serverList"), buildListTab());
    tabs.setSelectedIndex(0);
    add(tabs, BorderLayout.CENTER);

    updateListHeader();
    updateManagedHeader();
    updateManagedButtons();
  }

  private JPanel buildListTab() {
    JPanel root = new JPanel(new BorderLayout(0, 8));
    root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    JPanel controls = new JPanel(MigLayouts.fillX("[][][][][]push[][grow,fill]", "[]"));
    configureActionButton(
        runListButton,
        "refresh",
        message("channelList.action.runList.tooltip"),
        message("channelList.action.runList.accessible"));
    runListButton.setToolTipText(message("channelList.action.runList.tooltip"));
    runListButton.addActionListener(e -> runListRequested());

    configureActionButton(
        runAlisButton,
        "help",
        message("channelList.action.runAlis.tooltip"),
        message("channelList.action.runAlis.accessible"));
    runAlisButton.setIcon(runAlisDefaultIcon);
    runAlisButton.setDisabledIcon(runAlisDefaultDisabledIcon);
    runAlisButton.addActionListener(e -> runAlisRequested());

    configureActionButton(
        runMatrixNextButton,
        "play",
        message("channelList.action.runMatrixNext.tooltip"),
        message("channelList.action.runMatrixNext.accessible"));
    runMatrixNextButton.addActionListener(e -> runMatrixNextPageRequested());
    runMatrixNextButton.setVisible(false);

    configureActionButton(
        listDetailsButton,
        "eye",
        message("channelList.action.channelDetails.tooltip"),
        message("channelList.action.channelDetails.accessible"));
    listDetailsButton.addActionListener(e -> showServerListDetailsForSelection());

    configureActionButton(
        clearListButton,
        "trash",
        message("channelList.action.clearList.tooltip"),
        message("channelList.action.clearList.accessible"));
    clearListButton.addActionListener(e -> clearCurrentServerListData());

    controls.add(runListButton);
    controls.add(runAlisButton);
    controls.add(runMatrixNextButton);
    controls.add(listDetailsButton);
    controls.add(clearListButton);
    controls.add(new JLabel(message("channelList.filter.label")), MigConstraints.gapLeft(12));
    controls.add(filterField, MigConstraints.pushXGrowX());

    listSubtitle.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
    JPanel north = new JPanel(new BorderLayout(0, 6));
    north.add(controls, BorderLayout.NORTH);
    north.add(listSubtitle, BorderLayout.SOUTH);
    root.add(north, BorderLayout.NORTH);

    listTable.setFillsViewportHeight(true);
    listTable.setRowSelectionAllowed(true);
    listTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listTable.setShowHorizontalLines(false);
    listTable.setShowVerticalLines(false);
    listTable.setRowSorter(listSorter);
    listSorter.setSortsOnUpdates(false);
    listTable.getTableHeader().setReorderingAllowed(false);
    listTable.getColumnModel().getColumn(LIST_COL_CHANNEL).setPreferredWidth(220);
    listTable.getColumnModel().getColumn(LIST_COL_USERS).setPreferredWidth(90);
    listTable.getColumnModel().getColumn(LIST_COL_TOPIC).setPreferredWidth(880);
    listTable.setToolTipText(message("channelList.list.tooltip"));

    listTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) updateListButtons();
            });

    filterField.setToolTipText(message("channelList.filter.tooltip"));
    filterField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                applyListFilter();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                applyListFilter();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                applyListFilter();
              }
            });

    listTable.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            maybeShowListContextMenu(e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            maybeShowListContextMenu(e);
          }

          @Override
          public void mouseClicked(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            int viewRow = listTable.rowAtPoint(e.getPoint());
            if (viewRow < 0) return;
            if (e.getClickCount() < 2) return;
            joinServerListChannelAtViewRow(viewRow);
          }
        });

    listTable.addMouseMotionListener(
        new MouseAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            if (isCurrentServerListLoading()) {
              listTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
              return;
            }
            int viewRow = listTable.rowAtPoint(e.getPoint());
            listTable.setCursor(
                viewRow >= 0
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
          }
        });

    JScrollPane scroll = new JScrollPane(listTable);
    scroll.setBorder(null);
    root.add(scroll, BorderLayout.CENTER);
    configureListContextMenu();
    updateListActionPresentation();
    updateListButtons();
    return root;
  }

  private JPanel buildManagedChannelsTab() {
    JPanel root = new JPanel(new BorderLayout(0, 8));
    root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    managedSubtitle.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

    JPanel toolbar = new JPanel(MigLayouts.fillX("[][][][][][]push[][pref!]", "[]"));
    configureActionButton(
        addChannelButton,
        "plus",
        message("channelList.action.addManaged.tooltip"),
        message("channelList.action.addManaged.accessible"));
    configureActionButton(
        attachDetachButton,
        "play",
        message("channelList.action.reconnectDisconnect.tooltip"),
        message("channelList.action.reconnectDisconnect.accessible"));
    configureActionButton(
        closeChannelButton,
        "close",
        message("channelList.action.closeManaged.tooltip"),
        message("channelList.action.closeManaged.accessible"));
    configureActionButton(
        managedDetailsButton,
        "eye",
        message("channelList.action.managedDetails.tooltip"),
        message("channelList.action.channelDetails.accessible"));
    configureActionButton(
        moveUpButton,
        "arrow-up",
        message("channelList.action.moveUp.tooltip"),
        message("channelList.action.moveUp.accessible"));
    configureActionButton(
        moveDownButton,
        "arrow-down",
        message("channelList.action.moveDown.tooltip"),
        message("channelList.action.moveDown.accessible"));

    sortModeCombo.setToolTipText(message("channelList.sort.tooltip"));
    sortModeCombo.setRenderer(
        (list, value, index, isSelected, cellHasFocus) -> new JLabel(sortModeLabel(value)));

    addChannelButton.addActionListener(e -> addChannelRequested());
    attachDetachButton.addActionListener(e -> connectDisconnectSelectedRequested());
    closeChannelButton.addActionListener(e -> closeSelectedRequested());
    managedDetailsButton.addActionListener(e -> showManagedDetailsForSelection());
    moveUpButton.addActionListener(e -> moveSelectedBy(-1));
    moveDownButton.addActionListener(e -> moveSelectedBy(+1));
    sortModeCombo.addActionListener(e -> onSortModeChangedByUser());

    toolbar.add(addChannelButton);
    toolbar.add(attachDetachButton);
    toolbar.add(closeChannelButton);
    toolbar.add(managedDetailsButton);
    toolbar.add(moveUpButton);
    toolbar.add(moveDownButton);
    toolbar.add(new JLabel(message("channelList.sort.label")));
    toolbar.add(sortModeCombo, MigConstraints.width(200));

    managedTable.setFillsViewportHeight(true);
    managedTable.setRowSelectionAllowed(true);
    managedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    managedTable.setShowHorizontalLines(false);
    managedTable.setShowVerticalLines(false);
    managedTable.setAutoCreateRowSorter(false);
    managedTable.setToolTipText(message("channelList.managed.table.tooltip"));
    managedTable.getTableHeader().setReorderingAllowed(false);
    managedTable.getColumnModel().getColumn(MANAGED_COL_CHANNEL).setPreferredWidth(180);
    managedTable.getColumnModel().getColumn(MANAGED_COL_STATE).setPreferredWidth(110);
    managedTable.getColumnModel().getColumn(MANAGED_COL_USERS).setPreferredWidth(70);
    managedTable.getColumnModel().getColumn(MANAGED_COL_NOTIFICATIONS).setPreferredWidth(100);
    managedTable.getColumnModel().getColumn(MANAGED_COL_MODES).setPreferredWidth(100);
    managedTable.getColumnModel().getColumn(MANAGED_COL_AUTO_REATTACH).setPreferredWidth(120);
    managedTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                updateManagedButtons();
                notifyManagedChannelSelectionChanged();
              }
            });
    managedTable.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() < 2) return;
            showManagedDetailsForSelection();
          }
        });

    managedModel.setOnAutoReattachChanged(
        (channel, enabled) -> {
          BiConsumer<String, Boolean> cb = onAutoReattachChanged;
          if (cb != null) cb.accept(channel, enabled);
        });

    installManagedTableRowReorderDnD();

    JPanel north = new JPanel(new BorderLayout(0, 6));
    north.add(toolbar, BorderLayout.NORTH);
    north.add(managedSubtitle, BorderLayout.SOUTH);

    JScrollPane scroll = new JScrollPane(managedTable);
    scroll.setBorder(null);

    root.add(north, BorderLayout.NORTH);
    root.add(scroll, BorderLayout.CENTER);
    return root;
  }

  private void installManagedTableRowReorderDnD() {
    try {
      managedTable.setDragEnabled(true);
      managedTable.setDropMode(javax.swing.DropMode.INSERT_ROWS);
    } catch (Exception ignored) {
      return;
    }

    class ManagedRowTransferHandler extends TransferHandler {
      private final DataFlavor rowFlavor;

      ManagedRowTransferHandler() {
        try {
          rowFlavor =
              new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.Integer");
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      protected Transferable createTransferable(JComponent c) {
        if (!(c instanceof JTable table)) return null;
        int row = table.getSelectedRow();
        if (row < 0) return null;
        final Integer payload = row;
        return new Transferable() {
          @Override
          public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {rowFlavor};
          }

          @Override
          public boolean isDataFlavorSupported(DataFlavor flavor) {
            return rowFlavor.equals(flavor);
          }

          @Override
          public Object getTransferData(DataFlavor flavor) {
            if (!isDataFlavorSupported(flavor)) return null;
            return payload;
          }
        };
      }

      @Override
      public int getSourceActions(JComponent c) {
        return MOVE;
      }

      @Override
      public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) return false;
        if (!(support.getComponent() instanceof JTable)) return false;
        if (!support.isDataFlavorSupported(rowFlavor)) return false;
        if (currentManagedSortMode() != ManagedSortMode.CUSTOM) return false;
        support.setShowDropLocation(true);
        return true;
      }

      @Override
      public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;
        if (!(support.getComponent() instanceof JTable table)) return false;
        if (!(support.getDropLocation() instanceof JTable.DropLocation dl)) return false;

        int dropRow = dl.getRow();
        if (dropRow < 0) dropRow = table.getRowCount();

        Integer fromRow;
        try {
          Object payload = support.getTransferable().getTransferData(rowFlavor);
          if (!(payload instanceof Integer i)) return false;
          fromRow = i;
        } catch (Exception ex) {
          return false;
        }

        if (fromRow == dropRow || fromRow + 1 == dropRow) return false;
        int newRow = managedModel.moveRow(fromRow, dropRow);
        if (newRow < 0) return false;

        notifyManagedCustomOrderChanged();
        SwingUtilities.invokeLater(
            () -> {
              if (newRow >= 0 && newRow < managedModel.getRowCount()) {
                managedTable.getSelectionModel().setSelectionInterval(newRow, newRow);
                managedTable.scrollRectToVisible(managedTable.getCellRect(newRow, 0, true));
              }
              updateManagedButtons();
            });
        return true;
      }
    }

    managedTable.setTransferHandler(new ManagedRowTransferHandler());
  }

  public void setServerId(String serverId) {
    this.serverId = normalizeServerId(serverId);
    this.backendUiProfile = currentBackendUiProfile().withServerId(this.serverId);
    updateListActionPresentation();
    refreshCurrentServerViews();
    refreshOpenDetailsDialog();
  }

  public String currentServerId() {
    return serverId;
  }

  public void setOnJoinChannel(Consumer<String> onJoinChannel) {
    this.onJoinChannel = onJoinChannel;
  }

  public void setOnRunListRequest(Runnable onRunListRequest) {
    this.onRunListRequest = onRunListRequest;
  }

  public void setOnRunAlisRequest(Consumer<String> onRunAlisRequest) {
    this.onRunAlisRequest = onRunAlisRequest;
  }

  public void setBackendUiProfile(BackendUiProfile backendUiProfile) {
    BackendUiProfile profile =
        backendUiProfile == null ? BackendUiProfile.ircOnly("") : backendUiProfile;
    this.backendUiProfile = profile;
    this.serverId = profile.serverId();
    updateListActionPresentation();
    refreshCurrentServerViews();
    refreshOpenDetailsDialog();
    updateListHeader();
    updateListButtons();
  }

  public void setOnAddChannelRequest(Consumer<String> onAddChannelRequest) {
    this.onAddChannelRequest = onAddChannelRequest;
  }

  public void setOnReconnectChannelRequest(Consumer<String> onReconnectChannelRequest) {
    this.onReconnectChannelRequest = onReconnectChannelRequest;
  }

  public void setOnDisconnectChannelRequest(Consumer<String> onDisconnectChannelRequest) {
    this.onDisconnectChannelRequest = onDisconnectChannelRequest;
  }

  public void setOnCloseChannelRequest(Consumer<String> onCloseChannelRequest) {
    this.onCloseChannelRequest = onCloseChannelRequest;
  }

  public void setOnAutoReattachChanged(BiConsumer<String, Boolean> onAutoReattachChanged) {
    this.onAutoReattachChanged = onAutoReattachChanged;
  }

  public void setOnManagedSortModeChanged(Consumer<ManagedSortMode> onManagedSortModeChanged) {
    this.onManagedSortModeChanged = onManagedSortModeChanged;
  }

  public void setOnManagedCustomOrderChanged(Consumer<List<String>> onManagedCustomOrderChanged) {
    this.onManagedCustomOrderChanged = onManagedCustomOrderChanged;
  }

  public void setOnManagedChannelSelected(Consumer<String> onManagedChannelSelected) {
    this.onManagedChannelSelected = onManagedChannelSelected;
  }

  public void setOnChannelTopicRequest(BiFunction<String, String, String> onChannelTopicRequest) {
    this.onChannelTopicRequest = onChannelTopicRequest;
  }

  public void setOnChannelBanListSnapshotRequest(
      BiFunction<String, String, BanListSnapshot> onChannelBanListSnapshotRequest) {
    this.onChannelBanListSnapshotRequest = onChannelBanListSnapshotRequest;
  }

  public void setOnChannelBanListRefreshRequest(
      BiConsumer<String, String> onChannelBanListRefreshRequest) {
    this.onChannelBanListRefreshRequest = onChannelBanListRefreshRequest;
  }

  public void setOnChannelModeRefreshRequest(
      BiConsumer<String, String> onChannelModeRefreshRequest) {
    this.onChannelModeRefreshRequest = onChannelModeRefreshRequest;
  }

  public void setOnChannelModeSetRequest(ChannelModeCommandHandler onChannelModeSetRequest) {
    this.onChannelModeSetRequest = onChannelModeSetRequest;
  }

  public void setCanEditChannelModes(BiPredicate<String, String> canEditChannelModes) {
    this.canEditChannelModes =
        (canEditChannelModes == null) ? (server, channel) -> false : canEditChannelModes;
  }

  public void setModeVocabularyProvider(Function<String, ModeVocabulary> modeVocabularyProvider) {
    this.modeVocabularyProvider =
        (modeVocabularyProvider == null)
            ? server -> ModeVocabulary.fallback()
            : modeVocabularyProvider;
    refreshOpenDetailsDialog();
  }

  public void setChannelModeSnapshot(
      String serverId, String channel, String rawModes, String friendlySummary) {
    String sid = normalizeServerId(serverId);
    String ch = normalizeChannel(channel);
    if (sid.isEmpty() || ch.isEmpty()) return;

    String raw = Objects.toString(rawModes, "").trim();
    String summary = Objects.toString(friendlySummary, "").trim();
    String key = ch.toLowerCase(Locale.ROOT);

    if (raw.isEmpty() && summary.isEmpty()) {
      Map<String, ChannelModeSnapshot> byChannel = channelModeSnapshotsByServer.get(sid);
      if (byChannel != null) {
        byChannel.remove(key);
        if (byChannel.isEmpty()) {
          channelModeSnapshotsByServer.remove(sid);
        }
      }
    } else {
      channelModeSnapshotsByServer
          .computeIfAbsent(sid, __ -> new HashMap<>())
          .put(key, new ChannelModeSnapshot(raw, summary));
    }

    // Recompute row modes so managed table reflects snapshots even when the source row had unknown.
    if (sid.equals(this.serverId)) {
      refreshManagedRows();
    }
    refreshOpenChannelDetails(sid, ch);
  }

  public String rawChannelModeSnapshot(String serverId, String channel) {
    String sid = normalizeServerId(serverId);
    String ch = normalizeChannel(channel);
    if (sid.isEmpty() || ch.isEmpty()) return "";
    ChannelModeSnapshot snapshot = channelModeSnapshot(sid, ch);
    return snapshot == null ? "" : Objects.toString(snapshot.rawModes(), "").trim();
  }

  public void showChannelDetails(String serverId, String channel) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> showChannelDetails(serverId, channel));
      return;
    }

    String sid = normalizeServerId(serverId);
    String ch = normalizeChannel(channel);
    if (sid.isEmpty() || ch.isEmpty()) return;

    ChannelDetails details = channelDetailsFor(sid, ch);
    if (details == null) return;
    showChannelDetailsDialog(details);
  }

  private ChannelDetails channelDetailsFor(String serverId, String channel) {
    String sid = normalizeServerId(serverId);
    String ch = normalizeChannel(channel);
    if (sid.isEmpty() || ch.isEmpty()) return null;

    ManagedChannelRow managed = findManagedRowByChannel(sid, ch);
    Row list = findListRowByChannel(sid, ch);

    String state =
        managed == null
            ? message("channelList.state.notManaged")
            : (managed.detached()
                ? message("channelList.state.disconnected")
                : message("channelList.state.connected"));
    String modes = modeRawSnapshotForChannel(sid, ch, managed == null ? "" : managed.modes());
    String modeSummary = modeSummarySnapshotForChannel(sid, ch, modes);
    String topic = topicSnapshotForChannel(sid, ch, list == null ? "" : list.topic());
    int users =
        managed != null
            ? (managed.detached() ? -1 : managed.users())
            : (list == null ? 0 : Math.max(0, list.visibleUsers()));
    int notifications = managed == null ? 0 : Math.max(0, managed.notifications());
    boolean autoReattach = managed != null && managed.autoReattach();
    ChannelDetailsSource source =
        managed != null ? ChannelDetailsSource.MANAGED : ChannelDetailsSource.SERVER_LIST;

    return new ChannelDetails(
        sid, source, ch, state, topic, modes, modeSummary, users, notifications, autoReattach);
  }

  public void beginList(String serverId, String banner) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return;

    uxModeForServer(sid).onBeginList(sid, banner);

    rowsByServer.put(sid, new ArrayList<>());
    statusByServer.put(sid, normalizeBanner(banner));
    loadingByServer.put(sid, Boolean.TRUE);
    if (requestTypeByServer.getOrDefault(sid, ChannelListRequestType.UNKNOWN)
        == ChannelListRequestType.UNKNOWN) {
      requestTypeByServer.put(sid, uxModeForServer(sid).inferRequestTypeFromBanner(banner));
    }
    if (sid.equals(this.serverId)) {
      refreshListRows();
    }
    refreshOpenDetailsDialog();
  }

  public void appendEntry(String serverId, String channel, int visibleUsers, String topic) {
    appendEntries(serverId, List.of(new ListEntryRow(channel, visibleUsers, topic)));
  }

  public void appendEntries(String serverId, List<ListEntryRow> entries) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty() || entries == null || entries.isEmpty()) return;

    ArrayList<Row> toAppend = new ArrayList<>(entries.size());
    for (ListEntryRow entry : entries) {
      if (entry == null) continue;
      String ch = Objects.toString(entry.channel(), "").trim();
      if (ch.isEmpty()) continue;
      toAppend.add(
          new Row(
              ch, Math.max(0, entry.visibleUsers()), Objects.toString(entry.topic(), "").trim()));
    }
    if (toAppend.isEmpty()) return;

    ArrayList<Row> rows = rowsByServer.computeIfAbsent(sid, __ -> new ArrayList<>());
    rows.addAll(toAppend);

    if (sid.equals(this.serverId)) {
      listModel.addRows(toAppend);
      updateListHeader();
      updateListButtons();
    }
    refreshOpenDetailsDialog();
  }

  public void endList(String serverId, String summary) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return;

    String base = Objects.toString(summary, "").trim();
    if (base.isEmpty()) base = message("channelList.status.endOfList");
    uxModeForServer(sid).onEndList(sid, base);
    statusByServer.put(sid, base);
    loadingByServer.put(sid, Boolean.FALSE);
    if (sid.equals(this.serverId)) {
      updateListHeader();
      updateListButtons();
    }
    refreshOpenDetailsDialog();
  }

  public void setManagedChannels(
      String serverId, List<ManagedChannelRow> rows, ManagedSortMode mode) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return;

    ArrayList<ManagedChannelRow> normalized = new ArrayList<>();
    if (rows != null) {
      LinkedHashSet<String> seen = new LinkedHashSet<>();
      for (ManagedChannelRow row : rows) {
        if (row == null) continue;
        String channel = normalizeChannel(row.channel());
        if (channel.isEmpty()) continue;
        String key = channel.toLowerCase(Locale.ROOT);
        if (!seen.add(key)) continue;
        String modes = Objects.toString(row.modes(), "").trim();
        if (modes.isEmpty()) {
          ChannelModeSnapshot snapshot = channelModeSnapshot(sid, channel);
          modes = snapshot == null ? "" : Objects.toString(snapshot.rawModes(), "").trim();
        }
        normalized.add(
            new ManagedChannelRow(
                channel,
                row.detached(),
                row.autoReattach(),
                row.users(),
                row.notifications(),
                modes));
      }
    }

    managedRowsByServer.put(sid, normalized);
    managedSortModeByServer.put(sid, mode == null ? ManagedSortMode.CUSTOM : mode);
    if (sid.equals(this.serverId)) {
      refreshManagedRows();
    }
    refreshOpenDetailsDialog();
  }

  private void refreshCurrentServerViews() {
    refreshListRows();
    refreshManagedRows();
  }

  private void refreshListRows() {
    String sid = this.serverId;
    List<Row> rows =
        sid.isEmpty() ? List.of() : List.copyOf(rowsByServer.getOrDefault(sid, new ArrayList<>()));
    listModel.setRows(rows);
    updateListHeader();
    updateListButtons();
  }

  private void refreshManagedRows() {
    String sid = this.serverId;
    if (sid.isEmpty()) {
      syncingSortModeCombo = true;
      try {
        sortModeCombo.setSelectedItem(ManagedSortMode.CUSTOM);
      } finally {
        syncingSortModeCombo = false;
      }
      managedModel.setRows(List.of());
      updateManagedHeader();
      updateManagedButtons();
      return;
    }

    ManagedSortMode mode = managedSortModeByServer.getOrDefault(sid, ManagedSortMode.CUSTOM);
    List<ManagedChannelRow> rows =
        List.copyOf(managedRowsByServer.getOrDefault(sid, new ArrayList<>()));
    rows = mergeModeSnapshots(sid, rows);

    syncingSortModeCombo = true;
    try {
      sortModeCombo.setSelectedItem(mode);
    } finally {
      syncingSortModeCombo = false;
    }

    if (mode == ManagedSortMode.ALPHABETICAL) {
      ArrayList<ManagedChannelRow> sorted = new ArrayList<>(rows);
      sorted.sort((a, b) -> a.channel().compareToIgnoreCase(b.channel()));
      rows = List.copyOf(sorted);
    }
    managedModel.setRows(rows);

    updateManagedHeader();
    updateManagedButtons();
    notifyManagedChannelSelectionChanged();
  }

  private List<ManagedChannelRow> mergeModeSnapshots(String sid, List<ManagedChannelRow> rows) {
    if (rows == null || rows.isEmpty()) return List.of();
    if (sid == null || sid.isBlank()) return List.copyOf(rows);

    ArrayList<ManagedChannelRow> merged = new ArrayList<>(rows.size());
    for (ManagedChannelRow row : rows) {
      if (row == null) continue;
      String channel = normalizeChannel(row.channel());
      String modes = Objects.toString(row.modes(), "").trim();
      if (modes.isEmpty()) {
        ChannelModeSnapshot snapshot = channelModeSnapshot(sid, channel);
        modes = snapshot == null ? "" : Objects.toString(snapshot.rawModes(), "").trim();
      }
      merged.add(
          new ManagedChannelRow(
              row.channel(),
              row.detached(),
              row.autoReattach(),
              row.users(),
              row.notifications(),
              modes));
    }
    return List.copyOf(merged);
  }

  private void updateListHeader() {
    String sid = this.serverId;
    if (sid.isEmpty()) {
      listSubtitle.setText(ircListUxMode.defaultHint());
      return;
    }

    int totalCount = listModel.getRowCount();
    int visibleCount = listTable.getRowCount();
    String filter = Objects.toString(filterField.getText(), "").trim();
    boolean filtered = !filter.isEmpty();
    String status = Objects.toString(statusByServer.get(sid), "").trim();
    if (status.isEmpty()) {
      if (totalCount == 0) {
        listSubtitle.setText(defaultListHintForServer(sid));
      } else if (filtered) {
        listSubtitle.setText(
            message("channelList.summary.filtered", sid, visibleCount, totalCount));
      } else {
        listSubtitle.setText(message("channelList.summary.channels", sid, totalCount));
      }
      return;
    }
    if (totalCount > 0) {
      if (filtered) {
        listSubtitle.setText(
            message("channelList.summary.status.filtered", sid, status, visibleCount, totalCount));
      } else {
        listSubtitle.setText(
            message("channelList.summary.status.channels", sid, status, totalCount));
      }
    } else {
      listSubtitle.setText(message("channelList.summary.status", sid, status));
    }
  }

  private void updateManagedHeader() {
    String sid = this.serverId;
    if (sid.isEmpty()) {
      managedSubtitle.setText(message("channelList.managed.hint"));
      return;
    }
    int total = managedModel.getRowCount();
    int detached = managedModel.detachedCount();
    int attached = Math.max(0, total - detached);
    managedSubtitle.setText(message("channelList.managed.summary", sid, total, attached, detached));
  }

  private void applyListFilter() {
    String filter = Objects.toString(filterField.getText(), "").trim();
    if (filter.isEmpty()) {
      listSorter.setRowFilter(null);
      updateListHeader();
      return;
    }

    String[] terms = filter.toLowerCase(Locale.ROOT).split("\\s+");
    listSorter.setRowFilter(
        new RowFilter<>() {
          @Override
          public boolean include(Entry<? extends ChannelListTableModel, ? extends Integer> entry) {
            String channel =
                Objects.toString(entry.getStringValue(LIST_COL_CHANNEL), "")
                    .toLowerCase(Locale.ROOT);
            String users =
                Objects.toString(entry.getStringValue(LIST_COL_USERS), "").toLowerCase(Locale.ROOT);
            String topic =
                Objects.toString(entry.getStringValue(LIST_COL_TOPIC), "").toLowerCase(Locale.ROOT);
            for (String term : terms) {
              if (term == null || term.isBlank()) continue;
              if (channel.contains(term) || users.contains(term) || topic.contains(term)) continue;
              return false;
            }
            return true;
          }
        });
    updateListHeader();
  }

  private void runListRequested() {
    String sid = this.serverId;
    if (sid.isEmpty()) return;
    uxModeForServer(sid).runPrimaryAction(listUxContext, sid);
  }

  private boolean confirmFullListRequest() {
    Window owner = SwingUtilities.getWindowAncestor(this);
    int choice =
        JOptionPane.showConfirmDialog(
            owner,
            message("channelList.confirmFullList.message"),
            message("channelList.confirmFullList.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    return choice == JOptionPane.YES_OPTION;
  }

  private void runAlisRequested() {
    String sid = this.serverId;
    if (sid.isEmpty()) return;
    uxModeForServer(sid).runSecondaryAction(listUxContext, sid);
  }

  private void runMatrixNextPageRequested() {
    String sid = this.serverId;
    if (sid.isEmpty()) return;
    uxModeForServer(sid).runPagingAction(listUxContext, sid);
  }

  private void clearFilterText() {
    if (!filterField.getText().isBlank()) {
      filterField.setText("");
    }
  }

  private void clearCurrentServerListData() {
    String sid = normalizeServerId(this.serverId);
    if (sid.isEmpty()) return;
    rowsByServer.remove(sid);
    statusByServer.remove(sid);
    loadingByServer.remove(sid);
    requestTypeByServer.remove(sid);
    listTable.clearSelection();
    refreshListRows();
    refreshOpenDetailsDialog();
  }

  private void emitRunListRequest() {
    Runnable cb = onRunListRequest;
    if (cb != null) SwingUtilities.invokeLater(cb);
  }

  private void emitRunCommand(String command) {
    String cmd = Objects.toString(command, "").trim();
    if (cmd.isEmpty()) return;
    Consumer<String> cb = onRunAlisRequest;
    if (cb != null) SwingUtilities.invokeLater(() -> cb.accept(cmd));
  }

  static String buildAlisCommand(String query, boolean includeTopic) {
    return buildAlisCommand(query, AlisSearchOptions.defaults(includeTopic));
  }

  static String buildAlisCommand(String query, AlisSearchOptions options) {
    return IrcChannelListUxMode.buildAlisCommand(query, options);
  }

  static String buildMatrixListCommand(MatrixListOptions options) {
    return MatrixChannelListUxMode.buildMatrixListCommand(options);
  }

  private void addChannelRequested() {
    String sid = this.serverId;
    if (sid.isEmpty()) return;
    String channel =
        Objects.toString(
                JOptionPane.showInputDialog(
                    SwingUtilities.getWindowAncestor(this),
                    message("channelList.addChannel.prompt"),
                    message("channelList.addChannel.title"),
                    JOptionPane.PLAIN_MESSAGE),
                "")
            .trim();
    channel = normalizeChannel(channel);
    if (channel.isEmpty()) return;

    Consumer<String> cb = onAddChannelRequest;
    if (cb != null) cb.accept(channel);
  }

  private void connectDisconnectSelectedRequested() {
    int row = managedTable.getSelectedRow();
    if (row < 0) return;
    ManagedChannelRow current = managedModel.rowAt(row);
    if (current == null) return;
    String channel = normalizeChannel(current.channel());
    if (channel.isEmpty()) return;

    if (current.detached()) {
      Consumer<String> cb = onReconnectChannelRequest;
      if (cb != null) cb.accept(channel);
    } else {
      Consumer<String> cb = onDisconnectChannelRequest;
      if (cb != null) cb.accept(channel);
    }
  }

  private void closeSelectedRequested() {
    int row = managedTable.getSelectedRow();
    if (row < 0) return;
    ManagedChannelRow current = managedModel.rowAt(row);
    if (current == null) return;
    String channel = normalizeChannel(current.channel());
    if (channel.isEmpty()) return;

    int choice =
        JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            message("channelList.closeChannel.message", channel),
            message("channelList.closeChannel.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) return;

    Consumer<String> cb = onCloseChannelRequest;
    if (cb != null) cb.accept(channel);
  }

  private void moveSelectedBy(int delta) {
    if (currentManagedSortMode() != ManagedSortMode.CUSTOM) return;
    int row = managedTable.getSelectedRow();
    if (row < 0) return;
    int moved = managedModel.moveRelative(row, delta);
    if (moved < 0) return;
    notifyManagedCustomOrderChanged();
    managedTable.getSelectionModel().setSelectionInterval(moved, moved);
    managedTable.scrollRectToVisible(managedTable.getCellRect(moved, 0, true));
    updateManagedButtons();
  }

  private void onSortModeChangedByUser() {
    if (syncingSortModeCombo) return;
    ManagedSortMode mode = currentManagedSortMode();
    String sid = this.serverId;
    if (sid.isEmpty() || mode == null) return;

    managedSortModeByServer.put(sid, mode);
    if (mode == ManagedSortMode.ALPHABETICAL) {
      ArrayList<ManagedChannelRow> sorted = new ArrayList<>(managedModel.rowsSnapshot());
      sorted.sort((a, b) -> a.channel().compareToIgnoreCase(b.channel()));
      managedModel.setRows(sorted);
    } else {
      managedModel.setRows(managedRowsByServer.getOrDefault(sid, new ArrayList<>()));
    }

    Consumer<ManagedSortMode> cb = onManagedSortModeChanged;
    if (cb != null) cb.accept(mode);
    updateManagedButtons();
  }

  private void notifyManagedCustomOrderChanged() {
    if (currentManagedSortMode() != ManagedSortMode.CUSTOM) return;
    String sid = this.serverId;
    if (sid.isEmpty()) return;

    ArrayList<ManagedChannelRow> snapshot = new ArrayList<>(managedModel.rowsSnapshot());
    managedRowsByServer.put(sid, snapshot);

    ArrayList<String> order = new ArrayList<>(snapshot.size());
    for (ManagedChannelRow row : snapshot) {
      String channel = normalizeChannel(row.channel());
      if (channel.isEmpty()) continue;
      order.add(channel);
    }

    Consumer<List<String>> cb = onManagedCustomOrderChanged;
    if (cb != null) cb.accept(List.copyOf(order));
  }

  private ManagedSortMode currentManagedSortMode() {
    Object selected = sortModeCombo.getSelectedItem();
    if (selected instanceof ManagedSortMode mode) return mode;
    return ManagedSortMode.CUSTOM;
  }

  private String sortModeLabel(ManagedSortMode mode) {
    if (mode == null) return message("channelList.sort.manual");
    return switch (mode) {
      case ALPHABETICAL -> message("channelList.sort.alphabetical");
      case MOST_RECENT_ACTIVITY -> message("channelList.sort.mostRecentActivity");
      case MOST_UNREAD_MESSAGES -> message("channelList.sort.mostUnreadMessages");
      case MOST_UNREAD_NOTIFICATIONS -> message("channelList.sort.mostUnreadNotifications");
      case CUSTOM -> message("channelList.sort.manual");
    };
  }

  private void updateListButtons() {
    int row = listTable.getSelectedRow();
    boolean hasServer = !this.serverId.isBlank();
    boolean busy = isCurrentServerListLoading();
    ChannelListUxMode mode = uxModeForServer(this.serverId);
    String sid = normalizeServerId(this.serverId);

    applyListActionPresentation(mode.actionPresentation());
    listDetailsButton.setEnabled(row >= 0 && hasServer);
    clearListButton.setEnabled(hasServer && !busy && hasClearableListData(sid));
    runListButton.setEnabled(hasServer && !busy);
    runAlisButton.setEnabled(hasServer && !busy);
    runMatrixNextButton.setEnabled(hasServer && !busy && mode.isPagingActionEnabled(this.serverId));
    updateListBusyIndicator(busy);
  }

  private void updateListActionPresentation() {
    applyListActionPresentation(uxModeForServer(this.serverId).actionPresentation());
  }

  private void applyListActionPresentation(ChannelListUxMode.ActionPresentation presentation) {
    if (presentation == null) return;
    runListButton.setToolTipText(presentation.primaryTooltip());
    runListButton.getAccessibleContext().setAccessibleName(presentation.primaryAccessibleName());
    runAlisButton.setToolTipText(presentation.secondaryTooltip());
    runAlisButton.getAccessibleContext().setAccessibleName(presentation.secondaryAccessibleName());
    runMatrixNextButton.setVisible(presentation.pagingVisible());
    runMatrixNextButton.setToolTipText(presentation.pagingTooltip());
    runMatrixNextButton
        .getAccessibleContext()
        .setAccessibleName(presentation.pagingAccessibleName());
  }

  private String defaultListHintForServer(String sid) {
    String server = normalizeServerId(sid);
    if (server.isEmpty()) return ircListUxMode.defaultHint();
    return uxModeForServer(server).defaultHint();
  }

  private boolean isMatrixServer(String sid) {
    String serverId = normalizeServerId(sid);
    if (serverId.isEmpty()) return false;
    BackendUiContext context = currentBackendUiProfile().backendUiContext();
    if (context == null) return false;
    try {
      return context.isMatrixServer(serverId);
    } catch (Exception ignored) {
      return false;
    }
  }

  private ChannelListUxMode uxModeForServer(String sid) {
    return isMatrixServer(sid) ? matrixListUxMode : ircListUxMode;
  }

  private BackendUiProfile currentBackendUiProfile() {
    BackendUiProfile profile = backendUiProfile;
    return profile == null ? BackendUiProfile.ircOnly(serverId) : profile;
  }

  private final class ChannelListUxModeContext implements ChannelListUxMode.Context {
    @Override
    public Window ownerWindow() {
      return SwingUtilities.getWindowAncestor(ChannelListPanel.this);
    }

    @Override
    public boolean confirmFullListRequest() {
      return ChannelListPanel.this.confirmFullListRequest();
    }

    @Override
    public void clearFilterText() {
      ChannelListPanel.this.clearFilterText();
    }

    @Override
    public void rememberRequestType(String serverId, ChannelListRequestType requestType) {
      ChannelListPanel.this.rememberRequestType(serverId, requestType);
    }

    @Override
    public void beginList(String serverId, String banner) {
      ChannelListPanel.this.beginList(serverId, banner);
    }

    @Override
    public void emitRunListRequest() {
      ChannelListPanel.this.emitRunListRequest();
    }

    @Override
    public void emitRunCommand(String command) {
      ChannelListPanel.this.emitRunCommand(command);
    }

    @Override
    public void updateListButtons() {
      ChannelListPanel.this.updateListButtons();
    }
  }

  private void updateManagedButtons() {
    int row = managedTable.getSelectedRow();
    boolean hasSelection = row >= 0 && managedModel.rowAt(row) != null;
    ManagedChannelRow selected = hasSelection ? managedModel.rowAt(row) : null;

    if (selected != null && selected.detached()) {
      attachDetachButton.setIcon(SvgIcons.action("play", ACTION_ICON_SIZE));
      attachDetachButton.setDisabledIcon(SvgIcons.actionDisabled("play", ACTION_ICON_SIZE));
      attachDetachButton.setToolTipText(message("channelList.action.reconnect.tooltip"));
      attachDetachButton
          .getAccessibleContext()
          .setAccessibleName(message("channelList.action.reconnect.accessible"));
    } else {
      attachDetachButton.setIcon(SvgIcons.action("pause", ACTION_ICON_SIZE));
      attachDetachButton.setDisabledIcon(SvgIcons.actionDisabled("pause", ACTION_ICON_SIZE));
      attachDetachButton.setToolTipText(message("channelList.action.disconnect.tooltip"));
      attachDetachButton
          .getAccessibleContext()
          .setAccessibleName(message("channelList.action.disconnect.accessible"));
    }

    boolean customMode = currentManagedSortMode() == ManagedSortMode.CUSTOM;
    int total = managedModel.getRowCount();
    addChannelButton.setEnabled(!this.serverId.isBlank());
    attachDetachButton.setEnabled(hasSelection);
    closeChannelButton.setEnabled(hasSelection);
    managedDetailsButton.setEnabled(hasSelection);
    moveUpButton.setEnabled(customMode && hasSelection && row > 0);
    moveDownButton.setEnabled(customMode && hasSelection && row >= 0 && row < total - 1);
  }

  private void notifyManagedChannelSelectionChanged() {
    int row = managedTable.getSelectedRow();
    String channel = "";
    if (row >= 0) {
      ManagedChannelRow selected = managedModel.rowAt(row);
      if (selected != null) {
        channel = normalizeChannel(selected.channel());
      }
    }
    Consumer<String> cb = onManagedChannelSelected;
    if (cb != null && !channel.isEmpty()) cb.accept(channel);
  }

  private void showManagedDetailsForSelection() {
    int row = managedTable.getSelectedRow();
    if (row < 0) return;
    ManagedChannelRow selected = managedModel.rowAt(row);
    if (selected == null) return;

    String sid = this.serverId;
    if (sid.isBlank()) return;

    Row listRow = findListRowByChannel(sid, selected.channel());
    String listTopic = listRow == null ? "" : listRow.topic();
    String topic = topicSnapshotForChannel(sid, selected.channel(), listTopic);
    String modes = modeRawSnapshotForChannel(sid, selected.channel(), selected.modes());
    String modeSummary = modeSummarySnapshotForChannel(sid, selected.channel(), modes);

    ChannelDetails details =
        new ChannelDetails(
            sid,
            ChannelDetailsSource.MANAGED,
            selected.channel(),
            selected.detached()
                ? message("channelList.state.disconnected")
                : message("channelList.state.connected"),
            topic,
            modes,
            modeSummary,
            selected.detached() ? -1 : selected.users(),
            selected.notifications(),
            selected.autoReattach());
    showChannelDetailsDialog(details);
  }

  private void showServerListDetailsForSelection() {
    Row selected = selectedServerListRow();
    if (selected == null) return;

    String sid = this.serverId;
    if (sid.isBlank()) return;

    ManagedChannelRow managed = findManagedRowByChannel(sid, selected.channel());
    String state =
        managed == null
            ? message("channelList.state.notManaged")
            : (managed.detached()
                ? message("channelList.state.disconnected")
                : message("channelList.state.connected"));
    String modes =
        modeRawSnapshotForChannel(sid, selected.channel(), managed == null ? "" : managed.modes());
    String modeSummary = modeSummarySnapshotForChannel(sid, selected.channel(), modes);
    int notifications = managed == null ? 0 : managed.notifications();
    boolean autoReattach = managed != null && managed.autoReattach();

    ChannelDetails details =
        new ChannelDetails(
            sid,
            ChannelDetailsSource.SERVER_LIST,
            selected.channel(),
            state,
            topicSnapshotForChannel(sid, selected.channel(), selected.topic()),
            modes,
            modeSummary,
            selected.visibleUsers(),
            notifications,
            autoReattach);
    showChannelDetailsDialog(details);
  }

  private String topicSnapshotForChannel(String sid, String channel, String fallbackTopic) {
    BiFunction<String, String, String> cb = onChannelTopicRequest;
    if (cb != null) {
      try {
        String fromCallback = Objects.toString(cb.apply(sid, channel), "").trim();
        if (!fromCallback.isEmpty()) return fromCallback;
      } catch (Exception ignored) {
      }
    }
    Row row = findListRowByChannel(sid, channel);
    String fromList = row == null ? "" : Objects.toString(row.topic(), "").trim();
    if (!fromList.isEmpty()) return fromList;
    return Objects.toString(fallbackTopic, "").trim();
  }

  private BanListViewState banListViewStateForChannel(String sid, String channel) {
    BiFunction<String, String, BanListSnapshot> cb = onChannelBanListSnapshotRequest;
    if (cb == null) {
      return new BanListViewState(
          BanListSnapshot.empty(),
          message("channelList.banList.status.integrationUnavailable"),
          false);
    }
    try {
      BanListSnapshot snapshot = cb.apply(sid, channel);
      if (snapshot == null) snapshot = BanListSnapshot.empty();
      return new BanListViewState(
          snapshot, banListStatusText(snapshot), snapshot.hasEntries() || snapshot.hasSummary());
    } catch (Exception ignored) {
      return new BanListViewState(
          BanListSnapshot.empty(), message("channelList.banList.status.unavailable"), false);
    }
  }

  private String banListStatusText(BanListSnapshot snapshot) {
    if (snapshot == null) return message("channelList.banList.status.unavailable");
    if (snapshot.hasSummary()) return snapshot.summary();
    if (snapshot.hasEntries()) {
      int count = snapshot.entries().size();
      return count == 1
          ? message("channelList.banList.status.loadedOne")
          : message("channelList.banList.status.loadedMany", count);
    }
    return message("channelList.banList.status.empty");
  }

  private boolean hasCachedBanListRows(String sid, String channel) {
    return banListViewStateForChannel(sid, channel).snapshot().hasEntries();
  }

  private boolean requestBanListRefresh(String sid, String channel) {
    BiConsumer<String, String> cb = onChannelBanListRefreshRequest;
    if (cb == null) return false;
    try {
      cb.accept(sid, channel);
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  private void requestModeSnapshotRefresh(String sid, String channel) {
    BiConsumer<String, String> cb = onChannelModeRefreshRequest;
    if (cb == null) return;
    try {
      cb.accept(sid, channel);
    } catch (Exception ignored) {
    }
  }

  private void requestModeSet(String sid, String channel, String modeSpec) {
    ChannelModeCommandHandler cb = onChannelModeSetRequest;
    if (cb == null) return;
    try {
      cb.accept(sid, channel, modeSpec);
    } catch (Exception ignored) {
    }
  }

  private boolean canEditChannelModes(String sid, String channel) {
    BiPredicate<String, String> cb = canEditChannelModes;
    if (cb == null) return false;
    try {
      return cb.test(sid, channel);
    } catch (Exception ignored) {
      return false;
    }
  }

  private ChannelModeSnapshot channelModeSnapshot(String sid, String channel) {
    String serverId = normalizeServerId(sid);
    String ch = normalizeChannel(channel);
    if (serverId.isEmpty() || ch.isEmpty()) return null;
    Map<String, ChannelModeSnapshot> byChannel = channelModeSnapshotsByServer.get(serverId);
    if (byChannel == null || byChannel.isEmpty()) return null;
    return byChannel.get(ch.toLowerCase(Locale.ROOT));
  }

  private String modeRawSnapshotForChannel(String sid, String channel, String fallbackModes) {
    ChannelModeSnapshot snapshot = channelModeSnapshot(sid, channel);
    if (snapshot != null) {
      String raw = Objects.toString(snapshot.rawModes(), "").trim();
      if (!raw.isEmpty()) return raw;
    }
    String fallback = Objects.toString(fallbackModes, "").trim();
    if (fallback.isEmpty() || "(unknown)".equalsIgnoreCase(fallback)) return "(Unknown)";
    return fallback;
  }

  private String modeSummarySnapshotForChannel(
      String sid, String channel, String fallbackRawModes) {
    ChannelModeSnapshot snapshot = channelModeSnapshot(sid, channel);
    if (snapshot != null) {
      String summary = Objects.toString(snapshot.friendlySummary(), "").trim();
      if (!summary.isEmpty()) return summary;
    }
    return friendlyModeSummaryFromRaw(sid, fallbackRawModes);
  }

  private ManagedChannelRow findManagedRowByChannel(String sid, String channel) {
    String needle = normalizeChannel(channel);
    if (needle.isEmpty()) return null;
    List<ManagedChannelRow> rows = managedRowsByServer.getOrDefault(sid, new ArrayList<>());
    for (ManagedChannelRow row : rows) {
      if (row == null) continue;
      if (needle.equalsIgnoreCase(normalizeChannel(row.channel()))) return row;
    }
    return null;
  }

  private Row findListRowByChannel(String sid, String channel) {
    String needle = normalizeChannel(channel);
    if (needle.isEmpty()) return null;
    List<Row> rows = rowsByServer.getOrDefault(sid, new ArrayList<>());
    for (Row row : rows) {
      if (row == null) continue;
      if (needle.equalsIgnoreCase(normalizeChannel(row.channel()))) return row;
    }
    return null;
  }

  public void refreshOpenChannelDetails(String serverId, String channel) {
    ChannelDetailsDialogState state = channelDetailsDialog;
    if (state == null) return;
    String sid = normalizeServerId(serverId);
    String ch = normalizeChannel(channel);
    if (sid.isEmpty() || ch.isEmpty()) return;
    if (!sid.equals(state.serverId())) return;
    if (!ch.equalsIgnoreCase(state.channel())) return;
    refreshOpenDetailsDialog();
  }

  private void refreshOpenDetailsDialog() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::refreshOpenDetailsDialog);
      return;
    }

    ChannelDetailsDialogState state = channelDetailsDialog;
    if (state == null) return;
    if (!state.dialog().isDisplayable()) {
      // Newly-created modal dialogs are not displayable until setVisible(true) enters the nested
      // event loop. Keep the state so asynchronous updates can still land in the open dialog.
      return;
    }

    String sid = state.serverId();
    String channel = state.channel();
    ManagedChannelRow managed = findManagedRowByChannel(sid, channel);
    Row list = findListRowByChannel(sid, channel);

    String statusText =
        managed == null
            ? message("channelList.state.notManaged")
            : (managed.detached()
                ? message("channelList.state.disconnected")
                : message("channelList.state.connected"));
    String modesText =
        modeRawSnapshotForChannel(
            sid, channel, managed == null ? "" : displayManagedModes(managed));
    String modeSummaryText = modeSummarySnapshotForChannel(sid, channel, modesText);
    String notificationsText =
        managed == null ? "0" : String.valueOf(Math.max(0, managed.notifications()));
    String autoReattachText =
        managed == null
            ? message("channelList.state.notManaged")
            : (managed.autoReattach()
                ? message("channelList.details.value.enabled")
                : message("channelList.details.value.disabled"));
    String usersText;
    if (state.source() == ChannelDetailsSource.MANAGED) {
      if (managed == null) {
        usersText = message("channelList.details.value.notAvailable");
      } else {
        usersText =
            managed.detached()
                ? message("channelList.details.value.unavailableWhileDisconnected")
                : String.valueOf(managed.users());
      }
    } else if (list != null) {
      usersText = String.valueOf(Math.max(0, list.visibleUsers()));
    } else if (managed != null && !managed.detached()) {
      usersText = String.valueOf(managed.users());
    } else {
      usersText = message("channelList.details.value.notAvailable");
    }
    String topicText = topicSnapshotForChannel(sid, channel, list == null ? "" : list.topic());
    BanListViewState banListView = banListViewStateForChannel(sid, channel);

    setFieldText(state.stateField(), statusText);
    setFieldText(state.usersField(), usersText);
    setFieldText(state.notificationsField(), notificationsText);
    setFieldText(state.modesField(), modesText);
    setAreaText(state.modeSummaryArea(), modeSummaryText);
    setFieldText(state.autoReattachField(), autoReattachText);
    setAreaText(state.topicArea(), fallback(topicText, message("channelList.details.value.none")));
    if (banListView.hasResolvedSnapshot()) {
      state.pendingBanListRefresh().set(false);
      setBanListRows(state.banListTable(), banListView.snapshot().entries());
    } else if (!state.pendingBanListRefresh().get()) {
      setBanListRows(state.banListTable(), banListView.snapshot().entries());
    }
    String banListStatusText =
        state.pendingBanListRefresh().get() && !banListView.hasResolvedSnapshot()
            ? message("channelList.details.status.requestedBanListRefresh")
            : banListView.statusText();
    setAreaText(state.banListStatusArea(), banListStatusText);

    boolean canEditModes = canEditChannelModes(sid, channel);
    state.setModesButton().setEnabled(canEditModes);
    state
        .setModesButton()
        .setToolTipText(
            canEditModes
                ? message("channelList.details.button.setModes.tooltip.enabled")
                : message("channelList.details.editRequiresPrivileges"));
    updateBanManagementButtons(
        sid,
        channel,
        state.banListTable(),
        state.addBanButton(),
        state.editBanButton(),
        state.deleteBanButton());
  }

  private void showChannelDetailsDialog(ChannelDetails details) {
    if (details == null) return;

    ChannelDetailsDialogState existing = channelDetailsDialog;
    if (existing != null && existing.dialog().isDisplayable()) {
      existing.dialog().dispose();
    }

    String sid = normalizeServerId(details.serverId());
    String channel = normalizeChannel(details.channel());

    JTextField stateField = readOnlyField(details.state());
    JTextField usersField =
        readOnlyField(
            details.users() < 0
                ? message("channelList.details.value.unavailableWhileDisconnected")
                : String.valueOf(details.users()));
    JTextField notificationsField =
        readOnlyField(String.valueOf(Math.max(0, details.notifications())));
    JTextField modesField =
        readOnlyField(fallback(details.modes(), message("channelList.value.unknown")));
    JTextField autoReattachField =
        readOnlyField(
            details.autoReattach()
                ? message("channelList.details.value.enabled")
                : message("channelList.details.value.disabled"));
    JTextArea modeSummaryArea =
        readOnlyArea(
            fallback(details.modeSummary(), friendlyModeSummaryFromRaw(sid, details.modes())), 4);
    JTextArea topicArea =
        readOnlyArea(fallback(details.topic(), message("channelList.details.value.none")), 4);
    BanListViewState initialBanListView = banListViewStateForChannel(sid, channel);
    BanListTableModel banListTableModel =
        new BanListTableModel(
            message("channelList.details.banList.column.mask"),
            message("channelList.details.banList.column.setBy"),
            message("channelList.details.banList.column.setAt"));
    banListTableModel.setRows(initialBanListView.snapshot().entries());
    JTable banListTable = new JTable(banListTableModel);
    banListTable.setFillsViewportHeight(true);
    banListTable.setRowSelectionAllowed(true);
    banListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    banListTable.setAutoCreateRowSorter(true);
    banListTable.setShowHorizontalLines(false);
    banListTable.setShowVerticalLines(false);
    banListTable.getTableHeader().setReorderingAllowed(false);
    banListTable.getColumnModel().getColumn(0).setPreferredWidth(280);
    banListTable.getColumnModel().getColumn(1).setPreferredWidth(140);
    banListTable.getColumnModel().getColumn(2).setPreferredWidth(170);
    JTextArea banListStatusArea = readOnlyArea(initialBanListView.statusText(), 2);

    JScrollPane modeSummaryScroll = new JScrollPane(modeSummaryArea);
    JScrollPane topicScroll = new JScrollPane(topicArea);
    JScrollPane banListScroll = new JScrollPane(banListTable);
    modeSummaryScroll.setMinimumSize(new Dimension(180, 110));
    topicScroll.setMinimumSize(new Dimension(180, 120));
    banListScroll.setMinimumSize(new Dimension(180, 180));

    JButton refreshModesButton = new JButton(message("channelList.details.button.refreshModes"));
    refreshModesButton.setFocusable(false);
    refreshModesButton.addActionListener(
        e -> {
          requestModeSnapshotRefresh(sid, channel);
          setAreaText(
              modeSummaryArea, message("channelList.details.status.requestedMode", channel));
        });

    JButton setModesButton = new JButton(message("channelList.details.button.setModes"));
    setModesButton.setFocusable(false);
    setModesButton.addActionListener(
        e -> {
          String existingModes = Objects.toString(modesField.getText(), "").trim();
          String initial =
              message("channelList.value.unknown").equalsIgnoreCase(existingModes)
                  ? ""
                  : existingModes;
          Window owner = SwingUtilities.getWindowAncestor(this);
          String modeSpec =
              Objects.toString(
                      JOptionPane.showInputDialog(
                          owner, message("channelList.details.prompt.setModes"), initial),
                      "")
                  .trim();
          if (modeSpec.isEmpty()) return;
          requestModeSet(sid, channel, modeSpec);
          setAreaText(
              modeSummaryArea, message("channelList.details.status.sentMode", channel, modeSpec));
        });

    boolean canEditModes = canEditChannelModes(sid, channel);
    setModesButton.setEnabled(canEditModes);
    setModesButton.setToolTipText(
        canEditModes
            ? message("channelList.details.button.setModes.tooltip.enabled")
            : message("channelList.details.editRequiresPrivileges"));

    JButton refreshBanListButton = new JButton();
    configureActionButton(
        refreshBanListButton,
        "refresh",
        message("channelList.details.button.refreshBanList.tooltip"),
        message("channelList.details.button.refreshBanList.accessible"));
    refreshBanListButton.addActionListener(
        e -> requestBanListRefreshWithStatus(sid, channel, banListStatusArea));

    JButton addBanButton = new JButton();
    configureActionButton(
        addBanButton,
        "plus",
        message("channelList.details.button.addBan.tooltip"),
        message("channelList.details.button.addBan.accessible"));
    addBanButton.addActionListener(e -> promptAndAddBan(sid, channel, banListStatusArea));

    JButton editBanButton = new JButton();
    configureActionButton(
        editBanButton,
        "edit",
        message("channelList.details.button.editBan.tooltip"),
        message("channelList.details.button.editBan.accessible"));
    editBanButton.addActionListener(
        e -> promptAndEditBan(sid, channel, banListTable, banListStatusArea));

    JButton deleteBanButton = new JButton();
    configureActionButton(
        deleteBanButton,
        "trash",
        message("channelList.details.button.deleteBan.tooltip"),
        message("channelList.details.button.deleteBan.accessible"));
    deleteBanButton.addActionListener(
        e -> confirmAndDeleteBan(sid, channel, banListTable, banListStatusArea));

    banListTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (e.getValueIsAdjusting()) return;
              updateBanManagementButtons(
                  sid, channel, banListTable, addBanButton, editBanButton, deleteBanButton);
            });

    JPanel detailsTab =
        new JPanel(
            MigLayouts.fillWrap(
                12,
                6,
                "[right][grow,fill][right][grow,fill][right][grow,fill]",
                "[]8[]8[]8[]8[grow,fill]"));
    detailsTab.add(new JLabel(message("channelList.details.field.server")));
    detailsTab.add(readOnlyField(details.serverId()), MigConstraints.growX());
    detailsTab.add(new JLabel(message("channelList.details.field.channel")));
    detailsTab.add(readOnlyField(details.channel()), MigConstraints.spanXGrowX(3));

    detailsTab.add(new JLabel(message("channelList.details.field.source")));
    detailsTab.add(
        readOnlyField(
            details.source() == ChannelDetailsSource.MANAGED
                ? message("channelList.details.source.managed")
                : message("channelList.details.source.serverList")),
        MigConstraints.growX());
    detailsTab.add(new JLabel(message("channelList.details.field.state")));
    detailsTab.add(stateField, MigConstraints.growX());
    detailsTab.add(new JLabel(message("channelList.details.field.autoJoin")));
    detailsTab.add(autoReattachField, MigConstraints.growX());

    detailsTab.add(new JLabel(message("channelList.details.field.users")));
    detailsTab.add(usersField, MigConstraints.growX());
    detailsTab.add(new JLabel(message("channelList.details.field.notifications")));
    detailsTab.add(notificationsField, MigConstraints.growX());
    detailsTab.add(new JLabel(message("channelList.details.field.modes")));
    detailsTab.add(modesField, MigConstraints.growX());

    JPanel modeSummaryPanel = new JPanel(new BorderLayout(0, 6));
    modeSummaryPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    JPanel modeActions = new JPanel(MigLayouts.fillX("[][]push", "[]"));
    modeActions.add(refreshModesButton);
    modeActions.add(setModesButton);
    modeSummaryPanel.add(modeActions, BorderLayout.NORTH);
    modeSummaryPanel.add(modeSummaryScroll, BorderLayout.CENTER);

    JPanel topicPanel = new JPanel(new BorderLayout());
    topicPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    topicPanel.add(topicScroll, BorderLayout.CENTER);

    JPanel banPanel = new JPanel(new BorderLayout(0, 6));
    banPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    JPanel banActions = new JPanel(MigLayouts.fillX("[][][][]push", "[]"));
    banActions.add(addBanButton);
    banActions.add(editBanButton);
    banActions.add(deleteBanButton);
    banActions.add(refreshBanListButton);
    banPanel.add(banActions, BorderLayout.NORTH);
    banPanel.add(banListScroll, BorderLayout.CENTER);
    JPanel banStatusPanel = new JPanel(new BorderLayout(0, 4));
    banStatusPanel.add(new JLabel(message("channelList.details.field.status")), BorderLayout.NORTH);
    banStatusPanel.add(new JScrollPane(banListStatusArea), BorderLayout.CENTER);
    banPanel.add(banStatusPanel, BorderLayout.SOUTH);

    detailsTab.add(
        new JLabel(message("channelList.details.field.modeSummary")), MigConstraints.alignYTop());
    detailsTab.add(modeSummaryPanel, MigConstraints.spanXGrowXMinHeight(5, 140));

    detailsTab.add(
        new JLabel(message("channelList.details.field.topic")), MigConstraints.alignYTop());
    detailsTab.add(topicPanel, MigConstraints.spanXGrowPushYMinHeight(5, 160));

    JTabbedPane detailsTabs = new JTabbedPane();
    detailsTabs.addTab(message("channelList.details.tab.details"), detailsTab);
    detailsTabs.addTab(message("channelList.details.tab.banList"), banPanel);
    detailsTabs.setSelectedIndex(0);

    JButton closeButton = new JButton(message("common.button.close"));
    JPanel south = new JPanel(MigLayouts.fillX("0 12 12 12", "[grow, right]", "[]"));
    south.add(closeButton);

    JPanel root =
        new JPanel(
            MigLayouts.fillWrap(
                0, 1, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.GROW_FILL_TRAILING));
    root.add(detailsTabs, MigConstraints.growPush());
    root.add(south, MigConstraints.growX());

    Window owner = SwingUtilities.getWindowAncestor(this);
    JDialog dialog =
        new JDialog(
            owner,
            message("channelList.details.dialog.title", details.channel()),
            Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setContentPane(root);
    dialog.setSize(700, 520);
    dialog.setMinimumSize(new Dimension(620, 420));
    dialog.setLocationRelativeTo(owner);
    dialog.getRootPane().setDefaultButton(closeButton);
    closeButton.addActionListener(e -> dialog.dispose());

    channelDetailsDialog =
        new ChannelDetailsDialogState(
            dialog,
            sid,
            channel,
            details.source(),
            stateField,
            usersField,
            notificationsField,
            modesField,
            modeSummaryArea,
            setModesButton,
            autoReattachField,
            topicArea,
            banListTable,
            banListStatusArea,
            addBanButton,
            editBanButton,
            deleteBanButton,
            new AtomicBoolean(false));
    dialog.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosed(WindowEvent e) {
            if (channelDetailsDialog != null && channelDetailsDialog.dialog() == dialog) {
              channelDetailsDialog = null;
            }
          }
        });
    refreshOpenDetailsDialog();
    dialog.setVisible(true);
  }

  private static void setFieldText(JTextField field, String value) {
    if (field == null) return;
    String next = Objects.toString(value, "");
    if (!Objects.equals(field.getText(), next)) {
      field.setText(next);
    }
    field.setCaretPosition(0);
  }

  private static void setAreaText(JTextArea area, String value) {
    if (area == null) return;
    String next = Objects.toString(value, "");
    if (!Objects.equals(area.getText(), next)) {
      area.setText(next);
    }
    area.setCaretPosition(0);
  }

  private JTextField readOnlyField(String value) {
    JTextField field = new JTextField(fallback(value, message("channelList.value.unknown")));
    field.setEditable(false);
    field.setCaretPosition(0);
    return field;
  }

  private static JTextArea readOnlyArea(String value, int rows) {
    JTextArea area = new JTextArea(Objects.toString(value, ""));
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setRows(Math.max(1, rows));
    area.setCaretPosition(0);
    return area;
  }

  private static JTextArea createSubtitleArea(String text) {
    JTextArea area = new JTextArea(Objects.toString(text, ""));
    area.setRows(2);
    area.setEditable(false);
    area.setOpaque(false);
    area.setFocusable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setBorder(BorderFactory.createEmptyBorder());
    return area;
  }

  private static String fallback(String value, String fallback) {
    String v = Objects.toString(value, "").trim();
    return v.isEmpty() ? Objects.toString(fallback, "") : v;
  }

  private static void configureActionButton(
      JButton button, String iconName, String tooltip, String accessibleName) {
    button.setText("");
    button.setIcon(SvgIcons.action(iconName, ACTION_ICON_SIZE));
    button.setDisabledIcon(SvgIcons.actionDisabled(iconName, ACTION_ICON_SIZE));
    button.setToolTipText(tooltip);
    button.setFocusable(false);
    button.setPreferredSize(ACTION_BUTTON_SIZE);
    button.getAccessibleContext().setAccessibleName(accessibleName);
  }

  private String message(String code, Object... args) {
    return messages.text(code, args);
  }

  private static void repaintIfSized(JComponent component) {
    if (component == null) return;
    if (component.getWidth() <= 0 || component.getHeight() <= 0) return;
    component.repaint();
  }

  private static String normalizeServerId(String serverId) {
    return Objects.toString(serverId, "").trim();
  }

  private void configureListContextMenu() {
    listJoinSelectMenuItem.addActionListener(e -> joinSelectedServerListChannel());
    listShowDetailsMenuItem.setIcon(SvgIcons.action("eye", ACTION_ICON_SIZE));
    listShowDetailsMenuItem.addActionListener(e -> showServerListDetailsForSelection());
    listContextMenu.add(listJoinSelectMenuItem);
    listContextMenu.add(listShowDetailsMenuItem);
  }

  private void maybeShowListContextMenu(MouseEvent event) {
    if (event == null || !event.isPopupTrigger()) return;
    int viewRow = listTable.rowAtPoint(event.getPoint());
    if (viewRow < 0) return;
    listTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
    Row selected = selectedServerListRow();
    if (selected == null) return;
    prepareListContextMenuForChannel(selected.channel());
    listContextMenu.show(listTable, event.getX(), event.getY());
  }

  private void joinSelectedServerListChannel() {
    Row selected = selectedServerListRow();
    if (selected == null) return;
    triggerServerListChannelAction(selected.channel());
  }

  private void joinServerListChannelAtViewRow(int viewRow) {
    if (viewRow < 0) return;
    int modelRow = listTable.convertRowIndexToModel(viewRow);
    String channel = listModel.channelAt(modelRow);
    triggerServerListChannelAction(channel);
  }

  private void triggerServerListChannelAction(String channel) {
    String ch = normalizeChannel(channel);
    if (ch.isEmpty()) return;
    Consumer<String> cb = onJoinChannel;
    if (cb != null) cb.accept(ch);
  }

  private void prepareListContextMenuForChannel(String channel) {
    String ch = normalizeChannel(channel);
    ManagedChannelRow managed = findManagedRowByChannel(this.serverId, ch);
    if (managed != null && !managed.detached()) {
      listJoinSelectMenuItem.setText(message("channelList.menu.selectChannel"));
      listJoinSelectMenuItem.setIcon(SvgIcons.action("channel", ACTION_ICON_SIZE));
      listJoinSelectMenuItem.setToolTipText(message("channelList.menu.selectChannel.tooltip"));
      listJoinSelectMenuItem
          .getAccessibleContext()
          .setAccessibleName(message("channelList.menu.selectChannel.accessible"));
      return;
    }
    if (managed != null) {
      listJoinSelectMenuItem.setText(message("channelList.menu.reconnectChannel"));
      listJoinSelectMenuItem.setIcon(SvgIcons.action("play", ACTION_ICON_SIZE));
      listJoinSelectMenuItem.setToolTipText(message("channelList.menu.reconnectChannel.tooltip"));
      listJoinSelectMenuItem
          .getAccessibleContext()
          .setAccessibleName(message("channelList.menu.reconnectChannel.accessible"));
      return;
    }
    listJoinSelectMenuItem.setText(message("channelList.menu.joinChannel"));
    listJoinSelectMenuItem.setIcon(SvgIcons.action("play", ACTION_ICON_SIZE));
    listJoinSelectMenuItem.setToolTipText(message("channelList.menu.joinChannel.tooltip"));
    listJoinSelectMenuItem
        .getAccessibleContext()
        .setAccessibleName(message("channelList.menu.joinChannel.accessible"));
  }

  private Row selectedServerListRow() {
    int viewRow = listTable.getSelectedRow();
    if (viewRow < 0) return null;
    int modelRow = listTable.convertRowIndexToModel(viewRow);
    return listModel.rowAt(modelRow);
  }

  private boolean hasClearableListData(String serverId) {
    String sid = normalizeServerId(serverId);
    if (sid.isEmpty()) return false;
    if (!rowsByServer.getOrDefault(sid, new ArrayList<>()).isEmpty()) return true;
    return !Objects.toString(statusByServer.get(sid), "").trim().isEmpty();
  }

  private boolean isCurrentServerListLoading() {
    String sid = this.serverId;
    return !sid.isBlank() && Boolean.TRUE.equals(loadingByServer.get(sid));
  }

  private void updateListBusyIndicator(boolean busy) {
    String sid = normalizeServerId(this.serverId);
    ChannelListRequestType requestType =
        sid.isEmpty()
            ? ChannelListRequestType.UNKNOWN
            : requestTypeByServer.getOrDefault(sid, ChannelListRequestType.UNKNOWN);

    if (!busy) {
      listTable.setCursor(Cursor.getDefaultCursor());
      if ((requestType == ChannelListRequestType.ALIS
              || requestType == ChannelListRequestType.MATRIX_LIST)
          && alisActivityState == AlisActivityState.SPINNER) {
        startAlisConfirmedIndicator();
      } else if (alisActivityState != AlisActivityState.IDLE) {
        restoreAlisDefaultIcon();
      }
      if (!sid.isEmpty()) {
        requestTypeByServer.remove(sid);
      }
      return;
    }

    if (requestType == ChannelListRequestType.ALIS
        || requestType == ChannelListRequestType.MATRIX_LIST) {
      startAlisSpinnerIndicator();
    } else if (alisActivityState != AlisActivityState.IDLE) {
      restoreAlisDefaultIcon();
    }
    listTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  private void rememberRequestType(String sid, ChannelListRequestType type) {
    String serverId = normalizeServerId(sid);
    if (serverId.isEmpty() || type == null) return;
    requestTypeByServer.put(serverId, type);
  }

  private void startAlisSpinnerIndicator() {
    if (alisActivityState == AlisActivityState.SPINNER) return;
    alisActivityState = AlisActivityState.SPINNER;
    alisSpinnerAngleDeg = 0;
    applyAlisActivityIcon();
    if (!alisActivityTimer.isRunning()) {
      alisActivityTimer.start();
    }
  }

  private void startAlisConfirmedIndicator() {
    alisActivityState = AlisActivityState.CONFIRMED;
    alisConfirmedStartMs = System.currentTimeMillis();
    alisConfirmedAlpha = 1f;
    applyAlisActivityIcon();
    if (!alisActivityTimer.isRunning()) {
      alisActivityTimer.start();
    }
  }

  private void applyAlisActivityIcon() {
    runAlisButton.setIcon(runAlisActivityIcon);
    runAlisButton.setDisabledIcon(runAlisActivityIcon);
    repaintIfSized(runAlisButton);
  }

  private void onAlisActivityTick() {
    if (alisActivityState == AlisActivityState.SPINNER) {
      alisSpinnerAngleDeg += 18;
      if (alisSpinnerAngleDeg >= 360) {
        alisSpinnerAngleDeg -= 360;
      }
      repaintIfSized(runAlisButton);
      return;
    }

    if (alisActivityState == AlisActivityState.CONFIRMED) {
      long elapsed = Math.max(0L, System.currentTimeMillis() - alisConfirmedStartMs);
      if (elapsed <= 200L) {
        alisConfirmedAlpha = 1f;
      } else {
        float p = Math.min(1f, (float) (elapsed - 200L) / 900f);
        alisConfirmedAlpha = 1f - p;
        if (p >= 1f) {
          restoreAlisDefaultIcon();
          return;
        }
      }
      repaintIfSized(runAlisButton);
      return;
    }

    if (alisActivityTimer.isRunning()) {
      alisActivityTimer.stop();
    }
  }

  private void restoreAlisDefaultIcon() {
    alisActivityState = AlisActivityState.IDLE;
    alisSpinnerAngleDeg = 0;
    alisConfirmedStartMs = 0L;
    alisConfirmedAlpha = 1f;
    if (alisActivityTimer.isRunning()) {
      alisActivityTimer.stop();
    }
    runAlisButton.setIcon(runAlisDefaultIcon);
    runAlisButton.setDisabledIcon(runAlisDefaultDisabledIcon);
    repaintIfSized(runAlisButton);
  }

  @Override
  public void removeNotify() {
    restoreAlisDefaultIcon();
    super.removeNotify();
  }

  private static int normalizeMatrixListLimit(int limit) {
    int requested = limit <= 0 ? MATRIX_LIST_DEFAULT_LIMIT : limit;
    return Math.max(1, Math.min(requested, MATRIX_LIST_MAX_LIMIT));
  }

  private static String normalizeMatrixToken(String value) {
    return Objects.toString(value, "").trim();
  }

  private String normalizeBanner(String banner) {
    String b = Objects.toString(banner, "").trim();
    return b.isEmpty() ? message("channelList.status.loading") : b;
  }

  private static String normalizeChannel(String channel) {
    String c = Objects.toString(channel, "").trim();
    if (c.isEmpty()) return "";
    return (c.startsWith("#") || c.startsWith("&")) ? c : "";
  }

  private static String displayManagedUsers(ManagedChannelRow row) {
    if (row == null) return "";
    if (row.detached()) return "-";
    return String.valueOf(Math.max(0, row.users()));
  }

  private static String displayManagedNotifications(ManagedChannelRow row) {
    if (row == null) return "0";
    return String.valueOf(Math.max(0, row.notifications()));
  }

  private String displayManagedModes(ManagedChannelRow row) {
    if (row == null) return "";
    String modes = Objects.toString(row.modes(), "").trim();
    if (!modes.isEmpty()) return modes;
    return message("channelList.value.unknown");
  }

  private ModeVocabulary vocabularyForServer(String serverId) {
    Function<String, ModeVocabulary> provider = modeVocabularyProvider;
    if (provider == null) return ModeVocabulary.fallback();
    try {
      ModeVocabulary vocabulary = provider.apply(normalizeServerId(serverId));
      return vocabulary == null ? ModeVocabulary.fallback() : vocabulary;
    } catch (Exception ignored) {
      return ModeVocabulary.fallback();
    }
  }

  private String friendlyModeSummaryFromRaw(String serverId, String rawModes) {
    return friendlyModeSummaryFromRaw(vocabularyForServer(serverId), rawModes);
  }

  private String friendlyModeSummaryFromRaw(ModeVocabulary vocabulary, String rawModes) {
    String raw = Objects.toString(rawModes, "").trim();
    if (raw.isEmpty() || "(unknown)".equalsIgnoreCase(raw)) {
      return message("channelList.modeSummary.empty");
    }

    String[] toks = raw.split("\\s+");
    if (toks.length == 0) {
      return message("channelList.modeSummary.empty");
    }
    String modeSeq = toks[0];
    java.util.ArrayList<String> args = new java.util.ArrayList<>();
    for (int i = 1; i < toks.length; i++) {
      args.add(toks[i]);
    }

    java.util.ArrayList<String> lines = new java.util.ArrayList<>();
    int argIdx = 0;
    boolean adding = true;
    for (int i = 0; i < modeSeq.length(); i++) {
      char mode = modeSeq.charAt(i);
      if (mode == '+') {
        adding = true;
        continue;
      }
      if (mode == '-') {
        adding = false;
        continue;
      }
      String arg = null;
      if (modeTakesArg(vocabulary, mode, adding) && argIdx < args.size()) {
        arg = args.get(argIdx++);
      }
      lines.add(describeOneMode(vocabulary, mode, adding, arg));
    }
    if (lines.isEmpty()) {
      return message("channelList.modeSummary.empty");
    }
    return String.join("\n", lines);
  }

  private static boolean modeTakesArg(ModeVocabulary vocabulary, char mode, boolean adding) {
    return NegotiatedModeSemantics.takesArgument(vocabulary, mode, adding);
  }

  private String describeOneMode(ModeVocabulary vocabulary, char mode, boolean adding, String arg) {
    String sign = adding ? "+" : "-";
    String modeName = String.valueOf(mode);
    if (mode == 't') {
      return message(
          adding ? "channelList.modeSummary.topic.locked" : "channelList.modeSummary.topic.open");
    }
    if (mode == 'n') {
      return message(
          adding
              ? "channelList.modeSummary.noOutside.block"
              : "channelList.modeSummary.noOutside.allow");
    }
    if (mode == 'm') {
      return message(
          adding
              ? "channelList.modeSummary.moderated.enabled"
              : "channelList.modeSummary.moderated.disabled");
    }
    if (mode == 'i') {
      return message(
          adding
              ? "channelList.modeSummary.inviteOnly.enabled"
              : "channelList.modeSummary.inviteOnly.disabled");
    }
    if (mode == 's') {
      return message(
          adding
              ? "channelList.modeSummary.secret.enabled"
              : "channelList.modeSummary.secret.disabled");
    }
    if (mode == 'p') {
      return message(
          adding
              ? "channelList.modeSummary.private.enabled"
              : "channelList.modeSummary.private.disabled");
    }
    if (mode == 'r') {
      return message(
          adding
              ? "channelList.modeSummary.registeredOnly.enabled"
              : "channelList.modeSummary.registeredOnly.disabled");
    }
    if (mode == 'k') {
      return message(
          adding ? "channelList.modeSummary.key.set" : "channelList.modeSummary.key.removed");
    }
    if (mode == 'l') {
      return adding
          ? message(
              "channelList.modeSummary.limit.set",
              fallback(arg, message("channelList.modeSummary.fallback.set")))
          : message("channelList.modeSummary.limit.removed");
    }
    if (mode == 'b') {
      return adding
          ? message(
              "channelList.modeSummary.ban.add",
              fallback(arg, message("channelList.modeSummary.fallback.mask")))
          : message(
              "channelList.modeSummary.ban.remove",
              fallback(arg, message("channelList.modeSummary.fallback.mask")));
    }
    if (vocabulary.isExceptsMode(mode)) {
      return adding
          ? message(
              "channelList.modeSummary.banException.add",
              sign,
              modeName,
              fallback(arg, message("channelList.modeSummary.fallback.mask")))
          : message(
              "channelList.modeSummary.banException.remove",
              sign,
              modeName,
              fallback(arg, message("channelList.modeSummary.fallback.mask")));
    }
    if (vocabulary.isInvexMode(mode)) {
      return adding
          ? message(
              "channelList.modeSummary.inviteException.add",
              sign,
              modeName,
              fallback(arg, message("channelList.modeSummary.fallback.mask")))
          : message(
              "channelList.modeSummary.inviteException.remove",
              sign,
              modeName,
              fallback(arg, message("channelList.modeSummary.fallback.mask")));
    }

    boolean listMode = NegotiatedModeSemantics.isListMode(vocabulary, mode, arg);
    if (mode == 'q' && listMode) {
      return adding
          ? message(
              "channelList.modeSummary.quiet.add",
              fallback(arg, message("channelList.modeSummary.fallback.mask")))
          : message(
              "channelList.modeSummary.quiet.remove",
              fallback(arg, message("channelList.modeSummary.fallback.mask")));
    }

    if (NegotiatedModeSemantics.isStatusMode(vocabulary, mode, arg)) {
      String nick = fallback(arg, message("channelList.modeSummary.fallback.nick"));
      return switch (mode) {
        case 'q' ->
            message(
                adding
                    ? "channelList.modeSummary.status.owner.add"
                    : "channelList.modeSummary.status.owner.remove",
                nick);
        case 'o' ->
            message(
                adding
                    ? "channelList.modeSummary.status.operator.add"
                    : "channelList.modeSummary.status.operator.remove",
                nick);
        case 'h' ->
            message(
                adding
                    ? "channelList.modeSummary.status.halfOperator.add"
                    : "channelList.modeSummary.status.halfOperator.remove",
                nick);
        case 'a' ->
            message(
                adding
                    ? "channelList.modeSummary.status.admin.add"
                    : "channelList.modeSummary.status.admin.remove",
                nick);
        case 'v' ->
            message(
                adding
                    ? "channelList.modeSummary.status.voice.add"
                    : "channelList.modeSummary.status.voice.remove",
                nick);
        default ->
            message(
                adding
                    ? "channelList.modeSummary.status.generic.add"
                    : "channelList.modeSummary.status.generic.remove",
                sign,
                modeName,
                nick);
      };
    }

    if (listMode) {
      return adding
          ? message(
              "channelList.modeSummary.list.add",
              sign,
              modeName,
              fallback(arg, message("channelList.modeSummary.fallback.mask")))
          : message(
              "channelList.modeSummary.list.remove",
              sign,
              modeName,
              fallback(arg, message("channelList.modeSummary.fallback.mask")));
    }

    if (NegotiatedModeSemantics.takesArgument(vocabulary, mode, adding)) {
      if (adding) {
        return message(
            "channelList.modeSummary.networkSpecific.withArg",
            sign,
            modeName,
            fallback(arg, message("channelList.modeSummary.fallback.set")));
      }
      return arg == null || arg.isBlank()
          ? message("channelList.modeSummary.networkSpecific.removed", sign, modeName)
          : message("channelList.modeSummary.networkSpecific.removedWithArg", sign, modeName, arg);
    }
    return message("channelList.modeSummary.networkSpecific.noArg", sign, modeName);
  }

  private final class AlisActivityIcon implements Icon {
    @Override
    public int getIconWidth() {
      return ACTION_ICON_SIZE;
    }

    @Override
    public int getIconHeight() {
      return ACTION_ICON_SIZE;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int size = Math.max(8, ACTION_ICON_SIZE - 4);
        int px = x + (ACTION_ICON_SIZE - size) / 2;
        int py = y + (ACTION_ICON_SIZE - size) / 2;

        if (alisActivityState == AlisActivityState.SPINNER) {
          Color spinnerColor =
              c != null && c.getForeground() != null ? c.getForeground() : Color.GRAY;
          g2.setColor(spinnerColor);
          g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
          g2.drawArc(px, py, size, size, alisSpinnerAngleDeg, 270);
          return;
        }

        if (alisActivityState == AlisActivityState.CONFIRMED) {
          g2.setComposite(
              AlphaComposite.getInstance(
                  AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alisConfirmedAlpha))));
          g2.setColor(new Color(0x2ecc71));
          g2.fillOval(px, py, size, size);
        }
      } finally {
        g2.dispose();
      }
    }
  }

  private static final class ChannelListTableModel extends AbstractTableModel {

    private final String[] columns;

    ChannelListTableModel(String channelColumn, String usersColumn, String topicColumn) {
      this.columns = new String[] {channelColumn, usersColumn, topicColumn};
    }

    private final ArrayList<Row> rows = new ArrayList<>();

    void setRows(List<Row> rows) {
      this.rows.clear();
      if (rows != null && !rows.isEmpty()) this.rows.addAll(rows);
      fireTableDataChanged();
    }

    void addRow(Row row) {
      if (row == null) return;
      int idx = rows.size();
      rows.add(row);
      fireTableRowsInserted(idx, idx);
    }

    void addRows(List<Row> rows) {
      if (rows == null || rows.isEmpty()) return;
      int from = this.rows.size();
      for (Row row : rows) {
        if (row == null) continue;
        this.rows.add(row);
      }
      int to = this.rows.size() - 1;
      if (to >= from) {
        fireTableRowsInserted(from, to);
      }
    }

    String channelAt(int row) {
      if (row < 0 || row >= rows.size()) return "";
      Row r = rows.get(row);
      return r == null ? "" : r.channel();
    }

    Row rowAt(int row) {
      if (row < 0 || row >= rows.size()) return null;
      return rows.get(row);
    }

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public int getColumnCount() {
      return columns.length;
    }

    @Override
    public String getColumnName(int column) {
      return (column >= 0 && column < columns.length) ? columns[column] : "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return columnIndex == LIST_COL_USERS ? Integer.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < 0 || rowIndex >= rows.size()) return "";
      Row row = rows.get(rowIndex);
      if (row == null) return "";
      return switch (columnIndex) {
        case LIST_COL_CHANNEL -> row.channel();
        case LIST_COL_USERS -> row.visibleUsers();
        case LIST_COL_TOPIC -> row.topic();
        default -> "";
      };
    }
  }

  private static final class BanListTableModel extends AbstractTableModel {
    private final String[] columns;
    private final ArrayList<BanListEntryRow> rows = new ArrayList<>();

    BanListTableModel(String maskColumn, String setByColumn, String setAtColumn) {
      this.columns = new String[] {maskColumn, setByColumn, setAtColumn};
    }

    void setRows(List<BanListEntryRow> rows) {
      this.rows.clear();
      if (rows != null && !rows.isEmpty()) this.rows.addAll(rows);
      fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public int getColumnCount() {
      return columns.length;
    }

    @Override
    public String getColumnName(int column) {
      return (column >= 0 && column < columns.length) ? columns[column] : "";
    }

    BanListEntryRow rowAt(int rowIndex) {
      if (rowIndex < 0 || rowIndex >= rows.size()) return null;
      return rows.get(rowIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < 0 || rowIndex >= rows.size()) return "";
      BanListEntryRow row = rows.get(rowIndex);
      if (row == null) return "";
      return switch (columnIndex) {
        case 0 -> row.mask();
        case 1 -> row.setBy();
        case 2 -> row.setAt();
        default -> "";
      };
    }
  }

  private final class ManagedChannelTableModel extends AbstractTableModel {
    private final String[] columns = {
      message("channelList.column.channel"),
      message("channelList.column.state"),
      message("channelList.column.users"),
      message("channelList.column.notifications"),
      message("channelList.column.modes"),
      message("channelList.column.autoJoin")
    };

    private final ArrayList<ManagedChannelRow> rows = new ArrayList<>();
    private BiConsumer<String, Boolean> onAutoReattachChanged;

    void setRows(List<ManagedChannelRow> rows) {
      this.rows.clear();
      if (rows != null && !rows.isEmpty()) this.rows.addAll(rows);
      fireTableDataChanged();
    }

    void setOnAutoReattachChanged(BiConsumer<String, Boolean> onAutoReattachChanged) {
      this.onAutoReattachChanged = onAutoReattachChanged;
    }

    List<ManagedChannelRow> rowsSnapshot() {
      return List.copyOf(rows);
    }

    int detachedCount() {
      int count = 0;
      for (ManagedChannelRow row : rows) {
        if (row != null && row.detached()) count++;
      }
      return count;
    }

    ManagedChannelRow rowAt(int row) {
      if (row < 0 || row >= rows.size()) return null;
      return rows.get(row);
    }

    int moveRelative(int row, int delta) {
      int next = row + delta;
      if (row < 0 || row >= rows.size()) return -1;
      if (next < 0 || next >= rows.size()) return -1;
      ManagedChannelRow moving = rows.remove(row);
      rows.add(next, moving);
      fireTableDataChanged();
      return next;
    }

    int moveRow(int fromIndex, int insertBefore) {
      if (fromIndex < 0 || fromIndex >= rows.size()) return -1;
      int boundedInsert = Math.max(0, Math.min(insertBefore, rows.size()));
      ManagedChannelRow moving = rows.remove(fromIndex);
      if (boundedInsert > fromIndex) boundedInsert--;
      boundedInsert = Math.max(0, Math.min(boundedInsert, rows.size()));
      rows.add(boundedInsert, moving);
      fireTableDataChanged();
      return boundedInsert;
    }

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public int getColumnCount() {
      return columns.length;
    }

    @Override
    public String getColumnName(int column) {
      return (column >= 0 && column < columns.length) ? columns[column] : "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return columnIndex == MANAGED_COL_AUTO_REATTACH ? Boolean.class : String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex == MANAGED_COL_AUTO_REATTACH;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < 0 || rowIndex >= rows.size()) return "";
      ManagedChannelRow row = rows.get(rowIndex);
      if (row == null) return "";
      return switch (columnIndex) {
        case MANAGED_COL_CHANNEL -> row.channel();
        case MANAGED_COL_STATE ->
            row.detached()
                ? message("channelList.state.disconnected")
                : message("channelList.state.connected");
        case MANAGED_COL_USERS -> displayManagedUsers(row);
        case MANAGED_COL_NOTIFICATIONS -> displayManagedNotifications(row);
        case MANAGED_COL_MODES -> displayManagedModes(row);
        case MANAGED_COL_AUTO_REATTACH -> row.autoReattach();
        default -> "";
      };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (columnIndex != MANAGED_COL_AUTO_REATTACH) return;
      if (rowIndex < 0 || rowIndex >= rows.size()) return;
      ManagedChannelRow before = rows.get(rowIndex);
      if (before == null) return;
      boolean nextValue = Boolean.TRUE.equals(aValue);
      if (before.autoReattach() == nextValue) return;

      ManagedChannelRow next =
          new ManagedChannelRow(
              before.channel(),
              before.detached(),
              nextValue,
              before.users(),
              before.notifications(),
              before.modes());
      rows.set(rowIndex, next);
      fireTableCellUpdated(rowIndex, columnIndex);

      BiConsumer<String, Boolean> cb = onAutoReattachChanged;
      if (cb != null) cb.accept(before.channel(), nextValue);
    }
  }

  private record ChannelDetailsDialogState(
      JDialog dialog,
      String serverId,
      String channel,
      ChannelDetailsSource source,
      JTextField stateField,
      JTextField usersField,
      JTextField notificationsField,
      JTextField modesField,
      JTextArea modeSummaryArea,
      JButton setModesButton,
      JTextField autoReattachField,
      JTextArea topicArea,
      JTable banListTable,
      JTextArea banListStatusArea,
      JButton addBanButton,
      JButton editBanButton,
      JButton deleteBanButton,
      AtomicBoolean pendingBanListRefresh) {}

  private record BanListViewState(
      BanListSnapshot snapshot, String statusText, boolean hasResolvedSnapshot) {}

  private record ChannelModeSnapshot(String rawModes, String friendlySummary) {}

  private record Row(String channel, int visibleUsers, String topic) {}

  private static void setBanListRows(JTable table, List<BanListEntryRow> rows) {
    if (table == null) return;
    if (table.getModel() instanceof BanListTableModel model) {
      model.setRows(rows);
    }
  }

  private void updateBanManagementButtons(
      String sid,
      String channel,
      JTable banListTable,
      JButton addBanButton,
      JButton editBanButton,
      JButton deleteBanButton) {
    boolean canManageBans = canEditChannelModes(sid, channel);
    BanListEntryRow selected = selectedBanListEntry(banListTable);
    boolean hasSelection = selected != null;
    String denied = message("channelList.details.editRequiresPrivileges");

    if (addBanButton != null) {
      addBanButton.setEnabled(canManageBans);
      addBanButton.setToolTipText(
          canManageBans ? message("channelList.details.button.addBan.tooltip") : denied);
    }
    if (editBanButton != null) {
      editBanButton.setEnabled(canManageBans && hasSelection);
      editBanButton.setToolTipText(
          !canManageBans
              ? denied
              : (hasSelection
                  ? message("channelList.details.button.editBan.tooltip")
                  : message("channelList.details.button.editBan.tooltip.selectEntry")));
    }
    if (deleteBanButton != null) {
      deleteBanButton.setEnabled(canManageBans && hasSelection);
      deleteBanButton.setToolTipText(
          !canManageBans
              ? denied
              : (hasSelection
                  ? message("channelList.details.button.deleteBan.tooltip")
                  : message("channelList.details.button.deleteBan.tooltip.selectEntry")));
    }
  }

  private BanListEntryRow selectedBanListEntry(JTable banListTable) {
    if (banListTable == null) return null;
    int viewRow = banListTable.getSelectedRow();
    if (viewRow < 0) return null;
    int modelRow = banListTable.convertRowIndexToModel(viewRow);
    if (banListTable.getModel() instanceof BanListTableModel model) {
      return model.rowAt(modelRow);
    }
    return null;
  }

  private void requestBanListRefreshWithStatus(String sid, String channel, JTextArea statusArea) {
    setPendingBanListRefresh(sid, channel, true);
    if (requestBanListRefresh(sid, channel)) {
      setAreaText(statusArea, message("channelList.details.status.requestedBanListRefresh"));
      return;
    }
    setPendingBanListRefresh(sid, channel, false);
    setAreaText(statusArea, message("channelList.details.status.banListRefreshUnavailable"));
  }

  private void promptAndAddBan(String sid, String channel, JTextArea statusArea) {
    if (!canEditChannelModes(sid, channel) || GraphicsEnvironment.isHeadless()) return;
    Window owner = SwingUtilities.getWindowAncestor(this);
    String mask =
        Objects.toString(
                JOptionPane.showInputDialog(
                    owner, message("channelList.details.prompt.addBan", channel), ""),
                "")
            .trim();
    if (mask.isEmpty()) return;
    requestModeSet(sid, channel, "+b " + mask);
    requestBanListRefreshWithStatus(sid, channel, statusArea);
  }

  private void promptAndEditBan(
      String sid, String channel, JTable banListTable, JTextArea statusArea) {
    if (!canEditChannelModes(sid, channel) || GraphicsEnvironment.isHeadless()) return;
    BanListEntryRow selected = selectedBanListEntry(banListTable);
    if (selected == null) return;
    Window owner = SwingUtilities.getWindowAncestor(this);
    String nextMask =
        Objects.toString(
                JOptionPane.showInputDialog(
                    owner, message("channelList.details.prompt.editBan", channel), selected.mask()),
                "")
            .trim();
    if (nextMask.isEmpty() || nextMask.equals(selected.mask())) return;
    requestModeSet(sid, channel, "-b " + selected.mask());
    requestModeSet(sid, channel, "+b " + nextMask);
    requestBanListRefreshWithStatus(sid, channel, statusArea);
  }

  private void confirmAndDeleteBan(
      String sid, String channel, JTable banListTable, JTextArea statusArea) {
    if (!canEditChannelModes(sid, channel) || GraphicsEnvironment.isHeadless()) return;
    BanListEntryRow selected = selectedBanListEntry(banListTable);
    if (selected == null) return;
    Window owner = SwingUtilities.getWindowAncestor(this);
    int choice =
        JOptionPane.showConfirmDialog(
            owner,
            message("channelList.details.confirm.deleteBan.message", selected.mask(), channel),
            message("channelList.details.confirm.deleteBan.title"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (choice != JOptionPane.OK_OPTION) return;
    requestModeSet(sid, channel, "-b " + selected.mask());
    requestBanListRefreshWithStatus(sid, channel, statusArea);
  }

  private void setPendingBanListRefresh(String sid, String channel, boolean pending) {
    ChannelDetailsDialogState state = channelDetailsDialog;
    if (state == null) return;
    if (!Objects.equals(state.serverId(), sid)) return;
    if (!Objects.equals(normalizeChannel(state.channel()), normalizeChannel(channel))) return;
    state.pendingBanListRefresh().set(pending);
  }
}
