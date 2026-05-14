package cafe.woden.ircclient.ui.settings.notifications;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;

public final class NotificationSoundFileImportSupport {
  private NotificationSoundFileImportSupport() {}

  public static String importToRuntimeDir(Path runtimeConfigPath, File source) throws Exception {
    if (source == null) return null;

    String name = Objects.toString(source.getName(), "").trim();
    if (name.isBlank()) throw new IllegalArgumentException("Invalid file name");

    String lower = name.toLowerCase(Locale.ROOT);
    boolean mp3 = lower.endsWith(".mp3");
    boolean wav = lower.endsWith(".wav");
    if (!mp3 && !wav) {
      throw new IllegalArgumentException("Only .mp3 and .wav are supported");
    }

    Path base = runtimeConfigPath != null ? runtimeConfigPath.getParent() : null;
    if (base == null) {
      throw new IllegalStateException("Runtime config directory is unavailable");
    }

    Path soundsDir = base.resolve("sounds");
    Files.createDirectories(soundsDir);

    String sanitized = name.replaceAll("[^A-Za-z0-9._-]+", "_");
    if (sanitized.isBlank()) {
      sanitized = mp3 ? "notification.mp3" : "notification.wav";
    }

    String ext = mp3 ? "mp3" : "wav";
    String baseName = sanitized;
    int dot = sanitized.lastIndexOf('.');
    if (dot > 0) {
      baseName = sanitized.substring(0, dot);
    }

    Path dest = soundsDir.resolve(baseName + "." + ext);
    int i = 2;
    while (Files.exists(dest)) {
      dest = soundsDir.resolve(baseName + "-" + i + "." + ext);
      i++;
    }

    Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
    return "sounds/" + dest.getFileName();
  }
}
