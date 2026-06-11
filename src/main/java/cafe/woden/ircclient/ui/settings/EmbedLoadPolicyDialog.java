package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicyScope;
import cafe.woden.ircclient.config.api.EmbedLoadPolicyConfigPort.EmbedLoadPolicySnapshot;
import cafe.woden.ircclient.ui.chat.embed.EmbedLoadPolicyMatcher;
import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import cafe.woden.ircclient.ui.util.UiColorKeys;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Modal editor for advanced embed/link loading policy settings. */
@Component
@InterfaceLayer
@Lazy
public class EmbedLoadPolicyDialog {

  private final EmbedLoadPolicyConfigPort runtimeConfig;
  private final UiMessages messages;

  public EmbedLoadPolicyDialog(EmbedLoadPolicyConfigPort runtimeConfig, UiMessages messages) {
    this.runtimeConfig = runtimeConfig;
    this.messages = messages == null ? UiMessages.bundledDefaults() : messages;
  }

  public Optional<EmbedLoadPolicySnapshot> open(Window owner, EmbedLoadPolicySnapshot seed) {
    if (!SwingUtilities.isEventDispatchThread()) {
      final EmbedLoadPolicySnapshot[] out = {EmbedLoadPolicySnapshot.defaults()};
      final boolean[] changed = {false};
      try {
        SwingUtilities.invokeAndWait(
            () -> {
              Optional<EmbedLoadPolicySnapshot> result = open(owner, seed);
              changed[0] = result.isPresent();
              out[0] = result.orElse(EmbedLoadPolicySnapshot.defaults());
            });
      } catch (Exception ignored) {
      }
      return changed[0] ? Optional.of(out[0]) : Optional.empty();
    }

    EmbedLoadPolicySnapshot initial = seed == null ? EmbedLoadPolicySnapshot.defaults() : seed;

    final EmbedLoadPolicyScope[] globalRef = {initial.global()};
    final LinkedHashMap<String, EmbedLoadPolicyScope> byServerRef =
        new LinkedHashMap<>(initial.byServer());

    List<ScopeOption> options = buildScopeOptions(initial);
    JComboBox<ScopeOption> scope = new JComboBox<>(options.toArray(new ScopeOption[0]));
    scope.setSelectedIndex(0);

    JCheckBox inheritGlobal =
        new JCheckBox(message("preferences.embeds.advancedPolicy.inheritGlobal"));

    PolicyControls controls = buildPolicyControls();

    String whitelist = message("preferences.embeds.advancedPolicy.list.whitelist");
    String blacklist = message("preferences.embeds.advancedPolicy.list.blacklist");

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab(
        message("preferences.embeds.advancedPolicy.tab.users"),
        buildDualTablePanel(
            whitelist, blacklist, controls.userWhitelist(), controls.userBlacklist()));
    tabs.addTab(
        message("preferences.embeds.advancedPolicy.tab.channels"),
        buildDualTablePanel(
            whitelist, blacklist, controls.channelWhitelist(), controls.channelBlacklist()));
    tabs.addTab(
        message("preferences.embeds.advancedPolicy.tab.links"),
        buildDualTablePanel(
            whitelist, blacklist, controls.linkWhitelist(), controls.linkBlacklist()));
    tabs.addTab(
        message("preferences.embeds.advancedPolicy.tab.domains"),
        buildDualTablePanel(
            whitelist, blacklist, controls.domainWhitelist(), controls.domainBlacklist()));
    tabs.addTab(message("preferences.embeds.advancedPolicy.tab.gates"), buildGatePanel(controls));

    JPanel scopePanel =
        new JPanel(
            MigLayouts.fillXWrap(
                10, 2, MigLayoutConstraints.LEADING_GROW_FILL, MigLayouts.rows(3, 4)));
    scopePanel.add(new JLabel(message("preferences.embeds.advancedPolicy.scope.label")));
    scopePanel.add(scope, MigConstraints.growXWrap());
    scopePanel.add(inheritGlobal, MigConstraints.spanXWrap(2));
    scopePanel.add(
        new JLabel(message("preferences.embeds.advancedPolicy.help.patterns")),
        MigConstraints.spanXWrap(2));
    scopePanel.add(
        new JLabel(message("preferences.embeds.advancedPolicy.help.userRules")),
        MigConstraints.spanXWrap(2));

    JButton save = new JButton(message("common.button.save"));
    JButton cancel = new JButton(message("common.button.cancel"));
    JPanel buttons = new JPanel(MigLayouts.fillX("[grow,fill][pref!][pref!]", "[]"));
    buttons.add(new JPanel(), MigConstraints.pushXGrowX());
    buttons.add(save);
    buttons.add(cancel);

    JPanel root = new JPanel(MigLayouts.singleColumnFill(0, "[][grow,fill][]"));
    root.add(scopePanel, MigConstraints.growX());
    root.add(tabs, MigConstraints.grow());
    root.add(buttons, MigConstraints.growX());

    final EmbedLoadPolicySnapshot[] result = {null};
    Runnable refreshValidation =
        () -> save.setEnabled(validateAllPatternTables(controls, messages));
    installValidationListeners(controls, refreshValidation);

    Runnable applySelection =
        () -> {
          ScopeOption selected =
              PreferencesUiSupport.selectedComboItem(scope, ScopeOption.class, null);
          if (selected == null) return;
          stopTableEditing(controls);
          EmbedLoadPolicyScope currentScope = readScopeFromControls(controls);
          if (selected.global()) {
            globalRef[0] = currentScope;
          } else {
            if (inheritGlobal.isSelected()) {
              byServerRef.remove(selected.serverId());
            } else {
              byServerRef.put(selected.serverId(), currentScope);
            }
          }
        };

    Runnable loadSelection =
        () -> {
          ScopeOption selected =
              PreferencesUiSupport.selectedComboItem(scope, ScopeOption.class, null);
          if (selected == null) return;
          EmbedLoadPolicyScope show;
          boolean editable = true;
          if (selected.global()) {
            inheritGlobal.setVisible(false);
            show = globalRef[0];
          } else {
            inheritGlobal.setVisible(true);
            EmbedLoadPolicyScope override = byServerRef.get(selected.serverId());
            boolean usesGlobal = override == null;
            inheritGlobal.setSelected(usesGlobal);
            show = usesGlobal ? globalRef[0] : override;
            editable = !usesGlobal;
          }

          writeScopeToControls(show, controls);
          setEditable(editable || selected.global(), controls);
          refreshValidation.run();
        };

    scope.addActionListener(
        e -> {
          applySelection.run();
          loadSelection.run();
        });

    inheritGlobal.addActionListener(
        e -> {
          ScopeOption selected =
              PreferencesUiSupport.selectedComboItem(scope, ScopeOption.class, null);
          if (selected == null || selected.global()) return;
          if (inheritGlobal.isSelected()) {
            byServerRef.remove(selected.serverId());
          } else if (!byServerRef.containsKey(selected.serverId())) {
            byServerRef.put(selected.serverId(), EmbedLoadPolicyScope.defaults());
          }
          loadSelection.run();
        });

    JDialog dialog =
        new JDialog(
            owner,
            message("preferences.embeds.advancedPolicy.dialog.title"),
            Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setContentPane(root);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setMinimumSize(new Dimension(920, 680));

    save.addActionListener(
        e -> {
          stopTableEditing(controls);
          if (!validateAllPatternTables(controls, messages)) {
            PreferencesUiSupport.showWarningMessage(
                dialog,
                message("preferences.embeds.advancedPolicy.validation.invalidPattern.message"),
                message("preferences.embeds.advancedPolicy.validation.invalidPattern.title"));
            save.setEnabled(false);
            return;
          }
          applySelection.run();
          result[0] = new EmbedLoadPolicySnapshot(globalRef[0], byServerRef);
          dialog.dispose();
        });
    cancel.addActionListener(e -> dialog.dispose());

    loadSelection.run();
    refreshValidation.run();
    dialog.pack();
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);

    return Optional.ofNullable(result[0]);
  }

  private String message(String code, Object... args) {
    return messages.text(code, args);
  }

  private List<ScopeOption> buildScopeOptions(EmbedLoadPolicySnapshot initial) {
    LinkedHashMap<String, ScopeOption> out = new LinkedHashMap<>();
    out.put(
        "", new ScopeOption("", message("preferences.embeds.advancedPolicy.scope.global"), true));

    List<String> configured = runtimeConfig != null ? runtimeConfig.readServerIds() : List.of();
    for (String serverId : configured) {
      String sid = SettingsValueSupport.trimmedString(serverId);
      if (sid.isEmpty()) continue;
      out.putIfAbsent(
          sid.toLowerCase(java.util.Locale.ROOT),
          new ScopeOption(
              sid, message("preferences.embeds.advancedPolicy.scope.network", sid), false));
    }
    if (initial != null && initial.byServer() != null) {
      for (String serverId : initial.byServer().keySet()) {
        String sid = SettingsValueSupport.trimmedString(serverId);
        if (sid.isEmpty()) continue;
        out.putIfAbsent(
            sid.toLowerCase(java.util.Locale.ROOT),
            new ScopeOption(
                sid, message("preferences.embeds.advancedPolicy.scope.network", sid), false));
      }
    }
    return new ArrayList<>(out.values());
  }

  private PolicyControls buildPolicyControls() {
    PatternTableControls userWhitelist =
        buildPatternTable(
            message("preferences.embeds.advancedPolicy.userWhitelist.title"),
            message("preferences.embeds.advancedPolicy.userWhitelist.hint"));
    PatternTableControls userBlacklist =
        buildPatternTable(
            message("preferences.embeds.advancedPolicy.userBlacklist.title"),
            message("preferences.embeds.advancedPolicy.userBlacklist.hint"));
    PatternTableControls channelWhitelist =
        buildPatternTable(
            message("preferences.embeds.advancedPolicy.channelWhitelist.title"),
            message("preferences.embeds.advancedPolicy.channelWhitelist.hint"));
    PatternTableControls channelBlacklist =
        buildPatternTable(
            message("preferences.embeds.advancedPolicy.channelBlacklist.title"),
            message("preferences.embeds.advancedPolicy.channelBlacklist.hint"));
    PatternTableControls linkWhitelist =
        buildPatternTable(
            message("preferences.embeds.advancedPolicy.linkWhitelist.title"),
            message("preferences.embeds.advancedPolicy.linkWhitelist.hint"));
    PatternTableControls linkBlacklist =
        buildPatternTable(
            message("preferences.embeds.advancedPolicy.linkBlacklist.title"),
            message("preferences.embeds.advancedPolicy.linkBlacklist.hint"));
    PatternTableControls domainWhitelist =
        buildPatternTable(
            message("preferences.embeds.advancedPolicy.domainWhitelist.title"),
            message("preferences.embeds.advancedPolicy.domainWhitelist.hint"));
    PatternTableControls domainBlacklist =
        buildPatternTable(
            message("preferences.embeds.advancedPolicy.domainBlacklist.title"),
            message("preferences.embeds.advancedPolicy.domainBlacklist.hint"));

    JCheckBox requireVoiceOrOp =
        new JCheckBox(message("preferences.embeds.advancedPolicy.gates.requireVoiceOrOp"));
    JCheckBox requireLoggedIn =
        new JCheckBox(message("preferences.embeds.advancedPolicy.gates.requireLoggedIn"));
    JSpinner minAccountAgeDays = PreferencesUiSupport.numberSpinner(0, 0, 36500, 1);

    return new PolicyControls(
        userWhitelist,
        userBlacklist,
        channelWhitelist,
        channelBlacklist,
        linkWhitelist,
        linkBlacklist,
        domainWhitelist,
        domainBlacklist,
        requireVoiceOrOp,
        requireLoggedIn,
        minAccountAgeDays);
  }

  private static JPanel buildDualTablePanel(
      String leftTitle, String rightTitle, PatternTableControls left, PatternTableControls right) {
    JPanel panel =
        new JPanel(
            MigLayouts.fillWrap(
                10, 2, MigLayoutConstraints.GROW_FILL_PAIR, MigLayoutConstraints.GROW_FILL));
    panel.add(buildLabeledPanel(leftTitle, left.panel()), MigConstraints.grow());
    panel.add(buildLabeledPanel(rightTitle, right.panel()), MigConstraints.grow());
    return panel;
  }

  private static JPanel buildLabeledPanel(String title, JPanel content) {
    JPanel panel =
        new JPanel(
            MigLayouts.fillWrap(
                0, 1, MigLayoutConstraints.GROW_FILL, MigLayoutConstraints.LEADING_GROW_FILL));
    panel.add(new JLabel(title), MigConstraints.growX());
    panel.add(content, MigConstraints.grow());
    return panel;
  }

  private JPanel buildGatePanel(PolicyControls controls) {
    JPanel panel =
        new JPanel(
            MigLayouts.fillXWrap(
                10, 2, MigLayoutConstraints.LEADING_GROW_FILL, MigLayouts.rows(4, 6)));
    panel.add(controls.requireVoiceOrOp(), MigConstraints.spanXWrap(2));
    panel.add(controls.requireLoggedIn(), MigConstraints.spanXWrap(2));
    panel.add(new JLabel(message("preferences.embeds.advancedPolicy.gates.minAccountAgeDays")));
    panel.add(controls.minAccountAgeDays(), MigConstraints.widthWrap(120));
    panel.add(
        new JLabel(message("preferences.embeds.advancedPolicy.gates.failClosed")),
        MigConstraints.spanXWrap(2));
    return panel;
  }

  private PatternTableControls buildPatternTable(String title, String hint) {
    DefaultTableModel model =
        new DefaultTableModel(
            new Object[] {message("preferences.embeds.advancedPolicy.column.pattern")}, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return true;
          }
        };
    JTable table = new JTable(model);
    Set<Integer> invalidRows = new LinkedHashSet<>();
    Color errorBg = resolveValidationErrorBackground();
    Color errorFg = resolveValidationErrorForeground();
    table.setDefaultRenderer(
        Object.class,
        new DefaultTableCellRenderer() {
          @Override
          public java.awt.Component getTableCellRendererComponent(
              JTable table,
              Object value,
              boolean isSelected,
              boolean hasFocus,
              int row,
              int column) {
            java.awt.Component c =
                super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
              if (invalidRows.contains(row)) {
                c.setBackground(errorBg);
                c.setForeground(errorFg);
              } else {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
              }
            }
            return c;
          }
        });
    table.setFillsViewportHeight(true);
    table.setRowHeight(24);
    SettingsTableSupport.disableColumnReordering(table);

    JButton add = new JButton(message("common.button.add"));
    JButton remove = new JButton(message("common.button.remove"));
    JButton up = new JButton(message("common.button.up"));
    JButton down = new JButton(message("common.button.down"));

    add.addActionListener(
        e -> {
          model.addRow(new Object[] {""});
          int row = model.getRowCount() - 1;
          if (row >= 0) {
            SettingsTableSupport.selectModelRow(table, row);
            table.editCellAt(row, 0);
            if (table.getEditorComponent() != null) {
              table.getEditorComponent().requestFocus();
            }
          }
        });
    remove.addActionListener(
        e -> {
          SettingsTableSupport.stopEditing(table);
          int[] rows = table.getSelectedRows();
          for (int i = rows.length - 1; i >= 0; i--) {
            model.removeRow(rows[i]);
          }
        });
    up.addActionListener(
        e -> {
          SettingsTableSupport.stopEditing(table);
          int row = table.getSelectedRow();
          if (row <= 0) return;
          Object value = model.getValueAt(row, 0);
          model.removeRow(row);
          model.insertRow(row - 1, new Object[] {value});
          SettingsTableSupport.selectModelRow(table, row - 1);
        });
    down.addActionListener(
        e -> {
          SettingsTableSupport.stopEditing(table);
          int row = table.getSelectedRow();
          if (row < 0 || row >= model.getRowCount() - 1) return;
          Object value = model.getValueAt(row, 0);
          model.removeRow(row);
          model.insertRow(row + 1, new Object[] {value});
          SettingsTableSupport.selectModelRow(table, row + 1);
        });

    JPanel actions =
        new JPanel(MigLayouts.wrap(0, 1, MigLayoutConstraints.GROW_FILL, MigLayouts.rows(4, 4)));
    actions.add(add, MigConstraints.growX());
    actions.add(remove, MigConstraints.growX());
    actions.add(up, MigConstraints.growX());
    actions.add(down, MigConstraints.growX());

    JLabel validation = new JLabel(" ");
    validation.setForeground(errorFg);

    JPanel panel = new JPanel(MigLayouts.fillWrap(0, 2, "[grow,fill][pref!]", "[][][grow,fill][]"));
    panel.add(new JLabel(title), MigConstraints.span2GrowXWrap());
    panel.add(new JLabel(hint), MigConstraints.span2GrowXWrap());
    panel.add(new JScrollPane(table), MigConstraints.grow());
    panel.add(actions, MigConstraints.alignYTop());
    panel.add(validation, MigConstraints.span2GrowXWrap());

    return new PatternTableControls(
        model, table, add, remove, up, down, panel, validation, invalidRows);
  }

  private static EmbedLoadPolicyScope readScopeFromControls(PolicyControls controls) {
    stopTableEditing(controls);
    return new EmbedLoadPolicyScope(
        readPatternRows(controls.userWhitelist().model()),
        readPatternRows(controls.userBlacklist().model()),
        readPatternRows(controls.channelWhitelist().model()),
        readPatternRows(controls.channelBlacklist().model()),
        controls.requireVoiceOrOp().isSelected(),
        controls.requireLoggedIn().isSelected(),
        PreferencesUiSupport.spinnerInt(controls.minAccountAgeDays()),
        readPatternRows(controls.linkWhitelist().model()),
        readPatternRows(controls.linkBlacklist().model()),
        readPatternRows(controls.domainWhitelist().model()),
        readPatternRows(controls.domainBlacklist().model()));
  }

  private static void writeScopeToControls(EmbedLoadPolicyScope scope, PolicyControls controls) {
    EmbedLoadPolicyScope s = scope == null ? EmbedLoadPolicyScope.defaults() : scope;
    writePatternRows(controls.userWhitelist().model(), s.userWhitelist());
    writePatternRows(controls.userBlacklist().model(), s.userBlacklist());
    writePatternRows(controls.channelWhitelist().model(), s.channelWhitelist());
    writePatternRows(controls.channelBlacklist().model(), s.channelBlacklist());
    controls.requireVoiceOrOp().setSelected(s.requireVoiceOrOp());
    controls.requireLoggedIn().setSelected(s.requireLoggedIn());
    controls.minAccountAgeDays().setValue(Math.max(0, s.minAccountAgeDays()));
    writePatternRows(controls.linkWhitelist().model(), s.linkWhitelist());
    writePatternRows(controls.linkBlacklist().model(), s.linkBlacklist());
    writePatternRows(controls.domainWhitelist().model(), s.domainWhitelist());
    writePatternRows(controls.domainBlacklist().model(), s.domainBlacklist());
  }

  private static void setEditable(boolean enabled, PolicyControls controls) {
    setPatternTableEditable(controls.userWhitelist(), enabled);
    setPatternTableEditable(controls.userBlacklist(), enabled);
    setPatternTableEditable(controls.channelWhitelist(), enabled);
    setPatternTableEditable(controls.channelBlacklist(), enabled);
    setPatternTableEditable(controls.linkWhitelist(), enabled);
    setPatternTableEditable(controls.linkBlacklist(), enabled);
    setPatternTableEditable(controls.domainWhitelist(), enabled);
    setPatternTableEditable(controls.domainBlacklist(), enabled);
    controls.requireVoiceOrOp().setEnabled(enabled);
    controls.requireLoggedIn().setEnabled(enabled);
    controls.minAccountAgeDays().setEnabled(enabled);
  }

  private static void setPatternTableEditable(PatternTableControls controls, boolean enabled) {
    controls.table().setEnabled(enabled);
    controls.add().setEnabled(enabled);
    controls.remove().setEnabled(enabled);
    controls.up().setEnabled(enabled);
    controls.down().setEnabled(enabled);
  }

  private static void installValidationListeners(PolicyControls controls, Runnable onChanged) {
    installPatternTableValidationListener(controls.userWhitelist(), onChanged);
    installPatternTableValidationListener(controls.userBlacklist(), onChanged);
    installPatternTableValidationListener(controls.channelWhitelist(), onChanged);
    installPatternTableValidationListener(controls.channelBlacklist(), onChanged);
    installPatternTableValidationListener(controls.linkWhitelist(), onChanged);
    installPatternTableValidationListener(controls.linkBlacklist(), onChanged);
    installPatternTableValidationListener(controls.domainWhitelist(), onChanged);
    installPatternTableValidationListener(controls.domainBlacklist(), onChanged);
  }

  private static void installPatternTableValidationListener(
      PatternTableControls controls, Runnable onChanged) {
    if (controls == null || controls.model() == null || onChanged == null) return;
    controls.model().addTableModelListener(e -> onChanged.run());
  }

  private static boolean validateAllPatternTables(PolicyControls controls, UiMessages messages) {
    boolean valid = true;
    valid &= validatePatternTable(controls.userWhitelist(), messages);
    valid &= validatePatternTable(controls.userBlacklist(), messages);
    valid &= validatePatternTable(controls.channelWhitelist(), messages);
    valid &= validatePatternTable(controls.channelBlacklist(), messages);
    valid &= validatePatternTable(controls.linkWhitelist(), messages);
    valid &= validatePatternTable(controls.linkBlacklist(), messages);
    valid &= validatePatternTable(controls.domainWhitelist(), messages);
    valid &= validatePatternTable(controls.domainBlacklist(), messages);
    return valid;
  }

  private static boolean validatePatternTable(PatternTableControls controls, UiMessages messages) {
    if (controls == null) return true;
    PatternValidation validation = validatePatternRows(controls.model(), messages);
    controls.invalidRows().clear();
    controls.invalidRows().addAll(validation.invalidRows());
    controls.table().repaint();
    if (validation.isValid()) {
      controls.validation().setText(" ");
      return true;
    }
    controls.validation().setText(validation.message());
    return false;
  }

  private static PatternValidation validatePatternRows(
      DefaultTableModel model, UiMessages messages) {
    if (model == null || model.getRowCount() == 0) {
      return PatternValidation.clean();
    }
    List<Integer> invalidRows = new ArrayList<>();
    String message = "";
    for (int row = 0; row < model.getRowCount(); row++) {
      String value = SettingsValueSupport.trimmedString(model.getValueAt(row, 0));
      if (value.isEmpty()) continue;
      Optional<String> error = EmbedLoadPolicyMatcher.validatePatternSyntax(value);
      if (error.isEmpty()) continue;
      invalidRows.add(row);
      if (message.isBlank()) {
        message =
            messages.text("preferences.embeds.advancedPolicy.validation.row", row + 1, error.get());
      }
    }
    if (invalidRows.isEmpty()) {
      return PatternValidation.clean();
    }
    return new PatternValidation(List.copyOf(invalidRows), message);
  }

  private static Color resolveValidationErrorBackground() {
    Color c = UIManager.getColor(UiColorKeys.COMPONENT_ERROR_BACKGROUND);
    if (c != null) return c;
    c = UIManager.getColor(UiColorKeys.TEXT_FIELD_ERROR_BACKGROUND);
    if (c != null) return c;
    return new Color(255, 236, 236);
  }

  private static Color resolveValidationErrorForeground() {
    Color c = UIManager.getColor(UiColorKeys.COMPONENT_ERROR_FOREGROUND);
    if (c != null) return c;
    c = UIManager.getColor(UiColorKeys.COMPONENT_ERROR_FOCUSED_BORDER_COLOR);
    if (c != null) return c;
    return new Color(150, 25, 25);
  }

  private static void stopTableEditing(PolicyControls controls) {
    SettingsTableSupport.stopEditing(
        controls.userWhitelist().table(),
        controls.userBlacklist().table(),
        controls.channelWhitelist().table(),
        controls.channelBlacklist().table(),
        controls.linkWhitelist().table(),
        controls.linkBlacklist().table(),
        controls.domainWhitelist().table(),
        controls.domainBlacklist().table());
  }

  private static List<String> readPatternRows(DefaultTableModel model) {
    if (model == null || model.getRowCount() == 0) return List.of();
    LinkedHashMap<String, String> seen = new LinkedHashMap<>();
    for (int row = 0; row < model.getRowCount(); row++) {
      String v = SettingsValueSupport.trimmedString(model.getValueAt(row, 0));
      if (v.isEmpty()) continue;
      seen.putIfAbsent(v, v);
    }
    return seen.isEmpty() ? List.of() : List.copyOf(seen.values());
  }

  private static void writePatternRows(DefaultTableModel model, List<String> rows) {
    if (model == null) return;
    model.setRowCount(0);
    if (rows == null || rows.isEmpty()) return;
    LinkedHashMap<String, String> seen = new LinkedHashMap<>();
    for (String row : rows) {
      String v = SettingsValueSupport.trimmedString(row);
      if (v.isEmpty()) continue;
      seen.putIfAbsent(v, v);
    }
    for (String value : seen.values()) {
      model.addRow(new Object[] {value});
    }
  }

  private record PolicyControls(
      PatternTableControls userWhitelist,
      PatternTableControls userBlacklist,
      PatternTableControls channelWhitelist,
      PatternTableControls channelBlacklist,
      PatternTableControls linkWhitelist,
      PatternTableControls linkBlacklist,
      PatternTableControls domainWhitelist,
      PatternTableControls domainBlacklist,
      JCheckBox requireVoiceOrOp,
      JCheckBox requireLoggedIn,
      JSpinner minAccountAgeDays) {}

  private record PatternTableControls(
      DefaultTableModel model,
      JTable table,
      JButton add,
      JButton remove,
      JButton up,
      JButton down,
      JPanel panel,
      JLabel validation,
      Set<Integer> invalidRows) {}

  private record PatternValidation(List<Integer> invalidRows, String message) {
    private static PatternValidation clean() {
      return new PatternValidation(List.of(), "");
    }

    private boolean isValid() {
      return invalidRows == null || invalidRows.isEmpty();
    }
  }

  private record ScopeOption(String serverId, String label, boolean global) {
    @Override
    public String toString() {
      return label;
    }
  }
}
