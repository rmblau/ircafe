package cafe.woden.ircclient.ui.chat.embed;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;

final class FileUtil {

  private FileUtil() {}

  static void openBytesWithDesktop(String url, byte[] bytes) throws IOException {
    if (bytes == null || bytes.length == 0) {
      // Fall back to browser.
      try {
        Desktop.getDesktop().browse(URI.create(url));
      } catch (Exception ignored) {
      }
      return;
    }

    File f = writeTempFile(url, bytes);
    Desktop.getDesktop().open(f);
  }

  static File writeTempFile(String url, byte[] bytes) throws IOException {
    return writeTempFile(url, bytes, List.of());
  }

  static File writeTempFile(
      String url,
      byte[] bytes,
      List<? extends cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider>
          extensionProviders)
      throws IOException {
    String ext = ImageFileExtensionSupport.extensionFromUrl(url, extensionProviders);
    File f = Files.createTempFile("ircafe-image-", ext).toFile();
    Files.write(f.toPath(), bytes);
    f.deleteOnExit();
    return f;
  }
}
