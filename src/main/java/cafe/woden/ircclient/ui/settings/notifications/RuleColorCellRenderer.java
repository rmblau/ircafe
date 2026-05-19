package cafe.woden.ircclient.ui.settings.notifications;

import cafe.woden.ircclient.ui.settings.ColorSwatch;
import cafe.woden.ircclient.ui.settings.SettingsColorSupport;
import cafe.woden.ircclient.ui.settings.SettingsValueSupport;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

final class RuleColorCellRenderer extends DefaultTableCellRenderer {
  @Override
  public java.awt.Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    JLabel c =
        (JLabel)
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    String raw = SettingsValueSupport.trimmedString(value);
    Color col = SettingsColorSupport.parseHexColorLenient(raw);

    if (col != null) {
      c.setIcon(new ColorSwatch(col, 12, 12));
      c.setText(SettingsColorSupport.toHex(col));
    } else {
      c.setIcon(null);
      c.setText(raw.isEmpty() ? "" : raw);
    }
    return c;
  }
}
