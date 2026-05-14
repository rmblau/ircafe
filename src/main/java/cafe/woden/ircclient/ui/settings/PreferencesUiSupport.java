package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.model.BuiltInSound;
import cafe.woden.ircclient.ui.icons.SvgIcons;
import cafe.woden.ircclient.ui.util.MouseWheelDecorator;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;

public final class PreferencesUiSupport {
  private PreferencesUiSupport() {}

  public static JPanel padSubTab(JComponent panel) {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setOpaque(false);
    wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    wrapper.add(panel, BorderLayout.NORTH);
    return wrapper;
  }

  public static JLabel tabTitle(String text) {
    JLabel label = new JLabel(text);
    label.putClientProperty(FlatClientProperties.STYLE, "font:+4");
    Font font = label.getFont();
    if (font != null) {
      label.setFont(font.deriveFont(Font.BOLD, font.getSize2D() + 4f));
    }
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
    return label;
  }

  public static JLabel sectionTitle(String text) {
    JLabel label = new JLabel(text);
    label.putClientProperty(FlatClientProperties.STYLE, "font:+2");
    Font font = label.getFont();
    if (font != null) {
      label.setFont(font.deriveFont(Font.BOLD));
    }
    label.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
    return label;
  }

  public static JPanel captionPanel(String title, String layout, String columns, String rows) {
    return captionPanelWithPadding(title, layout, columns, rows, 6, 8, 8, 8);
  }

  public static JPanel captionPanelWithPadding(
      String title,
      String layout,
      String columns,
      String rows,
      int top,
      int left,
      int bottom,
      int right) {
    JPanel panel = new JPanel(new MigLayout(layout, columns, rows));
    panel.setOpaque(false);
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title),
            BorderFactory.createEmptyBorder(top, left, bottom, right)));
    return panel;
  }

  public static JTextArea helpText(String text) {
    JTextArea area = new JTextArea(text);
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setOpaque(false);
    area.setFocusable(false);
    area.setBorder(null);
    area.setFont(UIManager.getFont("Label.font"));
    area.setForeground(UIManager.getColor("Label.foreground"));
    Dimension preferred = area.getPreferredSize();
    area.setMinimumSize(new Dimension(0, preferred != null ? preferred.height : 0));
    return area;
  }

  public static JTextArea subtleInfoText() {
    JTextArea area = new JTextArea();
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setOpaque(false);
    area.setFocusable(false);
    area.setBorder(null);

    Font font = UIManager.getFont("Label.font");
    if (font != null) {
      area.setFont(font.deriveFont(Font.ITALIC));
    } else {
      area.setFont(area.getFont().deriveFont(Font.ITALIC));
    }

    Color hintColor = UIManager.getColor("Label.disabledForeground");
    if (hintColor != null) area.setForeground(hintColor);

    Dimension preferred = area.getPreferredSize();
    area.setMinimumSize(new Dimension(0, preferred != null ? preferred.height : 0));
    return area;
  }

  public static JTextArea subtleInfoTextWith(String text) {
    JTextArea area = subtleInfoText();
    area.setText(text);
    return area;
  }

  public static JTextArea textArea(int rows, int columns, boolean wrap) {
    JTextArea area = new JTextArea(rows, columns);
    area.setLineWrap(wrap);
    area.setWrapStyleWord(wrap);
    return area;
  }

  public static void placeholder(JComponent component, String text) {
    if (component == null) return;
    component.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, text);
  }

  public static JComponent wrapCheckBox(JCheckBox box, String labelText) {
    box.setText("");
    JPanel row = new JPanel(new MigLayout("insets 0, fillx", "[]6[grow,fill]", "[]"));
    row.setOpaque(false);

    JTextArea label = buttonWrapText(labelText);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.addMouseListener(
        new java.awt.event.MouseAdapter() {
          @Override
          public void mouseClicked(java.awt.event.MouseEvent event) {
            if (box.isEnabled()) box.doClick();
          }
        });

    row.add(box, "aligny top");
    row.add(label, "growx, pushx, wmin 0");
    return row;
  }

  public static JButton whyHelpButton(String title, String message) {
    JButton button = new JButton("?");
    button.putClientProperty("JButton.buttonType", "help");
    button.setFocusable(false);
    button.setMargin(new Insets(0, 8, 0, 8));
    button.setToolTipText("Why do I need this?");
    button.addActionListener(
        event -> showHelpDialog(SwingUtilities.getWindowAncestor(button), title, message));
    return button;
  }

  public static void configureBuiltInSoundCombo(JComboBox<BuiltInSound> combo) {
    if (combo == null) return;
    combo.setRenderer(
        new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof BuiltInSound sound) {
              setText(sound.displayNameForUi());
            }
            return this;
          }
        });
  }

  public static void configureIconOnlyButton(JButton button, String iconName, String tooltip) {
    if (button == null) return;
    button.setText("");
    configureButtonIcon(button, iconName, 16);
    button.setMargin(new Insets(2, 6, 2, 6));
    button.setToolTipText(tooltip);
    button.setFocusable(false);
  }

  public static JButton iconOnlyButton(String text, String iconName, String tooltip) {
    JButton button = new JButton(text);
    configureIconOnlyButton(button, iconName, tooltip);
    return button;
  }

  public static JButton buttonWithIcon(String text, String iconName) {
    JButton button = new JButton(text);
    configureButtonIcon(button, iconName, 16);
    return button;
  }

  public static void configureButtonIcon(JButton button, String iconName, int size) {
    if (button == null) return;
    button.setIcon(SvgIcons.action(iconName, size));
    button.setDisabledIcon(SvgIcons.actionDisabled(iconName, size));
  }

  public static void decorateComboBoxSelection(JComboBox<?> combo, List<AutoCloseable> closeables) {
    decorateComboBoxSelection(combo, closeables, false);
  }

  public static void decorateComboBoxSelection(
      JComboBox<?> combo, List<AutoCloseable> closeables, boolean decorateWhenUntracked) {
    if (combo == null) return;
    if (closeables == null && !decorateWhenUntracked) return;
    try {
      addCloseable(MouseWheelDecorator.decorateComboBoxSelection(combo), closeables);
    } catch (Exception ignored) {
    }
  }

  public static JSpinner numberSpinner(
      int value, int min, int max, int step, List<AutoCloseable> closeables) {
    JSpinner spinner = numberSpinner(value, min, max, step);
    decorateNumberSpinner(spinner, closeables);
    return spinner;
  }

  public static JSpinner numberSpinner(int value, int min, int max, int step) {
    return new JSpinner(new SpinnerNumberModel(value, min, max, step));
  }

  public static JSpinner numberSpinner(
      double value, double min, double max, double step, List<AutoCloseable> closeables) {
    JSpinner spinner = numberSpinner(value, min, max, step);
    decorateNumberSpinner(spinner, closeables);
    return spinner;
  }

  public static JSpinner numberSpinner(double value, double min, double max, double step) {
    return new JSpinner(new SpinnerNumberModel(value, min, max, step));
  }

  private static void decorateNumberSpinner(JSpinner spinner, List<AutoCloseable> closeables) {
    if (spinner == null) return;
    try {
      addCloseable(MouseWheelDecorator.decorateNumberSpinner(spinner), closeables);
    } catch (Exception ignored) {
    }
  }

  private static void addCloseable(AutoCloseable closeable, List<AutoCloseable> closeables) {
    if (closeable != null && closeables != null) closeables.add(closeable);
  }

  private static JTextArea buttonWrapText(String text) {
    JTextArea area = new JTextArea(text);
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setOpaque(false);
    area.setFocusable(false);
    area.setBorder(null);

    Font font = UIManager.getFont("CheckBox.font");
    if (font == null) font = UIManager.getFont("Button.font");
    if (font == null) font = UIManager.getFont("Label.font");
    if (font != null) area.setFont(font);

    Color color = UIManager.getColor("CheckBox.foreground");
    if (color == null) color = UIManager.getColor("Label.foreground");
    if (color != null) area.setForeground(color);

    Dimension preferred = area.getPreferredSize();
    area.setMinimumSize(new Dimension(0, preferred != null ? preferred.height : 0));
    return area;
  }

  private static void showHelpDialog(Component parent, String title, String message) {
    JTextArea area = new JTextArea(message);
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setOpaque(false);
    area.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    area.setFont(UIManager.getFont("Label.font"));

    JScrollPane scroll = new JScrollPane(area);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setPreferredSize(new Dimension(460, 240));

    JOptionPane.showMessageDialog(parent, scroll, title, JOptionPane.INFORMATION_MESSAGE);
  }
}
