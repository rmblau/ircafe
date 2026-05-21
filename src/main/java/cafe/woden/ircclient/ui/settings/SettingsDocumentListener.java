package cafe.woden.ircclient.ui.settings;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class SettingsDocumentListener implements DocumentListener {
  private final Runnable onChange;

  public SettingsDocumentListener(Runnable onChange) {
    this.onChange = onChange;
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    onChange.run();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    onChange.run();
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    onChange.run();
  }
}
