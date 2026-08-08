package cafe.woden.ircclient.notify.api.sound;

import java.nio.file.Path;
import java.util.Objects;

/** Feature-owned path resolution rules for custom notification sounds. */
public final class NotificationSoundPathResolver {
  private NotificationSoundPathResolver() {}

  public static Path resolveCustomSoundPath(Path runtimeConfigPath, String relativePath) {
    Path base = runtimeConfigPath != null ? runtimeConfigPath.getParent() : null;
    return resolveCustomSoundPathFromBase(base, relativePath);
  }

  public static Path resolveCustomSoundPathFromBase(Path runtimeConfigDirectory, String relativePath) {
    String normalizedRelativePath = Objects.toString(relativePath, "").trim();
    if (normalizedRelativePath.isEmpty() || runtimeConfigDirectory == null) {
      return null;
    }

    Path base = runtimeConfigDirectory.normalize();
    Path resolved = base.resolve(normalizedRelativePath).normalize();
    if (!resolved.startsWith(base)) {
      return null;
    }
    return resolved;
  }
}
