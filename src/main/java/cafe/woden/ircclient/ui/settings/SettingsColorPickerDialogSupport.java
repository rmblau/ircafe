package cafe.woden.ircclient.ui.settings;

import cafe.woden.ircclient.ui.localization.UiMessages;
import cafe.woden.ircclient.ui.util.MigConstraints;
import cafe.woden.ircclient.ui.util.MigLayoutConstraints;
import cafe.woden.ircclient.ui.util.MigLayouts;
import cafe.woden.ircclient.ui.util.UiFontKeys;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public final class SettingsColorPickerDialogSupport {
  private static final int MAX_RECENT_COLORS = 12;
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();
  private static final Deque<String> RECENT_COLOR_HEX = new ArrayDeque<>();

  private SettingsColorPickerDialogSupport() {}

  public static Color showColorPickerDialog(
      Window owner, String title, Color initial, Color previewBackground) {
    Color bg =
        previewBackground != null
            ? previewBackground
            : SettingsColorSupport.preferredPreviewBackground();
    Color init = initial != null ? initial : Color.WHITE;

    final JDialog d = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
    d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    final Color[] current = new Color[] {init};
    final Color[] result = new Color[1];

    JLabel preview = new JLabel(" " + MESSAGES.text("settings.colorPicker.preview") + " ");
    preview.setOpaque(true);
    preview.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    preview.setBackground(bg);

    JLabel contrast = new JLabel();
    contrast.setFont(UIManager.getFont(UiFontKeys.LABEL_SMALL_FONT));

    JTextField hex = new JTextField(SettingsColorSupport.toHex(init), 10);
    PreferencesUiSupport.placeholder(hex, "#RRGGBB");

    JLabel hexStatus = new JLabel(" ");
    hexStatus.setFont(UIManager.getFont(UiFontKeys.LABEL_SMALL_FONT));

    JButton more = new JButton(MESSAGES.text("settings.colorPicker.button.more"));
    JButton ok = new JButton(MESSAGES.text("common.button.ok"));
    JButton cancel = new JButton(MESSAGES.text("common.button.cancel"));

    final boolean[] internalUpdate = new boolean[] {false};

    Runnable updatePreview =
        () -> {
          Color fg = current[0];
          preview.setForeground(fg);
          preview.setText(
              MESSAGES.text(
                  "settings.colorPicker.preview.withHex", SettingsColorSupport.toHex(fg)));
          double cr = SettingsColorSupport.contrastRatio(fg, bg);
          String verdict =
              cr >= 4.5
                  ? MESSAGES.text("settings.colorPicker.contrast.ok")
                  : (cr >= 3.0
                      ? MESSAGES.text("settings.colorPicker.contrast.low")
                      : MESSAGES.text("settings.colorPicker.contrast.bad"));
          contrast.setText(
              MESSAGES.text(
                  "settings.colorPicker.contrast",
                  String.format(Locale.ROOT, "%.1f", cr),
                  verdict));
          ok.setEnabled(fg != null);
        };

    Consumer<Color> setColor =
        c -> {
          if (c == null) return;
          current[0] = c;
          internalUpdate[0] = true;
          hex.setText(SettingsColorSupport.toHex(c));
          internalUpdate[0] = false;
          hexStatus.setText(" ");
          updatePreview.run();
        };

    hex.getDocument()
        .addDocumentListener(
            new SettingsDocumentListener(
                () -> {
                  if (internalUpdate[0]) return;

                  Color parsed = SettingsColorSupport.parseHexColorLenient(hex.getText());
                  if (parsed == null) {
                    hexStatus.setText(MESSAGES.text("settings.colorPicker.invalidHex"));
                    ok.setEnabled(false);
                    return;
                  }
                  current[0] = parsed;
                  hexStatus.setText(" ");
                  updatePreview.run();
                }));

    JPanel palette = new JPanel(MigLayouts.wrapWithGap(0, 8, 6, "[]", "[]"));
    Color[] colors =
        new Color[] {
          new Color(0xFFFFFF), new Color(0xD9D9D9), new Color(0xA6A6A6), new Color(0x4D4D4D),
              new Color(0x000000), new Color(0xFF6B6B), new Color(0xFFA94D), new Color(0xFFD43B),
          new Color(0x69DB7C), new Color(0x38D9A9), new Color(0x22B8CF), new Color(0x4DABF7),
              new Color(0x748FFC), new Color(0x9775FA), new Color(0xDA77F2), new Color(0xF783AC),
          new Color(0xC92A2A), new Color(0xE8590C), new Color(0xF08C00), new Color(0x2F9E44),
              new Color(0x0CA678), new Color(0x1098AD), new Color(0x1971C2), new Color(0x5F3DC4)
        };
    for (Color c : colors) {
      palette.add(colorSwatchButton(c, setColor));
    }

    JPanel recent = new JPanel(MigLayouts.wrapWithGap(0, 8, 6, "[]", "[]"));
    Runnable refreshRecent =
        () -> {
          recent.removeAll();
          List<String> rec = snapshotRecentColorHex();
          if (rec.isEmpty()) {
            recent.add(
                PreferencesUiSupport.helpText(MESSAGES.text("settings.colorPicker.noRecent")),
                MigConstraints.spanX(8));
          } else {
            for (String hx : rec) {
              Color c = SettingsColorSupport.parseHexColorLenient(hx);
              if (c == null) continue;
              recent.add(colorSwatchButton(c, setColor));
            }
          }
          recent.revalidate();
          recent.repaint();
        };
    refreshRecent.run();

    more.addActionListener(
        e -> {
          Color picked =
              JColorChooser.showDialog(
                  d, MESSAGES.text("settings.colorPicker.moreColors.title"),
                  current[0] != null ? current[0] : init);
          if (picked != null) setColor.accept(picked);
        });

    ok.addActionListener(
        e -> {
          if (current[0] == null) return;
          result[0] = current[0];
          rememberRecentColorHex(SettingsColorSupport.toHex(current[0]));
          d.dispose();
        });

    cancel.addActionListener(
        e -> {
          result[0] = null;
          d.dispose();
        });

    JPanel content =
        new JPanel(
            MigLayouts.fillXWrap(
                12,
                2,
                MigLayoutConstraints.GROW_FILL_GAP_12_GROW_FILL,
                MigLayouts.rowGaps(10, 6, 10, 6, 10)));
    content.add(preview, MigConstraints.span2GrowXWrap());
    content.add(contrast, MigConstraints.span2GrowXWrap());

    content.add(new JLabel(MESSAGES.text("settings.colorPicker.field.hex")));
    JPanel hexRow =
        new JPanel(
            MigLayouts.fillXWrap(0, 3, "[grow,fill]6[nogrid]6[nogrid]", MigLayouts.rows(2, 2)));
    hexRow.setOpaque(false);
    hexRow.add(hex, MigConstraints.width(110));
    hexRow.add(more);
    hexRow.add(new JLabel(), MigConstraints.push());
    hexRow.add(hexStatus, MigConstraints.spanXGrowX(3));
    content.add(hexRow, MigConstraints.growXWrap());

    content.add(
        new JLabel(MESSAGES.text("settings.colorPicker.field.palette")),
        MigConstraints.alignYTop());
    content.add(palette, MigConstraints.growXWrap());

    content.add(
        new JLabel(MESSAGES.text("settings.colorPicker.field.recent")),
        MigConstraints.alignYTop());
    content.add(recent, MigConstraints.growXWrap());

    JPanel buttons = PreferencesUiSupport.rightComponentRow(8, 0, cancel, ok);

    JPanel outer = new JPanel(new BorderLayout());
    outer.add(content, BorderLayout.CENTER);
    outer.add(buttons, BorderLayout.SOUTH);

    d.setContentPane(outer);
    d.getRootPane().setDefaultButton(ok);
    d.getRootPane()
        .registerKeyboardAction(
            ev -> cancel.doClick(),
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

    updatePreview.run();
    d.pack();
    d.setLocationRelativeTo(owner);
    d.setVisible(true);

    return result[0];
  }

  private static void rememberRecentColorHex(String hex) {
    if (hex == null) return;
    String s = hex.trim().toUpperCase(Locale.ROOT);
    if (s.isEmpty()) return;
    if (!s.startsWith("#")) s = "#" + s;
    if (s.length() == 4) {
      char r = s.charAt(1);
      char g = s.charAt(2);
      char b = s.charAt(3);
      s = "#" + r + r + g + g + b + b;
    }
    if (s.length() != 7) return;

    final String needle = s;

    synchronized (RECENT_COLOR_HEX) {
      RECENT_COLOR_HEX.removeIf(v -> v != null && v.equalsIgnoreCase(needle));
      RECENT_COLOR_HEX.addFirst(needle);
      while (RECENT_COLOR_HEX.size() > MAX_RECENT_COLORS) {
        RECENT_COLOR_HEX.removeLast();
      }
    }
  }

  private static List<String> snapshotRecentColorHex() {
    synchronized (RECENT_COLOR_HEX) {
      return new ArrayList<>(RECENT_COLOR_HEX);
    }
  }

  private static JButton colorSwatchButton(Color c, Consumer<Color> onPick) {
    JButton b = new JButton();
    b.setFocusable(false);
    b.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    b.setContentAreaFilled(false);
    b.setIcon(new ColorSwatch(c, 18, 18));
    b.setToolTipText(SettingsColorSupport.toHex(c));
    b.addActionListener(
        e -> {
          if (onPick != null) onPick.accept(c);
        });
    return b;
  }
}
