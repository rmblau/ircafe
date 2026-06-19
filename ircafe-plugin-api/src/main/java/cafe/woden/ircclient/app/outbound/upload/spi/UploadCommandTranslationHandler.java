package cafe.woden.ircclient.app.outbound.upload.spi;

/** Backend translator for semantic /upload command arguments into outbound protocol lines. */
public interface UploadCommandTranslationHandler {

  default String backendId() {
    return "";
  }

  String translateUpload(String target, String msgType, String sourcePath, String displayBody);
}
