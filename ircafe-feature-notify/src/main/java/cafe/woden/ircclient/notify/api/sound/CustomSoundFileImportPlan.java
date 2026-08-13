package cafe.woden.ircclient.notify.api.sound;

import java.util.Objects;

/** Feature-owned filename plan for custom sound imports. */
public record CustomSoundFileImportPlan(
    boolean validFileName, boolean supportedType, String baseName, String extension) {

  public CustomSoundFileImportPlan {
    baseName = Objects.toString(baseName, "").trim();
    extension = Objects.toString(extension, "").trim();
  }

  public boolean importable() {
    return validFileName && supportedType && !baseName.isBlank() && !extension.isBlank();
  }

  public String fileName(int sequence) {
    if (!importable()) return "";
    int safeSequence = Math.max(1, sequence);
    String suffix = safeSequence <= 1 ? "" : "-" + safeSequence;
    return baseName + suffix + "." + extension;
  }

  public static CustomSoundFileImportPlan invalidFileName() {
    return new CustomSoundFileImportPlan(false, false, null, null);
  }

  public static CustomSoundFileImportPlan unsupportedType() {
    return new CustomSoundFileImportPlan(true, false, null, null);
  }

  public static CustomSoundFileImportPlan importable(String baseName, String extension) {
    return new CustomSoundFileImportPlan(true, true, baseName, extension);
  }
}
