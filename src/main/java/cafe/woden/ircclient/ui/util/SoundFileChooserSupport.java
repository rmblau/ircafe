package cafe.woden.ircclient.ui.util;

import cafe.woden.ircclient.ui.localization.UiMessages;
import java.awt.Component;
import java.io.File;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Shared chooser setup for importing custom notification/interceptor sound files. */
public final class SoundFileChooserSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private SoundFileChooserSupport() {}

  public static Optional<File> chooseSoundFile(Component owner, String dialogTitle) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(
        Objects.toString(dialogTitle, MESSAGES.text("common.fileChooser.sound.defaultTitle")));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(audioFileFilter());
    int result = chooser.showOpenDialog(owner);
    if (result != JFileChooser.APPROVE_OPTION) return Optional.empty();
    return Optional.ofNullable(chooser.getSelectedFile());
  }

  private static FileNameExtensionFilter audioFileFilter() {
    return new FileNameExtensionFilter(
        MESSAGES.text("common.fileChooser.audioFiles.mp3Wav"), "mp3", "wav");
  }
}
