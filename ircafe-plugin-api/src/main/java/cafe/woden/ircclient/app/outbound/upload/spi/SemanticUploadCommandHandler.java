package cafe.woden.ircclient.app.outbound.upload.spi;

/** Backend-specific translator/validator for semantic /upload commands. */
public interface SemanticUploadCommandHandler {

  void appendUploadHelp(UploadCommandTargetView out);

  void appendUploadUsage(UploadCommandTargetView out);

  UploadPreparation prepareUpload(
      UploadCommandTargetView target, String msgType, String path, String caption);

  record UploadPreparation(String line, String statusMessage, boolean showUsage) {}
}
