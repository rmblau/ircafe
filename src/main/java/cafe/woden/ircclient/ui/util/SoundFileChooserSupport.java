package cafe.woden.ircclient.ui.util;

import cafe.woden.ircclient.notify.api.CustomSoundFileImportSupport;
import cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider;
import cafe.woden.ircclient.ui.localization.UiMessages;
import java.awt.Component;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Shared chooser setup for importing custom notification/interceptor sound files. */
public final class SoundFileChooserSupport {
  private static final UiMessages MESSAGES = UiMessages.bundledDefaults();

  private SoundFileChooserSupport() {}

  public static Optional<File> chooseSoundFile(Component owner, String dialogTitle) {
    return chooseSoundFile(owner, dialogTitle, List.of());
  }

  public static Optional<File> chooseSoundFile(
      Component owner,
      String dialogTitle,
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(
        Objects.toString(dialogTitle, MESSAGES.text("common.fileChooser.sound.defaultTitle")));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(audioFileFilter(extensionProviders));
    int result = chooser.showOpenDialog(owner);
    if (result != JFileChooser.APPROVE_OPTION) return Optional.empty();
    return Optional.ofNullable(chooser.getSelectedFile());
  }

  static FileNameExtensionFilter audioFileFilter(
      List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    Set<String> extensions = CustomSoundFileImportSupport.supportedExtensions(extensionProviders);
    return new FileNameExtensionFilter(
        audioFilterLabel(extensions, extensionProviders), extensions.toArray(String[]::new));
  }

  private static String audioFilterLabel(
      Set<String> extensions, List<? extends CustomSoundFileExtensionProvider> extensionProviders) {
    if (CustomSoundFileImportSupport.hasOnlyBuiltInExtensions(extensionProviders)) {
      return MESSAGES.text("common.fileChooser.audioFiles.mp3Wav");
    }
    String suffix =
        extensions.stream()
            .map(ext -> "*." + ext)
            .sorted()
            .reduce((a, b) -> a + ", " + b)
            .orElse("*");
    return "Audio files (" + suffix + ")";
  }
}
