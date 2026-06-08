package cafe.woden.ircclient.ui.ignore;

import cafe.woden.ircclient.ignore.IgnoreListService;
import cafe.woden.ircclient.ignore.api.IgnoreAddMaskResult;
import cafe.woden.ircclient.ignore.api.IgnoreLevels;
import cafe.woden.ircclient.ignore.api.IgnoreTextPatternMode;
import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.localization.UiMessages;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
@Lazy
public class IgnoreListDialog {

  public enum Tab {
    IGNORE,
    SOFT_IGNORE
  }

  private final IgnoreListService ignores;
  private final UiMessages messages;

  private JDialog dialog;
  private String currentServerId;
  private JTabbedPane tabs;
  private boolean hardIgnoreAdvancedMode;

  private DefaultListModel<MaskRow> ignoreModel;
  private DefaultListModel<MaskRow> softModel;

  public IgnoreListDialog(IgnoreListService ignores, UiMessages messages) {
    this.ignores = ignores;
    this.messages = messages == null ? UiMessages.bundledDefaults() : messages;
  }

  public void open(Window owner, String serverId) {
    open(owner, serverId, Tab.IGNORE);
  }

  public void open(Window owner, String serverId, Tab initialTab) {
    final String sid = Objects.toString(serverId, "").trim();
    if (sid.isEmpty()) return;

    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> open(owner, sid, initialTab));
      return;
    }

    // If already open for the same server, just focus it and switch tab.
    if (dialog != null && dialog.isShowing() && Objects.equals(currentServerId, sid)) {
      if (tabs != null) {
        tabs.setSelectedIndex(initialTab == Tab.SOFT_IGNORE ? 1 : 0);
      }
      dialog.toFront();
      dialog.requestFocus();
      return;
    }

    // If open for a different server, rebuild.
    if (dialog != null) {
      try {
        dialog.dispose();
      } catch (Exception ignored) {
      }
      dialog = null;
    }
    currentServerId = sid;
    hardIgnoreAdvancedMode = false;

    ignoreModel = new DefaultListModel<>();
    softModel = new DefaultListModel<>();
    refreshIgnore(ignoreModel, sid);
    refreshSoft(softModel, sid);

    JLabel help = new JLabel(message("ignoreLists.help"));
    help.putClientProperty(FlatClientProperties.STYLE, "font: -1");

    tabs = new JTabbedPane();
    tabs.addTab(message("ignoreLists.tab.ignore"), buildMaskPanel(sid, Kind.IGNORE, ignoreModel));
    tabs.addTab(
        message("ignoreLists.tab.softIgnore"), buildMaskPanel(sid, Kind.SOFT_IGNORE, softModel));
    tabs.setSelectedIndex(initialTab == Tab.SOFT_IGNORE ? 1 : 0);

    JButton close = new JButton(message("common.button.close"));
    close.setIcon(SvgIcons.action("close", 16));
    close.setDisabledIcon(SvgIcons.actionDisabled("close", 16));
    close.addActionListener(e -> dialog.dispose());

    JCheckBox hardCtcpToggle = new JCheckBox(message("ignoreLists.ctcp.hard"));
    hardCtcpToggle.setSelected(ignores != null && ignores.hardIgnoreIncludesCtcp());
    hardCtcpToggle.setToolTipText(message("ignoreLists.ctcp.hard.tooltip"));
    hardCtcpToggle.addActionListener(
        e -> {
          if (ignores == null) return;
          ignores.setHardIgnoreIncludesCtcp(hardCtcpToggle.isSelected());
        });

    JCheckBox softCtcpToggle = new JCheckBox(message("ignoreLists.ctcp.soft"));
    softCtcpToggle.setSelected(ignores != null && ignores.softIgnoreIncludesCtcp());
    softCtcpToggle.setToolTipText(message("ignoreLists.ctcp.soft.tooltip"));
    softCtcpToggle.addActionListener(
        e -> {
          if (ignores == null) return;
          ignores.setSoftIgnoreIncludesCtcp(softCtcpToggle.isSelected());
        });

    JPanel toggles = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    toggles.setOpaque(false);
    toggles.add(hardCtcpToggle);
    toggles.add(softCtcpToggle);

    JPanel footer = new JPanel(new BorderLayout());
    footer.add(toggles, BorderLayout.WEST);
    footer.add(close, BorderLayout.EAST);

    JPanel root = new JPanel(new BorderLayout(10, 10));
    root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    root.add(help, BorderLayout.NORTH);
    root.add(tabs, BorderLayout.CENTER);
    root.add(footer, BorderLayout.SOUTH);

    dialog = new JDialog(owner, message("ignoreLists.title", sid));
    dialog.setModal(false);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog
        .getRootPane()
        .registerKeyboardAction(
            e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    dialog.setContentPane(root);
    dialog.pack();
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);
  }

  private enum Kind {
    IGNORE,
    SOFT_IGNORE
  }

  private enum HardIgnoreEditorMode {
    ADD,
    EDIT
  }

  private JPanel buildMaskPanel(String serverId, Kind kind, DefaultListModel<MaskRow> model) {
    JList<MaskRow> list = new JList<>(model);
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    JScrollPane scroll = new JScrollPane(list);
    scroll.setPreferredSize(new Dimension(540, 300));

    JButton add = new JButton(message("ignoreLists.button.add"));
    JButton edit = new JButton(message("ignoreLists.button.editRule"));
    JButton remove = new JButton(message("common.button.remove"));
    JButton copy = new JButton(message("common.button.copy"));

    add.setIcon(SvgIcons.action("plus", 16));
    add.setDisabledIcon(SvgIcons.actionDisabled("plus", 16));
    edit.setIcon(SvgIcons.action("edit", 16));
    edit.setDisabledIcon(SvgIcons.actionDisabled("edit", 16));
    remove.setIcon(SvgIcons.action("trash", 16));
    remove.setDisabledIcon(SvgIcons.actionDisabled("trash", 16));
    copy.setIcon(SvgIcons.action("copy", 16));
    copy.setDisabledIcon(SvgIcons.actionDisabled("copy", 16));

    boolean allowEdit = kind == Kind.IGNORE;
    JCheckBox advancedModeToggle = null;
    JLabel modeHint = null;
    if (allowEdit) {
      advancedModeToggle = new JCheckBox(message("ignoreLists.advancedMode"));
      advancedModeToggle.setSelected(hardIgnoreAdvancedMode);
      advancedModeToggle.setToolTipText(message("ignoreLists.advancedMode.tooltip"));
      modeHint = new JLabel();
      modeHint.putClientProperty(FlatClientProperties.STYLE, "font: -1");
      updateHardIgnoreModeHint(modeHint, hardIgnoreAdvancedMode);
    }
    edit.setEnabled(false);
    remove.setEnabled(false);
    copy.setEnabled(false);

    list.addListSelectionListener(
        e -> {
          if (e.getValueIsAdjusting()) return;
          boolean hasSel = list.getSelectedIndices().length > 0;
          if (allowEdit) {
            edit.setEnabled(list.getSelectedIndices().length == 1);
          }
          remove.setEnabled(hasSel);
          copy.setEnabled(list.getSelectedIndices().length == 1);
        });

    add.addActionListener(
        e -> {
          if (allowEdit && hardIgnoreAdvancedMode) {
            boolean changed = openHardIgnoreRuleEditor(serverId, "", HardIgnoreEditorMode.ADD);
            if (changed) {
              refresh(model, serverId, kind);
            }
            return;
          }
          String title =
              kind == Kind.SOFT_IGNORE
                  ? message("ignoreLists.add.soft.title")
                  : message("ignoreLists.add.hard.title");
          String prompt =
              kind == Kind.SOFT_IGNORE
                  ? message("ignoreLists.add.soft.prompt")
                  : message("ignoreLists.add.hard.prompt");

          String input =
              (String)
                  JOptionPane.showInputDialog(
                      dialog, prompt, title, JOptionPane.PLAIN_MESSAGE, null, null, "");
          if (input == null) return;
          String trimmed = input.trim();
          if (trimmed.isEmpty()) return;

          boolean added;
          if (kind == Kind.SOFT_IGNORE) {
            added = ignores.addSoftMask(serverId, trimmed);
          } else {
            added = ignores.addMask(serverId, trimmed);
          }

          String stored = IgnoreListService.normalizeMaskOrNickToHostmask(trimmed);
          if (!added) {
            JOptionPane.showMessageDialog(
                dialog,
                message("ignoreLists.add.alreadyInList", stored),
                title,
                JOptionPane.INFORMATION_MESSAGE);
          }

          refresh(model, serverId, kind);
        });

    edit.addActionListener(
        e -> {
          if (!allowEdit) return;
          MaskRow row = list.getSelectedValue();
          if (row == null || row.mask().isBlank()) return;
          boolean changed =
              openHardIgnoreRuleEditor(serverId, row.mask(), HardIgnoreEditorMode.EDIT);
          if (changed) {
            refresh(model, serverId, kind);
          }
        });

    if (advancedModeToggle != null) {
      final JCheckBox advancedToggle = advancedModeToggle;
      final JLabel hint = modeHint;
      advancedModeToggle.addActionListener(
          e -> {
            hardIgnoreAdvancedMode = advancedToggle.isSelected();
            updateHardIgnoreModeHint(hint, hardIgnoreAdvancedMode);
          });
    }

    remove.addActionListener(
        e -> {
          List<MaskRow> sel = list.getSelectedValuesList();
          if (sel == null || sel.isEmpty()) return;

          String title =
              kind == Kind.SOFT_IGNORE
                  ? message("ignoreLists.remove.soft.title")
                  : message("ignoreLists.remove.hard.title");
          int ok =
              JOptionPane.showConfirmDialog(
                  dialog,
                  message("ignoreLists.remove.confirm.message"),
                  title,
                  JOptionPane.OK_CANCEL_OPTION,
                  JOptionPane.WARNING_MESSAGE);
          if (ok != JOptionPane.OK_OPTION) return;

          for (MaskRow row : sel) {
            if (row == null || row.mask().isBlank()) continue;
            if (kind == Kind.SOFT_IGNORE) {
              ignores.removeSoftMask(serverId, row.mask());
            } else {
              ignores.removeMask(serverId, row.mask());
            }
          }
          refresh(model, serverId, kind);
        });

    copy.addActionListener(
        e -> {
          MaskRow row = list.getSelectedValue();
          if (row == null || row.mask().isBlank()) return;
          try {
            Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(row.mask()), null);
          } catch (Exception ignored) {
          }
        });

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
    left.add(add);
    if (allowEdit) {
      left.add(edit);
    }
    left.add(remove);
    left.add(copy);

    JPanel footer = new JPanel(new BorderLayout());
    footer.add(left, BorderLayout.WEST);
    if (advancedModeToggle != null && modeHint != null) {
      JPanel right = new JPanel();
      right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
      right.add(advancedModeToggle);
      right.add(modeHint);
      footer.add(right, BorderLayout.EAST);
    }

    JPanel root = new JPanel(new BorderLayout(10, 10));
    root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    root.add(scroll, BorderLayout.CENTER);
    root.add(footer, BorderLayout.SOUTH);
    return root;
  }

  private void refresh(DefaultListModel<MaskRow> model, String serverId, Kind kind) {
    model.clear();
    if (ignores == null) return;
    List<String> masks =
        (kind == Kind.SOFT_IGNORE) ? ignores.listSoftMasks(serverId) : ignores.listMasks(serverId);
    for (String m : masks) {
      if (m == null || m.isBlank()) continue;
      if (kind == Kind.SOFT_IGNORE) {
        model.addElement(MaskRow.forSoftMask(m));
      } else {
        model.addElement(
            MaskRow.forHardMask(
                m,
                formatHardMaskDisplay(
                    messages,
                    m,
                    ignores.levelsForHardMask(serverId, m),
                    ignores.channelsForHardMask(serverId, m),
                    ignores.expiresAtEpochMsForHardMask(serverId, m),
                    ignores.patternForHardMask(serverId, m),
                    ignores.patternModeForHardMask(serverId, m),
                    ignores.repliesForHardMask(serverId, m))));
      }
    }
  }

  private void refreshIgnore(DefaultListModel<MaskRow> model, String serverId) {
    refresh(model, serverId, Kind.IGNORE);
  }

  private void refreshSoft(DefaultListModel<MaskRow> model, String serverId) {
    refresh(model, serverId, Kind.SOFT_IGNORE);
  }

  private boolean openHardIgnoreRuleEditor(
      String serverId, String mask, HardIgnoreEditorMode editorMode) {
    String sid = Objects.toString(serverId, "").trim();
    String m = Objects.toString(mask, "").trim();
    if (sid.isEmpty() || ignores == null) return false;
    if (editorMode == HardIgnoreEditorMode.EDIT && m.isEmpty()) return false;

    JTextField maskField = new JTextField(m);
    JTextField levelsField =
        new JTextField(renderLevelsForEditor(ignores.levelsForHardMask(sid, m)));
    JTextField channelsField =
        new JTextField(String.join(",", ignores.channelsForHardMask(sid, m)));
    JTextField expiresField =
        new JTextField(renderExpiryForEditor(ignores.expiresAtEpochMsForHardMask(sid, m)));
    JTextField patternField =
        new JTextField(Objects.toString(ignores.patternForHardMask(sid, m), ""));
    JComboBox<IgnoreTextPatternMode> patternModeBox =
        new JComboBox<>(IgnoreTextPatternMode.values());
    patternModeBox.setSelectedItem(ignores.patternModeForHardMask(sid, m));
    JCheckBox repliesBox = new JCheckBox(message("ignoreLists.editor.replies"));
    repliesBox.setSelected(ignores.repliesForHardMask(sid, m));

    JPanel form = new JPanel();
    form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
    form.add(fieldRow(message("ignoreLists.editor.mask"), maskField));
    form.add(fieldRow(message("ignoreLists.editor.levels"), levelsField));
    form.add(fieldRow(message("ignoreLists.editor.channels"), channelsField));
    form.add(fieldRow(message("ignoreLists.editor.expiresAt"), expiresField));
    form.add(fieldRow(message("ignoreLists.editor.pattern"), patternField));
    form.add(fieldRow(message("ignoreLists.editor.patternMode"), patternModeBox));
    form.add(fieldRow("", repliesBox));

    String instructions =
        "<html>"
            + (editorMode == HardIgnoreEditorMode.ADD
                ? message("ignoreLists.editor.addIntro") + "<br>"
                : message("ignoreLists.editor.editIntro") + "<br>")
            + message("ignoreLists.editor.help.levels")
            + "<br>"
            + message("ignoreLists.editor.help.channels")
            + "<br>"
            + message("ignoreLists.editor.help.expires")
            + "</html>";

    while (true) {
      int result =
          JOptionPane.showConfirmDialog(
              dialog,
              new Object[] {instructions, form},
              editorMode == HardIgnoreEditorMode.ADD
                  ? message("ignoreLists.editor.addTitle")
                  : message("ignoreLists.editor.editTitle"),
              JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.PLAIN_MESSAGE);
      if (result != JOptionPane.OK_OPTION) return false;

      ParseResult<String> normalizedMask = parseMaskInput(messages, maskField.getText());
      if (normalizedMask.error() != null) {
        showValidationError(normalizedMask.error());
        continue;
      }

      ParseResult<List<String>> levels = parseLevelsInput(messages, levelsField.getText());
      if (levels.error() != null) {
        showValidationError(levels.error());
        continue;
      }

      ParseResult<List<String>> channels = parseChannelsInput(messages, channelsField.getText());
      if (channels.error() != null) {
        showValidationError(channels.error());
        continue;
      }

      ParseResult<Long> expiry = parseExpiryInputEpochMs(messages, expiresField.getText());
      if (expiry.error() != null) {
        showValidationError(expiry.error());
        continue;
      }

      String pattern = Objects.toString(patternField.getText(), "").trim();
      IgnoreTextPatternMode patternMode =
          (IgnoreTextPatternMode)
              Objects.requireNonNullElse(
                  patternModeBox.getSelectedItem(), IgnoreTextPatternMode.GLOB);
      if (!pattern.isEmpty() && patternMode == IgnoreTextPatternMode.REGEXP) {
        if (!isValidRegexPattern(pattern)) {
          showValidationError(message("ignoreLists.validation.pattern.invalidRegex"));
          continue;
        }
      }

      IgnoreAddMaskResult addResult =
          ignores.addMaskWithLevels(
              sid,
              normalizedMask.value(),
              levels.value(),
              channels.value(),
              expiry.value(),
              pattern,
              patternMode,
              repliesBox.isSelected());
      if (addResult == IgnoreAddMaskResult.UNCHANGED) {
        JOptionPane.showMessageDialog(
            dialog,
            message("ignoreLists.editor.noChanges"),
            editorMode == HardIgnoreEditorMode.ADD
                ? message("ignoreLists.editor.addTitle")
                : message("ignoreLists.editor.editTitle"),
            JOptionPane.INFORMATION_MESSAGE);
        return false;
      }
      return true;
    }
  }

  private void updateHardIgnoreModeHint(JLabel hint, boolean advancedMode) {
    if (hint == null) return;
    hint.setText(hardIgnoreModeHintText(messages, advancedMode));
  }

  static String hardIgnoreModeHintText(boolean advancedMode) {
    return hardIgnoreModeHintText(UiMessages.bundledDefaults(), advancedMode);
  }

  static String hardIgnoreModeHintText(UiMessages messages, boolean advancedMode) {
    UiMessages uiMessages = messages == null ? UiMessages.bundledDefaults() : messages;
    return advancedMode
        ? uiMessages.text("ignoreLists.advancedMode.hint.advanced")
        : uiMessages.text("ignoreLists.advancedMode.hint.simple");
  }

  private static JPanel fieldRow(String label, java.awt.Component input) {
    JPanel row = new JPanel(new BorderLayout(8, 0));
    if (!Objects.toString(label, "").isBlank()) {
      JLabel lbl = new JLabel(label + ":");
      lbl.setPreferredSize(new Dimension(110, lbl.getPreferredSize().height));
      row.add(lbl, BorderLayout.WEST);
    } else {
      JLabel spacer = new JLabel();
      spacer.setPreferredSize(new Dimension(110, 1));
      row.add(spacer, BorderLayout.WEST);
    }
    row.add(input, BorderLayout.CENTER);
    row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    return row;
  }

  private void showValidationError(String text) {
    JOptionPane.showMessageDialog(
        dialog, text, message("ignoreLists.validation.title"), JOptionPane.WARNING_MESSAGE);
  }

  private String message(String code, Object... args) {
    return messages.text(code, args);
  }

  static ParseResult<List<String>> parseLevelsInput(String raw) {
    return parseLevelsInput(UiMessages.bundledDefaults(), raw);
  }

  static ParseResult<List<String>> parseLevelsInput(UiMessages messages, String raw) {
    String input = Objects.toString(raw, "").trim();
    if (input.isEmpty()) return ParseResult.ok(List.of("ALL"));

    LinkedHashSet<String> out = new LinkedHashSet<>();
    for (String token : input.split("[,\\s]+")) {
      String normalized = normalizeLevelToken(token);
      if (normalized.isEmpty()) {
        return ParseResult.error(
            message(messages, "ignoreLists.validation.unknownLevel", token));
      }
      out.add(normalized);
    }
    if (out.isEmpty()) return ParseResult.ok(List.of("ALL"));
    return ParseResult.ok(List.copyOf(out));
  }

  static ParseResult<List<String>> parseChannelsInput(String raw) {
    return parseChannelsInput(UiMessages.bundledDefaults(), raw);
  }

  static ParseResult<List<String>> parseChannelsInput(UiMessages messages, String raw) {
    String input = Objects.toString(raw, "").trim();
    if (input.isEmpty()) return ParseResult.ok(List.of());

    ArrayList<String> out = new ArrayList<>();
    for (String token : input.split("[,\\s]+")) {
      String channel = Objects.toString(token, "").trim();
      if (channel.isEmpty()) continue;
      if (!(channel.startsWith("#") || channel.startsWith("&"))) {
        return ParseResult.error(
            message(messages, "ignoreLists.validation.channelPrefix", channel));
      }
      if (out.stream().noneMatch(existing -> existing.equalsIgnoreCase(channel))) {
        out.add(channel);
      }
    }
    return ParseResult.ok(List.copyOf(out));
  }

  static ParseResult<Long> parseExpiryInputEpochMs(String raw) {
    return parseExpiryInputEpochMs(UiMessages.bundledDefaults(), raw);
  }

  static ParseResult<Long> parseExpiryInputEpochMs(UiMessages messages, String raw) {
    String input = Objects.toString(raw, "").trim();
    if (input.isEmpty()) return ParseResult.ok(null);

    if (input.chars().allMatch(Character::isDigit)) {
      try {
        long epochMs = Long.parseLong(input);
        if (epochMs <= 0L) {
          return ParseResult.error(
              message(messages, "ignoreLists.validation.expiry.positive"));
        }
        return ParseResult.ok(epochMs);
      } catch (Exception ex) {
        return ParseResult.error(
            message(messages, "ignoreLists.validation.expiry.invalidEpoch"));
      }
    }

    try {
      long epochMs = Instant.parse(input).toEpochMilli();
      if (epochMs <= 0L) {
        return ParseResult.error(
            message(messages, "ignoreLists.validation.expiry.afterEpoch"));
      }
      return ParseResult.ok(epochMs);
    } catch (Exception ex) {
      return ParseResult.error(
          message(messages, "ignoreLists.validation.expiry.invalidFormat"));
    }
  }

  static ParseResult<String> parseMaskInput(String raw) {
    return parseMaskInput(UiMessages.bundledDefaults(), raw);
  }

  static ParseResult<String> parseMaskInput(UiMessages messages, String raw) {
    String normalized = IgnoreListService.normalizeMaskOrNickToHostmask(raw);
    if (normalized.isBlank()) {
      return ParseResult.error(message(messages, "ignoreLists.validation.maskRequired"));
    }
    return ParseResult.ok(normalized);
  }

  private static String normalizeLevelToken(String raw) {
    String token = Objects.toString(raw, "").trim().toUpperCase(Locale.ROOT);
    if (token.isEmpty()) return "";
    while (token.startsWith("+") || token.startsWith("-")) {
      token = token.substring(1).trim();
    }
    if (token.isEmpty()) return "";
    if ("*".equals(token)) token = "ALL";
    return IgnoreLevels.KNOWN.contains(token) ? token : "";
  }

  private static boolean isValidRegexPattern(String pattern) {
    try {
      Pattern.compile(pattern);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  private static String renderLevelsForEditor(List<String> levels) {
    List<String> normalized = IgnoreLevels.normalizeConfigured(levels);
    if (normalized.size() == 1 && "ALL".equalsIgnoreCase(normalized.getFirst())) {
      return "";
    }
    return String.join(",", normalized);
  }

  private static String renderExpiryForEditor(long expiresAtEpochMs) {
    if (expiresAtEpochMs <= 0L) return "";
    return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(expiresAtEpochMs));
  }

  static String formatHardMaskDisplay(
      String mask,
      List<String> levels,
      List<String> channels,
      long expiresAtEpochMs,
      String pattern,
      IgnoreTextPatternMode patternMode,
      boolean replies) {
    return formatHardMaskDisplay(
        UiMessages.bundledDefaults(),
        mask,
        levels,
        channels,
        expiresAtEpochMs,
        pattern,
        patternMode,
        replies);
  }

  static String formatHardMaskDisplay(
      UiMessages messages,
      String mask,
      List<String> levels,
      List<String> channels,
      long expiresAtEpochMs,
      String pattern,
      IgnoreTextPatternMode patternMode,
      boolean replies) {
    String m = Objects.toString(mask, "").trim();
    if (m.isEmpty()) return "";

    List<String> metadata = new ArrayList<>();
    List<String> normalizedLevels = IgnoreLevels.normalizeConfigured(levels);
    if (!(normalizedLevels.size() == 1
        && "ALL".equalsIgnoreCase(normalizedLevels.getFirst()))) {
      metadata.add(
          message(
              messages, "ignoreLists.metadata.levels", String.join(",", normalizedLevels)));
    }
    if (channels != null && !channels.isEmpty()) {
      metadata.add(
          message(messages, "ignoreLists.metadata.channels", String.join(",", channels)));
    }
    if (expiresAtEpochMs > 0L) {
      metadata.add(
          message(
              messages,
              "ignoreLists.metadata.expires",
              DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(expiresAtEpochMs))));
    }

    String normalizedPattern = Objects.toString(pattern, "").trim();
    if (!normalizedPattern.isEmpty()) {
      metadata.add(
          message(
              messages,
              "ignoreLists.metadata.pattern",
              renderPattern(messages, normalizedPattern, patternMode)));
    }
    if (replies) {
      metadata.add(message(messages, "ignoreLists.metadata.replies"));
    }

    if (metadata.isEmpty()) return m;
    return m + " [" + String.join("; ", metadata) + "]";
  }

  private static String renderPattern(
      UiMessages messages, String pattern, IgnoreTextPatternMode mode) {
    String p = Objects.toString(pattern, "").trim();
    if (p.isEmpty()) return "";
    IgnoreTextPatternMode m = (mode == null) ? IgnoreTextPatternMode.GLOB : mode;
    return switch (m) {
      case REGEXP -> message(messages, "ignoreLists.metadata.pattern.regexp", p);
      case FULL -> message(messages, "ignoreLists.metadata.pattern.full", p);
      case GLOB -> p;
    };
  }

  private static String message(UiMessages messages, String code, Object... args) {
    UiMessages uiMessages = messages == null ? UiMessages.bundledDefaults() : messages;
    return uiMessages.text(code, args);
  }

  private record MaskRow(String mask, String display) {
    static MaskRow forHardMask(String mask, String display) {
      String m = Objects.toString(mask, "").trim();
      String d = Objects.toString(display, "").trim();
      if (d.isEmpty()) d = m;
      return new MaskRow(m, d);
    }

    static MaskRow forSoftMask(String mask) {
      String m = Objects.toString(mask, "").trim();
      return new MaskRow(m, m);
    }

    @Override
    public String toString() {
      return display;
    }
  }

  record ParseResult<T>(T value, String error) {
    static <T> ParseResult<T> ok(T value) {
      return new ParseResult<>(value, null);
    }

    static <T> ParseResult<T> error(String error) {
      return new ParseResult<>(
          null,
          Objects.toString(
              error,
              UiMessages.bundledDefaults().text("ignoreLists.validation.invalidValue")));
    }
  }
}
