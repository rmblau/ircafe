package cafe.woden.ircclient.ui.util;

import java.awt.Component;
import java.io.File;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Shared chooser setup for importing custom notification/interceptor sound files. */
public final class SoundFileChooserSupport {
  private static final FileNameExtensionFilter AUDIO_FILE_FILTER =
      new FileNameExtensionFilter("Audio files (MP3, WAV)", "mp3", "wav");

  private SoundFileChooserSupport() {}

  public static Optional<File> chooseSoundFile(Component owner, String dialogTitle) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(Objects.toString(dialogTitle, "Choose sound file (MP3 or WAV)"));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(AUDIO_FILE_FILTER);
    int result = chooser.showOpenDialog(owner);
    if (result != JFileChooser.APPROVE_OPTION) return Optional.empty();
    return Optional.ofNullable(chooser.getSelectedFile());
  }
}
