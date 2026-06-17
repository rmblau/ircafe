package cafe.woden.ircclient.app.outbound.upload.spi;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Backend translator for semantic /upload command arguments into outbound protocol lines. */
@SecondaryPort
@ApplicationLayer
public interface UploadCommandTranslationHandler {

  default String backendId() {
    return "";
  }

  String translateUpload(String target, String msgType, String sourcePath, String displayBody);
}
