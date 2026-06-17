package cafe.woden.ircclient.ui.input.spi;

import java.util.List;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * ServiceLoader-backed contribution point for Matrix upload msgtype inference.
 *
 * <p>Plugins register implementations in {@code
 * META-INF/services/cafe.woden.ircclient.ui.input.spi.MatrixUploadMsgTypeProvider}.
 */
@InterfaceLayer
public interface MatrixUploadMsgTypeProvider {

  /** Returns extension-to-msgtype rules contributed by this provider. */
  List<MatrixUploadMsgTypeRule> uploadMsgTypeRules();
}
