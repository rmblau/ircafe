package cafe.woden.ircclient.ui.input;

import cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeProvider;
import cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeRule;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;
import org.jmolecules.architecture.layered.InterfaceLayer;

/** Built-in Matrix upload msgtype inference rules for common media extensions. */
@InterfaceLayer
@AutoService(MatrixUploadMsgTypeProvider.class)
public final class BuiltInMatrixUploadMsgTypeProvider implements MatrixUploadMsgTypeProvider {
  private static final Set<String> MATRIX_IMAGE_EXTENSIONS =
      Set.of(
          "png", "jpg", "jpeg", "gif", "webp", "bmp", "svg", "heic", "heif", "avif", "tif", "tiff");
  private static final Set<String> MATRIX_VIDEO_EXTENSIONS =
      Set.of("mp4", "m4v", "mov", "mkv", "webm", "avi", "wmv", "flv", "mpeg", "mpg", "3gp", "ogv");
  private static final Set<String> MATRIX_AUDIO_EXTENSIONS =
      Set.of("mp3", "m4a", "aac", "wav", "flac", "ogg", "oga", "opus", "weba", "amr");

  private static final List<MatrixUploadMsgTypeRule> RULES =
      List.of(
          new MatrixUploadMsgTypeRule("m.image", MATRIX_IMAGE_EXTENSIONS.toArray(String[]::new)),
          new MatrixUploadMsgTypeRule("m.video", MATRIX_VIDEO_EXTENSIONS.toArray(String[]::new)),
          new MatrixUploadMsgTypeRule("m.audio", MATRIX_AUDIO_EXTENSIONS.toArray(String[]::new)));

  @Override
  public List<MatrixUploadMsgTypeRule> uploadMsgTypeRules() {
    return RULES;
  }
}
