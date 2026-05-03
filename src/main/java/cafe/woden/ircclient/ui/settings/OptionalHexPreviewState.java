package cafe.woden.ircclient.ui.settings;

import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

final class OptionalHexPreviewState {
  private String lastValidHex;

  OptionalHexPreviewState(String initialHex) {
    this.lastValidHex = initialHex;
  }

  void set(String hex) {
    lastValidHex = hex;
  }

  String resolve(JTextField field) {
    String raw = field != null ? field.getText() : null;
    raw = raw != null ? raw.trim() : "";
    if (raw.isBlank()) {
      lastValidHex = null;
      return null;
    }

    Color color = SettingsColorSupport.parseHexColorLenient(raw);
    if (color == null) {
      return lastValidHex;
    }

    lastValidHex = SettingsColorSupport.toHex(color);
    return lastValidHex;
  }

  void remember(JTextField field) {
    resolve(field);
  }

  void attachTo(JTextField field, Runnable schedulePreview) {
    field
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                changed();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                changed();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                changed();
              }

              private void changed() {
                remember(field);
                schedulePreview.run();
              }
            });
  }
}
