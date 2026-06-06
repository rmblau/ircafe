package cafe.woden.ircclient.ui.interceptors;

import cafe.woden.ircclient.app.api.InterceptorEventType;
import cafe.woden.ircclient.interceptors.InterceptorHit;
import cafe.woden.ircclient.interceptors.InterceptorScope;
import cafe.woden.ircclient.interceptors.InterceptorStore;
import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.model.InterceptorDefinition;
import cafe.woden.ircclient.model.InterceptorRule;
import cafe.woden.ircclient.model.InterceptorRuleMode;
import cafe.woden.ircclient.model.TargetRef;
import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import cafe.woden.ircclient.ui.util.PopupMenuThemeSupport;
import cafe.woden.ircclient.ui.util.SoundFileChooserSupport;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import cafe.woden.ircclient.ui.util.UiFontKeys;
import cafe.woden.ircclient.util.VirtualThreads;
import com.formdev.flatlaf.FlatClientProperties;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

/** Editor/view for a single interceptor node. */
public final class InterceptorPanel extends JPanel implements AutoCloseable {
  private static final DateTimeFormatter TIME_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private static final String RULE_ANY_EVENT_KEY = "interceptors.rule.event.anyMessage";
  private static final InterceptorRuleMode[] CHANNEL_FILTER_MODES = {
    InterceptorRuleMode.ALL,
    InterceptorRuleMode.NONE,
    InterceptorRuleMode.LIKE,
    InterceptorRuleMode.GLOB,
    InterceptorRuleMode.REGEX
  };

  private static final InterceptorRuleMode[] RULE_DIMENSION_MODES_WITH_ANY = {
    InterceptorRuleMode.ALL,
    InterceptorRuleMode.LIKE,
    InterceptorRuleMode.GLOB,
    InterceptorRuleMode.REGEX
  };

  private final InterceptorStore store;
  private final CompositeDisposable disposables = new CompositeDisposable();
  private final ExecutorService refreshExecutor;
  private final boolean ownsRefreshExecutor;
  private final AtomicLong refreshSeq = new AtomicLong(0L);
  private final AtomicLong pendingLocalDefinitionRefreshSkips = new AtomicLong(0L);

  private final JLabel title = new JLabel(message("interceptors.title.single"));
  private final JLabel subtitle = new JLabel(message("interceptors.subtitle.selectNode"));
  private final JLabel status = new JLabel(" ");

  private final JTextField interceptorName = new JTextField();
  private final JCheckBox enabled = new JCheckBox(message("interceptors.field.enabled"));
  private final JComboBox<ServerScopeOption> serverScope =
      new JComboBox<>(
          new ServerScopeOption[] {ServerScopeOption.THIS_SERVER, ServerScopeOption.ANY_SERVER});

  private final JComboBox<InterceptorRuleMode> includeMode =
      new JComboBox<>(CHANNEL_FILTER_MODES.clone());
  private final JTextField includes = new JTextField();
  private final JComboBox<InterceptorRuleMode> excludeMode =
      new JComboBox<>(CHANNEL_FILTER_MODES.clone());
  private final JTextField excludes = new JTextField();

  private final JCheckBox actionStatusBarEnabled = new JCheckBox(message("interceptors.action.statusBar"));
  private final JCheckBox actionToastEnabled = new JCheckBox(message("interceptors.action.desktopToast"));
  private final JCheckBox actionSoundEnabled = new JCheckBox(message("interceptors.action.playSound"));
  private final JComboBox<BuiltInSound> actionSoundId = new JComboBox<>(BuiltInSound.valuesForUi());
  private final JCheckBox actionSoundUseCustom = new JCheckBox(message("interceptors.action.customFile"));
  private final JTextField actionSoundCustomPath = new JTextField();
  private final JButton browseSoundCustomPath = new JButton(message("common.button.browse.ellipsis"));
  private final JButton testSound = new JButton(message("interceptors.button.testSound"));

  private final JCheckBox actionScriptEnabled = new JCheckBox(message("interceptors.action.runScript"));
  private final JTextField actionScriptPath = new JTextField();
  private final JTextField actionScriptArgs = new JTextField();
  private final JTextField actionScriptWorkingDirectory = new JTextField();
  private final JButton browseScriptPath = new JButton(message("common.button.browse.ellipsis"));
  private final JButton browseScriptWorkingDirectory = new JButton(message("common.button.browse.ellipsis"));

  private final JButton addRule = new JButton(message("common.button.add.ellipsis"));
  private final JButton editRule = new JButton(message("common.button.edit.ellipsis"));
  private final JButton removeRule = new JButton(message("common.button.remove"));
  private final JPopupMenu rulesPopupMenu = new JPopupMenu();
  private final JMenuItem rulesPopupEdit = new JMenuItem(message("common.button.edit.ellipsis"));
  private final JMenuItem rulesPopupRemove = new JMenuItem(message("common.button.delete.ellipsis"));
  private final JButton clearSelectedHits = new JButton(message("interceptors.button.clearSelected"));
  private final JButton clearHits = new JButton(message("common.button.clear"));
  private final JButton exportHitsCsv = new JButton(message("interceptors.button.exportCsv"));
  private final JPopupMenu hitsPopupMenu = new JPopupMenu();
  private final JMenuItem hitsPopupJumpToMessage = new JMenuItem(message("interceptors.hits.action.jumpToMessage"));
  private final JMenuItem hitsPopupClearSelected = new JMenuItem(message("interceptors.hits.action.clearSelected"));
  private final JMenuItem hitsPopupClearAll = new JMenuItem(message("interceptors.hits.action.clearAll"));
  private final JMenuItem hitsPopupExportSelectedCsv =
      new JMenuItem(message("interceptors.hits.action.exportSelectedCsv"));
  private final JMenuItem hitsPopupExportAllCsv = new JMenuItem(message("interceptors.hits.action.exportAllCsv"));
  private final JButton createInterceptorButton = new JButton(message("interceptors.button.create"));

  private final RulesTableModel rulesModel = new RulesTableModel();
  private final JTable rulesTable = new JTable(rulesModel);
  private final HitsTableModel hitsModel = new HitsTableModel();
  private final JTable hitsTable = new JTable(hitsModel);
  private final JTabbedPane tabs = new JTabbedPane();
  private final JPanel centerPanel = new JPanel(new CardLayout());
  private final JPanel emptyStatePanel =
      new JPanel(
          MigLayouts.fillWrapWithHideMode(20, 1, 3, "[grow,center]", "[grow]8[]8[]12[][grow]"));
  private final JLabel emptyStateTitle = new JLabel(message("interceptors.title.overview"));
  private final JLabel emptyStateBody = new JLabel();
  private static final String CENTER_CARD_EDITOR = "editor";
  private static final String CENTER_CARD_EMPTY = "empty";

  private volatile String serverId = "";
  private volatile String interceptorId = "";
  private boolean loading = false;
  private boolean controlsEnabled = false;
  private Consumer<TargetRef> onSelectTarget;
  private BiConsumer<TargetRef, String> onJumpToMessage = (ref, messageId) -> {};
  private Runnable onLocalDefinitionNameChanged = () -> {};
  private boolean hasExternalStoreChangeRefreshConsumer = false;

  private static String message(String code, Object... args) {
    return MESSAGES.text(code, args);
  }

  public InterceptorPanel(InterceptorStore store) {
    this(store, VirtualThreads.newSingleThreadExecutor("ircafe-interceptor-panel-refresh"), true);
  }

  public InterceptorPanel(InterceptorStore store, ExecutorService refreshExecutor) {
    this(store, refreshExecutor, false);
  }

  private InterceptorPanel(
      InterceptorStore store, ExecutorService refreshExecutor, boolean ownsRefreshExecutor) {
    super(new BorderLayout());
    this.store = Objects.requireNonNull(store, "store");
    this.refreshExecutor = Objects.requireNonNull(refreshExecutor, "refreshExecutor");
    if (this.refreshExecutor.isShutdown()) {
      throw new IllegalArgumentException("refreshExecutor must be active");
    }
    this.ownsRefreshExecutor = ownsRefreshExecutor;

    buildHeader();
    buildBody();
    applyDerivedFonts();
    installListeners();
    createInterceptorButton.setVisible(false);
    setControlsEnabled(false);

    disposables.add(
        store
            .changes()
            .subscribe(
                ch -> {
                  String sid = serverId;
                  String iid = interceptorId;
                  if (sid.isBlank() || iid.isBlank()) return;
                  if (!sid.equals(ch.serverId())) return;
                  if (!iid.equals(ch.interceptorId())) return;
                  if (consumeLocalDefinitionStoreChangeRefreshSkip()) return;
                  SwingUtilities.invokeLater(this::refreshFromStore);
                },
                err -> {
                  // Keep UI alive even if the update stream fails.
                }));
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (title == null || emptyStateTitle == null) return;
    SwingUtilities.invokeLater(this::applyDerivedFonts);
  }

  public void setInterceptorTarget(String serverId, String interceptorId) {
    setInterceptorTarget(serverId, "", interceptorId);
  }

  public void setInterceptorTarget(String serverId, String networkToken, String interceptorId) {
    this.serverId = InterceptorScope.scopedServerId(serverId, networkToken);
    this.interceptorId = Objects.toString(interceptorId, "").trim();
    refreshFromStore();
  }

  public void setOnSelectTarget(Consumer<TargetRef> onSelectTarget) {
    this.onSelectTarget = onSelectTarget;
  }

  public void setOnJumpToMessage(BiConsumer<TargetRef, String> onJumpToMessage) {
    this.onJumpToMessage = onJumpToMessage == null ? (ref, messageId) -> {} : onJumpToMessage;
  }

  public void setOnLocalDefinitionNameChanged(Runnable onLocalDefinitionNameChanged) {
    this.onLocalDefinitionNameChanged =
        onLocalDefinitionNameChanged == null ? () -> {} : onLocalDefinitionNameChanged;
    if (onLocalDefinitionNameChanged != null) {
      hasExternalStoreChangeRefreshConsumer = true;
    }
  }

  @Override
  public void close() {
    refreshSeq.incrementAndGet();
    disposables.dispose();
    if (ownsRefreshExecutor) {
      refreshExecutor.shutdownNow();
    }
  }

  private void buildHeader() {
    JPanel header =
        new JPanel(
            MigLayouts.fillXWrap(
                "8 10 4 10", 1, MigLayoutConstraints.GROW_FILL, MigLayouts.rows(2, 2)));
    header.add(title, MigConstraints.growX());
    header.add(subtitle, MigConstraints.growX());
    add(header, BorderLayout.NORTH);
  }

  private void applyDerivedFonts() {
    Font base = UIManager.getFont(UiFontKeys.LABEL_FONT);
    if (base == null) base = title.getFont();
    if (base == null) return;
    title.setFont(base.deriveFont(Font.BOLD));
    emptyStateTitle.setFont(base.deriveFont(Font.BOLD, base.getSize2D() + 2f));
  }

  private void buildBody() {
    interceptorName.setToolTipText(message("interceptors.name.tooltip"));
    serverScope.setToolTipText(message("interceptors.serverScope.tooltip"));
    includes.setToolTipText(message("interceptors.includeChannels.tooltip"));
    excludes.setToolTipText(message("interceptors.excludeChannels.tooltip"));
    includes.putClientProperty(
        FlatClientProperties.PLACEHOLDER_TEXT, message("interceptors.includeChannels.placeholder"));
    excludes.putClientProperty(
        FlatClientProperties.PLACEHOLDER_TEXT, message("interceptors.excludeChannels.placeholder"));

    serverScope.setRenderer(plainComboRenderer(serverScope));
    includeMode.setRenderer(modeComboRenderer(includeMode));
    excludeMode.setRenderer(modeComboRenderer(excludeMode));
    actionSoundId.setRenderer(soundComboRenderer(actionSoundId));

    actionSoundCustomPath.setToolTipText(message("interceptors.soundCustomPath.tooltip"));
    actionSoundCustomPath.setEditable(false);
    actionScriptPath.setToolTipText(message("interceptors.scriptPath.tooltip"));
    actionScriptArgs.setToolTipText(message("interceptors.scriptArgs.tooltip"));
    actionScriptWorkingDirectory.setToolTipText(message("interceptors.scriptWorkingDirectory.tooltip"));
    configureActionButtons();

    configureRulesTable();
    configureHitsTable();

    tabs.addTab(message("interceptors.tab.definition"), buildDefinitionTab());
    tabs.addTab(message("interceptors.tab.triggers"), buildTriggersTab());
    tabs.addTab(message("interceptors.tab.actions"), wrapVerticalScroll(buildActionsTab()));
    tabs.addTab(message("interceptors.tab.hits"), buildHitsTab());

    buildEmptyStatePanel();
    centerPanel.add(tabs, CENTER_CARD_EDITOR);
    centerPanel.add(emptyStatePanel, CENTER_CARD_EMPTY);
    add(centerPanel, BorderLayout.CENTER);

    JPanel footer = new JPanel(new BorderLayout());
    footer.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));
    footer.add(status, BorderLayout.CENTER);
    add(footer, BorderLayout.SOUTH);
  }

  private void buildEmptyStatePanel() {
    emptyStatePanel.setOpaque(false);

    emptyStateTitle.setHorizontalAlignment(SwingConstants.CENTER);

    emptyStateBody.setHorizontalAlignment(SwingConstants.CENTER);
    emptyStateBody.setVerticalAlignment(SwingConstants.TOP);

    createInterceptorButton.setMargin(new Insets(4, 10, 4, 10));

    emptyStatePanel.add(new JLabel(""), MigConstraints.growYPushY());
    emptyStatePanel.add(emptyStateTitle, MigConstraints.alignCenter());
    emptyStatePanel.add(emptyStateBody, MigConstraints.alignCenterMinWidth(320));
    emptyStatePanel.add(createInterceptorButton, MigConstraints.alignCenter());
    emptyStatePanel.add(new JLabel(""), MigConstraints.growYPushY());
  }

  private JPanel buildDefinitionTab() {
    JPanel tab =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                "8 10 8 10", 1, 3, MigLayoutConstraints.GROW_FILL, MigLayouts.rowGaps(8, 6)));

    JPanel identity =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                8, 4, 3, "[right][grow,fill]16[right][pref!]", MigLayouts.rows(2, 6)));
    identity.setBorder(BorderFactory.createTitledBorder(message("interceptors.section.identity")));
    identity.add(new JLabel(message("interceptors.field.name")));
    identity.add(interceptorName, MigConstraints.growX());
    identity.add(new JLabel(message("interceptors.field.enabled.label")));
    identity.add(enabled, MigConstraints.wrap());
    identity.add(new JLabel(message("interceptors.field.serverScope")));
    identity.add(serverScope, MigConstraints.spanXWidthWrap(3, 170));

    JPanel channels =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                8, 3, 3, "[right][pref!][grow,fill]", MigLayouts.rows(3, 6)));
    channels.setBorder(BorderFactory.createTitledBorder(message("interceptors.section.channelFiltering")));
    channels.add(new JLabel(message("interceptors.field.include")));
    channels.add(includeMode, MigConstraints.width(78));
    channels.add(includes, MigConstraints.growXPushXMinWidth0Wrap());
    channels.add(new JLabel(message("interceptors.field.exclude")));
    channels.add(excludeMode, MigConstraints.width(78));
    channels.add(excludes, MigConstraints.growXPushXMinWidth0Wrap());
    channels.add(
        wrappedHint(message("interceptors.channelFiltering.help")),
        MigConstraints.spanXGrowXPushXMinWidth(3, 0));

    tab.add(identity, MigConstraints.growXWrap());
    tab.add(channels, MigConstraints.growXWrap());
    tab.add(
        wrappedHint(message("interceptors.definition.help")),
        MigConstraints.growXPushXMinWidth0());
    return tab;
  }

  private JPanel buildTriggersTab() {
    JPanel tab = new JPanel(new BorderLayout());

    JPanel toolbar = new JPanel(MigLayouts.fillX("6 10 6 10", "[][ ][][grow,fill]", "[]"));
    toolbar.add(addRule);
    toolbar.add(editRule);
    toolbar.add(removeRule);
    toolbar.add(new JLabel(""), MigConstraints.growX());

    tab.add(toolbar, BorderLayout.NORTH);
    tab.add(new JScrollPane(rulesTable), BorderLayout.CENTER);
    return tab;
  }

  private JPanel buildActionsTab() {
    JPanel tab =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                "8 10 8 10", 1, 3, MigLayoutConstraints.GROW_FILL, MigLayouts.rows(3, 8)));

    JPanel notifications =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(8, 2, 3, MigLayoutConstraints.GROW_FILL_PAIR, "[]"));
    notifications.setBorder(BorderFactory.createTitledBorder(message("interceptors.section.notifications")));
    notifications.add(actionStatusBarEnabled, MigConstraints.growX());
    notifications.add(actionToastEnabled, MigConstraints.growX());

    JPanel sound =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                8, 3, 3, "[pref!][grow,fill][pref!]", MigLayouts.rows(3, 6)));
    sound.setBorder(BorderFactory.createTitledBorder(message("interceptors.section.sound")));
    sound.add(actionSoundEnabled, MigConstraints.span2GrowX());
    sound.add(testSound, MigConstraints.widthHeightAlignRightWrap(36, 28));
    sound.add(new JLabel(message("interceptors.field.builtInSound")));
    sound.add(actionSoundId, MigConstraints.span2GrowXWrap());
    sound.add(actionSoundUseCustom, MigConstraints.spanXWrap(3));
    sound.add(new JLabel(message("interceptors.field.file")));
    sound.add(actionSoundCustomPath, MigConstraints.growXPushXMinWidth0());
    sound.add(browseSoundCustomPath, MigConstraints.widthHeight(36, 28));

    JPanel script =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                8, 3, 3, "[pref!][grow,fill][pref!]", MigLayouts.rows(3, 6)));
    script.setBorder(BorderFactory.createTitledBorder(message("interceptors.section.script")));
    script.add(actionScriptEnabled, MigConstraints.spanXWrap(3));
    script.add(new JLabel(message("interceptors.field.path")));
    script.add(actionScriptPath, MigConstraints.growX());
    script.add(browseScriptPath, MigConstraints.widthHeight(36, 28));
    script.add(new JLabel(message("interceptors.field.args")));
    script.add(actionScriptArgs, MigConstraints.span2GrowXWrap());
    script.add(new JLabel(message("interceptors.field.cwd")));
    script.add(actionScriptWorkingDirectory, MigConstraints.growX());
    script.add(browseScriptWorkingDirectory, MigConstraints.widthHeight(36, 28));

    tab.add(notifications, MigConstraints.growX());
    tab.add(sound, MigConstraints.growX());
    tab.add(script, MigConstraints.growX());

    return tab;
  }

  private static JScrollPane wrapVerticalScroll(JPanel content) {
    JScrollPane scroll =
        new JScrollPane(
            content,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    return scroll;
  }

  private JPanel buildHitsTab() {
    JPanel tab = new JPanel(new BorderLayout());

    JPanel toolbar = new JPanel(MigLayouts.fillX("6 10 6 10", "[][][][grow,fill]", "[]"));
    toolbar.add(exportHitsCsv);
    toolbar.add(clearSelectedHits);
    toolbar.add(clearHits);
    toolbar.add(new JLabel(""), MigConstraints.growX());

    tab.add(toolbar, BorderLayout.NORTH);
    tab.add(new JScrollPane(hitsTable), BorderLayout.CENTER);
    return tab;
  }

  private void configureRulesTable() {
    rulesTable.setFillsViewportHeight(true);
    rulesTable.setRowSelectionAllowed(true);
    rulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    rulesTable.setShowHorizontalLines(false);
    rulesTable.setShowVerticalLines(false);
    rulesTable.setAutoCreateRowSorter(true);
    rulesTable.getTableHeader().setReorderingAllowed(false);
    // Force dialog-only editing flow (no inline cell editor).
    rulesTable.setDefaultEditor(Object.class, null);
    rulesTable.setDefaultEditor(Boolean.class, null);
    rulesTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

    rulesPopupEdit.addActionListener(e -> editSelectedRule());
    rulesPopupRemove.addActionListener(e -> removeSelectedRule());
    rulesPopupEdit.setIcon(SvgIcons.action("edit", 16));
    rulesPopupRemove.setIcon(SvgIcons.action("trash", 16));
    rulesPopupEdit.setDisabledIcon(SvgIcons.actionDisabled("edit", 16));
    rulesPopupRemove.setDisabledIcon(SvgIcons.actionDisabled("trash", 16));
    rulesPopupMenu.setLightWeightPopupEnabled(true);
    rulesPopupMenu.add(rulesPopupEdit);
    rulesPopupMenu.add(rulesPopupRemove);

    rulesTable.getColumnModel().getColumn(0).setPreferredWidth(56); // On
    rulesTable.getColumnModel().getColumn(1).setPreferredWidth(220); // Why
    rulesTable.getColumnModel().getColumn(2).setPreferredWidth(220); // Events
    rulesTable.getColumnModel().getColumn(3).setPreferredWidth(260); // Message
    rulesTable.getColumnModel().getColumn(4).setPreferredWidth(180); // Nick
    rulesTable.getColumnModel().getColumn(5).setPreferredWidth(220); // Hostmask
  }

  private void configureHitsTable() {
    hitsTable.setFillsViewportHeight(true);
    hitsTable.setRowSelectionAllowed(true);
    hitsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    hitsTable.setShowHorizontalLines(false);
    hitsTable.setShowVerticalLines(false);
    hitsTable.getTableHeader().setReorderingAllowed(false);
    hitsTable.setAutoCreateRowSorter(true);

    hitsTable.getColumnModel().getColumn(0).setPreferredWidth(160); // Time
    hitsTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Server
    hitsTable.getColumnModel().getColumn(2).setPreferredWidth(120); // From
    hitsTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Hostmask
    hitsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Channel
    hitsTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Why
    hitsTable.getColumnModel().getColumn(6).setPreferredWidth(90); // Event
    hitsTable.getColumnModel().getColumn(7).setPreferredWidth(600); // Message
  }

  private void installListeners() {
    installSaveOnEnterAndBlur(interceptorName);
    enabled.addActionListener(e -> saveCurrentDefinition());
    serverScope.addActionListener(e -> saveCurrentDefinition());

    includeMode.addActionListener(
        e -> {
          refreshChannelFilterControlEnabledState();
          saveCurrentDefinition();
        });
    excludeMode.addActionListener(
        e -> {
          refreshChannelFilterControlEnabledState();
          saveCurrentDefinition();
        });
    installSaveOnEnterAndBlur(includes);
    installSaveOnEnterAndBlur(excludes);

    actionStatusBarEnabled.addActionListener(e -> saveCurrentDefinition());
    actionToastEnabled.addActionListener(e -> saveCurrentDefinition());

    actionSoundEnabled.addActionListener(
        e -> {
          refreshActionControlEnabledState();
          saveCurrentDefinition();
        });
    actionSoundId.addActionListener(e -> saveCurrentDefinition());
    actionSoundUseCustom.addActionListener(
        e -> {
          refreshActionControlEnabledState();
          saveCurrentDefinition();
        });
    browseSoundCustomPath.addActionListener(e -> browseForCustomSoundPath());
    installSaveOnEnterAndBlur(actionSoundCustomPath);
    testSound.addActionListener(e -> previewSelectedSound());

    actionScriptEnabled.addActionListener(
        e -> {
          refreshActionControlEnabledState();
          saveCurrentDefinition();
        });
    browseScriptPath.addActionListener(e -> browseForScriptPath());
    browseScriptWorkingDirectory.addActionListener(e -> browseForScriptWorkingDirectory());
    installSaveOnEnterAndBlur(actionScriptPath);
    installSaveOnEnterAndBlur(actionScriptArgs);
    installSaveOnEnterAndBlur(actionScriptWorkingDirectory);

    addRule.addActionListener(
        e -> {
          InterceptorRule next =
              promptRuleDialog(message("interceptors.rule.dialog.addTitle"), defaultRule(rulesModel.getRowCount() + 1));
          if (next == null) return;
          int row = rulesModel.addRule(next);
          if (row >= 0) {
            int viewRow = rulesTable.convertRowIndexToView(row);
            rulesTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
          }
          saveCurrentDefinition();
        });

    editRule.addActionListener(e -> editSelectedRule());
    rulesTable.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            maybeShowRulesPopup(e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            maybeShowRulesPopup(e);
          }

          @Override
          public void mouseClicked(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            if (e.getClickCount() < 2) return;
            int viewRow = rulesTable.rowAtPoint(e.getPoint());
            if (viewRow >= 0) {
              rulesTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
            }
            editSelectedRule();
          }
        });

    removeRule.addActionListener(e -> removeSelectedRule());

    rulesTable.getSelectionModel().addListSelectionListener(e -> updateRuleButtons());

    hitsPopupJumpToMessage.addActionListener(e -> jumpToSelectedHitMessage());
    hitsPopupClearSelected.addActionListener(e -> clearSelectedHitRows());
    hitsPopupClearAll.addActionListener(e -> clearAllHitRows());
    hitsPopupExportSelectedCsv.addActionListener(e -> exportSelectedHitsTableAsCsv());
    hitsPopupExportAllCsv.addActionListener(e -> exportHitsTableAsCsv());

    hitsPopupMenu.setLightWeightPopupEnabled(true);
    hitsPopupMenu.add(hitsPopupJumpToMessage);
    hitsPopupMenu.addSeparator();
    hitsPopupMenu.add(hitsPopupClearSelected);
    hitsPopupMenu.add(hitsPopupClearAll);
    hitsPopupMenu.addSeparator();
    hitsPopupMenu.add(hitsPopupExportSelectedCsv);
    hitsPopupMenu.add(hitsPopupExportAllCsv);

    hitsTable.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            maybeShowHitsPopup(e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            maybeShowHitsPopup(e);
          }
        });
    hitsTable.getSelectionModel().addListSelectionListener(e -> updateHitButtons());

    exportHitsCsv.addActionListener(e -> exportHitsTableAsCsv());
    clearSelectedHits.addActionListener(e -> clearSelectedHitRows());
    createInterceptorButton.addActionListener(e -> createInterceptorFromOverview());

    clearHits.addActionListener(e -> clearAllHitRows());
  }

  private void editSelectedRule() {
    int row = selectedRuleModelRow();
    if (row < 0) return;
    InterceptorRule current = rulesModel.ruleAt(row);
    if (current == null) return;

    InterceptorRule updated = promptRuleDialog(message("interceptors.rule.dialog.editTitle"), current);
    if (updated == null) return;

    rulesModel.setRule(row, updated);
    saveCurrentDefinition();
  }

  private void maybeShowRulesPopup(MouseEvent e) {
    if (e == null || !e.isPopupTrigger()) return;
    int viewRow = rulesTable.rowAtPoint(e.getPoint());
    if (viewRow >= 0) {
      rulesTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
    } else {
      rulesTable.clearSelection();
    }
    boolean hasSelection = controlsEnabled && selectedRuleModelRow() >= 0;
    rulesPopupEdit.setEnabled(hasSelection);
    rulesPopupRemove.setEnabled(hasSelection);
    PopupMenuThemeSupport.prepareForDisplay(rulesPopupMenu);
    rulesPopupMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  private void maybeShowHitsPopup(MouseEvent e) {
    if (e == null || !e.isPopupTrigger()) return;
    int viewRow = hitsTable.rowAtPoint(e.getPoint());
    if (viewRow >= 0) {
      if (!hitsTable.isRowSelected(viewRow)) {
        hitsTable.setRowSelectionInterval(viewRow, viewRow);
      }
    } else {
      hitsTable.clearSelection();
    }
    updateHitButtons();
    PopupMenuThemeSupport.prepareForDisplay(hitsPopupMenu);
    hitsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  private void clearAllHitRows() {
    String sid = serverId;
    String iid = interceptorId;
    if (sid.isBlank() || iid.isBlank()) return;
    store.clearHits(sid, iid);
  }

  private void clearSelectedHitRows() {
    String sid = serverId;
    String iid = interceptorId;
    if (sid.isBlank() || iid.isBlank()) return;
    List<InterceptorHit> selectedHits = selectedHits();
    if (selectedHits.isEmpty()) return;
    store.clearHits(sid, iid, selectedHits);
  }

  private void jumpToSelectedHitMessage() {
    InterceptorHit hit = selectedSingleHit();
    if (hit == null) return;
    String messageId = Objects.toString(hit.messageId(), "").trim();
    if (messageId.isEmpty()) return;
    TargetRef target = targetRefForHit(hit);
    if (target == null) return;
    onJumpToMessage.accept(target, messageId);
  }

  private void removeSelectedRule() {
    int row = selectedRuleModelRow();
    if (row < 0) return;
    if (!confirmRuleRemoval(row)) return;
    rulesModel.removeRow(row);
    saveCurrentDefinition();
    updateRuleButtons();
  }

  private boolean confirmRuleRemoval(int modelRow) {
    InterceptorRule rule = rulesModel.ruleAt(modelRow);
    String label = rule == null ? "" : Objects.toString(rule.label(), "").trim();
    if (label.isEmpty()) label = message("interceptors.rule.selectedRuleFallback");
    Window owner = SwingUtilities.getWindowAncestor(this);
    int choice =
        JOptionPane.showConfirmDialog(
            owner,
            message("interceptors.rule.delete.confirm", label),
            message("interceptors.rule.delete.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    return choice == JOptionPane.YES_OPTION;
  }

  private int selectedRuleModelRow() {
    int row = rulesTable.getSelectedRow();
    if (row < 0) return -1;
    return rulesTable.convertRowIndexToModel(row);
  }

  private List<Integer> allHitViewRows() {
    int rowCount = hitsTable.getRowCount();
    if (rowCount <= 0) return List.of();
    ArrayList<Integer> rows = new ArrayList<>(rowCount);
    for (int viewRow = 0; viewRow < rowCount; viewRow++) {
      rows.add(viewRow);
    }
    return rows;
  }

  private List<Integer> selectedHitViewRows() {
    int[] selectedRows = hitsTable.getSelectedRows();
    if (selectedRows == null || selectedRows.length == 0) return List.of();
    ArrayList<Integer> rows = new ArrayList<>(selectedRows.length);
    for (int viewRow : selectedRows) {
      if (viewRow >= 0) {
        rows.add(viewRow);
      }
    }
    return rows;
  }

  private List<InterceptorHit> selectedHits() {
    List<Integer> viewRows = selectedHitViewRows();
    if (viewRows.isEmpty()) return List.of();
    ArrayList<InterceptorHit> rows = new ArrayList<>(viewRows.size());
    for (int viewRow : viewRows) {
      int modelRow = hitsTable.convertRowIndexToModel(viewRow);
      InterceptorHit hit = hitsModel.rowAt(modelRow);
      if (hit != null) {
        rows.add(hit);
      }
    }
    return rows;
  }

  private InterceptorHit selectedSingleHit() {
    if (hitsTable.getSelectedRowCount() != 1) return null;
    int viewRow = hitsTable.getSelectedRow();
    if (viewRow < 0) return null;
    return hitsModel.rowAt(hitsTable.convertRowIndexToModel(viewRow));
  }

  static TargetRef targetRefForHit(InterceptorHit hit) {
    if (hit == null) return null;
    String scopeServerId = InterceptorScope.normalizeScopeServerId(hit.serverId());
    String serverId = InterceptorScope.baseServerId(scopeServerId);
    if (serverId.isBlank()) {
      serverId = Objects.toString(hit.serverId(), "").trim();
    }
    if (serverId.isBlank()) return null;

    TargetRef.QualifiedTarget parsed = TargetRef.parseQualifiedTarget(hit.channel());
    String target = Objects.toString(parsed.baseTarget(), "").trim();
    if (target.regionMatches(true, 0, "pm:", 0, 3)) {
      target = target.substring(3).trim();
    }
    if (target.isBlank()) {
      target = "status";
    }

    String networkToken = parsed.networkToken();
    if (networkToken.isBlank()) {
      networkToken = InterceptorScope.networkToken(scopeServerId);
    }

    try {
      return new TargetRef(
          serverId,
          networkToken.isBlank() ? target : TargetRef.withNetworkQualifier(target, networkToken));
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }

  private DefaultListCellRenderer modeComboRenderer(JComboBox<?> combo) {
    return new DefaultListCellRenderer() {
      @Override
      public java.awt.Component getListCellRendererComponent(
          javax.swing.JList<?> list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof InterceptorRuleMode mode) {
          setText(modeLabel(mode));
        }
        applyComboDisplayPalette(this, combo, index);
        return this;
      }
    };
  }

  private DefaultListCellRenderer plainComboRenderer(JComboBox<?> combo) {
    return new DefaultListCellRenderer() {
      @Override
      public java.awt.Component getListCellRendererComponent(
          javax.swing.JList<?> list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        applyComboDisplayPalette(this, combo, index);
        return this;
      }
    };
  }

  private DefaultListCellRenderer ruleDimensionModeComboRenderer(JComboBox<?> combo) {
    return new DefaultListCellRenderer() {
      @Override
      public java.awt.Component getListCellRendererComponent(
          javax.swing.JList<?> list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof InterceptorRuleMode mode) {
          setText(mode == InterceptorRuleMode.ALL ? message("interceptors.rule.mode.any") : modeLabel(mode));
        }
        applyComboDisplayPalette(this, combo, index);
        return this;
      }
    };
  }

  private DefaultListCellRenderer soundComboRenderer(JComboBox<?> combo) {
    return new DefaultListCellRenderer() {
      @Override
      public java.awt.Component getListCellRendererComponent(
          javax.swing.JList<?> list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof BuiltInSound sound) {
          setText(sound.displayNameForUi());
        }
        applyComboDisplayPalette(this, combo, index);
        return this;
      }
    };
  }

  private static void applyComboDisplayPalette(
      DefaultListCellRenderer renderer, JComboBox<?> combo, int index) {
    if (renderer == null || combo == null) return;
    renderer.setFont(combo.getFont());
    if (index >= 0) return;

    Color foreground =
        combo.isEnabled()
            ? firstUiColor(
                UiColorKeys.COMBO_BOX_FOREGROUND,
                UiColorKeys.TEXT_FIELD_FOREGROUND,
                UiColorKeys.LABEL_FOREGROUND)
            : firstUiColor(
                UiColorKeys.COMBO_BOX_DISABLED_TEXT,
                UiColorKeys.LABEL_DISABLED_FOREGROUND,
                UiColorKeys.LABEL_FOREGROUND);
    Color background =
        firstUiColor(
            UiColorKeys.COMBO_BOX_BACKGROUND,
            UiColorKeys.TEXT_FIELD_BACKGROUND,
            UiColorKeys.PANEL_BACKGROUND);
    if (foreground != null) renderer.setForeground(foreground);
    if (background != null) renderer.setBackground(background);
  }

  private static Color firstUiColor(String... keys) {
    if (keys == null) return null;
    for (String key : keys) {
      String k = Objects.toString(key, "").trim();
      if (k.isEmpty()) continue;
      Color c = UIManager.getColor(k);
      if (c != null) return c;
    }
    return null;
  }

  private static Runnable bindRuleDimensionModeFieldEnabled(
      JComboBox<InterceptorRuleMode> messageMode,
      JTextField messagePattern,
      JComboBox<InterceptorRuleMode> nickMode,
      JTextField nickPattern,
      JComboBox<InterceptorRuleMode> hostmaskMode,
      JTextField hostmaskPattern) {
    Runnable refresh =
        () -> {
          setRuleDimensionFieldEnabled(messageMode, messagePattern);
          setRuleDimensionFieldEnabled(nickMode, nickPattern);
          setRuleDimensionFieldEnabled(hostmaskMode, hostmaskPattern);
        };
    if (messageMode != null) messageMode.addActionListener(e -> refresh.run());
    if (nickMode != null) nickMode.addActionListener(e -> refresh.run());
    if (hostmaskMode != null) hostmaskMode.addActionListener(e -> refresh.run());
    return refresh;
  }

  private static void setRuleDimensionFieldEnabled(
      JComboBox<InterceptorRuleMode> modeCombo, JTextField field) {
    if (field == null) return;
    boolean enabled =
        isPatternTextFieldEnabledForMode(selectedMode(modeCombo, InterceptorRuleMode.LIKE));
    field.setEnabled(enabled);
    field.setEditable(enabled);
  }

  static void refreshRuleMessageControlEnabledState(
      JCheckBox anyEventType,
      JCheckBox messageEventSelector,
      JComboBox<InterceptorRuleMode> messageMode,
      JTextField messagePattern) {
    boolean enabled =
        anyEventType != null
            && !anyEventType.isSelected()
            && messageEventSelector != null
            && messageEventSelector.isSelected();
    if (messageMode != null) {
      messageMode.setEnabled(enabled);
    }
    if (messagePattern == null) return;
    boolean patternEnabled =
        enabled
            && isPatternTextFieldEnabledForMode(
                selectedMode(messageMode, InterceptorRuleMode.LIKE));
    messagePattern.setEnabled(patternEnabled);
    messagePattern.setEditable(patternEnabled);
  }

  static boolean isPatternTextFieldEnabledForMode(InterceptorRuleMode mode) {
    return mode != InterceptorRuleMode.ALL && mode != InterceptorRuleMode.NONE;
  }

  private static String effectiveRulePattern(
      JComboBox<InterceptorRuleMode> modeCombo, JTextField field) {
    if (selectedMode(modeCombo, InterceptorRuleMode.LIKE) == InterceptorRuleMode.ALL) {
      return "";
    }
    return field == null ? "" : field.getText();
  }

  private void installSaveOnEnterAndBlur(JTextField field) {
    field.addActionListener(e -> saveCurrentDefinition());
    field.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            saveCurrentDefinition();
          }
        });
  }

  private InterceptorRule promptRuleDialog(String dialogTitle, InterceptorRule seed) {
    InterceptorRule base = seed == null ? defaultRule(rulesModel.getRowCount() + 1) : seed;

    JCheckBox ruleEnabled = new JCheckBox(message("interceptors.field.enabled"), base.enabled());
    JTextField ruleLabel = new JTextField(base.label());
    JCheckBox anyEventType =
        new JCheckBox(
            message(RULE_ANY_EVENT_KEY), Objects.toString(base.eventTypesCsv(), "").trim().isBlank());
    LinkedHashMap<InterceptorEventType, JCheckBox> eventSelectors =
        buildRuleEventSelectors(base.eventTypesCsv());

    JComboBox<InterceptorRuleMode> messageMode =
        new JComboBox<>(RULE_DIMENSION_MODES_WITH_ANY.clone());
    messageMode.setRenderer(ruleDimensionModeComboRenderer(messageMode));
    messageMode.setSelectedItem(base.messageMode());
    JTextField messagePattern = new JTextField(base.messagePattern());
    messagePattern.putClientProperty(
        FlatClientProperties.PLACEHOLDER_TEXT, message("interceptors.rule.message.placeholder"));

    String seedMessagePattern = Objects.toString(base.messagePattern(), "").trim();
    String seedCtcpCommand = "";
    String seedCtcpValue = "";
    if (base.messageMode() == InterceptorRuleMode.LIKE && !seedMessagePattern.isEmpty()) {
      int split = seedMessagePattern.indexOf(' ');
      if (split < 0) {
        seedCtcpCommand = seedMessagePattern;
      } else {
        seedCtcpCommand = seedMessagePattern.substring(0, split).trim();
        seedCtcpValue = seedMessagePattern.substring(split + 1).trim();
      }
    }
    JTextField ctcpCommand = new JTextField(seedCtcpCommand);
    ctcpCommand.putClientProperty(
        FlatClientProperties.PLACEHOLDER_TEXT, message("interceptors.rule.ctcpCommand.placeholder"));
    JTextField ctcpValue = new JTextField(seedCtcpValue);
    ctcpValue.putClientProperty(
        FlatClientProperties.PLACEHOLDER_TEXT, message("interceptors.rule.ctcpValue.placeholder"));
    JButton applyCtcpHelper = new JButton(message("common.button.apply"));
    configureIconButton(
        applyCtcpHelper, "check", message("interceptors.rule.ctcpHelper.apply.tooltip"));

    JComboBox<InterceptorRuleMode> nickMode =
        new JComboBox<>(RULE_DIMENSION_MODES_WITH_ANY.clone());
    nickMode.setRenderer(ruleDimensionModeComboRenderer(nickMode));
    nickMode.setSelectedItem(base.nickMode());
    JTextField nickPattern = new JTextField(base.nickPattern());
    nickPattern.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, message("interceptors.rule.nick.placeholder"));

    JComboBox<InterceptorRuleMode> hostmaskMode =
        new JComboBox<>(RULE_DIMENSION_MODES_WITH_ANY.clone());
    hostmaskMode.setRenderer(ruleDimensionModeComboRenderer(hostmaskMode));
    hostmaskMode.setSelectedItem(base.hostmaskMode());
    JTextField hostmaskPattern = new JTextField(base.hostmaskPattern());
    hostmaskPattern.putClientProperty(
        FlatClientProperties.PLACEHOLDER_TEXT, message("interceptors.rule.hostmask.placeholder"));

    JPanel panel =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                10, 3, 3, "[right][pref!][grow,fill]", MigLayouts.rows(7, 6)));

    panel.add(ruleEnabled, MigConstraints.spanXWrap(3));

    panel.add(new JLabel(message("interceptors.rule.field.label")));
    panel.add(ruleLabel, MigConstraints.span2GrowXPushXMinWidth0Wrap());

    JPanel eventsGrid =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                0, 2, 3, MigLayoutConstraints.GROW_FILL_PAIR, MigLayouts.rows(2, 2)));
    for (JCheckBox selector : eventSelectors.values()) {
      selector.setToolTipText(message("interceptors.rule.event.tooltip", selector.getText()));
      eventsGrid.add(selector, MigConstraints.growX());
    }

    JScrollPane eventsScroll = new JScrollPane(eventsGrid);
    eventsScroll.setBorder(BorderFactory.createEmptyBorder());
    eventsScroll.setPreferredSize(new java.awt.Dimension(340, 130));
    eventsScroll.getVerticalScrollBar().setUnitIncrement(14);

    JPanel eventsPanel =
        new JPanel(
            MigLayouts.fillXWrapWithHideMode(
                0, 1, 3, MigLayoutConstraints.GROW_FILL, MigLayouts.rows(2, 4)));
    eventsPanel.add(anyEventType, MigConstraints.growX());
    eventsPanel.add(eventsScroll, MigConstraints.growXPushXMinWidth0());

    Runnable refreshEventSelectorState =
        () -> {
          boolean enabledSelectors = !anyEventType.isSelected();
          for (JCheckBox selector : eventSelectors.values()) {
            selector.setEnabled(enabledSelectors);
          }
        };
    JCheckBox messageEventSelector = eventSelectors.get(InterceptorEventType.MESSAGE);

    panel.add(new JLabel(message("interceptors.rule.field.events")));
    panel.add(eventsPanel, MigConstraints.span2GrowXPushXMinWidth0Wrap());

    panel.add(new JLabel(message("interceptors.rule.field.message")));
    panel.add(messageMode, MigConstraints.width(110));
    panel.add(messagePattern, MigConstraints.growXPushXMinWidth0Wrap());

    JPanel ctcpHelperRow = new JPanel(MigLayouts.fillX("[grow,fill]8[grow,fill]8[]", "[]"));
    ctcpHelperRow.add(ctcpCommand, MigConstraints.growXPushXMinWidth0());
    ctcpHelperRow.add(ctcpValue, MigConstraints.growXPushXMinWidth0());
    ctcpHelperRow.add(applyCtcpHelper, MigConstraints.widthHeight(36, 28));
    panel.add(new JLabel(message("interceptors.rule.field.ctcpHelper")));
    panel.add(ctcpHelperRow, MigConstraints.span2GrowXPushXMinWidth0Wrap());

    panel.add(new JLabel(message("interceptors.rule.field.nick")));
    panel.add(nickMode, MigConstraints.width(110));
    panel.add(nickPattern, MigConstraints.growXPushXMinWidth0Wrap());

    panel.add(new JLabel(message("interceptors.rule.field.hostmask")));
    panel.add(hostmaskMode, MigConstraints.width(110));
    panel.add(hostmaskPattern, MigConstraints.growXPushXMinWidth0Wrap());

    Runnable refreshDimensionFieldState =
        bindRuleDimensionModeFieldEnabled(
            messageMode, messagePattern, nickMode, nickPattern, hostmaskMode, hostmaskPattern);

    JCheckBox ctcpEventSelector = eventSelectors.get(InterceptorEventType.CTCP);
    Runnable refreshCtcpHelperState =
        () -> {
          boolean ctcpSelected =
              !anyEventType.isSelected()
                  && ctcpEventSelector != null
                  && ctcpEventSelector.isSelected();
          ctcpCommand.setEnabled(ctcpSelected);
          ctcpCommand.setEditable(ctcpSelected);
          ctcpValue.setEnabled(ctcpSelected);
          ctcpValue.setEditable(ctcpSelected);
          applyCtcpHelper.setEnabled(ctcpSelected);
        };
    Runnable refreshEventDependentControlState =
        () -> {
          refreshEventSelectorState.run();
          refreshRuleMessageControlEnabledState(
              anyEventType, messageEventSelector, messageMode, messagePattern);
          refreshCtcpHelperState.run();
        };
    anyEventType.addActionListener(e -> refreshEventDependentControlState.run());
    for (JCheckBox selector : eventSelectors.values()) {
      selector.addActionListener(e -> refreshEventDependentControlState.run());
    }
    messageMode.addActionListener(
        e ->
            refreshRuleMessageControlEnabledState(
                anyEventType, messageEventSelector, messageMode, messagePattern));
    refreshDimensionFieldState.run();
    refreshEventDependentControlState.run();

    applyCtcpHelper.addActionListener(
        e -> {
          String command = Objects.toString(ctcpCommand.getText(), "").trim();
          String value = Objects.toString(ctcpValue.getText(), "").trim();
          if (command.isEmpty()) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                message("interceptors.rule.ctcpHelper.commandRequired"),
                message("interceptors.rule.ctcpHelper.title"),
                JOptionPane.WARNING_MESSAGE);
            return;
          }

          if (ctcpEventSelector != null) {
            anyEventType.setSelected(false);
            for (var entry : eventSelectors.entrySet()) {
              JCheckBox selector = entry.getValue();
              if (selector == null) continue;
              selector.setSelected(entry.getKey() == InterceptorEventType.CTCP);
            }
            refreshEventSelectorState.run();
            refreshCtcpHelperState.run();
          }

          messageMode.setSelectedItem(InterceptorRuleMode.LIKE);
          String normalizedCommand = command.toUpperCase(Locale.ROOT);
          messagePattern.setText(
              value.isBlank() ? normalizedCommand : (normalizedCommand + " " + value));
          refreshDimensionFieldState.run();
          refreshRuleMessageControlEnabledState(
              anyEventType, messageEventSelector, messageMode, messagePattern);
        });

    Window owner = SwingUtilities.getWindowAncestor(this);
    int choice =
        JOptionPane.showConfirmDialog(
            owner, panel, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (choice != JOptionPane.OK_OPTION) return null;

    String eventTypesCsv = selectedRuleEvents(anyEventType, eventSelectors);
    if (eventTypesCsv == null) {
      JOptionPane.showMessageDialog(
          owner,
          message("interceptors.rule.eventTypesRequired", message(RULE_ANY_EVENT_KEY)),
          message("interceptors.rule.eventTypesRequired.title"),
          JOptionPane.WARNING_MESSAGE);
      return null;
    }
    boolean persistMessagePattern =
        messageMode.isEnabled()
            || (!anyEventType.isSelected()
                && ctcpEventSelector != null
                && ctcpEventSelector.isSelected());

    return new InterceptorRule(
        ruleEnabled.isSelected(),
        ruleLabel.getText(),
        eventTypesCsv,
        selectedMode(messageMode, InterceptorRuleMode.LIKE),
        persistMessagePattern ? effectiveRulePattern(messageMode, messagePattern) : "",
        selectedMode(nickMode, InterceptorRuleMode.LIKE),
        effectiveRulePattern(nickMode, nickPattern),
        selectedMode(hostmaskMode, InterceptorRuleMode.GLOB),
        effectiveRulePattern(hostmaskMode, hostmaskPattern));
  }

  private LinkedHashMap<InterceptorEventType, JCheckBox> buildRuleEventSelectors(
      String selectedCsv) {
    LinkedHashMap<InterceptorEventType, JCheckBox> out = new LinkedHashMap<>();
    EnumSet<InterceptorEventType> selected = InterceptorEventType.parseCsv(selectedCsv);
    boolean any = Objects.toString(selectedCsv, "").trim().isBlank();
    for (InterceptorEventType eventType : InterceptorEventType.values()) {
      if (eventType == null) continue;
      JCheckBox box = new JCheckBox(eventType.toString(), !any && selected.contains(eventType));
      out.put(eventType, box);
    }
    return out;
  }

  private static String formatEventTypesForDisplay(String rawCsv) {
    String raw = Objects.toString(rawCsv, "").trim();
    if (raw.isEmpty()) return message(RULE_ANY_EVENT_KEY);

    String[] parts = raw.split("[,\\n;]");
    ArrayList<String> labels = new ArrayList<>(parts.length);
    for (String part : parts) {
      String token = Objects.toString(part, "").trim();
      if (token.isEmpty()) continue;
      InterceptorEventType type = resolveEventType(token);
      labels.add(type != null ? type.toString() : token);
    }
    if (labels.isEmpty()) return message(RULE_ANY_EVENT_KEY);
    return String.join(", ", labels);
  }

  private static String selectedRuleEvents(
      JCheckBox anyEventType, LinkedHashMap<InterceptorEventType, JCheckBox> selectors) {
    if (anyEventType == null || anyEventType.isSelected()) return "";
    if (selectors == null || selectors.isEmpty()) return "";

    ArrayList<String> tokens = new ArrayList<>(selectors.size());
    for (var entry : selectors.entrySet()) {
      InterceptorEventType type = entry.getKey();
      JCheckBox box = entry.getValue();
      if (type == null || box == null || !box.isSelected()) continue;
      tokens.add(type.token());
    }
    if (tokens.isEmpty()) return null;
    if (tokens.size() == selectors.size()) {
      // Preserve explicit synthetic/derived selectors (e.g. Highlight) instead of
      // collapsing to blank "Any", which intentionally excludes them for compatibility.
      boolean hasSynthetic =
          selectors.containsKey(InterceptorEventType.HIGHLIGHT)
              && selectors.get(InterceptorEventType.HIGHLIGHT) != null
              && selectors.get(InterceptorEventType.HIGHLIGHT).isSelected();
      if (!hasSynthetic) return "";
    }
    return String.join(",", tokens);
  }

  private static InterceptorEventType resolveEventType(String raw) {
    String value = Objects.toString(raw, "").trim();
    if (value.isEmpty()) return null;

    InterceptorEventType byToken = InterceptorEventType.fromToken(value);
    if (byToken != null) return byToken;

    String foldedValue = foldEventLabel(value);
    for (InterceptorEventType type : InterceptorEventType.values()) {
      if (type == null) continue;
      if (value.equalsIgnoreCase(type.toString())) return type;
      if (foldedValue.equals(foldEventLabel(type.toString()))) return type;
    }
    return null;
  }

  private static String foldEventLabel(String raw) {
    String value = Objects.toString(raw, "").trim().toLowerCase(Locale.ROOT);
    if (value.isEmpty()) return "";
    value = value.replace('-', ' ').replace('_', ' ');
    return value.replaceAll("\\s+", " ").trim();
  }

  private void configureActionButtons() {
    configureIconButton(addRule, "plus", message("interceptors.button.addRule.tooltip"));
    configureIconButton(editRule, "edit", message("interceptors.button.editRule.tooltip"));
    configureIconButton(removeRule, "trash", message("interceptors.button.removeRule.tooltip"));
    configureIconButton(clearSelectedHits, "trash", message("interceptors.button.clearSelectedHits.tooltip"));
    configureIconButton(clearHits, "close", message("interceptors.button.clearHits.tooltip"));
    configureIconButton(exportHitsCsv, "copy", message("interceptors.button.exportHitsCsv.tooltip"));
    configureMenuItem(hitsPopupJumpToMessage, "channel");
    configureMenuItem(hitsPopupClearSelected, "trash");
    configureMenuItem(hitsPopupClearAll, "close");
    configureMenuItem(hitsPopupExportSelectedCsv, "copy");
    configureMenuItem(hitsPopupExportAllCsv, "copy");
    createInterceptorButton.setIcon(SvgIcons.action("plus", 16));
    createInterceptorButton.setDisabledIcon(SvgIcons.actionDisabled("plus", 16));
    createInterceptorButton.setMargin(new Insets(2, 8, 2, 8));
    createInterceptorButton.setToolTipText(message("interceptors.button.create.tooltip"));
    createInterceptorButton.setFocusable(false);
    configureIconButton(testSound, "play", message("interceptors.button.testSound.tooltip"));
    configureIconButton(browseSoundCustomPath, "folder-open", message("interceptors.button.browseSound.tooltip"));
    configureIconButton(browseScriptPath, "terminal", message("interceptors.button.browseScript.tooltip"));
    configureIconButton(
        browseScriptWorkingDirectory, "settings", message("interceptors.button.browseScriptCwd.tooltip"));
  }

  private static void configureIconButton(JButton button, String iconName, String tooltip) {
    if (button == null) return;
    button.setText("");
    button.setIcon(SvgIcons.action(iconName, 16));
    button.setDisabledIcon(SvgIcons.actionDisabled(iconName, 16));
    button.setMargin(new Insets(2, 6, 2, 6));
    button.setToolTipText(tooltip);
    button.setFocusable(false);
  }

  private static void configureMenuItem(JMenuItem item, String iconName) {
    if (item == null) return;
    item.setIcon(SvgIcons.action(iconName, 16));
    item.setDisabledIcon(SvgIcons.actionDisabled(iconName, 16));
  }

  private void previewSelectedSound() {
    if (!controlsEnabled) return;
    if (!actionSoundEnabled.isSelected()) return;
    store.previewSoundOverride(
        selectedSoundId(), actionSoundUseCustom.isSelected(), actionSoundCustomPath.getText());
  }

  private void createInterceptorFromOverview() {
    String sid = Objects.toString(serverId, "").trim();
    if (sid.isBlank()) {
      status.setText(message("interceptors.status.selectServerFirst"));
      return;
    }
    if (!Objects.toString(interceptorId, "").trim().isBlank()) {
      status.setText(message("interceptors.status.createFromOverview"));
      return;
    }

    try {
      InterceptorDefinition created = store.createInterceptor(sid, message("interceptors.defaultName"));
      if (created == null || Objects.toString(created.id(), "").isBlank()) {
        status.setText(message("interceptors.status.createFailed"));
        return;
      }
      status.setText(message("interceptors.status.created", created.name()));

      Consumer<TargetRef> cb = onSelectTarget;
      TargetRef createdRef = InterceptorScope.interceptorRef(sid, created.id());
      if (cb != null) {
        if (createdRef != null) {
          cb.accept(createdRef);
        }
      } else {
        setInterceptorTarget(sid, created.id());
      }
    } catch (Exception ex) {
      String msg = Objects.toString(ex.getMessage(), ex.getClass().getSimpleName());
      status.setText(message("interceptors.status.createFailedWithMessage", msg));
      JOptionPane.showMessageDialog(
          SwingUtilities.getWindowAncestor(this),
          message("interceptors.create.error.message", msg),
          message("interceptors.create.error.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void exportHitsTableAsCsv() {
    exportHitsAsCsv(allHitViewRows(), message("interceptors.export.dialogTitle.all"), false);
  }

  private void exportSelectedHitsTableAsCsv() {
    exportHitsAsCsv(selectedHitViewRows(), message("interceptors.export.dialogTitle.selected"), true);
  }

  private void exportHitsAsCsv(List<Integer> viewRows, String dialogTitle, boolean selectedOnly) {
    if (viewRows == null || viewRows.isEmpty()) {
      status.setText(
          selectedOnly
              ? message("interceptors.status.noSelectedHitsToExport")
              : message("interceptors.status.noHitsToExport"));
      return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(dialogTitle);
    chooser.setFileFilter(new FileNameExtensionFilter(message("interceptors.export.csvFilter"), "csv"));
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.setSelectedFile(new File(defaultHitsExportFileName(selectedOnly)));

    int result = chooser.showSaveDialog(SwingUtilities.getWindowAncestor(this));
    if (result != JFileChooser.APPROVE_OPTION) return;

    File selected = chooser.getSelectedFile();
    if (selected == null) return;

    Path path = selected.toPath();
    String fileName = path.getFileName() == null ? "" : path.getFileName().toString();
    if (!fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
      path = path.resolveSibling(fileName + ".csv");
    }

    try {
      writeHitsCsv(path, viewRows);
      status.setText(message("interceptors.status.exportedHits", viewRows.size(), path.toAbsolutePath()));
    } catch (Exception ex) {
      String msg = Objects.toString(ex.getMessage(), ex.getClass().getSimpleName());
      status.setText(message("interceptors.status.exportFailed", msg));
      JOptionPane.showMessageDialog(
          SwingUtilities.getWindowAncestor(this),
          message("interceptors.export.error.message", msg),
          message("interceptors.export.error.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void writeHitsCsv(Path path, List<Integer> viewRows) throws Exception {
    if (path == null) throw new IllegalArgumentException("Output path is required.");
    if (viewRows == null || viewRows.isEmpty()) {
      throw new IllegalArgumentException("At least one row is required.");
    }
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

      int viewColumnCount = hitsTable.getColumnCount();
      ArrayList<String> headers = new ArrayList<>(viewColumnCount);
      for (int viewCol = 0; viewCol < viewColumnCount; viewCol++) {
        headers.add(Objects.toString(hitsTable.getColumnName(viewCol), ""));
      }
      out.write(joinCsv(headers));
      out.newLine();

      for (int viewRow : viewRows) {
        if (viewRow < 0 || viewRow >= hitsTable.getRowCount()) continue;
        int modelRow = hitsTable.convertRowIndexToModel(viewRow);
        ArrayList<String> row = new ArrayList<>(viewColumnCount);

        for (int viewCol = 0; viewCol < viewColumnCount; viewCol++) {
          int modelCol = hitsTable.convertColumnIndexToModel(viewCol);
          row.add(Objects.toString(hitsModel.getValueAt(modelRow, modelCol), ""));
        }

        out.write(joinCsv(row));
        out.newLine();
      }
    }
  }

  private static String joinCsv(List<String> cols) {
    if (cols == null || cols.isEmpty()) return "";
    StringBuilder sb = new StringBuilder(cols.size() * 24);
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

  private String defaultHitsExportFileName(boolean selectedOnly) {
    String sid = serverId.isBlank() ? "server" : serverId.replaceAll("[^A-Za-z0-9._-]+", "_");
    String iid =
        interceptorId.isBlank() ? "interceptor" : interceptorId.replaceAll("[^A-Za-z0-9._-]+", "_");
    String ts =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT)
            .withZone(ZoneId.systemDefault())
            .format(Instant.now());
    return "ircafe-interceptor-hits"
        + (selectedOnly ? "-selected" : "")
        + "-"
        + sid
        + "-"
        + iid
        + "-"
        + ts
        + ".csv";
  }

  private void browseForCustomSoundPath() {
    if (!controlsEnabled) return;
    File selected =
        SoundFileChooserSupport.chooseSoundFile(
                SwingUtilities.getWindowAncestor(this), message("interceptors.sound.chooseDialogTitle"))
            .orElse(null);
    if (selected == null) return;

    try {
      String rel = store.importInterceptorCustomSoundFile(selected);
      if (rel == null || rel.isBlank()) return;
      actionSoundCustomPath.setText(rel);
      actionSoundUseCustom.setSelected(true);
      actionSoundEnabled.setSelected(true);
      refreshActionControlEnabledState();
      saveCurrentDefinition();
      status.setText(message("interceptors.status.importedCustomSound", rel));
    } catch (Exception ex) {
      String msg = Objects.toString(ex.getMessage(), ex.getClass().getSimpleName());
      JOptionPane.showMessageDialog(
          SwingUtilities.getWindowAncestor(this),
          message("interceptors.sound.importFailed.message", msg),
          message("interceptors.sound.importFailed.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void browseForScriptPath() {
    if (!controlsEnabled) return;
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(message("interceptors.script.chooseDialogTitle"));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setAcceptAllFileFilterUsed(true);
    seedChooserPath(chooser, actionScriptPath.getText(), false);
    int result = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
    if (result != JFileChooser.APPROVE_OPTION) return;

    File selected = chooser.getSelectedFile();
    if (selected == null) return;

    actionScriptPath.setText(selected.getAbsolutePath());
    actionScriptEnabled.setSelected(true);
    refreshActionControlEnabledState();
    saveCurrentDefinition();
  }

  private void browseForScriptWorkingDirectory() {
    if (!controlsEnabled) return;
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(message("interceptors.script.chooseWorkingDirectoryTitle"));
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    seedChooserPath(chooser, actionScriptWorkingDirectory.getText(), true);
    int result = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
    if (result != JFileChooser.APPROVE_OPTION) return;

    File selected = chooser.getSelectedFile();
    if (selected == null) return;

    actionScriptWorkingDirectory.setText(selected.getAbsolutePath());
    actionScriptEnabled.setSelected(true);
    refreshActionControlEnabledState();
    saveCurrentDefinition();
  }

  private static void seedChooserPath(
      JFileChooser chooser, String rawPath, boolean preferDirectory) {
    if (chooser == null) return;
    String path = Objects.toString(rawPath, "").trim();
    if (path.isEmpty()) return;
    File candidate = new File(path);

    if (preferDirectory) {
      if (candidate.isDirectory()) {
        chooser.setCurrentDirectory(candidate);
        chooser.setSelectedFile(candidate);
        return;
      }
      File parent = candidate.getParentFile();
      if (parent != null && parent.isDirectory()) {
        chooser.setCurrentDirectory(parent);
      }
      return;
    }

    if (candidate.isDirectory()) {
      chooser.setCurrentDirectory(candidate);
      return;
    }
    File parent = candidate.getParentFile();
    if (parent != null && parent.isDirectory()) {
      chooser.setCurrentDirectory(parent);
    }
    chooser.setSelectedFile(candidate);
  }

  private static JTextArea wrappedHint(String text) {
    JTextArea hint = new JTextArea(2, 1);
    hint.setEditable(false);
    hint.setFocusable(false);
    hint.setLineWrap(true);
    hint.setWrapStyleWord(true);
    hint.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    Font font = UIManager.getFont(UiFontKeys.LABEL_FONT);
    if (font == null) font = new JLabel().getFont();
    if (font != null) hint.setFont(font);
    Color foreground = firstUiColor(UiColorKeys.LABEL_FOREGROUND, UiColorKeys.TEXT_AREA_FOREGROUND);
    if (foreground != null) hint.setForeground(foreground);
    Color background = firstUiColor(UiColorKeys.PANEL_BACKGROUND, UiColorKeys.CONTROL);
    if (background != null) {
      hint.setOpaque(true);
      hint.setBackground(background);
    } else {
      hint.setOpaque(false);
    }
    hint.setText(Objects.toString(text, ""));
    return hint;
  }

  private void setEditorTabsVisible(boolean visible) {
    tabs.setVisible(visible);
    showCenterCard(visible ? CENTER_CARD_EDITOR : CENTER_CARD_EMPTY);
    revalidate();
    repaint();
  }

  private void showCenterCard(String cardName) {
    CardLayout layout = (CardLayout) centerPanel.getLayout();
    layout.show(centerPanel, cardName);
  }

  private void setEmptyStateContent(String heading, String bodyHtml, boolean showCreateButton) {
    emptyStateTitle.setText(Objects.toString(heading, ""));
    emptyStateBody.setText(Objects.toString(bodyHtml, ""));
    createInterceptorButton.setVisible(showCreateButton);
    createInterceptorButton.setEnabled(showCreateButton);
  }

  private void refreshFromStore() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::refreshFromStore);
      return;
    }

    long req = refreshSeq.incrementAndGet();
    String sid = serverId;
    String iid = interceptorId;
    applyLoadingState();

    if (sid.isBlank() || iid.isBlank()) {
      applyNoSelectionState(req);
      return;
    }

    final String loadSid = sid;
    final String loadIid = iid;
    refreshExecutor.execute(() -> loadInterceptorSnapshot(req, loadSid, loadIid));
  }

  private void applyLoadingState() {
    loading = true;
    title.setText(message("interceptors.title.single"));
    subtitle.setText(message("interceptors.subtitle.loading"));
    status.setText(message("interceptors.status.loading"));
    setEmptyStateContent(
        message("interceptors.title.overview"),
        message("interceptors.empty.loading"),
        false);
    setEditorTabsVisible(!Objects.toString(interceptorId, "").trim().isBlank());
    setControlsEnabled(false);
  }

  private void applyNoSelectionState(long req) {
    if (req != refreshSeq.get()) return;
    loading = true;

    boolean interceptorsOverview =
        !Objects.toString(serverId, "").trim().isBlank()
            && Objects.toString(interceptorId, "").trim().isBlank();
    if (interceptorsOverview) {
      title.setText(message("interceptors.title.overview"));
      subtitle.setText(message("interceptors.subtitle.overview"));
      setEmptyStateContent(
          message("interceptors.title.overview"),
          message("interceptors.empty.overview"),
          true);
      status.setText(message("interceptors.status.overviewTip"));
      setEditorTabsVisible(false);
    } else {
      title.setText(message("interceptors.title.single"));
      subtitle.setText(message("interceptors.subtitle.selectNode"));
      setEmptyStateContent(
          message("interceptors.title.single"),
          message("interceptors.empty.selectNode"),
          false);
      status.setText(" ");
      setEditorTabsVisible(false);
    }

    rulesModel.setRows(List.of());
    hitsModel.setRows(List.of());
    resetControls();
    loading = false;
    setControlsEnabled(false);
  }

  private void applyMissingInterceptorState(long req) {
    if (req != refreshSeq.get()) return;
    loading = true;
    title.setText(message("interceptors.title.single"));
    subtitle.setText(message("interceptors.subtitle.removed"));
    rulesModel.setRows(List.of());
    hitsModel.setRows(List.of());
    status.setText(" ");
    setEmptyStateContent(
        message("interceptors.title.single"),
        message("interceptors.empty.removed"),
        false);
    setEditorTabsVisible(false);
    resetControls();
    loading = false;
    setControlsEnabled(false);
  }

  private void loadInterceptorSnapshot(long req, String sid, String iid) {
    InterceptorDefinition def;
    List<InterceptorHit> sorted;
    try {
      def = store.interceptor(sid, iid);
      if (def == null) {
        SwingUtilities.invokeLater(() -> applyMissingInterceptorState(req));
        return;
      }

      List<InterceptorHit> hits = store.listHits(sid, iid, 2_000);
      ArrayList<InterceptorHit> tmp = new ArrayList<>(hits);
      tmp.sort(
          (a, b) -> {
            Instant aa = a == null ? null : a.at();
            Instant bb = b == null ? null : b.at();
            if (aa == null && bb == null) return 0;
            if (aa == null) return 1;
            if (bb == null) return -1;
            return bb.compareTo(aa);
          });
      sorted = List.copyOf(tmp);
    } catch (Exception ignored) {
      SwingUtilities.invokeLater(() -> applyMissingInterceptorState(req));
      return;
    }

    InterceptorDefinition loadedDef = def;
    List<InterceptorHit> loadedHits = sorted;
    SwingUtilities.invokeLater(
        () -> applyLoadedInterceptorSnapshot(req, sid, iid, loadedDef, loadedHits));
  }

  private void applyLoadedInterceptorSnapshot(
      long req,
      String sid,
      String iid,
      InterceptorDefinition def,
      List<InterceptorHit> sortedHits) {
    if (req != refreshSeq.get()) return;
    if (!Objects.equals(serverId, sid) || !Objects.equals(interceptorId, iid)) return;
    if (def == null) {
      applyMissingInterceptorState(req);
      return;
    }

    loading = true;
    title.setText(message("interceptors.title.named", def.name()));
    subtitle.setText(
        def.scopeAnyServer()
            ? message("interceptors.subtitle.scopeAnyServer")
            : message("interceptors.subtitle.scopeThisServer"));
    setEmptyStateContent("", "", false);
    setEditorTabsVisible(true);

    interceptorName.setText(def.name());
    enabled.setSelected(def.enabled());
    serverScope.setSelectedItem(
        def.scopeAnyServer() ? ServerScopeOption.ANY_SERVER : ServerScopeOption.THIS_SERVER);

    includeMode.setSelectedItem(def.channelIncludeMode());
    includes.setText(def.channelIncludes());
    excludeMode.setSelectedItem(def.channelExcludeMode());
    excludes.setText(def.channelExcludes());

    actionStatusBarEnabled.setSelected(def.actionStatusBarEnabled());
    actionToastEnabled.setSelected(def.actionToastEnabled());

    actionSoundEnabled.setSelected(def.actionSoundEnabled());
    actionSoundId.setSelectedItem(BuiltInSound.fromId(def.actionSoundId()));
    actionSoundUseCustom.setSelected(def.actionSoundUseCustom());
    actionSoundCustomPath.setText(def.actionSoundCustomPath());

    actionScriptEnabled.setSelected(def.actionScriptEnabled());
    actionScriptPath.setText(def.actionScriptPath());
    actionScriptArgs.setText(def.actionScriptArgs());
    actionScriptWorkingDirectory.setText(def.actionScriptWorkingDirectory());

    rulesModel.setRows(def.rules());
    List<InterceptorHit> hits = sortedHits == null ? List.of() : sortedHits;
    hitsModel.setRows(hits);
    status.setText(message("interceptors.status.hitsAndRules", hits.size(), def.rules().size()));

    loading = false;
    setControlsEnabled(true);
    updateRuleButtons();
    updateHitButtons();
  }

  private void resetControls() {
    enabled.setSelected(false);
    interceptorName.setText("");
    serverScope.setSelectedItem(ServerScopeOption.THIS_SERVER);

    includeMode.setSelectedItem(InterceptorRuleMode.ALL);
    includes.setText("");
    excludeMode.setSelectedItem(InterceptorRuleMode.NONE);
    excludes.setText("");

    actionStatusBarEnabled.setSelected(false);
    actionToastEnabled.setSelected(false);

    actionSoundEnabled.setSelected(false);
    actionSoundId.setSelectedItem(BuiltInSound.NOTIF_1);
    actionSoundUseCustom.setSelected(false);
    actionSoundCustomPath.setText("");

    actionScriptEnabled.setSelected(false);
    actionScriptPath.setText("");
    actionScriptArgs.setText("");
    actionScriptWorkingDirectory.setText("");

    refreshChannelFilterControlEnabledState();
    refreshActionControlEnabledState();
    updateRuleButtons();
    updateHitButtons();
  }

  private void refreshActionControlEnabledState() {
    boolean soundOn = controlsEnabled && actionSoundEnabled.isSelected();
    boolean soundCustom = soundOn && actionSoundUseCustom.isSelected();
    actionSoundId.setEnabled(soundOn && !actionSoundUseCustom.isSelected());
    actionSoundUseCustom.setEnabled(soundOn);
    actionSoundCustomPath.setEnabled(soundCustom);
    browseSoundCustomPath.setEnabled(soundCustom);
    testSound.setEnabled(soundOn);

    boolean scriptOn = controlsEnabled && actionScriptEnabled.isSelected();
    actionScriptPath.setEnabled(scriptOn);
    actionScriptArgs.setEnabled(scriptOn);
    actionScriptWorkingDirectory.setEnabled(scriptOn);
    browseScriptPath.setEnabled(scriptOn);
    browseScriptWorkingDirectory.setEnabled(scriptOn);
  }

  private void refreshChannelFilterControlEnabledState() {
    boolean includePatternEnabled =
        controlsEnabled
            && isPatternTextFieldEnabledForMode(selectedMode(includeMode, InterceptorRuleMode.ALL));
    includes.setEnabled(includePatternEnabled);
    includes.setEditable(includePatternEnabled);

    boolean excludePatternEnabled =
        controlsEnabled
            && isPatternTextFieldEnabledForMode(
                selectedMode(excludeMode, InterceptorRuleMode.NONE));
    excludes.setEnabled(excludePatternEnabled);
    excludes.setEditable(excludePatternEnabled);
  }

  private void setControlsEnabled(boolean enabled) {
    controlsEnabled = enabled;

    this.enabled.setEnabled(enabled);
    interceptorName.setEnabled(enabled);
    serverScope.setEnabled(enabled);
    includeMode.setEnabled(enabled);
    excludeMode.setEnabled(enabled);

    actionStatusBarEnabled.setEnabled(enabled);
    actionToastEnabled.setEnabled(enabled);

    actionSoundEnabled.setEnabled(enabled);
    actionScriptEnabled.setEnabled(enabled);

    rulesTable.setEnabled(enabled);
    hitsTable.setEnabled(enabled);

    refreshChannelFilterControlEnabledState();
    refreshActionControlEnabledState();
    updateRuleButtons();
    updateHitButtons();
  }

  private void updateRuleButtons() {
    boolean hasSelection = selectedRuleModelRow() >= 0;
    addRule.setEnabled(controlsEnabled);
    editRule.setEnabled(controlsEnabled && hasSelection);
    removeRule.setEnabled(controlsEnabled && hasSelection);
  }

  private void updateHitButtons() {
    boolean hasRows = hitsModel.getRowCount() > 0;
    boolean hasSelection = hitsTable.getSelectedRowCount() > 0;
    boolean canJump = false;
    if (controlsEnabled && hitsTable.getSelectedRowCount() == 1) {
      InterceptorHit hit = selectedSingleHit();
      canJump =
          hit != null
              && !Objects.toString(hit.messageId(), "").trim().isEmpty()
              && targetRefForHit(hit) != null;
    }

    clearSelectedHits.setEnabled(controlsEnabled && hasSelection);
    clearHits.setEnabled(controlsEnabled && hasRows);
    exportHitsCsv.setEnabled(controlsEnabled && hasRows);
    hitsPopupJumpToMessage.setEnabled(controlsEnabled && canJump);
    hitsPopupClearSelected.setEnabled(controlsEnabled && hasSelection);
    hitsPopupClearAll.setEnabled(controlsEnabled && hasRows);
    hitsPopupExportSelectedCsv.setEnabled(controlsEnabled && hasSelection);
    hitsPopupExportAllCsv.setEnabled(controlsEnabled && hasRows);
  }

  public boolean consumeLocalDefinitionStoreChangeRefreshSkip() {
    while (true) {
      long pending = pendingLocalDefinitionRefreshSkips.get();
      if (pending <= 0L) return false;
      if (pendingLocalDefinitionRefreshSkips.compareAndSet(pending, pending - 1L)) {
        return true;
      }
    }
  }

  private void releasePendingLocalDefinitionRefreshSkip() {
    while (true) {
      long pending = pendingLocalDefinitionRefreshSkips.get();
      if (pending <= 0L) return;
      if (pendingLocalDefinitionRefreshSkips.compareAndSet(pending, pending - 1L)) {
        return;
      }
    }
  }

  private void applySavedDefinitionPresentation(InterceptorDefinition definition, int ruleCount) {
    String name = definition == null ? "" : Objects.toString(definition.name(), "").trim();
    if (name.isEmpty()) name = message("interceptors.defaultName");
    title.setText(message("interceptors.title.named", name));
    subtitle.setText(
        definition != null && definition.scopeAnyServer()
            ? message("interceptors.subtitle.scopeAnyServer")
            : message("interceptors.subtitle.scopeThisServer"));
    status.setText(
        message("interceptors.status.hitsAndRules", hitsModel.getRowCount(), Math.max(0, ruleCount)));
  }

  private void saveCurrentDefinition() {
    if (loading) return;
    String sid = serverId;
    String iid = interceptorId;
    if (sid.isBlank() || iid.isBlank()) return;

    InterceptorDefinition current = store.interceptor(sid, iid);
    if (current == null) return;

    List<InterceptorRule> rules = rulesModel.snapshot();
    String soundId =
        defaultInterceptorSoundIdForRulesIfGeneric(
            selectedSoundId(), actionSoundUseCustom.isSelected(), rules);

    InterceptorDefinition updated =
        new InterceptorDefinition(
            current.id(),
            interceptorName.getText(),
            enabled.isSelected(),
            serverScope.getSelectedItem() == ServerScopeOption.ANY_SERVER ? "" : sid,
            selectedMode(includeMode, InterceptorRuleMode.ALL),
            includes.getText(),
            selectedMode(excludeMode, InterceptorRuleMode.NONE),
            excludes.getText(),
            actionSoundEnabled.isSelected(),
            actionStatusBarEnabled.isSelected(),
            actionToastEnabled.isSelected(),
            soundId,
            actionSoundUseCustom.isSelected(),
            actionSoundCustomPath.getText(),
            actionScriptEnabled.isSelected(),
            actionScriptPath.getText(),
            actionScriptArgs.getText(),
            actionScriptWorkingDirectory.getText(),
            rules);

    int skipConsumers = 1 + (hasExternalStoreChangeRefreshConsumer ? 1 : 0);
    pendingLocalDefinitionRefreshSkips.addAndGet(skipConsumers);
    boolean changed = store.saveInterceptor(sid, updated);
    if (!changed) {
      for (int i = 0; i < skipConsumers; i++) {
        releasePendingLocalDefinitionRefreshSkip();
      }
      return;
    }
    applySavedDefinitionPresentation(updated, rules.size());
    if (!Objects.equals(current.name(), updated.name())) {
      onLocalDefinitionNameChanged.run();
    }
  }

  private static InterceptorRuleMode selectedMode(
      JComboBox<InterceptorRuleMode> combo, InterceptorRuleMode fallback) {
    Object selected = combo.getSelectedItem();
    if (selected instanceof InterceptorRuleMode mode) return mode;
    return fallback;
  }

  private static InterceptorRule defaultRule(int index) {
    int n = Math.max(1, index);
    return new InterceptorRule(
        true,
        message("interceptors.rule.defaultLabel", n),
        "",
        InterceptorRuleMode.LIKE,
        "",
        InterceptorRuleMode.ALL,
        "",
        InterceptorRuleMode.ALL,
        "");
  }

  private String selectedSoundId() {
    Object selected = actionSoundId.getSelectedItem();
    if (selected instanceof BuiltInSound sound) return sound.name();
    return BuiltInSound.NOTIF_1.name();
  }

  private static String defaultInterceptorSoundIdForRulesIfGeneric(
      String selectedSoundId, boolean useCustomSound, List<InterceptorRule> rules) {
    BuiltInSound current = BuiltInSound.fromId(selectedSoundId);
    if (useCustomSound || current != BuiltInSound.NOTIF_1) return current.name();

    BuiltInSound suggested = suggestedDefaultSoundForRules(rules);
    return suggested != null ? suggested.name() : current.name();
  }

  private static BuiltInSound suggestedDefaultSoundForRules(List<InterceptorRule> rules) {
    if (rules == null || rules.isEmpty()) return null;

    BuiltInSound best = null;
    int bestPriority = Integer.MIN_VALUE;

    // If multiple enabled rules/event types are present, prefer the most specific
    // event sound over generic message/action sounds.
    for (InterceptorRule rule : rules) {
      if (rule == null || !rule.enabled()) continue;

      EnumSet<InterceptorEventType> eventTypes =
          InterceptorEventType.parseCsv(rule.eventTypesCsv());
      for (InterceptorEventType eventType : eventTypes) {
        if (eventType == null) continue;

        BuiltInSound candidate = defaultBuiltInSoundForInterceptorEventType(eventType);
        if (candidate == null) continue;

        int priority = defaultSoundPriorityForInterceptorEventType(eventType);
        if (best == null || priority > bestPriority) {
          best = candidate;
          bestPriority = priority;
        }
      }
    }
    return best;
  }

  private static BuiltInSound defaultBuiltInSoundForInterceptorEventType(
      InterceptorEventType eventType) {
    if (eventType == null) return null;
    return switch (eventType) {
      case HIGHLIGHT -> BuiltInSound.YOU_HIGHLIGHTED_1;
      case PRIVATE_MESSAGE -> BuiltInSound.PM_RECEIVED_1;
      case PRIVATE_ACTION -> BuiltInSound.PM_RECEIVED_1;
      case NOTICE -> BuiltInSound.NOTICE_RECEIVED_1;
      case INVITE -> BuiltInSound.CHANNEL_INVITE_1;
      case KICK -> BuiltInSound.SOMEBODY_GOT_KICKED;
      case CTCP -> BuiltInSound.SOMEBODY_SENT_CTCP_1;
      case JOIN -> BuiltInSound.USER_JOINED;
      case PART -> BuiltInSound.USER_LEFT_CHANNEL;
      case QUIT -> BuiltInSound.USER_DISCONNECTED_SERVER;
      case NICK -> BuiltInSound.SOMEBODY_NICK_CHANGED;
      case TOPIC -> BuiltInSound.TOPIC_CHANGED_1;
      case MESSAGE -> BuiltInSound.SOMEBODY_SAID_SOMETHING_1;
      case ACTION -> BuiltInSound.SOMEBODY_SAID_SOMETHING_1;
      case MODE -> BuiltInSound.UNKNOWN_EVENT_2;
      case SERVER -> BuiltInSound.UNKNOWN_EVENT_1;
      case ERROR -> BuiltInSound.UNKNOWN_EVENT_3;
    };
  }

  private static int defaultSoundPriorityForInterceptorEventType(InterceptorEventType eventType) {
    if (eventType == null) return Integer.MIN_VALUE;
    return switch (eventType) {
      case HIGHLIGHT -> 1000;
      case PRIVATE_MESSAGE, PRIVATE_ACTION -> 900;
      case ERROR -> 850;
      case NOTICE -> 800;
      case INVITE -> 750;
      case KICK -> 700;
      case CTCP -> 650;
      case QUIT -> 600;
      case PART -> 590;
      case JOIN -> 580;
      case NICK -> 570;
      case TOPIC -> 560;
      case MODE -> 550;
      case SERVER -> 500;
      case MESSAGE, ACTION -> 100;
    };
  }

  private static String modeLabel(InterceptorRuleMode mode) {
    if (mode == null) return message("interceptors.mode.like");
    return switch (mode) {
      case ALL -> message("interceptors.mode.all");
      case NONE -> message("interceptors.mode.none");
      case LIKE -> message("interceptors.mode.like");
      case GLOB -> message("interceptors.mode.glob");
      case REGEX -> message("interceptors.mode.regex");
    };
  }

  private enum ServerScopeOption {
    THIS_SERVER("interceptors.scope.thisServer"),
    ANY_SERVER("interceptors.scope.anyServer");

    private final String label;

    ServerScopeOption(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return message(label);
    }
  }

  private static final class RulesTableModel extends AbstractTableModel {
    private static final String[] COL_KEYS = {
      "interceptors.rules.column.on",
      "interceptors.rules.column.why",
      "interceptors.rules.column.events",
      "interceptors.rules.column.message",
      "interceptors.rules.column.nick",
      "interceptors.rules.column.hostmask"
    };
    private final List<InterceptorRule> rows = new ArrayList<>();

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public int getColumnCount() {
      return COL_KEYS.length;
    }

    @Override
    public String getColumnName(int column) {
      return column >= 0 && column < COL_KEYS.length ? message(COL_KEYS[column]) : "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return columnIndex == 0 ? Boolean.class : String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < 0 || rowIndex >= rows.size()) return "";
      InterceptorRule r = rows.get(rowIndex);
      if (r == null) return "";
      return switch (columnIndex) {
        case 0 -> r.enabled();
        case 1 -> r.label();
        case 2 -> formatEventTypesForDisplay(r.eventTypesCsv());
        case 3 -> summarizeDimension(r.messageMode(), r.messagePattern());
        case 4 -> summarizeDimension(r.nickMode(), r.nickPattern());
        case 5 -> summarizeDimension(r.hostmaskMode(), r.hostmaskPattern());
        default -> "";
      };
    }

    void setRows(List<InterceptorRule> rules) {
      rows.clear();
      if (rules != null) {
        for (InterceptorRule r : rules) {
          if (r == null) continue;
          rows.add(r);
        }
      }
      fireTableDataChanged();
    }

    int addRule(InterceptorRule rule) {
      if (rule == null) return -1;
      rows.add(rule);
      int row = rows.size() - 1;
      fireTableRowsInserted(row, row);
      return row;
    }

    InterceptorRule ruleAt(int row) {
      if (row < 0 || row >= rows.size()) return null;
      return rows.get(row);
    }

    void setRule(int row, InterceptorRule updated) {
      if (row < 0 || row >= rows.size() || updated == null) return;
      rows.set(row, updated);
      fireTableRowsUpdated(row, row);
    }

    void removeRow(int row) {
      if (row < 0 || row >= rows.size()) return;
      rows.remove(row);
      fireTableRowsDeleted(row, row);
    }

    List<InterceptorRule> snapshot() {
      return List.copyOf(rows);
    }

    private static String summarizeDimension(InterceptorRuleMode mode, String pattern) {
      if (mode == InterceptorRuleMode.ALL) return message("interceptors.rules.summary.any");
      String p = Objects.toString(pattern, "").trim();
      if (p.isEmpty()) return message("interceptors.rules.summary.any");
      return modeLabel(mode) + ": " + p;
    }
  }

  private static final class HitsTableModel extends AbstractTableModel {
    private static final String[] COL_KEYS = {
      "interceptors.hits.column.time",
      "interceptors.hits.column.server",
      "interceptors.hits.column.from",
      "interceptors.hits.column.hostmask",
      "interceptors.hits.column.channel",
      "interceptors.hits.column.why",
      "interceptors.hits.column.event",
      "interceptors.hits.column.message"
    };

    private List<InterceptorHit> rows = List.of();

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public int getColumnCount() {
      return COL_KEYS.length;
    }

    @Override
    public String getColumnName(int column) {
      return column >= 0 && column < COL_KEYS.length ? message(COL_KEYS[column]) : "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < 0 || rowIndex >= rows.size()) return "";
      InterceptorHit r = rows.get(rowIndex);
      if (r == null) return "";
      return switch (columnIndex) {
        case 0 -> formatTime(r.at());
        case 1 -> r.serverId();
        case 2 -> r.fromNick();
        case 3 -> r.fromHostmask();
        case 4 -> r.channel();
        case 5 -> r.reason();
        case 6 -> r.eventType();
        case 7 -> r.message();
        default -> "";
      };
    }

    InterceptorHit rowAt(int rowIndex) {
      if (rowIndex < 0 || rowIndex >= rows.size()) return null;
      return rows.get(rowIndex);
    }

    void setRows(List<InterceptorHit> rows) {
      this.rows = rows == null ? List.of() : List.copyOf(rows);
      fireTableDataChanged();
    }

    private static String formatTime(Instant at) {
      if (at == null) return "";
      try {
        return TIME_FMT.format(at);
      } catch (Exception ignored) {
        return at.toString();
      }
    }
  }
}
